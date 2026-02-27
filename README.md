[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![CI](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml/badge.svg)](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml)
[![Build avec Gradle](https://github.com/AlvesMartim/SportsIn/actions/workflows/ant.yml/badge.svg)](https://github.com/AlvesMartim/SportsIn/actions/workflows/ant.yml)
# 🏃 InSport - Conquête Sportive en Territoire Urbain

**InSport** est un projet innovant s’inspirant de la logique de jeux en réalité augmentée (type Ingress ou Pokémon Go), mais centré sur la **pratique sportive réelle**.

Le jeu se déroule sur une carte de l’Île-de-France, découpée en points d’intérêt sportifs (parcs, city-stades, gymnases). Les joueurs s'affrontent physiquement pour conquérir ces territoires.

---

## 🎯 Concept Général

1.  **Équipes** : Les joueurs rejoignent des équipes.
2.  **Exploration** : Ils se rendent physiquement sur des points d'intérêt (Points).
3.  **Action** : Ils réalisent une session de sport (Foot, Basket, Running, Musculation...).
4.  **Conquête** : Le Backend analyse la performance et attribue de l'influence. Si l'influence est suffisante, l'équipe **contrôle** le point.

> **Innovation : Les Routes Sportives**
> Certains points sont reliés pour former des chemins stratégiques. Contrôler une route offre des bonus (avantages, protection, missions avancées).

---

## 🚀 Démarrage Rapide

### ⭐ Méthode recommandée (une seule commande)

```bash
./start-dev.sh
```

Cela démarre automatiquement :
- ✅ La base de données SQLite
- ✅ Le backend Spring Boot (Moteur de jeu & API) - Port 8080
- ✅ Le frontend React (Carte & Interface Joueur) - Port 5173

Accès : **http://localhost:5173**

---

## 📚 Documentation

- **[GAME_MECHANICS.md](docs/GAME_MECHANICS.md)** : Détail des règles (Zones, Routes, Calcul d'influence).
- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** : Architecture technique (API REST, Moteur de règles).
- **[DATABASE.md](docs/DATABASE.md)** : Modèle de données.
- **[CONNECTION_GUIDE.md](docs/CONNECTION_GUIDE.md)** : Guide d'intégration.

---

## 🛠 Architecture Technique

Le projet respecte une séparation stricte :

*   **Backend (Java/Spring)** : C'est le cœur du système. Il est totalement autonome et contient toute la logique métier (règles sportives, algorithmes de graphes pour les routes, validation des sessions).
*   **API REST** : Expose les données de manière agnostique (utilisable par n'importe quel client).
*   **Frontend (React)** : Interface visuelle pour la carte et les interactions joueurs.

---

## 👥 Crédits

MOREIRA ALVES Martim
ARNAUD Noé
HASHANI Art
MOUMEN MOKHTARY Aya
