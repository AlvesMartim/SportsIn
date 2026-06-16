[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![CI](https://github.com/AlvesMartim/SportsIn/actions/workflows/ci.yml/badge.svg)](https://github.com/AlvesMartim/SportsIn/actions/workflows/ci.yml)
[![Build avec Gradle](https://github.com/AlvesMartim/SportsIn/actions/workflows/ant.yml/badge.svg)](https://github.com/AlvesMartim/SportsIn/actions/workflows/ant.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AlvesMartim_SportsIn&metric=coverage&token=0451fed156d904596d1f2244ffcc5586244cd67d)](https://sonarcloud.io/summary/new_code?id=AlvesMartim_SportsIn)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AlvesMartim_SportsIn&metric=alert_status&token=0451fed156d904596d1f2244ffcc5586244cd67d)](https://sonarcloud.io/summary/new_code?id=AlvesMartim_SportsIn)
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

### 1. Configuration des variables d'environnement

Avant le premier lancement, créez un fichier `.env` à la racine du projet à partir du modèle fourni :

```bash
cp .env.example .env
```

Puis éditez `.env` et renseignez votre clé API OpenWeather (gratuite via [openweathermap.org/api](https://openweathermap.org/api)) :

```
OPENWEATHER_API_KEY=votre_cle_ici
```

> ⚠️ **Important** : le fichier `.env` est ignoré par Git (voir `.gitignore`). Ne committez **jamais** vos clés réelles.
> Sans clé valide, la **Feature 7 (météo active)** — missions flash météo, affinités d'équipe, usure territoriale accélérée — bascule en mode désactivé silencieux ; le reste du projet fonctionne normalement.
> Une clé OpenWeather fraîchement créée peut nécessiter jusqu'à ~1-2 h avant d'être activée côté serveur.

### 2. ⭐ Méthode recommandée (une seule commande)

```bash
set -a; source .env; set +a   # charge les variables du .env dans l'environnement
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

MOREIRA ALVES Martim<br>
ARNAUD Noé<br>
HASHANI Art<br>
MOUMEN MOKHTARY Aya
