# 📋 Cahier des Charges Frontend - Intégration Backend

Ce document détaille les écrans manquants à développer pour connecter l'application React au backend Spring Boot.

Pour chaque écran, vous trouverez :
1.  **À quoi ça sert ?** (L'objectif utilisateur)
2.  **Ce qu'il faut faire** (Les fonctionnalités)
3.  **L'API à utiliser** (Les fonctions techniques déjà prêtes dans `src/api/api.js`)

---

## 1. Écran : Classement des Équipes (`TeamListPage.jsx`)
**🚨 Priorité : Haute (Manquant)**

### À quoi ça sert ?
C'est le cœur de la compétition. Les joueurs doivent pouvoir voir quelles équipes existent, qui domine le territoire, et choisir une équipe à rejoindre s'ils sont seuls. Sans ça, on ne sait pas contre qui on joue.

### Ce qu'il faut faire
*   Créer une page qui liste toutes les équipes inscrites.
*   Afficher pour chaque équipe : son **Nom**, sa **Couleur**, et idéalement son nombre de joueurs (si dispo).
*   Ajouter un bouton "Rejoindre" (si l'utilisateur n'a pas d'équipe).

### Technique
*   **Fichier :** `src/pages/TeamListPage.jsx`
*   **API :** `equipeAPI.getAll()`
*   **Données reçues :** Liste d'objets `[{ id, nom, couleur, ... }]`.

---



## 3. Écran : Séance en cours (`ActiveSessionPage.jsx`)
**🚨 Priorité : Haute**

### À quoi ça sert ?
C'est l'écran que le joueur regarde **pendant** qu'il fait du sport. Il doit être simple et lisible (gros boutons). C'est ici qu'on déclenche l'enregistrement de l'activité pour valider la prise de territoire.

### Ce qu'il faut faire
*   Un gros bouton **"DÉMARRER"** qui lance le chrono.
*   Un affichage du temps écoulé (00:00:00).
*   Un gros bouton **"TERMINER"** qui arrête la séance et sauvegarde.

### Technique
*   **Fichier :** `src/pages/ActiveSessionPage.jsx`
*   **API Démarrage :** `sessionAPI.create({ sportId: ..., date: ... })`
*   **API Fin :** `sessionAPI.terminate(sessionId)`

---

## 4. Écran : Historique & Journal (`ActivityHistoryPage.jsx`)
**🚨 Priorité : Moyenne**

### À quoi ça sert ?
Permet au joueur de voir sa progression. "Est-ce que j'ai couru plus longtemps qu'hier ?". C'est essentiel pour la motivation et pour vérifier que les points ont bien été comptabilisés.

### Ce qu'il faut faire
*   Afficher une liste chronologique des séances passées.
*   Chaque ligne doit montrer : La date, le sport pratiqué, et la durée totale.
*   Au clic sur une ligne, on va vers le détail (voir point 5).

### Technique
*   **Fichier :** `src/pages/ActivityHistoryPage.jsx`
*   **API :** `sessionAPI.getAll()` (Le backend filtrera pour renvoyer celles de l'utilisateur).

---

## 5. Écran : Détail d'une séance (`SessionDetailPage.jsx`)
**🚨 Priorité : Basse (Peut être fait après l'historique)**

### À quoi ça sert ?
L'analyse post-effort. Le joueur veut voir ses stats précises pour une séance donnée.

### Ce qu'il faut faire
*   Récupérer les métriques liées à une session spécifique (ID).
*   Afficher : Vitesse moyenne, Calories, Distance, etc.

### Technique
*   **Fichier :** `src/pages/SessionDetailPage.jsx`
*   **API :** `metricValueAPI.getBySession(sessionId)`
