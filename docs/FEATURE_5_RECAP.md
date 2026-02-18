# Feature 5 : Systeme de Missions Dynamiques

## 1. Vue d'ensemble

La Feature 5 ajoute un **systeme de missions dynamiques** au projet SportsIn. Chaque equipe recoit automatiquement des missions basees sur l'etat du jeu (arenes controlees, sports pratiques, routes adverses). Les missions sont generees quotidiennement, evaluees en continu, et recompensent les equipes avec des points et de l'XP.

### Objectifs
- Generation automatique de missions contextuelles (max 3 par equipe)
- Trois types de missions : Reconquete, Diversite de sport, Rupture de route
- Evaluation periodique via scheduler Spring
- Systeme de recompenses (points + XP)
- Affichage des missions sur la carte interactive (marqueurs dores)

---

## 2. Architecture & Design Patterns

### Strategy Pattern
`MissionEvaluationService` utilise une evaluation specifique par type de mission. Chaque type (`RECAPTURE_RECENT_LOSS`, `DIVERSITY_SPORT`, `BREAK_ROUTE`) possede sa propre logique de validation.

### Repository Pattern
`MissionRepository` abstrait l'acces aux donnees avec des requetes optimisees et des index performants.

### DTO Pattern
Separation API via `MissionSummaryDTO` (liste) et `MissionDetailDTO` (detail complet avec payload JSON parse).

### Payload Pattern
Chaque mission stocke un `payloadJson` flexible contenant les parametres specifiques (arenaId, sportCode, etc.), permettant d'ajouter de nouveaux types sans modifier le schema.

### Scheduled Tasks
Spring `@Scheduled` pour la gestion automatique du cycle de vie des missions (generation, evaluation, expiration).

---

## 3. Types de Missions

| Type | Objectif | Duree | Points | XP | Priorite |
|------|----------|-------|--------|----|----------|
| `RECAPTURE_RECENT_LOSS` | Reconquerir une arene controlee par l'adversaire | 3 jours | 50 | 30 | HIGH |
| `DIVERSITY_SPORT` | Pratiquer un sport non joue depuis 14 jours sur une arene | 7 jours | 30 | 20 | LOW |
| `BREAK_ROUTE` | Capturer une 2e arene adverse pour briser une route | 5 jours | 75 | 50 | MEDIUM |

### Regles de generation (R1, R2, R3)
- **R1 (Reconquete)** : Declenchee quand une arene est controlee par un adversaire. Payload : `{arenaId, arenaName, windowDays: 7}`
- **R2 (Diversite)** : Declenchee quand un sport disponible sur une arene n'a pas ete joue depuis 14 jours. Payload : `{arenaId, arenaName, sportCode, windowDays: 14}`
- **R3 (Rupture)** : Declenchee quand un adversaire controle 2+ arenes. Payload : `{arenaId, arenaName, adversaryTeamId, minCount: 1}`

---

## 4. Scheduler

`MissionScheduler` gere le cycle de vie automatique :

| Tache | Frequence | Action |
|-------|-----------|--------|
| Expiration & Evaluation | Toutes les 10 minutes | Expire les missions depassees, evalue les missions actives |
| Generation quotidienne | Chaque jour a 06:00 (Europe/Paris) | Genere de nouvelles missions pour toutes les equipes |

Configuration : desactivable via `mission.scheduler.enabled=false` dans `application.properties`.

---

## 5. Endpoints API REST

### `GET /api/teams/{teamId}/missions`
Liste les missions d'une equipe (filtre optionnel par statut).

**Parametre query :** `?status=ACTIVE`

**Reponse :** Liste de `MissionSummaryDTO`
```json
[
  {
    "id": 1,
    "type": "RECAPTURE_RECENT_LOSS",
    "status": "ACTIVE",
    "title": "Reconquerir Chatelet",
    "priority": "HIGH",
    "endsAt": "2025-01-15T06:00:00",
    "rewardTeamPoints": 50,
    "progressCurrent": 0,
    "progressTarget": 1
  }
]
```

### `GET /api/missions/{missionId}`
Retourne le detail complet d'une mission (`MissionDetailDTO`).

### `POST /api/teams/{teamId}/missions/generate`
Force la generation de missions (debug/test).

### `POST /api/missions/{missionId}/refresh`
Force l'evaluation d'une mission (debug/test).

**Tri des resultats :**
- Missions ACTIVE : triees par `endsAt` ASC (urgentes en premier)
- Missions terminees : triees par `completedAt` DESC (recentes en premier)

---

## 6. Fichiers crees (15 fichiers)

### Modeles
| Fichier | Description |
|---------|-------------|
| `model/mission/Mission.java` | Entite JPA — mission avec cycle de vie complet (statut, progression, payload JSON) |
| `model/mission/MissionType.java` | Enum — 3 types : `RECAPTURE_RECENT_LOSS`, `BREAK_ROUTE`, `DIVERSITY_SPORT` |
| `model/mission/MissionStatus.java` | Enum — 4 statuts : `ACTIVE`, `SUCCESS`, `FAILED`, `EXPIRED` |
| `model/mission/MissionPriority.java` | Enum — 3 niveaux : `LOW`, `MEDIUM`, `HIGH` |

### DTOs
| Fichier | Description |
|---------|-------------|
| `dto/MissionSummaryDTO.java` | Donnees minimales pour les listes (id, type, statut, progression, recompense) |
| `dto/MissionDetailDTO.java` | Donnees completes avec payload JSON parse pour le frontend |

### Services
| Fichier | Description |
|---------|-------------|
| `services/MissionGenerationService.java` | Generation dynamique des 3 types de missions (R1, R2, R3) avec deduplication |
| `services/MissionEvaluationService.java` | Evaluation des conditions de reussite, attribution des recompenses, gestion expiration |

### Scheduler
| Fichier | Description |
|---------|-------------|
| `scheduler/MissionScheduler.java` | Taches planifiees : generation quotidienne + evaluation toutes les 10 min |

### Controleur
| Fichier | Description |
|---------|-------------|
| `controller/MissionController.java` | 4 endpoints REST (liste, detail, generation, refresh) |

### Repository
| Fichier | Description |
|---------|-------------|
| `repository/MissionRepository.java` | CRUD + requetes optimisees (par equipe, statut, expiration) |

### Tests
| Fichier | Description |
|---------|-------------|
| `test/.../MissionServiceTest.java` | Tests unitaires complets avec stubs in-memory |

### Frontend
| Fichier | Description |
|---------|-------------|
| `frontend/src/api/api.js` | Facade API missions (getByTeam, getById, generate, refresh) |
| `frontend/src/pages/MapPage.jsx` | Affichage des missions sur la carte (marqueurs dores, popups, countdown) |

---

## 7. Fichiers modifies

| Fichier | Modification |
|---------|-------------|
| `resources/schema.sql` | Ajout table `mission` + 3 index de performance |

---

## 8. Tests

### MissionServiceTest — 9 tests

**Tests de generation :**
- `testGenerateDoesNotExceedMaxActive()` — Respecte la limite de 3 missions actives
- `testGenerateRecaptureMission()` — Creation correcte d'une mission RECAPTURE_RECENT_LOSS
- `testGenerateDiversityMission()` — Creation correcte d'une mission DIVERSITY_SPORT
- `testGenerateBreakRouteMissionFromArenas()` — Creation correcte d'une mission BREAK_ROUTE

**Tests d'evaluation :**
- `testMissionExpires()` — Logique d'expiration (statut → EXPIRED)
- `testEvaluateRecaptureSuccess()` — Detection du controle d'arene
- `testEvaluateRecaptureStillActive()` — Condition non remplie → reste ACTIVE
- `testEvaluateBreakRouteSuccess()` — Capture de la 2e arene adverse
- `testEvaluateAllActiveMissions()` — Evaluation par lot

**Stubs in-memory :**
- `InMemoryMissionRepository`
- `InMemoryEquipeRepository`
- `InMemoryAreneRepository`
- `InMemorySessionRepository`

---

## 9. Schema de base de donnees

### Table `mission`
```sql
CREATE TABLE IF NOT EXISTS mission (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id INTEGER NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('RECAPTURE_RECENT_LOSS', 'BREAK_ROUTE', 'DIVERSITY_SPORT')),
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    title TEXT NOT NULL,
    description TEXT,
    priority TEXT NOT NULL DEFAULT 'MEDIUM',
    reward_team_points INTEGER NOT NULL DEFAULT 0,
    reward_team_xp INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    starts_at TEXT NOT NULL,
    ends_at TEXT NOT NULL,
    completed_at TEXT,
    payload_json TEXT,
    progress_current INTEGER NOT NULL DEFAULT 0,
    progress_target INTEGER NOT NULL DEFAULT 1,
    last_evaluated_at TEXT,
    FOREIGN KEY (team_id) REFERENCES equipe(id) ON DELETE CASCADE
);
```

### Index de performance
```sql
CREATE INDEX idx_mission_team_id ON mission(team_id);
CREATE INDEX idx_mission_status ON mission(status);
CREATE INDEX idx_mission_team_status ON mission(team_id, status);
```

---

## 10. Diagramme de flux

```
=== GENERATION (quotidien 06:00) ===

MissionScheduler.generateMissionsForAllTeams()
     │
     ▼
Pour chaque equipe :
     │
     ▼
MissionGenerationService.generateForTeam(teamId)
     │
     ├─► Verifier : missions actives < 3 ?
     │
     ├─► R1 : Arene adverse ? → RECAPTURE_RECENT_LOSS (50 pts, 30 XP)
     ├─► R2 : Sport non joue depuis 14j ? → DIVERSITY_SPORT (30 pts, 20 XP)
     ├─► R3 : Adversaire controle 2+ arenes ? → BREAK_ROUTE (75 pts, 50 XP)
     │
     ├─► Deduplication via payloadKey()
     │
     ▼
Sauvegarde en BDD (MissionRepository)


=== EVALUATION (toutes les 10 min) ===

MissionScheduler.expireAndEvaluate()
     │
     ├─► Expiration : missions depassees → statut EXPIRED
     │
     ▼
MissionEvaluationService.evaluateAllActiveMissions()
     │
     ├─► RECAPTURE : equipe controle l'arene ? → SUCCESS
     ├─► DIVERSITY : session du sport sur l'arene apres debut mission ? → SUCCESS
     ├─► BREAK_ROUTE : equipe controle l'arene cible ? → SUCCESS
     │
     ▼
Si SUCCESS :
     ├─► Ajout rewardTeamPoints a l'equipe
     ├─► Ajout rewardTeamXp a l'equipe
     ├─► Statut → SUCCESS, progression → 1/1
     │
     ▼
Mise a jour en BDD


=== AFFICHAGE FRONTEND ===

MapPage.jsx
     │
     ├─► Fetch missions actives pour l'equipe courante
     ├─► Construction map missionsByArena
     │
     ▼
Carte interactive :
     ├─► Marqueurs dores sur les arenes avec missions
     ├─► Popup : type (Reconquete / Rupture / Diversite)
     ├─► Barre de progression
     ├─► Recompense en points
     └─► Compte a rebours (jours / heures / minutes)
```

---

## 11. Integration Frontend

### Marqueurs de mission
Les arenes associees a une mission active sont identifiees par un **marqueur dore** special sur la carte Leaflet.

### Labels francais
| Type | Label affiche |
|------|---------------|
| `RECAPTURE_RECENT_LOSS` | Reconquete |
| `BREAK_ROUTE` | Rupture de route |
| `DIVERSITY_SPORT` | Diversite sport |

### Popup de mission
Chaque marqueur de mission affiche :
- Le type de mission avec son label
- Le titre et la description
- Une barre de progression (`progressCurrent / progressTarget`)
- Les points de recompense
- Le temps restant (formate en jours/heures/minutes)

---

## 12. Branche Git

Commits associes :
- `8b3f00d feat(feature5): ajout systeme missions dynamiques (entity, service, controller, scheduler)`
- `298a025 feat(feature5): ajout systeme de missions dynamiques (entity, service, controller, scheduler)`
- `2323296 mini reorganisation - ajouter recap F5`
