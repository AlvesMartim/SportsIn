# 📐 Architecture Technique - InSport

Ce document décrit l'architecture technique de l'application InSport, décomposée en deux parties principales : le backend et le frontend.

## 🏢 Backend (Spring Boot)

Le backend est le cœur du projet. Il est totalement autonome, solide et cohérent. Il gère toute la logique métier, les règles du jeu et l'API REST.

### 🧠 Moteur de Jeu & Logique Métier

Le backend gère :
*   **Modèles de données** : Équipes, Sports, Sessions, Résultats, Territoires (Points, Zones, Routes).
*   **Moteur de règles multi-sports** : Applique les règles spécifiques à chaque sport pour valider les sessions.
*   **Calcul d'influence** : Algorithmes pour déterminer le contrôle des points et des zones.
*   **Algorithmes de graphes** : Gestion des routes sportives et des bonus associés.
*   **Missions dynamiques** : Génération et suivi des missions.
*   **Progression d'équipe** : Système de niveaux et de récompenses.

### 🔌 API REST

L'API est indépendante du front-end et expose :
*   La liste des sports et leurs règles.
*   Les points, zones et routes.
*   Les sessions et résultats.
*   Les missions.
*   Les bonus et perks.
*   Des endpoints de validation et de résolution de conflits.

### Structure des packages

Le code source est organisé de la manière suivante :
-   `com.example.sportsin.model` : Contient les entités JPA (User, Team, Event, Point, Zone, Route).
-   `com.example.sportsin.repository` : Interfaces Spring Data JPA.
-   `com.example.sportsin.service` : Logique métier (GameEngine, TerritoryService, MissionService).
-   `com.example.sportsin.controller` : Contrôleurs REST.
-   `com.example.sportsin.config` : Configuration (Sécurité, Base de données).

### Base de données

L'application utilise une base de données **SQLite** pour la persistance des données. Le schéma est détaillé dans [DATABASE.md](DATABASE.md).

### Sécurité

La sécurité est gérée par **Spring Security**. L'accès à l'API est protégé et nécessite une authentification (JWT).

## 🎨 Frontend (React)

Le frontend est une application monopage (SPA) développée avec **React** et **Vite**. Il sert d'interface utilisateur pour interagir avec le jeu.

### Fonctionnalités Clés

*   **Carte dynamique** : Affichage des points, zones et routes sur une carte interactive.
*   **Interaction joueur** : Création d'équipes, lancement de sessions, soumission de résultats.
*   **Tableau de bord** : Suivi des missions, des bonus et de la progression.

### Communication avec le backend

Le frontend communique avec le backend via des requêtes HTTP aux endpoints de l'API REST. Il est conçu pour être remplaçable par n'importe quel autre client (mobile, CLI, etc.), car toute la logique réside dans le backend.

## 📊 Diagrammes UML

### Diagramme de Classes (Modèle de Données)

![Diagramme de Classes](assets/diagrams/class_diagram.png)

### Diagramme de Séquence (Conquête d'un Point)

![Diagramme de Séquence](assets/diagrams/sequence_conquest.png)
