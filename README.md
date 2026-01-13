[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![CI](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml/badge.svg)](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml)

# 🏆 SportsIn - Plateforme de Gestion des Équipes Sportives

## 🚀 Démarrage Rapide

### ⭐ Méthode recommandée (une seule commande)

```bash
./start-dev.sh
```

Cela démarre automatiquement :
- ✅ La base de données SQLite (en la recréeant si déjà crée)
- ✅ Le backend Spring Boot (port 8080)
- ✅ Le frontend React (port 5173)

Puis accédez à **http://localhost:5173**

---

## 📋 Alternative : Démarrage manuel

### Premier lancement (installation initiale)

1. Cloner le dépôt :
   ```bash
   git clone <url-du-repo>
   cd DevOps
   ```

2. Créer la base de données SQLite :
   ```bash
   ./create_database.sh
   ```

3. Terminal 1 - Lancer le backend :
   ```bash
   ./gradlew bootRun
   ```
   Backend accessible : http://localhost:8080

4. Terminal 2 - Lancer le frontend :
   ```bash
   cd frontend
   npm install  # Une seule fois
   npm run dev
   ```
   Frontend accessible : http://localhost:5173

---

## 📚 Documentation

- **[CONNECTION_GUIDE.md](CONNECTION_GUIDE.md)** - Guide complet de l'intégration
- **[DATABASE.md](DATABASE.md)** - Schéma de la base de données

---

## 🔗 Accès

- **Backend** : http://localhost:8080
- **Frontend** : http://localhost:5173
- **Test API** : http://localhost:5173/api-test

---

## 👥 Crédits

MOREIRA ALVES Martim
ARNAUD Noé
HASHANI Art 
MOUMEN MOKHTARY Aya

