# 📋 Cahier des Charges Frontend - Intégration Backend

Ce document détaille les écrans manquants à développer pour connecter l'application React au backend Spring Boot.

Pour chaque écran, vous trouverez :
1.  **À quoi ça sert ?** (L'objectif utilisateur)
2.  **Ce qu'il faut faire** (Les fonctionnalités)
3.  **L'API à utiliser** (Les fonctions techniques déjà prêtes dans `src/api/api.js`)

---

## 1. Écran : Gestion d'Équipe (Rejoindre / Créer)
**🚨 Priorité : Haute (Bloquant pour le gameplay)**

### À quoi ça sert ?
Actuellement, la page "Mon équipe" affiche juste un bouton inactif si on n'a pas d'équipe. Il faut rendre ce processus fonctionnel. Le joueur doit pouvoir choisir son camp pour commencer à jouer.

### Ce qu'il faut faire
Dans la page `TeamPage.jsx` (ou via une nouvelle page dédiée), gérer le cas où le joueur n'a pas d'équipe :

**Option A : Rejoindre une équipe existante**
*   Afficher la liste des équipes disponibles (`equipeAPI.getAll()`).
*   Ajouter un bouton "Rejoindre" à côté de chaque équipe.
*   Action : Mettre à jour le profil du joueur avec l'ID de l'équipe choisie (`joueurAPI.update(id, { equipe: { id: ... } })`).

**Option B : Créer une nouvelle équipe**
*   Afficher un formulaire simple : "Nom de l'équipe" et "Couleur".
*   Action : Créer l'équipe (`equipeAPI.create(...)`), puis assigner le joueur à cette nouvelle équipe.

### Technique
*   **Fichier :** `src/pages/TeamPage.jsx` (à modifier) ou créer `src/pages/TeamSelectionPage.jsx`.
*   **APIs :** `equipeAPI.getAll()`, `equipeAPI.create()`, `joueurAPI.update()`.

---

## 2. Écran : La Carte des Parcours (`MapPage.jsx` - Amélioration)
**🚨 Priorité : Moyenne**

### À quoi ça sert ?
Afficher les routes Bonus
### Ce qu'il faut faire
*   En plus des marqueurs (Arènes), récupérer les "Routes" (itinéraires).
*   Tracer ces itinéraires sur la carte sous forme de lignes colorées.

### Technique
*   **Fichier :** `src/pages/MapPage.jsx`
*   **API :** `routeAPI.getAll()`
*   **Composant :** Utiliser `<Polyline positions={...} />` de la librairie `react-leaflet`.

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
