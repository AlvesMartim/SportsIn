# Feature 7 : État Réel d'Implémentation

Ce document résume ce qui est réellement codé dans le projet pour la Feature 7 (météo active), et distingue BackEnd / FrontEnd.

## Statut global

- BackEnd : complet — moteur météo, endpoints dédiés, missions flash, affinités d'équipe, usure territoriale et scheduling.
- FrontEnd : complet — widget météo réutilisable intégré sur 5 pages, prévisions 24h, badges météo, alertes sur la carte, recommandation de sport.

---

## BackEnd

### 1) Moteur météo sur la fin de session

- Intégration d'un client OpenWeatherMap (`WeatherClient`) et d'un moteur de pénibilité par sport (Strategy Pattern — `WeatherHardshipEngine`).
- Calcul d'un indice de pénibilité, puis conversion en bonus d'influence appliqué à la conquête.
- Enrichissement de `SessionResult` avec les métadonnées météo (source, tags, résumé, température, vent, pluie, multiplicateurs).

> Note : la météo interrogée est la météo courante au moment de la session. La météo historique n'est pas branchée.

### 2) Quêtes Flash météo

- Génération automatique de missions "alerte" quand un événement extrême approche (prévu dans les 24h).
- Évaluation de réussite : victoire d'équipe sur l'arène avant l'heure de l'événement.
- Stockées en base comme `DIVERSITY_SPORT` avec `missionCategory=WEATHER_FLASH` dans le `payload_json` (contrainte schéma SQL).

### 3) Affinités d'équipe (perks météo)

- Effet `WEATHER_AFFINITY` côté perks (stratégie `WeatherAffinityEffect`).
- Bonus d'affinité calculé selon les tags météo actifs (RAIN, HEAT, WIND, COLD, STORM, EXTREME).
- 3 perks météo définis : `AMPHIBIEN` (pluie, niv.4), `THERMO_RUNNER` (chaleur, niv.5), `AERO_STRIKE` (vent, niv.5).

### 4) Usure territoriale PvE

- Décroissance naturelle quotidienne de l'influence par arène.
- Décroissance accélérée si météo extrême prolongée.
- Perte automatique du contrôle d'arène si influence épuisée.

### 5) WeatherController — nouveaux endpoints

| Endpoint | Description |
|---|---|
| `GET /api/weather/current?lat=X&lng=Y&sport=S` | Météo courante + hardshipIndex + influenceBonus |
| `GET /api/weather/arena/{id}?sport=S` | Météo de l'arène via ses coordonnées |
| `GET /api/weather/alerts` | Arènes sous conditions extrêmes (influence ×2) |
| `GET /api/weather/arena/{id}/forecast` | Prévisions 24h (fenêtres de 3h) |
| `GET /api/weather/arena/{id}/best-sport` | Classement des sports par bonus météo |
| `GET /api/weather/teams/{teamId}/badges` | Progression des badges météo d'une équipe |

### 6) Scheduling et configuration

- Scheduler météo pour génération des missions flash et application de l'usure territoriale.
- Propriétés dans `application.properties` : `weather.openweather.*`, `weather.scheduler.*`.
- **Fix critique** : chargement du `.env` dans `start-dev.sh` (`set -a; source .env; set +a`) — sans ça, la clé API ne remontait pas jusqu'à Spring Boot.

---

## FrontEnd

### 1) WeatherWidget (composant réutilisable)

- Affiche : température, vent, précipitations, tags météo, bonus d'influence, label dominant.
- Prop `compact` pour les popups de carte.
- Bordure rouge animée si conditions extrêmes (`--extreme`).
- Message explicite si météo indisponible (plutôt que ne rien afficher).
- Intégré sur : **HomePage**, **CreateGamePage**, **ActiveSessionPage**, **MapPage**, **MissionsPage**.

### 2) WeatherForecastPanel (composant réutilisable)

- Timeline 24h des prévisions par fenêtre de 3h.
- Chaque fenêtre affiche : heure, température, tags, multiplicateur d'influence.
- Fenêtres extrêmes : animation rouge pulsée.
- Fenêtres à bonus : teinte verte.

### 3) Feature 1 — Badges météo (TeamPage)

- Section "🌦️ Badges Météo" dans la page équipe.
- 6 badges définis : RAIN_WARRIOR, STORM_MASTER, ICE_BREAKER, WIND_RIDER, HEAT_KING, EXTREME_SURVIVOR.
- Affichage icon + nom + progression + état verrouillé/déverrouillé (or vs grisé).
- Données issues de `GET /api/weather/teams/{teamId}/badges`.

> ⚠️ Les badges sont calculés sur les sessions jouées **en direct** (SessionRepository in-memory). Ils se réinitialisent au redémarrage du serveur.

### 4) Feature 2 — Alertes météo sur la carte (MapPage)

- Marqueur violet animé sur les arènes en conditions extrêmes.
- Bannière rouge "🌪️ ALERTE MÉTÉO — Influence ×2 si victoire !" dans la popup.
- Données issues de `GET /api/weather/alerts` au chargement de la carte.

### 5) Feature 3 — Prévisions 24h (MissionsPage)

- Bouton "📅 Prévisions météo 24h" dépliable dans la page missions.
- Affiche `WeatherForecastPanel` pour la première arène disponible.

### 6) Feature 8 — Recommandation de sport (CreateGamePage)

- Après sélection d'une arène, appel à `GET /api/weather/arena/{id}/best-sport`.
- Bannière dorée cliquable "⭐ Recommandé par la météo — {sport} — +X% influence".
- Clic → sélection automatique du sport recommandé.
- Options du `<select>` enrichies avec le pourcentage de bonus et l'étoile ⭐.
- Widget météo affiché dans le récapitulatif avant lancement.

### 7) ActiveSessionPage

- Widget météo live pendant la session, lié à l'arène du match en cours.
- Affiche le multiplicateur d'influence attendu à la fin.

### 8) HomePage

- Géolocalisation du joueur → widget météo local au chargement.

### 9) GameResultPage (existant — données enrichies)

- `WeatherCard` affiche les données météo appliquées lors de la clôture de session (hardshipIndex, tags, influenceBonus, affinityBonus, totalInfluenceModifier).

---

## Jeu de données de test

Fichier : `app/src/main/resources/data.sql`  
Script de réinitialisation : `reset-testdata.sh` (supprime `sportsin.db`)

| Catégorie | Contenu |
|---|---|
| Équipes | 5 équipes, niveaux 2 à 5, couleurs définies |
| Joueurs | 20 joueurs avec email + password (`Sportsin1`) |
| Arènes | 6 stades répartis en France, sports élargis |
| Sessions | 14 sessions TERMINATED avec scores et vainqueurs |
| Active perks | 7 perks actifs (AMPHIBIEN, AERO_STRIKE, THERMO_RUNNER, SHIELD×2, XP_BOOST×2) |
| Missions | 13 missions actives (RECAPTURE, BREAK_ROUTE, DIVERSITY_SPORT, 1 WEATHER_FLASH) |
| Messages | 17 messages de chat répartis sur les 5 équipes |

**Comptes de test :**

| Pseudo | Email | Équipe | Niveau |
|---|---|---|---|
| BenYedder | benyedder@sportsin.test | AS Monaco | 5 |
| Mbappe | mbappe@sportsin.test | PSG | 5 |
| Lacazette | lacazette@sportsin.test | OL | 4 |
| KoloMuani | kolomuani@sportsin.test | FC Nantes | 3 |
| Camara | camara@sportsin.test | Stade Brest | 2 |

Mot de passe universel : `Sportsin1`

---

## Tests

### Tests unitaires

- `SessionServiceWeatherUnitTest` : calcul gain d'influence avec route/perks + météo + affinité.
- `WeatherAffinityServiceTest`, `WeatherFlashMissionServiceTest`, `WeatherHardshipEngineTest`, `TerritoryDecayServiceTest`.
- Extension de `MissionServiceTest` pour le scénario `WEATHER_FLASH`.

### Tests d'intégration

- `SessionWeatherFlowIntegrationTest` : flux Spring complet de fin de session avec météo mockée.
- `WeatherFlashMissionIntegrationTest` : génération de missions flash via service Spring.
