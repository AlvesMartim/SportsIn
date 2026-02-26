# 🎮 Mécaniques de Jeu - InSport

Ce document détaille les règles et les mécaniques de jeu d'InSport.

## 🗺️ Territoire & Points d'Intérêt

Le jeu se déroule sur une carte de l'Île-de-France.

### 📍 Points d'Intérêt (POI)
Chaque point représente un lieu sportif réel (parc, city-stade, gymnase, piste, salle...).
*   **Sports disponibles** : Chaque point peut accueillir plusieurs sports (Foot, Basket, Musculation, Course...).
*   **Influence** : Les équipes gagnent de l'influence sur un point en réalisant des sessions sportives.
*   **Contrôle** : Lorsqu'une équipe domine un point de manière suffisante, elle le contrôle officiellement.

### 🏙️ Zones
Les points sont regroupés en zones géographiques (quartiers, parcs, secteurs).
*   **Domination de Zone** : Si une équipe contrôle 3 points dans une zone, elle domine la zone entière.

## 🛣️ Routes Sportives (Innovation)

Certains points sont reliés entre eux pour former des chemins stratégiques (via un quartier, un axe ou une ligne de RER).

*   **Contrôle de Route** : Si une équipe contrôle plusieurs points consécutifs sur une route, elle débloque un **bonus spécial**.
*   **Bonus possibles** :
    *   Avantage dans un sport spécifique.
    *   Protection temporaire contre les attaques adverses.
    *   Accès à des missions avancées.

Ce système pousse à une conquête organisée : contrôle de zones, coupures de routes adverses, stratégies d’expansion.

## 🏆 Sessions Sportives & Conquête

1.  **Regroupement** : Les joueurs se regroupent en équipes.
2.  **Déplacement** : Ils se rendent physiquement sur un point de la carte.
3.  **Session** : Ils organisent une session sportive selon un sport disponible sur ce point.
4.  **Soumission** : Le résultat de la session (score, performance, temps, etc.) est soumis via le front-end.
5.  **Résolution** : Le back-end Java applique les règles du sport pour déterminer le vainqueur et met à jour l'influence.

## 🎯 Missions Dynamiques

Le jeu propose des missions pour encourager l'activité physique :
*   Missions à durée limitée.
*   Missions liées à la conquête de routes spécifiques.
*   Missions de défense de territoire.

## 📊 Diagramme des Cas d'Utilisation

Ce diagramme illustre les actions possibles pour les joueurs et les administrateurs.

![Diagramme des Cas d'Utilisation](assets/diagrams/use_case.png)
