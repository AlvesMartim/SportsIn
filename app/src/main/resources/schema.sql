-- Script SQL pour créer la base de données SQLite du projet SportsIn
-- Basé sur le schéma défini dans Stockage.txt

-- Table EQUIPE
CREATE TABLE IF NOT EXISTS equipe (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom TEXT NOT NULL UNIQUE
);

-- Table JOUEUR
CREATE TABLE IF NOT EXISTS joueur (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pseudo TEXT NOT NULL UNIQUE,
    equipe_id INTEGER,
    FOREIGN KEY (equipe_id) REFERENCES equipe(id) ON DELETE SET NULL
);

-- Table ARENE
CREATE TABLE IF NOT EXISTS arene (
    id TEXT PRIMARY KEY,
    nom TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    equipe_controle INTEGER,
    FOREIGN KEY (equipe_controle) REFERENCES equipe(id) ON DELETE SET NULL
);

-- Table de jointure ARENE_SPORT (ManyToMany entre ARENE et SportType)
CREATE TABLE IF NOT EXISTS arene_sport (
    arene_id TEXT NOT NULL,
    sport_type TEXT NOT NULL CHECK (sport_type IN ('FOOTBALL', 'MUSCULATION', 'BASKET', 'TENNIS')),
    PRIMARY KEY (arene_id, sport_type),
    FOREIGN KEY (arene_id) REFERENCES arene(id) ON DELETE CASCADE
);

-- Table SPORT
CREATE TABLE IF NOT EXISTS sport (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    victory_rule_id INTEGER,
    scoring_rule_id INTEGER
);

-- Table SESSION
CREATE TABLE IF NOT EXISTS session (
    id TEXT PRIMARY KEY,
    sport_id INTEGER NOT NULL,
    point_id TEXT,
    state TEXT NOT NULL CHECK (state IN ('ACTIVE', 'TERMINATED')),
    created_at TEXT NOT NULL,
    ended_at TEXT,
    winner_participant_id TEXT,
    FOREIGN KEY (sport_id) REFERENCES sport(id) ON DELETE RESTRICT
);

-- Table de jointure SESSION_PARTICIPANT (ManyToMany entre SESSION et Participant)
CREATE TABLE IF NOT EXISTS session_participant (
    session_id TEXT NOT NULL,
    participant_id TEXT NOT NULL,
    participant_type TEXT NOT NULL CHECK (participant_type IN ('PLAYER', 'TEAM')),
    participant_name TEXT NOT NULL,
    PRIMARY KEY (session_id, participant_id),
    FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE
);

-- Table METRIC_VALUE
CREATE TABLE IF NOT EXISTS metric_value (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id TEXT NOT NULL,
    participant_id TEXT NOT NULL,
    metric_type TEXT NOT NULL CHECK (metric_type IN ('GOALS', 'POINTS', 'TIME_SECONDS', 'REPS', 'CUSTOM')),
    value REAL NOT NULL,
    context TEXT,
    FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances des requêtes fréquentes
CREATE INDEX IF NOT EXISTS idx_joueur_equipe_id ON joueur(equipe_id);
CREATE INDEX IF NOT EXISTS idx_arene_equipe_controle ON arene(equipe_controle);
CREATE INDEX IF NOT EXISTS idx_session_sport_id ON session(sport_id);
CREATE INDEX IF NOT EXISTS idx_session_state ON session(state);
CREATE INDEX IF NOT EXISTS idx_metric_value_session_id ON metric_value(session_id);
CREATE INDEX IF NOT EXISTS idx_session_participant_session_id ON session_participant(session_id);

