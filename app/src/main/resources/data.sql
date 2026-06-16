-- ============================================================
-- DONNÉES DE TEST COMPLÈTES — SportsIn
-- Couvre toutes les features : auth, météo, missions, perks, chat
-- ============================================================
-- Pour repartir de zéro : supprimez sportsin.db puis relancez
-- l'application (schema.sql + ce fichier se ré-exécutent).
-- ============================================================

-- Nettoyage des anciennes sessions héritées (legacy IDs)
DELETE FROM metric_value       WHERE session_id IN ('session_001','session_002','session_003');
DELETE FROM session_participant WHERE session_id IN ('session_001','session_002','session_003');
DELETE FROM session            WHERE id          IN ('session_001','session_002','session_003');

-- ============================================================
-- ÉQUIPES
-- Niveaux (XP requis) : L2=100 | L3=300 | L4=600 | L5=1000
-- ============================================================
INSERT OR IGNORE INTO equipe (id, nom, points, xp, couleur) VALUES
  (1, 'AS Monaco',           180, 1200, '#E40510'),   -- Niveau 5 → tous les perks débloqués
  (2, 'Paris Saint-Germain', 210, 1050, '#004170'),   -- Niveau 5
  (3, 'Olympique Lyonnais',   90,  650, '#0033A0'),   -- Niveau 4 → AMPHIBIEN débloqué
  (4, 'FC Nantes',            55,  320, '#FFCD00'),   -- Niveau 3 → SHIELD_QUARTIER débloqué
  (5, 'Stade Brest',          35,  110, '#E30513');   -- Niveau 2 → XP_BOOST débloqué

-- ============================================================
-- JOUEURS (avec email + password pour tester l'authentification)
-- Login : pseudo OU email   |   Mot de passe : Sportsin1
-- ============================================================
INSERT OR IGNORE INTO joueur (id, pseudo, email, password, equipe_id) VALUES
  -- Équipe 1 : AS Monaco
  (1,  'BenYedder',    'benyedder@sportsin.test',   'Sportsin1', 1),
  (2,  'Golovin',      'golovin@sportsin.test',     'Sportsin1', 1),
  (3,  'CaioHenrique', 'caiohenrique@sportsin.test','Sportsin1', 1),
  (4,  'Vanderson',    'vanderson@sportsin.test',   'Sportsin1', 1),
  (5,  'Magalhaes',    'magalhaes@sportsin.test',   'Sportsin1', 1),
  -- Équipe 2 : PSG
  (6,  'Mbappe',       'mbappe@sportsin.test',      'Sportsin1', 2),
  (7,  'Neymar',       'neymar@sportsin.test',      'Sportsin1', 2),
  (8,  'Cavani',       'cavani@sportsin.test',      'Sportsin1', 2),
  (9,  'Verratti',     'verratti@sportsin.test',    'Sportsin1', 2),
  (10, 'Marquinhos',   'marquinhos@sportsin.test',  'Sportsin1', 2),
  -- Équipe 3 : Olympique Lyonnais
  (11, 'Lacazette',    'lacazette@sportsin.test',   'Sportsin1', 3),
  (12, 'Cherki',       'cherki@sportsin.test',      'Sportsin1', 3),
  (13, 'ThiagoMendes', 'thiagomendes@sportsin.test','Sportsin1', 3),
  (14, 'Denayer',      'denayer@sportsin.test',     'Sportsin1', 3),
  -- Équipe 4 : FC Nantes
  (15, 'KoloMuani',    'kolomuani@sportsin.test',   'Sportsin1', 4),
  (16, 'Simon',        'simon@sportsin.test',       'Sportsin1', 4),
  (17, 'Castelletto',  'castelletto@sportsin.test', 'Sportsin1', 4),
  -- Équipe 5 : Stade Brest
  (18, 'Camara',       'camara@sportsin.test',      'Sportsin1', 5),
  (19, 'Faivre',       'faivre@sportsin.test',      'Sportsin1', 5),
  (20, 'Duverne',      'duverne@sportsin.test',     'Sportsin1', 5);

-- ============================================
-- ZONES DÉPARTEMENTS IDF
-- ============================================
INSERT OR IGNORE INTO zone_departement (code, nom, total_influence) VALUES ('75', 'Paris', 0);
INSERT OR IGNORE INTO zone_departement (code, nom, total_influence) VALUES ('77', 'Seine-et-Marne', 0);
INSERT OR IGNORE INTO zone_departement (code, nom, total_influence) VALUES ('78', 'Yvelines', 0);
INSERT OR IGNORE INTO zone_departement (code, nom, total_influence) VALUES ('91', 'Essonne', 0);
INSERT OR IGNORE INTO zone_departement (code, nom, total_influence) VALUES ('92', 'Hauts-de-Seine', 0);
INSERT OR IGNORE INTO zone_departement (code, nom, total_influence) VALUES ('93', 'Seine-Saint-Denis', 0);
INSERT OR IGNORE INTO zone_departement (code, nom, total_influence) VALUES ('94', 'Val-de-Marne', 0);
INSERT OR IGNORE INTO zone_departement (code, nom, total_influence) VALUES ('95', "Val-d'Oise", 0);

-- ============================================
-- ARÈNES (Stades hors IDF - données existantes)
-- ============================================
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle) VALUES
  ('stade_louis_ii', 'Stade Louis II', 43.7384, 7.4246, 1);
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle) VALUES
  ('groupama_stadium', 'Groupama Stadium', 45.7735, 4.8922, 3);
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle) VALUES
  ('stade_beaujoire', 'Stade de la Beaujoire', 47.2679, -1.4804, 4);
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle) VALUES
  ('stade_francis_le_ble', 'Stade Francis Le Blé', 48.3988, -4.4860, 5);
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle) VALUES
  ('velodrome', 'Stade Vélodrome', 43.2620, 5.3963, NULL);

-- ============================================
-- ARÈNES IDF - Paris (75)
-- ============================================
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('parc_princes', 'Parc des Princes', 48.8432, 2.2527, 2, '75');
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_charlety', 'Stade Charléty', 48.8188, 2.3453, NULL, '75');
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('accor_arena', 'Accor Arena', 48.8382, 2.3783, NULL, '75');

-- ============================================
-- ARÈNES IDF - Seine-et-Marne (77)
-- ============================================
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_melun', 'Stade Municipal de Melun', 48.5380, 2.6600, NULL, '77');
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('complexe_fontainebleau', 'Complexe sportif Fontainebleau', 48.4046, 2.7028, NULL, '77');

-- ============================================
-- ARÈNES IDF - Yvelines (78)
-- ============================================
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_montbauron', 'Stade Montbauron Versailles', 48.8023, 2.1380, NULL, '78');
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('golf_national', 'Golf National Guyancourt', 48.7449, 2.0714, NULL, '78');

-- ============================================
-- ARÈNES IDF - Essonne (91)
-- ============================================
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_corbeil', 'Stade de Corbeil-Essonnes', 48.6143, 2.4778, NULL, '91');
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('complexe_evry', 'Complexe sportif Évry', 48.6325, 2.4218, NULL, '91');

-- ============================================
-- ARÈNES IDF - Hauts-de-Seine (92)
-- ============================================
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('paris_la_defense_arena', 'Paris La Défense Arena', 48.8974, 2.2261, NULL, '92');
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_fauvettes', 'Stade des Fauvettes Bagneux', 48.7893, 2.3060, NULL, '92');

-- ============================================
-- ARÈNES IDF - Seine-Saint-Denis (93)
-- ============================================
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_de_france', 'Stade de France', 48.9244, 2.3601, NULL, '93');
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_bauer', 'Stade Bauer Saint-Ouen', 48.9104, 2.3291, NULL, '93');

-- ============================================
-- ARÈNES IDF - Val-de-Marne (94)
-- ============================================
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_vincennes', 'Stade des Sports de Vincennes', 48.8456, 2.4337, NULL, '94');
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_duvauchelle', 'Stade Duvauchelle Créteil', 48.7847, 2.4583, NULL, '94');

-- ============================================
-- ARÈNES IDF - Val-d'Oise (95)
-- ============================================
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_cergy', 'Stade de Cergy', 49.0362, 2.0669, NULL, '95');
INSERT OR IGNORE INTO arene (id, nom, latitude, longitude, equipe_controle, departement) VALUES
  ('stade_montmorency', 'Stade de Montmorency', 49.0028, 2.3236, NULL, '95');

-- ============================================================
-- SPORTS DISPONIBLES PAR ARÈNE
-- Volontairement élargis par rapport aux données initiales
-- pour permettre le test des missions DIVERSITY_SPORT
-- ============================================================
INSERT OR IGNORE INTO arene_sport (arene_id, sport_type) VALUES
  ('stade_louis_ii',       'FOOTBALL'),
  ('stade_louis_ii',       'BASKET'),
  ('stade_louis_ii',       'MUSCULATION'),
  ('parc_princes',         'FOOTBALL'),
  ('parc_princes',         'BASKET'),
  ('parc_princes',         'TENNIS'),
  ('groupama_stadium',     'FOOTBALL'),
  ('groupama_stadium',     'BASKET'),
  ('groupama_stadium',     'MUSCULATION'),
  ('stade_beaujoire',      'FOOTBALL'),
  ('stade_beaujoire',      'BASKET'),
  ('stade_beaujoire',      'MUSCULATION'),
  ('stade_francis_le_ble', 'FOOTBALL'),
  ('stade_francis_le_ble', 'TENNIS'),
  ('velodrome',            'FOOTBALL'),
  ('velodrome',            'BASKET'),
  ('velodrome',            'TENNIS'),
  ('velodrome',            'MUSCULATION');

-- ============================================================
-- SPORTS
-- ============================================================
INSERT OR IGNORE INTO sport (id, code, name) VALUES
  (1, 'FOOTBALL',    'Football'),
  (2, 'BASKET',      'Basketball'),
  (3, 'TENNIS',      'Tennis'),
  (4, 'MUSCULATION', 'Musculation');

-- ============================================================
-- SESSIONS TERMINÉES (historique de matchs)
-- ⚠️  Ces sessions sont persistées en base JPA mais NE SONT PAS
--     visibles par le SessionRepository in-memory.
--     Les badges météo (Feature 1) nécessitent des sessions
--     jouées EN DIRECT pendant l'exécution du serveur.
-- ============================================================
INSERT OR IGNORE INTO session (id, sport_id, point_id, state, created_at, ended_at, winner_participant_id) VALUES
  ('sess_01', 1, 'stade_louis_ii',      'TERMINATED','2026-06-01T10:00:00Z','2026-06-01T11:30:00Z','1'),
  ('sess_02', 1, 'parc_princes',        'TERMINATED','2026-06-01T14:00:00Z','2026-06-01T15:30:00Z','2'),
  ('sess_03', 2, 'groupama_stadium',    'TERMINATED','2026-06-02T09:00:00Z','2026-06-02T10:00:00Z','3'),
  ('sess_04', 1, 'velodrome',           'TERMINATED','2026-06-02T16:00:00Z','2026-06-02T17:30:00Z','2'),
  ('sess_05', 3, 'parc_princes',        'TERMINATED','2026-06-03T11:00:00Z','2026-06-03T12:00:00Z','1'),
  ('sess_06', 4, 'stade_beaujoire',     'TERMINATED','2026-06-03T15:00:00Z','2026-06-03T16:00:00Z','4'),
  ('sess_07', 1, 'stade_francis_le_ble','TERMINATED','2026-06-04T10:00:00Z','2026-06-04T11:30:00Z','1'),
  ('sess_08', 2, 'velodrome',           'TERMINATED','2026-06-04T14:00:00Z','2026-06-04T15:00:00Z','1'),
  ('sess_09', 1, 'groupama_stadium',    'TERMINATED','2026-06-05T09:00:00Z','2026-06-05T10:30:00Z','3'),
  ('sess_10', 4, 'stade_louis_ii',      'TERMINATED','2026-06-05T15:00:00Z','2026-06-05T16:00:00Z','1'),
  ('sess_11', 1, 'stade_beaujoire',     'TERMINATED','2026-06-06T10:00:00Z','2026-06-06T11:30:00Z','4'),
  ('sess_12', 2, 'stade_louis_ii',      'TERMINATED','2026-06-07T14:00:00Z','2026-06-07T15:00:00Z','2'),
  ('sess_13', 3, 'velodrome',           'TERMINATED','2026-06-08T09:00:00Z','2026-06-08T10:00:00Z','2'),
  ('sess_14', 1, 'groupama_stadium',    'TERMINATED','2026-06-08T16:00:00Z','2026-06-08T17:30:00Z','1');

-- ============================================================
-- PARTICIPANTS AUX SESSIONS
-- ============================================================
INSERT OR IGNORE INTO session_participant (session_id, participant_id, participant_type, participant_name) VALUES
  ('sess_01','1','TEAM','AS Monaco'),       ('sess_01','3','TEAM','Olympique Lyonnais'),
  ('sess_02','2','TEAM','PSG'),             ('sess_02','5','TEAM','Stade Brest'),
  ('sess_03','3','TEAM','Olympique Lyonnais'),('sess_03','4','TEAM','FC Nantes'),
  ('sess_04','2','TEAM','PSG'),             ('sess_04','1','TEAM','AS Monaco'),
  ('sess_05','1','TEAM','AS Monaco'),       ('sess_05','2','TEAM','PSG'),
  ('sess_06','4','TEAM','FC Nantes'),       ('sess_06','5','TEAM','Stade Brest'),
  ('sess_07','1','TEAM','AS Monaco'),       ('sess_07','3','TEAM','Olympique Lyonnais'),
  ('sess_08','1','TEAM','AS Monaco'),       ('sess_08','4','TEAM','FC Nantes'),
  ('sess_09','3','TEAM','Olympique Lyonnais'),('sess_09','2','TEAM','PSG'),
  ('sess_10','1','TEAM','AS Monaco'),       ('sess_10','5','TEAM','Stade Brest'),
  ('sess_11','4','TEAM','FC Nantes'),       ('sess_11','2','TEAM','PSG'),
  ('sess_12','2','TEAM','PSG'),             ('sess_12','3','TEAM','Olympique Lyonnais'),
  ('sess_13','2','TEAM','PSG'),             ('sess_13','5','TEAM','Stade Brest'),
  ('sess_14','1','TEAM','AS Monaco'),       ('sess_14','4','TEAM','FC Nantes');

-- ============================================================
-- MÉTRIQUES DES SESSIONS (scores)
-- ============================================================
INSERT OR IGNORE INTO metric_value (id, session_id, participant_id, metric_type, value) VALUES
  (1, 'sess_01','1','GOALS',     3.0),(2, 'sess_01','3','GOALS',     1.0),
  (3, 'sess_02','2','GOALS',     2.0),(4, 'sess_02','5','GOALS',     0.0),
  (5, 'sess_03','3','POINTS',   88.0),(6, 'sess_03','4','POINTS',   76.0),
  (7, 'sess_04','2','GOALS',     4.0),(8, 'sess_04','1','GOALS',     2.0),
  (9, 'sess_05','1','POINTS',   95.0),(10,'sess_05','2','POINTS',   82.0),
  (11,'sess_06','4','REPS',    120.0),(12,'sess_06','5','REPS',     95.0),
  (13,'sess_07','1','GOALS',     2.0),(14,'sess_07','3','GOALS',     0.0),
  (15,'sess_08','1','POINTS',   91.0),(16,'sess_08','4','POINTS',   78.0),
  (17,'sess_09','3','GOALS',     2.0),(18,'sess_09','2','GOALS',     1.0),
  (19,'sess_10','1','REPS',    145.0),(20,'sess_10','5','REPS',    110.0),
  (21,'sess_11','4','GOALS',     1.0),(22,'sess_11','2','GOALS',     0.0),
  (23,'sess_12','2','POINTS',   89.0),(24,'sess_12','3','POINTS',   74.0),
  (25,'sess_13','2','POINTS',   92.0),(26,'sess_13','5','POINTS',   71.0),
  (27,'sess_14','1','GOALS',     3.0),(28,'sess_14','4','GOALS',     2.0);

-- ============================================================
-- PERK DEFINITIONS (Feature 6 — catalogue complet)
-- ============================================================
INSERT OR IGNORE INTO perk_definition
  (id, code, name, description, effect_type, required_level, duration_seconds, cooldown_seconds, max_active_instances, stackable, parameters_json)
VALUES
  (1,'SHIELD_QUARTIER','Bouclier de quartier',
     'Réduit de 50 % les gains d''influence adverses sur un point contrôlé pendant 3 jours.',
     'INFLUENCE_REDUCTION', 3, 259200, 604800, 1, 0,
     '{"reductionPercent":50,"targetType":"POINT"}'),
  (2,'BOOST_INFLUENCE','Surge d''influence',
     'Augmente de 25 % les gains d''influence sur un point pendant 2 jours.',
     'INFLUENCE_BOOST', 5, 172800, 432000, 1, 0,
     '{"boostPercent":25,"targetType":"POINT"}'),
  (3,'XP_BOOST','Entraînement intensif',
     'Multiplie par 1,5 les gains d''XP pendant 24 heures.',
     'XP_MULTIPLIER', 2, 86400, 259200, 1, 0,
     '{"multiplier":1.5}'),
  (4,'AMPHIBIEN','Amphibien',
     'Exploite la pluie : supprime la pénalité météo et ajoute un bonus offensif.',
     'WEATHER_AFFINITY', 4, 172800, 345600, 1, 0,
     '{"condition":"RAIN","weatherBonusBoostPercent":100,"flatInfluenceBonusPercent":15}'),
  (5,'THERMO_RUNNER','Thermo Runner',
     'Spécialisation canicule : bonus d''influence en forte chaleur.',
     'WEATHER_AFFINITY', 5, 172800, 345600, 1, 0,
     '{"condition":"HEAT","weatherBonusBoostPercent":70,"flatInfluenceBonusPercent":10}'),
  (6,'AERO_STRIKE','Aero Strike',
     'Exploite les rafales de vent pour renforcer les offensives.',
     'WEATHER_AFFINITY', 5, 172800, 345600, 1, 0,
     '{"condition":"WIND","weatherBonusBoostPercent":60,"flatInfluenceBonusPercent":8}');

-- ============================================================
-- ACTIVE PERKS (Feature 6 — perks en cours d'utilisation)
-- expires_at >= 2026-06-12 pour rester valides lors des tests
-- ============================================================
INSERT OR IGNORE INTO active_perk
  (id, team_id, perk_definition_id, target_id, activated_at, expires_at, usage_count)
VALUES
  -- AS Monaco (niv.5) : Boost influence sur son fief + Amphibien sur le Vélodrome ennemi
  (1, 1, 2, 'stade_louis_ii',   '2026-06-09T08:00:00Z','2026-06-11T08:00:00Z', 1),
  (2, 1, 4, 'velodrome',        '2026-06-09T08:00:00Z','2026-06-11T08:00:00Z', 0),
  -- PSG (niv.5) : Aero Strike sur le Vélodrome + Thermo Runner sur le Parc
  (3, 2, 6, 'velodrome',        '2026-06-09T10:00:00Z','2026-06-11T10:00:00Z', 2),
  (4, 2, 5, 'parc_princes',     '2026-06-09T10:00:00Z','2026-06-11T10:00:00Z', 1),
  -- OL (niv.4) : Bouclier sur Groupama Stadium
  (5, 3, 1, 'groupama_stadium', '2026-06-08T14:00:00Z','2026-06-11T14:00:00Z', 3),
  -- FC Nantes (niv.3) : Bouclier sur la Beaujoire
  (6, 4, 1, 'stade_beaujoire',  '2026-06-08T14:00:00Z','2026-06-11T14:00:00Z', 1),
  -- Stade Brest (niv.2) : XP Boost global (pas de target spécifique)
  (7, 5, 3, NULL,               '2026-06-09T06:00:00Z','2026-06-10T06:00:00Z', 0);

-- ============================================================
-- MISSIONS (Feature 5 — max 3 actives par équipe)
-- Toutes les missions expirent le 2026-06-16 (7 jours)
-- sauf la mission flash PSG qui expire le 2026-06-11 (48h)
-- ============================================================
INSERT OR IGNORE INTO mission
  (id, team_id, type, status, title, description, priority,
   reward_team_points, reward_team_xp,
   created_at, starts_at, ends_at,
   payload_json, progress_current, progress_target)
VALUES

  -- ── AS Monaco ──────────────────────────────────────────────
  (1, 1,'RECAPTURE_RECENT_LOSS','ACTIVE',
   'Reconquête du Vélodrome',
   'Le Vélodrome est aux mains du PSG. Reprenez-le avant la fin de la semaine !',
   'HIGH', 50, 150,
   '2026-06-09T08:00:00Z','2026-06-09T08:00:00Z','2026-06-16T08:00:00Z',
   '{"arenaId":"velodrome","arenaName":"Stade Vélodrome","rivalTeamId":2}',
   0, 1),

  (2, 1,'DIVERSITY_SPORT','ACTIVE',
   'Basketball au Stade Louis II',
   'Disputez 2 sessions de Basketball à domicile pour marquer de l''XP bonus.',
   'MEDIUM', 20, 60,
   '2026-06-09T08:00:00Z','2026-06-09T08:00:00Z','2026-06-16T08:00:00Z',
   '{"sport":"BASKET","arenaId":"stade_louis_ii"}',
   0, 2),

  (3, 1,'BREAK_ROUTE','ACTIVE',
   'Briser la route du PSG',
   'PSG contrôle le Vélodrome ET le Parc des Princes. Remportez une victoire au Parc !',
   'HIGH', 40, 120,
   '2026-06-09T08:00:00Z','2026-06-09T08:00:00Z','2026-06-16T08:00:00Z',
   '{"targetTeamId":2,"targetArenaId":"parc_princes"}',
   0, 1),

  -- ── Paris Saint-Germain ─────────────────────────────────────
  (4, 2,'BREAK_ROUTE','ACTIVE',
   'Stoppez Monaco à domicile',
   'Monaco tient le Stade Louis II et le Francis Le Blé. Battez-les sur leur terrain !',
   'HIGH', 45, 130,
   '2026-06-09T09:00:00Z','2026-06-09T09:00:00Z','2026-06-16T09:00:00Z',
   '{"targetTeamId":1,"targetArenaId":"stade_louis_ii"}',
   0, 1),

  -- Mission WEATHER_FLASH : DIVERSITY_SPORT avec catégorie spéciale
  (5, 2,'DIVERSITY_SPORT','ACTIVE',
   '⚡ Flash Météo — Tempête au Parc',
   'Remportez une victoire Football au Parc des Princes sous la pluie pour un bonus d''influence exceptionnel.',
   'HIGH', 60, 200,
   '2026-06-09T09:00:00Z','2026-06-09T09:00:00Z','2026-06-11T09:00:00Z',
   '{"missionCategory":"WEATHER_FLASH","sport":"FOOTBALL","conditionTag":"RAIN","arenaId":"parc_princes","bonusInfluencePercent":50}',
   0, 1),

  (6, 2,'RECAPTURE_RECENT_LOSS','ACTIVE',
   'Reprendre Groupama Stadium',
   'L''OL a pris Groupama. Il est temps de le reconquérir !',
   'MEDIUM', 30, 90,
   '2026-06-09T09:00:00Z','2026-06-09T09:00:00Z','2026-06-16T09:00:00Z',
   '{"arenaId":"groupama_stadium","arenaName":"Groupama Stadium","rivalTeamId":3}',
   0, 1),

  -- ── Olympique Lyonnais ──────────────────────────────────────
  (7, 3,'RECAPTURE_RECENT_LOSS','ACTIVE',
   'Attaque sur la Beaujoire',
   'FC Nantes tient la Beaujoire. Remportez-y une victoire pour OL.',
   'MEDIUM', 30, 90,
   '2026-06-09T08:00:00Z','2026-06-09T08:00:00Z','2026-06-16T08:00:00Z',
   '{"arenaId":"stade_beaujoire","arenaName":"Stade de la Beaujoire","rivalTeamId":4}',
   0, 1),

  (8, 3,'DIVERSITY_SPORT','ACTIVE',
   'Musculation à Groupama',
   'Explorez la Musculation au Groupama Stadium : 2 sessions pour débloquer un XP bonus.',
   'LOW', 15, 45,
   '2026-06-09T08:00:00Z','2026-06-09T08:00:00Z','2026-06-16T08:00:00Z',
   '{"sport":"MUSCULATION","arenaId":"groupama_stadium"}',
   0, 2),

  (9, 3,'BREAK_ROUTE','ACTIVE',
   'Neutraliser Monaco au Sud',
   'Monaco domine le Sud de la France. Battez-les au Stade Louis II.',
   'HIGH', 40, 110,
   '2026-06-09T08:00:00Z','2026-06-09T08:00:00Z','2026-06-16T08:00:00Z',
   '{"targetTeamId":1,"targetArenaId":"stade_louis_ii"}',
   0, 1),

  -- ── FC Nantes ───────────────────────────────────────────────
  (10, 4,'DIVERSITY_SPORT','ACTIVE',
   'Basketball à la Beaujoire',
   'Organisez 3 sessions Basketball à domicile pour consolider votre position.',
   'MEDIUM', 20, 55,
   '2026-06-09T07:00:00Z','2026-06-09T07:00:00Z','2026-06-16T07:00:00Z',
   '{"sport":"BASKET","arenaId":"stade_beaujoire"}',
   0, 3),

  (11, 4,'BREAK_ROUTE','ACTIVE',
   'Stoppez Monaco au Francis Le Blé',
   'Monaco contrôle deux arènes en série. Battez-les à Brest.',
   'MEDIUM', 30, 80,
   '2026-06-09T07:00:00Z','2026-06-09T07:00:00Z','2026-06-16T07:00:00Z',
   '{"targetTeamId":1,"targetArenaId":"stade_francis_le_ble"}',
   0, 1),

  -- ── Stade Brest ─────────────────────────────────────────────
  (12, 5,'DIVERSITY_SPORT','ACTIVE',
   'Tennis au Francis Le Blé',
   'Découvrez le Tennis à domicile : 2 sessions pour diversifier votre jeu.',
   'LOW', 15, 40,
   '2026-06-09T06:00:00Z','2026-06-09T06:00:00Z','2026-06-16T06:00:00Z',
   '{"sport":"TENNIS","arenaId":"stade_francis_le_ble"}',
   0, 2),

  (13, 5,'RECAPTURE_RECENT_LOSS','ACTIVE',
   'Reprendre le Francis Le Blé',
   'Monaco nous a pris notre stade ! Reprenons-le dès que possible.',
   'HIGH', 40, 120,
   '2026-06-09T06:00:00Z','2026-06-09T06:00:00Z','2026-06-16T06:00:00Z',
   '{"arenaId":"stade_francis_le_ble","arenaName":"Stade Francis Le Blé","rivalTeamId":1}',
   0, 1);

-- ============================================================
-- MESSAGES CHAT (Feature chat d'équipe)
-- envoye_a : ISO 8601, joueur_id doit appartenir à l''equipe_id
-- ============================================================
INSERT OR IGNORE INTO message (id, contenu, envoye_a, joueur_id, equipe_id) VALUES
  -- Équipe 1 : AS Monaco
  (1,  'On reconquiert le Vélodrome ce soir ! 💪',         '2026-06-09T09:10:00Z',  1, 1),
  (2,  'J''ai activé l''Amphibien, on est prêts pour la pluie 🌧️', '2026-06-09T09:15:00Z', 2, 1),
  (3,  'Qui est dispo pour le match à 20h ?',              '2026-06-09T10:00:00Z',  3, 1),
  (4,  'Présent ! On va leur montrer 🔥',                  '2026-06-09T10:05:00Z',  4, 1),

  -- Équipe 2 : PSG
  (5,  'La mission flash est là, il faut jouer sous la pluie ⚡', '2026-06-09T09:30:00Z', 6, 2),
  (6,  'Aero Strike activé sur le Vélodrome, go win 💨',   '2026-06-09T09:35:00Z',  7, 2),
  (7,  'Monaco nous cherche, on tient le Parc des Princes !', '2026-06-09T10:20:00Z', 8, 2),
  (8,  'Quelqu''un vérifie la météo avant le match ?',     '2026-06-09T10:45:00Z',  9, 2),

  -- Équipe 3 : Olympique Lyonnais
  (9,  'Le Bouclier de Groupama tient, bien joué !',       '2026-06-09T08:50:00Z', 11, 3),
  (10, 'Il faut 2 sessions muscu pour la mission 🏋️',      '2026-06-09T09:00:00Z', 12, 3),
  (11, 'J''ai vu les prévisions, vent fort demain → profitez-en si vous avez Aero Strike', '2026-06-09T11:00:00Z', 13, 3),

  -- Équipe 4 : FC Nantes
  (12, 'La Beaujoire est à nous, on la garde ! 💛',        '2026-06-09T08:30:00Z', 15, 4),
  (13, 'Mission basket : 3 sessions, qui commence ce soir ?', '2026-06-09T09:00:00Z', 16, 4),
  (14, 'Monaco rôde au Francis Le Blé, soyez vigilants',   '2026-06-09T09:45:00Z', 17, 4),

  -- Équipe 5 : Stade Brest
  (15, 'Monaco nous a pris le Francis Le Blé 😤 On reprend ça maintenant', '2026-06-09T08:00:00Z', 18, 5),
  (16, 'XP Boost activé jusqu''à demain, profitez-en !',   '2026-06-09T08:05:00Z', 19, 5),
  (17, 'Tennis en soirée ? La mission attend',             '2026-06-09T10:30:00Z', 20, 5);
