# Feature 7 : État Réel d'Implémentation

Ce document résume ce qui est réellement codé dans le projet pour la Feature 7 (météo active), et distingue BackEnd / FrontEnd.

## Statut global

- BackEnd : implémenté partiellement à fortement, avec moteur météo, missions flash, affinités d'équipe, usure territoriale et scheduling.

## BackEnd (implémenté)

### 1) Hard Mode météo sur la fin de session

- Intégration d'un client OpenWeather et d'un moteur de pénibilité météo par sport (Strategy Pattern).
- Calcul d'un indice de pénibilité, puis conversion en bonus d'influence appliqué à la conquête.
- Enrichissement de `SessionResult` avec les métadonnées météo calculées (source, tags, résumé, température, vent, pluie, multiplicateurs).

Note technique importante : l'implémentation interroge la météo courante et prévisionnelle; la météo historique n'est pas encore branchée.

### 2) Quêtes Flash météo

- Ajout d'un service de génération basé sur les prévisions à 24h.
- Création automatique de missions de type "alerte" quand un événement extrême approche.
- Évaluation de réussite implémentée (victoire d'équipe sur l'arène avant l'heure d'événement).

Contrainte actuelle : pour rester compatible avec le schéma SQL existant, les missions flash sont stockées en `DIVERSITY_SPORT` avec `missionCategory=WEATHER_FLASH` dans le payload.

### 3) Affinités d'équipe (perks météo)

- Ajout de l'effet `WEATHER_AFFINITY` côté perks.
- Service de calcul de bonus d'affinité selon les tags météo (RAIN, HEAT, WIND, etc.).
- Seeds SQL ajoutés pour des perks météo (AMPHIBIEN, THERMO_RUNNER, AERO_STRIKE).

### 4) Usure territoriale PvE

- Ajout d'un état d'influence territoriale runtime par arène.
- Décroissance naturelle quotidienne.
- Décroissance accélérée si météo extrême prolongée détectée.
- Perte automatique du contrôle d'arène si influence épuisée.

### 5) Scheduling et configuration

- Nouveau scheduler météo pour :
	- génération des missions flash,
	- application de l'usure territoriale.
- Nouvelles propriétés de configuration météo dans `application.properties`.

## FrontEnd (implémenté)

### 1) WeatherController (nouveau)

- Endpoint `GET /api/weather/current?lat=X&lng=Y&sport=S` : météo courante à des coordonnées, avec hardshipIndex et influenceBonus calculés par sport.
- Endpoint `GET /api/weather/arena/{id}?sport=S` : météo à l'arène via ses coordonnées en base.

### 2) WeatherWidget (composant réutilisable)

- Composant React affichant les conditions météo (température, vent, précipitations, tags, bonus d'influence).
- Mode compact pour les popups de carte.
- Indicateur visuel rouge animé si conditions extrêmes.
- Intégré dans 4 pages.

### 3) HomePage

- Géolocalisation du joueur → widget météo local au chargement.
- Affiche les conditions actuelles et leur impact sur les arènes proches.

### 4) CreateGamePage

- Après sélection d'une arène, appel météo sur cette arène avec le sport choisi.
- Affiche le bonus d'influence attendu dans le récapitulatif avant de lancer le matchmaking.

### 5) ActiveSessionPage

- Widget météo live pendant la session, lié à l'arène du match.
- Affiche le multiplicateur d'influence attendu à la fin.
- Alerte visuelle si conditions extrêmes.

### 6) MapPage

- Widget compact dans chaque popup d'arène.
- Conditions + bonus d'influence affiché selon le sport sélectionné.

### 7) GameResultPage (existant — données enrichies)

- `WeatherCard` déjà en place : affiche les données météo appliquées lors de la clôture de session (hardshipIndex, tags, influenceBonus, affinityBonus, totalInfluenceModifier).

### 8) MissionsPage (existant)

- Filtre "⚡ Flash météo" pour les missions générées lors d'événements extrêmes.
- Badge alerte par type d'événement (orage, canicule, vent, etc.).

## Tests ajoutés

### Tests unitaires

- `SessionServiceWeatherUnitTest` : validation du calcul de gain d'influence intégrant route/perks + météo + affinité.
- `WeatherAffinityServiceTest`, `WeatherFlashMissionServiceTest`, `WeatherHardshipEngineTest`, `TerritoryDecayServiceTest` : couverture des briques métier météo.
- Extension de `MissionServiceTest` pour le scénario `WEATHER_FLASH`.

### Tests d'intégration

- `SessionWeatherFlowIntegrationTest` : flux Spring complet de fin de session avec météo mockée, persistance des métadonnées et mise à jour de contrôle d'arène.
- `WeatherFlashMissionIntegrationTest` : génération de missions flash via service Spring + repository réel avec météo mockée.

## Conclusion courte

 Les tests unitaires et d'intégration couvrent désormais les chemins métier essentiels ajoutés.

