# Feature 3 : Calcul Automatique de Contrôle de Points et de Zones

Cette fonctionnalité introduit la dimension stratégique territoriale dans l'application SportsIn. Elle permet de gérer dynamiquement la conquête de points sportifs et de zones géographiques en fonction des résultats des sessions de sport.

## 1. Objectifs
*   **Contrôle de Point** : Une équipe prend le contrôle d'un point sportif (ex: un City Stade) lorsqu'elle gagne une session sur ce point.
*   **Génération de Zones** : Regroupement automatique des points proches géographiquement pour former des "Zones".
*   **Contrôle de Zone** : Une équipe prend le contrôle d'une zone entière si elle possède au moins **3 points** dans cette zone.

## 2. Fonctionnement Technique

### A. Génération Automatique des Zones (`ZoneGeneratorService`)
Au démarrage (ou sur demande), l'application analyse tous les points sportifs disponibles.
*   **Algorithme** : Clustering géographique simple.
*   **Critères** :
    *   **Proximité** : Les points doivent être dans un rayon défini (ex: 2 km).
    *   **Densité** : Une zone n'est créée que si elle contient un minimum de points (ex: 3 points).
*   **Résultat** : Création d'objets `Zone` persistés en base, contenant la liste des points associés.

### B. Mécanique de Conquête (`TerritoryService`)
À la fin de chaque session de sport (`SessionService`), si un vainqueur est désigné :
1.  **Mise à jour du Point** : Le point sportif change de propriétaire (`controllingTeamId`).
2.  **Calcul d'Impact Zone** : Le système vérifie toutes les zones contenant ce point.
3.  **Application de la Règle des 3 Points** :
    *   Le système compte les points contrôlés par chaque équipe dans la zone.
    *   **Conquête** : Si une équipe atteint **>= 3 points**, elle devient propriétaire de la zone.
    *   **Perte** : Si le propriétaire actuel passe **< 3 points**, la zone redevient neutre (ou change de main si une autre équipe a >= 3 points).

## 3. Architecture et Composants Clés

| Composant | Rôle |
| :--- | :--- |
| **`Zone`** (Model) | Représente un ensemble de points. Contient la logique `updateZoneControl()` pour vérifier la règle des 3 points. |
| **`GeoUtils`** (Utils) | Fournit le calcul de distance GPS (formule de Haversine). |
| **`ZoneGeneratorService`** | Service responsable de créer les zones à partir des coordonnées GPS des points. |
| **`TerritoryService`** | Service centralisant la logique de mise à jour des territoires (Points & Zones). |
| **`SessionService`** | Chef d'orchestre qui appelle `TerritoryService` une fois qu'une session est validée. |

## 4. Exemple de Scénario (Logique)

1.  **Initialisation** : Le système détecte 3 stades proches à Paris (Châtelet, Louvre, Notre-Dame) et crée la "Zone Paris".
2.  **Match 1** : L'équipe "Les Requins" gagne à Châtelet.
    *   *État* : Châtelet = Requins. Zone Paris = Neutre (1/3).
3.  **Match 2** : "Les Requins" gagnent au Louvre.
    *   *État* : Louvre = Requins. Zone Paris = Neutre (2/3).
4.  **Match 3** : "Les Requins" gagnent à Notre-Dame.
    *   *État* : Notre-Dame = Requins. **Zone Paris = CONQUISE par Les Requins** (3/3).

## 5. Tests
Les tests unitaires couvrent l'ensemble de la logique :
*   `ZoneGeneratorServiceTest` : Vérifie que les points proches sont bien groupés et les points lointains exclus.
*   `TerritoryServiceTest` : Vérifie la bascule de propriété des points et des zones (conquête et perte).
*   `SessionServiceTest` : Vérifie l'intégration complète depuis la fin d'une session.
