[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![CI](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml/badge.svg)](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml)

---------------------------------------------------------------------------------------------------------------------------------
Premier lancement (installation initiale)
---------------------------------------------------------------------------------------------------------------------------------
1. Cloner le dépôt :
   git clone <url-du-repo>
   cd DevOps

2. Créer la base de données SQLite :
   ./create_database.sh

3. Installer les dépendances du frontend :
   Voir FRONTEND_SETUP.md pour les instructions détaillées.
   
   En résumé :
   - Si Node.js est installé dans WSL : cd frontend && npm install
   - Si vous avez des problèmes, consultez FRONTEND_SETUP.md

4. Lancer l'application :
   ./gradlew bootRun
   
   Note : Le backend peut démarrer sans le frontend construit.

6. Accéder à l'application :
   Ouvrez votre navigateur et allez sur http://localhost:8080

Note : Ne vous inquiétez pas si le chargement semble bloqué à 80%, c'est normal.

---------------------------------------------------------------------------------------------------------------------------------
Lancement normal (après la première installation)
---------------------------------------------------------------------------------------------------------------------------------
Pour lancer le projet après la première installation, exécutez simplement :

   ./gradlew bootRun

Puis accédez à http://localhost:8080 sur votre navigateur.

Note : La base de données n'a besoin d'être créée qu'une seule fois. Si vous supprimez le fichier 
sportsin.db, vous devrez réexécuter ./create_database.sh pour le recréer.

---------------------------------------------------------------------------------------------------------------------------------
Développement du frontend (mode développement)
---------------------------------------------------------------------------------------------------------------------------------
Pour développer le frontend avec hot-reload :

1. Terminal 1 - Lancer le backend :
   ./gradlew bootRun

2. Terminal 2 - Lancer le frontend en mode développement :
   cd frontend
   npm run dev

Le frontend sera accessible sur http://localhost:5173 avec proxy automatique vers le backend.

---------------------------------------------------------------------------------------------------------------------------------
Crédits
---------------------------------------------------------------------------------------------------------------------------------
MOREIRA ALVES Martim
ARNAUD Noé
HASHANI Art 
MOUMEN MOKHTARY Aya
