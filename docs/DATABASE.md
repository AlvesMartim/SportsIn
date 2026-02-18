# Base de données SQLite - SportsIn

Ce document décrit la structure de la base de données SQLite du projet SportsIn.

## Création de la base de données

Pour créer la base de données SQLite, exécutez le script suivant :

```bash
./create_database.sh
```

Ce script crée un fichier `sportsin.db` dans le répertoire racine du projet.

## Structure de la base de données

### Tables principales

1. **EQUIPE** - Stocke les équipes
   - `id` (INTEGER, PK, AUTOINCREMENT)
   - `nom` (TEXT, UNIQUE, NOT NULL)

2. **JOUEUR** - Stocke les joueurs individuels
   - `id` (INTEGER, PK, AUTOINCREMENT)
   - `pseudo` (TEXT, UNIQUE, NOT NULL)
   - `equipe_id` (INTEGER, FK vers EQUIPE, nullable)

3. **ARENE** - Stocke les arènes (points sportifs sur la carte)
   - `id` (TEXT, PK)
   - `nom` (TEXT, NOT NULL)
   - `latitude` (REAL, NOT NULL)
   - `longitude` (REAL, NOT NULL)
   - `equipe_controle` (INTEGER, FK vers EQUIPE, nullable)

4. **ARENE_SPORT** - Table de jointure (ManyToMany)
   - `arene_id` (TEXT, FK vers ARENE, PK)
   - `sport_type` (TEXT, PK) - Valeurs: 'FOOTBALL', 'MUSCULATION', 'BASKET', 'TENNIS'

5. **SPORT** - Stocke les sports disponibles
   - `id` (INTEGER, PK, AUTOINCREMENT)
   - `code` (TEXT, UNIQUE, NOT NULL)
   - `name` (TEXT, NOT NULL)
   - `victory_rule_id` (INTEGER, nullable)
   - `scoring_rule_id` (INTEGER, nullable)

6. **SESSION** - Stocke les sessions de sport
   - `id` (TEXT, PK)
   - `sport_id` (INTEGER, FK vers SPORT, NOT NULL)
   - `point_id` (TEXT, nullable)
   - `state` (TEXT, NOT NULL) - Valeurs: 'ACTIVE', 'TERMINATED'
   - `created_at` (TEXT, NOT NULL)
   - `ended_at` (TEXT, nullable)
   - `winner_participant_id` (TEXT, nullable)

7. **SESSION_PARTICIPANT** - Table de jointure (ManyToMany)
   - `session_id` (TEXT, FK vers SESSION, PK)
   - `participant_id` (TEXT, PK)
   - `participant_type` (TEXT, PK) - Valeurs: 'PLAYER', 'TEAM'
   - `participant_name` (TEXT, NOT NULL)

8. **METRIC_VALUE** - Stocke les métriques des sessions
   - `id` (INTEGER, PK, AUTOINCREMENT)
   - `session_id` (TEXT, FK vers SESSION, NOT NULL)
   - `participant_id` (TEXT, NOT NULL)
   - `metric_type` (TEXT, NOT NULL) - Valeurs: 'GOALS', 'POINTS', 'TIME_SECONDS', 'REPS', 'CUSTOM'
   - `value` (REAL, NOT NULL)
   - `context` (TEXT, nullable)

## Relations

- **EQUIPE** 1──N **JOUEUR** (OneToMany)
- **EQUIPE** 1──0..1 **ARENE** (OneToMany, nullable)
- **ARENE** N──N **SportType** (ManyToMany via ARENE_SPORT)
- **SPORT** 1──N **SESSION** (OneToMany)
- **SESSION** N──N **Participant** (ManyToMany via SESSION_PARTICIPANT)
- **SESSION** 1──N **METRIC_VALUE** (OneToMany)

## Utilisation

### Accéder à la base de données

```bash
sqlite3 sportsin.db
```

### Commandes SQLite utiles

```sql
-- Voir toutes les tables
.tables

-- Voir le schéma d'une table
.schema nom_table

-- Voir le schéma complet
.schema

-- Quitter
.quit
```

### Exemples de requêtes

```sql
-- Lister toutes les équipes
SELECT * FROM equipe;

-- Lister tous les joueurs avec leur équipe
SELECT j.id, j.pseudo, e.nom AS equipe
FROM joueur j
LEFT JOIN equipe e ON j.equipe_id = e.id;

-- Lister toutes les arènes avec leur équipe contrôlante
SELECT a.id, a.nom, a.latitude, a.longitude, e.nom AS equipe_controle
FROM arene a
LEFT JOIN equipe e ON a.equipe_controle = e.id;

-- Lister toutes les sessions actives
SELECT s.id, sp.name AS sport, s.state, s.created_at
FROM session s
JOIN sport sp ON s.sport_id = sp.id
WHERE s.state = 'ACTIVE';
```

## Fichiers

- `app/src/main/resources/schema.sql` - Script SQL de création du schéma
- `create_database.sh` - Script shell pour créer la base de données
- `sportsin.db` - Fichier de base de données SQLite (créé après exécution du script)

