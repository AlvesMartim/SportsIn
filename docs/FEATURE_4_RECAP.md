# Feature 4 : Routes sportives & bonus de combo

## Objectif
Implémenter un système de "routes sportives" (graphe de points) permettant aux équipes de débloquer des bonus lorsqu'elles contrôlent une suite de points consécutifs.

## Statut
**OPÉRATIONNELLE** - La feature est entièrement implémentée, testée et intégrée.

## Architecture & Implémentation

### 1. Modèle de Données
*   **Route** (`org.SportsIn.model.territory.Route`) : Séquence ordonnée de `PointSportif`.
*   **RouteBonus** (`org.SportsIn.model.territory.RouteBonus`) : Objet représentant un bonus actif (ex: +10% de score).
*   **RouteRepository** (`org.SportsIn.model.territory.RouteRepository`) : Interface et implémentation (`InMemoryRouteRepository`) pour la persistance des routes.

### 2. Services Métier
*   **RouteService** :
    *   Algorithme de détection de chaînes consécutives (`getMaxConsecutivePoints`).
    *   Calcul des bonus actifs (`calculateBonuses`).
*   **RouteGeneratorService** :
    *   Algorithme "Greedy Nearest Neighbor" pour générer automatiquement des routes à partir des points géographiques.
*   **TerritoryService** :
    *   Orchestre la conquête.
    *   `initializeRoutesAutomatically` : Initialise le graphe au démarrage.
    *   `getScoreBonusForTeamOnPoint` : Vérifie si un bonus s'applique lors d'une session.
    *   `updateTerritoryControl` : Met à jour le propriétaire du point, vérifie les zones et les routes.
*   **SessionService** :
    *   Intègre le bonus de route dans le flux de fin de session (`processSessionCompletion`).
    *   Loggue l'application du bonus (prêt pour modification du score).

### 3. API & Configuration
*   **RouteController** (`/api/routes`) :
    *   Endpoint `GET` pour exposer les routes au Frontend (visualisation sur carte).
*   **RouteInitializer** :
    *   `CommandLineRunner` qui génère automatiquement les routes au démarrage de l'application (distance max 2km, min 3 points).

### 4. Tests
*   `RouteServiceTest` : Algorithmes de graphe.
*   `RouteGeneratorServiceTest` : Génération géographique.
*   `TerritoryServiceTest` : Intégration complète (Repository, Services).
*   `SessionServiceTest` : Vérification de la non-régression sur la fin de session.

## Flux Fonctionnel
1.  **Démarrage** : `RouteInitializer` appelle le générateur -> Les routes sont créées et stockées dans `RouteRepository`.
2.  **Jeu** : Une équipe gagne une session sur un point.
3.  **Calcul** : `SessionService` demande à `TerritoryService` si un bonus s'applique.
4.  **Bonus** : Si l'équipe contrôle une suite de points (>=3) sur une route incluant le point actuel, un bonus est appliqué.
5.  **Conquête** : Le point change de main, `TerritoryService` recalcule les zones et les routes pour la prochaine fois.
