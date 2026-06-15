# Intégration Strava — SportsIn

## Vue d'ensemble

L'intégration Strava permet aux joueurs de connecter leur compte Strava pour que leurs activités sportives réelles (course, vélo, natation, etc.) contribuent au jeu territorial de SportsIn.

---

## Architecture

### Backend

```
org.SportsIn/
├── config/
│   └── StravaProperties.java          # @ConfigurationProperties(prefix="strava")
├── model/strava/
│   ├── StravaAccount.java             # Entité JPA — tokens OAuth par joueur
│   ├── StravaActivity.java            # Entité JPA — activités importées (dedup)
│   └── ActivityProvider.java          # Interface Adapter (Strava, Garmin, etc.)
├── dto/
│   ├── StravaTokenResponse.java       # Réponse token OAuth Strava
│   ├── StravaAthleteDTO.java          # Sous-objet "athlete" dans le token
│   ├── StravaActivityDTO.java         # Activité telle que retournée par l'API v3
│   └── StravaSyncResultDTO.java       # Résultat d'une synchronisation
├── repository/
│   ├── StravaAccountRepository.java   # JPA — accès aux comptes Strava
│   └── StravaActivityRepository.java  # JPA — accès aux activités importées
├── services/
│   ├── StravaPolylineDecoder.java     # Décode les polylines encodées Google/Strava
│   ├── StravaRateLimiter.java         # Respecte 100 req/15min + 1000/jour
│   ├── StravaApiClient.java           # Client HTTP Java 11 → API Strava v3
│   ├── StravaOAuthService.java        # Gestion du cycle OAuth2 (URL, callback, disconnect)
│   ├── StravaSyncService.java         # Orchestration : fetch → anti-cheat → zones → XP
│   ├── StravaInfluenceModifier.java   # InfluenceModifier ordre 30 (Chain of Responsibility)
│   └── StravaWebhookService.java      # Réception webhook Strava (asynchrone, Spring Events)
└── controller/
    └── StravaController.java          # 12 endpoints REST /api/strava/*
```

### Frontend

```
frontend/src/
├── pages/StravaPage.jsx               # Page complète : OAuth, sync, stats, classement
├── styles/strava.css                  # Styles dédiés
├── api/api.js                         # stravaAPI ajouté (8 fonctions)
├── App.jsx                            # Route /strava ajoutée
├── components/Header.jsx              # Lien "Strava" ajouté dans la navigation
└── pages/MapPage.jsx                  # Couche polylines Strava orange superposée
```

---

## Flux OAuth2

```
[Joueur clique "Connecter"]
        │
        ▼
GET /api/strava/auth/url?joueurId=<id>
        │  → retourne { url: "https://www.strava.com/oauth/authorize?..." }
        ▼
[Frontend redirige vers Strava]
        │
        ▼  [Joueur autorise sur Strava]
        │
GET /api/strava/callback?code=<code>&state=<joueurId>
        │  → échange code → tokens
        │  → persiste StravaAccount en DB
        │  → redirige vers /strava?connected=true
        ▼
[Frontend affiche confirmation]
```

---

## Endpoints API

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET`   | `/api/strava/auth/url?joueurId=<id>` | URL d'autorisation OAuth Strava |
| `GET`   | `/api/strava/callback` | Callback OAuth (paramètres: code, state, error) |
| `GET`   | `/api/strava/status?joueurId=<id>` | Statut de connexion |
| `DELETE`| `/api/strava/disconnect?joueurId=<id>` | Déconnexion RGPD (supprime les tokens) |
| `POST`  | `/api/strava/sync?joueurId=<id>` | Synchronisation manuelle |
| `GET`   | `/api/strava/webhook` | Vérification challenge webhook Strava |
| `POST`  | `/api/strava/webhook` | Réception événements webhook |
| `GET`   | `/api/strava/activities?joueurId=<id>` | Activités importées d'un joueur |
| `GET`   | `/api/strava/activities/team?equipeId=<id>` | Activités de toute l'équipe |
| `GET`   | `/api/strava/stats/player?joueurId=<id>` | Stats agrégées d'un joueur |
| `GET`   | `/api/strava/stats/leaderboard?period=weekly\|monthly` | Classement équipes |
| `GET`   | `/api/strava/rate-limit` | Statut du rate limiter |

---

## Modèle de données

### Table `strava_account`
| Colonne | Type | Description |
|---------|------|-------------|
| `id` | INTEGER PK | Auto-incrémenté |
| `joueur_id` | INTEGER UNIQUE FK | Lien vers `joueur.id` |
| `strava_athlete_id` | TEXT UNIQUE | ID de l'athlète côté Strava |
| `access_token` | TEXT | Token d'accès (à chiffrer en production) |
| `refresh_token` | TEXT | Token de rafraîchissement |
| `token_expires_at` | TEXT | ISO-8601 d'expiration |
| `connected_at` | TEXT | ISO-8601 de la connexion initiale |

### Table `strava_activity`
| Colonne | Type | Description |
|---------|------|-------------|
| `id` | INTEGER PK | Auto-incrémenté |
| `strava_activity_id` | TEXT UNIQUE | Clé de déduplication |
| `joueur_id` | INTEGER FK | Joueur propriétaire |
| `equipe_id` | INTEGER FK | Équipe au moment de l'import |
| `name` | TEXT | Nom de l'activité |
| `sport_type` | TEXT | Type Strava (Run, Ride, Swim…) |
| `distance_meters` | REAL | Distance en mètres |
| `moving_time_seconds` | INTEGER | Temps en mouvement |
| `total_elevation_gain` | REAL | Dénivelé positif en mètres |
| `average_speed` | REAL | Vitesse moyenne en m/s |
| `summary_polyline` | TEXT | Polyline encodée Google |
| `influence_granted` | REAL | Influence accordée à l'équipe |
| `zones_traversed` | TEXT | JSON array d'IDs d'arènes traversées |
| `route_bonus_triggered` | INTEGER | 1 si la route bonus a été activée |
| `anti_cheat_flagged` | INTEGER | 1 si flaggée pour vitesse irréaliste |
| `anti_cheat_reason` | TEXT | Raison du flag anti-cheat |
| `synced_at` | TEXT | ISO-8601 de l'import |

---

## Calcul d'influence (StravaInfluenceModifier)

Le `StravaInfluenceModifier` s'insère dans la Chain of Responsibility existante à l'**ordre 30** (après Route=10, Perk=20).

**Logique :**
```
Pour chaque Session sur une arène :
  bonus += Σ (activity.influenceGranted × 0.02)
         pour chaque activité Strava de l'équipe
         des 7 derniers jours
         qui mentionne l'arène dans zones_traversed

bonus = min(bonus, 0.40)   ← plafond à 40% de modificateur
```

**Calcul de l'influence par activité :**
```
influence = (distance_km × 1.0) + (elevation_km × 1.0)
           × 1.25 si route activée
           ≤ 50 (plafond par activité)
```

---

## Anti-cheat

| Sport | Vitesse max autorisée |
|-------|-----------------------|
| Run, TrailRun | 7 m/s (25 km/h) |
| Ride | 22 m/s (79 km/h) |
| MountainBikeRide | 15 m/s (54 km/h) |
| Swim | 2.5 m/s (9 km/h) |
| Walk, Hike | 2.5–3 m/s |
| WeightTraining, Yoga, Workout | Pas de contrôle vitesse |

Les activités flaggées sont persistées (avec `anti_cheat_flagged=1`) mais **n'accordent aucune influence ni XP**.

---

## Rate Limiting

Respecte les limites Strava :
- **100 requêtes / 15 minutes** (fenêtre glissante)
- **1 000 requêtes / jour** (reset à minuit UTC)

Implémentation en mémoire (`StravaRateLimiter`). Pour du multi-instance, remplacer par Redis.

---

## Webhook Strava

Le webhook permet de recevoir les nouvelles activités en temps réel sans polling.

**Configuration sur Strava :**
```bash
curl -X POST https://www.strava.com/api/v3/push_subscriptions \
  -F client_id=<STRAVA_CLIENT_ID> \
  -F client_secret=<STRAVA_CLIENT_SECRET> \
  -F callback_url=https://<votre-domaine>/api/strava/webhook \
  -F verify_token=<STRAVA_WEBHOOK_VERIFY_TOKEN>
```

Le contrôleur répond en < 2s (exigence Strava) et délègue le traitement à `StravaWebhookService` via `@Async` + Spring Events.

---

## Variables d'environnement

| Variable | Obligatoire | Description |
|----------|-------------|-------------|
| `STRAVA_CLIENT_ID` | Oui | ID client de l'app Strava |
| `STRAVA_CLIENT_SECRET` | Oui | Secret client de l'app Strava |
| `STRAVA_WEBHOOK_VERIFY_TOKEN` | Non | Token de vérification webhook (défaut: `sportsin_webhook_verify`) |
| `STRAVA_REDIRECT_URI` | Non | URI de callback OAuth (défaut: `http://localhost:8080/api/strava/callback`) |
| `STRAVA_FRONTEND_URL` | Non | URL du frontend (défaut: `http://localhost:5173`) |

**Pour démarrer localement :**
```bash
# Linux/Mac
export STRAVA_CLIENT_ID=your_client_id
export STRAVA_CLIENT_SECRET=your_client_secret

# Windows PowerShell
$env:STRAVA_CLIENT_ID="your_client_id"
$env:STRAVA_CLIENT_SECRET="your_client_secret"
```

> Ne jamais committer de secrets dans `application.properties`. Utiliser des variables d'environnement ou un coffre-fort (Vault, AWS Secrets Manager).

---

## Tests

| Fichier | Couverture |
|---------|-----------|
| `StravaPolylineDecoderTest` | Décodage polylines (null, vide, connu, multi-points) |
| `StravaRateLimiterTest` | Fenêtre 15 min, limite journalière, compteurs |
| `StravaInfluenceModifierTest` | Bonus, plafond, cumul, nulls, ordre=30 |
| `StravaOAuthServiceTest` | URL OAuth, callback create/update, disconnect, isConnected |
| `StravaSyncServiceTest` | Import, dédup, anti-cheat, XP, zones, route bonus |
| `StravaApiClientWireMockTest` | HTTP réel mockée avec WireMock (200, 401, rate limit) |

**Lancer les tests :**
```bash
./gradlew test
./gradlew jacocoTestReport
```

---

## Pattern Adapter — ActivityProvider

L'interface `ActivityProvider` (dans `org.SportsIn.model.strava`) permet d'ajouter de futures sources d'activités sans modifier la logique métier :

```java
public interface ActivityProvider {
    List<StravaActivityDTO> fetchRecentActivities(Long joueurId, int limit);
    StravaActivityDTO fetchActivity(Long joueurId, String providerActivityId);
    void disconnect(Long joueurId);
    String getProviderName(); // "strava", "garmin", etc.
}
```

`StravaApiClient` en est la première implémentation. Pour ajouter Garmin : implémenter `ActivityProvider` et le déclarer `@Service`.
