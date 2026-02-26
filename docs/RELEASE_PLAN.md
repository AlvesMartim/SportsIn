# 📅 Plan de Livraisons - InSport

Ce document détaille le plan de développement du projet InSport, découpé en 3 livraisons majeures, chacune apportant 2 fonctionnalités clés.

L'objectif est de construire progressivement la complexité du jeu, en partant du moteur de règles sportives pour arriver à la couche stratégique (routes, missions, progression).

---

## 📦 Livraison 1 : Socle du Jeu & Règles Sportives

Cette première livraison pose les fondations du moteur de jeu. Elle permet aux joueurs de réaliser des sessions sportives et au système de déterminer les vainqueurs selon des règles configurables.

### ✨ Feature 1 : Moteur de règles de victoire multi-sports configurable

**Concept :**
Chaque sport possède ses propres conditions de victoire et de scoring, définies côté Backend et exposées via l'API. Le Frontend est agnostique et se contente d'afficher les règles et de soumettre les résultats bruts.

**Exemples de règles configurables :**
*   **Foot :** "Gagne l'équipe qui marque le plus de buts".
*   **Musculation :** "Gagne l'équipe avec le meilleur total pondéré sur 3 exercices".
*   **Course :** "Gagne le meilleur temps moyen sur une distance donnée".
*   **Basket 3x3 :** "Premier à 21 points ou meilleur score après 10 min".

**Complexité Technique :**
*   Implémentation d'un **Pattern Strategy** ou d'un moteur de règles pour interpréter les conditions de victoire.
*   Séparation stricte entre les données brutes (temps, score) et la logique de validation.

### ✨ Feature 2 : Gestion des sessions de défi & Validation

**Concept :**
Permettre aux équipes de s'affronter via un workflow complet de gestion de session.

**Workflow :**
1.  **Création :** Une équipe lance un défi sur un Point (Lieu + Sport + Créneau).
2.  **Inscription :** Une équipe adverse relève le défi.
3.  **Réalisation :** Le match a lieu physiquement.
4.  **Soumission :** Une équipe saisit le résultat.
5.  **Validation Croisée :** L'autre équipe doit confirmer le score.
6.  **Conflit :** En cas de désaccord, la session passe en statut "Conflit" pour arbitrage admin.

**Complexité Technique :**
*   Machine à états (State Machine) pour gérer le cycle de vie d'une session.
*   Gestion de la concurrence et des délais de validation.

---

## 📦 Livraison 2 : Territoire, Zones & Routes (Innovation)

Cette livraison introduit la dimension stratégique et territoriale, transformant l'application en un véritable jeu de conquête.

### ✨ Feature 3 : Calcul de contrôle de Points & Zones

**Concept :**
Le Backend calcule en continu l'influence des équipes sur la carte en fonction de l'historique des sessions.

**Mécaniques :**
*   **Influence par Point :** Basée sur les victoires récentes (avec décroissance temporelle possible).
*   **Domination de Zone :** Si une équipe contrôle X points dans une zone (quartier/parc), elle contrôle la zone entière.
*   **Feedback Joueur :** "Vous contrôlez 4/7 points de la zone La Défense".

**Complexité Technique :**
*   Algorithmes d'agrégation de scores sur des fenêtres temporelles.
*   Gestion des transitions d'état (Point Neutre -> Contesté -> Contrôlé).

### ✨ Feature 4 : Routes Sportives & Bonus de Combo (Innovation Majeure)

**Concept :**
Les points sont reliés entre eux pour former un **Graphe**. Contrôler une suite de points connectés (une "Route") octroie des bonus stratégiques.

**Exemple :**
*   **Route "RER B Sud" :** Relie les points A -> B -> C -> D -> E.
*   **Combo :** Si l'équipe Rouge contrôle A, B et C (3 points consécutifs), elle active un bonus (ex: +10% score running).
*   **Stratégie :** Les adversaires peuvent tenter de prendre le point B pour "couper" la route et désactiver le bonus.

**Complexité Technique :**
*   Modélisation de graphe (Noeuds = Points, Arêtes = Routes).
*   Algorithmes de détection de sous-graphes connectés (chaînes consécutives) appartenant à une même équipe.

---

## 📦 Livraison 3 : Engagement & Personnalisation

Cette dernière livraison vise à fidéliser les joueurs via des objectifs dynamiques et un système de progression RPG.

### ✨ Feature 5 : Missions Dynamiques

**Concept :**
Le système génère des quêtes contextuelles pour orienter l'action des joueurs.

**Types de Missions :**
*   **Reconquête :** "Reprendre le point X perdu il y a 2 jours".
*   **Sabotage :** "Briser la route de l'équipe Verte sur la ligne RER B".
*   **Diversité :** "Faire une session de Musculation dans une zone où ce sport est peu pratiqué".

**Complexité Technique :**
*   Moteur de génération procédurale de missions basé sur l'état actuel du monde (World State).
*   Suivi de la complétion des objectifs en temps réel.

### ✨ Feature 6 : Progression d'Équipe & Perks

**Concept :**
Les équipes gagnent de l'expérience (XP) et débloquent des avantages passifs ou actifs (Perks).

**Exemples de Perks :**
*   **Bouclier :** Protéger un point clé contre les attaques pendant 24h.
*   **Spécialiste :** Bonus de points sur un sport spécifique.
*   **Résistance :** Réduction de la perte d'influence quotidienne.

**Complexité Technique :**
*   Système de calcul d'XP multi-sources (Matchs, Missions, Routes).
*   Gestion des effets actifs/passifs et de leur impact sur les calculs du moteur de jeu.

---

## 📝 Résumé

| Livraison | Features Clés | Valeur Ajoutée |
| :--- | :--- | :--- |
| **L1** | Règles Multi-sports, Workflow Sessions | **Jouabilité de base** (Faire du sport et compter les points) |
| **L2** | Contrôle Territoire, **Routes Sportives** | **Stratégie & Innovation** (Jeu de conquête, Graphes) |
| **L3** | Missions, Progression RPG | **Engagement & Rétention** (Objectifs à long terme) |
