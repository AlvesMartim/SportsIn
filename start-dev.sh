#!/bin/bash

# ============================================
# SCRIPT DE DÉMARRAGE COMPLET DU PROJET
# ============================================
# Ce script démarre le backend et le frontend
# de manière coordonnée pour le développement

set -e

echo "🚀 Démarrage du projet SportsIn..."
echo ""

# Charger les variables d'environnement depuis .env
if [ -f ".env" ]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
    echo "✅ Variables d'environnement chargées depuis .env"
fi

# Couleurs pour l'affichage
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Vérifier que nous sommes dans le bon répertoire
if [ ! -f "create_database.sh" ]; then
    echo "❌ Erreur: Veuillez exécuter ce script depuis la racine du projet"
    exit 1
fi

# Vérifier les dépendances critiques
if ! command -v curl &> /dev/null; then
    echo -e "${RED}❌ Erreur: 'curl' est requis pour ce script mais n'est pas installé.${NC}"
    exit 1
fi

# Fonction pour installer Node.js localement
install_node_local() {
    echo -e "${YELLOW}⚠️  Node.js n'est pas installé ou la version est incompatible.${NC}"
    echo -e "${BLUE}🔄 Tentative d'installation d'une version portable locale (v22.12.0 LTS)...${NC}"
    
    # Version LTS plus récente compatible avec Vite 6
    NODE_VERSION="v22.12.0"
    NODE_DIR=".node_local"
    NODE_DIST="node-$NODE_VERSION-win-x64"
    ZIP_FILE="$NODE_DIR/node.zip"
    
    # Nettoyer l'ancienne installation si elle existe
    if [ -d "$NODE_DIR" ]; then
        echo "    🧹 Nettoyage de l'ancienne version..."
        rm -rf "$NODE_DIR"
    fi
    
    mkdir -p "$NODE_DIR"
    
    echo "    ⬇️  Téléchargement de Node.js $NODE_VERSION..."
    # Utiliser -k pour ignorer les erreurs SSL si nécessaire, et -L pour suivre les redirections
    if ! curl -L -o "$ZIP_FILE" "https://nodejs.org/dist/$NODE_VERSION/$NODE_DIST.zip"; then
            echo -e "${RED}❌ Échec du téléchargement.${NC}"
            return 1
    fi
    
    echo "    📦 Extraction..."
    if command -v unzip &> /dev/null; then
        unzip -q "$ZIP_FILE" -d "$NODE_DIR"
    elif command -v powershell &> /dev/null; then
        # Conversion du chemin pour PowerShell (Git Bash -> Windows path)
        if command -v cygpath &> /dev/null; then
            WIN_ZIP_PATH=$(cygpath -w "$PWD/$ZIP_FILE")
            WIN_DEST_PATH=$(cygpath -w "$PWD/$NODE_DIR")
        else
            # Fallback simple si cygpath n'est pas là (peu probable sous Git Bash)
            WIN_ZIP_PATH="$PWD/$ZIP_FILE"
            WIN_DEST_PATH="$PWD/$NODE_DIR"
        fi
        powershell -Command "Expand-Archive -Path '$WIN_ZIP_PATH' -DestinationPath '$WIN_DEST_PATH' -Force"
    else
        echo -e "${RED}❌ Impossible d'extraire l'archive (ni unzip ni powershell trouvés).${NC}"
        rm "$ZIP_FILE"
        return 1
    fi
    rm "$ZIP_FILE"
    
    # Ajouter au PATH pour cette session
    export PATH="$PWD/$NODE_DIR/$NODE_DIST:$PATH"
    
    if command -v npm &> /dev/null; then
        echo -e "${GREEN}    ✅ Node.js $(node -v) configuré pour cette session${NC}"
        return 0
    else
        echo -e "${RED}    ❌ Erreur de configuration de Node.js${NC}"
        return 1
    fi
}

# ============================================
# ÉTAPE 1: Créer la base de données
# ============================================
echo -e "${BLUE}[1/4]${NC} Configuration de la base de données..."
# On laisse Spring Boot gérer la création de la DB via schema.sql
# car sqlite3 n'est pas forcément installé sur le système
if [ ! -f "sportsin.db" ]; then
    echo "    ℹ️  La base de données sera initialisée au démarrage du backend"
else
    echo "    ℹ️  Base de données existante détectée"
fi
echo ""

# ============================================
# ÉTAPE 2: Construire le backend
# ============================================
echo -e "${BLUE}[2/4]${NC} Construction du backend Spring Boot..."
./gradlew clean build -x test --quiet
echo -e "${GREEN}    ✅ Backend compilé avec succès${NC}"
echo ""

# ============================================
# ÉTAPE 3: Installer les dépendances frontend
# ============================================
echo -e "${BLUE}[3/4]${NC} Installation des dépendances frontend..."

# Vérifier la version de Node.js
NEED_INSTALL=true
if command -v node &> /dev/null; then
    NODE_VER=$(node -v)
    # Vérifier si la version est >= v20.19 ou >= v22.12 (requis par Vite 6)
    # Simplification: on force la réinstallation si ce n'est pas notre version locale v22.12.0
    # ou si la version système est trop ancienne.
    if [[ "$NODE_VER" == "v22.12.0" ]]; then
        NEED_INSTALL=false
    fi
fi

if [ "$NEED_INSTALL" = true ]; then
    install_node_local || true
fi

HAS_NPM=false
if command -v npm &> /dev/null; then
    HAS_NPM=true
    cd frontend
    if [ ! -d "node_modules" ]; then
        npm install --silent
        echo -e "${GREEN}    ✅ Dépendances installées${NC}"
    else
        echo "    ℹ️  node_modules détecté"
        # Rebuild esbuild si nécessaire (souvent requis après changement de version de Node)
        npm rebuild esbuild --silent
    fi
    cd ..
else
    echo -e "${YELLOW}    ⚠️  npm non trouvé. Le frontend ne sera pas installé ni démarré.${NC}"
fi
echo ""

# ============================================
# ÉTAPE 4: Démarrer le projet
# ============================================
echo -e "${BLUE}[4/4]${NC} Démarrage des services..."
echo ""

# Logs dans le dossier courant pour accès facile
BACKEND_LOG="backend.log"
FRONTEND_LOG="frontend.log"

# Créer des fichiers temporaires pour les PIDs
BACKEND_PID_FILE="backend.pid"
FRONTEND_PID_FILE="frontend.pid"

# Fonction de nettoyage
cleanup() {
    echo ""
    echo -e "${YELLOW}⏹️  Arrêt des services...${NC}"
    
    if [ -f "$BACKEND_PID_FILE" ]; then
        kill $(cat "$BACKEND_PID_FILE") 2>/dev/null || true
        rm "$BACKEND_PID_FILE"
    fi
    
    if [ "$HAS_NPM" = true ] && [ -f "$FRONTEND_PID_FILE" ]; then
        kill $(cat "$FRONTEND_PID_FILE") 2>/dev/null || true
        rm "$FRONTEND_PID_FILE"
    fi
    
    echo -e "${GREEN}✅ Services arrêtés${NC}"
    exit 0
}

# Gérer les signaux d'arrêt
trap cleanup SIGINT SIGTERM

# Démarrer le backend en arrière-plan
echo -e "${GREEN}🔧 Démarrage du backend...${NC}"
echo "   Logs: $BACKEND_LOG"
./gradlew bootRun > "$BACKEND_LOG" 2>&1 &
echo $! > "$BACKEND_PID_FILE"

# Attendre que le backend soit prêt
echo "⏳ Attente du backend..."
for i in {1..60}; do
    if curl -s http://localhost:8080/api/equipes > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Backend démarré sur http://localhost:8080${NC}"
        break
    fi
    # On vérifie aussi si le processus est toujours en vie
    if ! ps -p $(cat "$BACKEND_PID_FILE") > /dev/null; then
        echo -e "${RED}❌ Le backend s'est arrêté inopinément${NC}"
        echo ""
        echo "--- Dernières lignes de $BACKEND_LOG ---"
        tail -n 20 "$BACKEND_LOG"
        cleanup
        exit 1
    fi
    if [ $i -eq 60 ]; then
        echo -e "${RED}❌ Le backend n'a pas démarré à temps${NC}"
        echo ""
        echo "--- Dernières lignes de $BACKEND_LOG ---"
        tail -n 20 "$BACKEND_LOG"
        cleanup
        exit 1
    fi
    sleep 1
done

# Démarrer le frontend en arrière-plan si npm est disponible
if [ "$HAS_NPM" = true ]; then
    echo ""
    echo -e "${GREEN}📱 Démarrage du frontend...${NC}"
    echo "   Logs: $FRONTEND_LOG"
    cd frontend
    # Utiliser --host pour exposer sur 0.0.0.0
    npm run dev -- --host > "../$FRONTEND_LOG" 2>&1 &
    FRONTEND_PID=$!
    cd ..
    echo $FRONTEND_PID > "$FRONTEND_PID_FILE"

    # Attendre que le frontend soit prêt
    echo "⏳ Attente du frontend..."
    sleep 5
    
    # Vérifier si le processus est toujours en vie
    if ! ps -p $FRONTEND_PID > /dev/null; then
        echo -e "${RED}❌ Le frontend s'est arrêté immédiatement${NC}"
        echo ""
        echo "--- Contenu de $FRONTEND_LOG ---"
        cat "$FRONTEND_LOG"
        echo "--------------------------------"
        echo -e "${YELLOW}Conseil: Essayez de supprimer 'frontend/node_modules' et relancez le script.${NC}"
        cleanup
        exit 1
    fi

    echo -e "${GREEN}✅ Frontend démarré sur http://localhost:5173${NC}"
fi

# Afficher le résumé
echo ""
echo -e "${GREEN}════════════════════════════════════════════${NC}"
echo -e "${GREEN}🎉 Projet SportsIn démarré !${NC}"
echo -e "${GREEN}════════════════════════════════════════════${NC}"
echo ""
echo "📍 Accès:"
echo "   • Backend:  ${BLUE}http://localhost:8080${NC}"
if [ "$HAS_NPM" = true ]; then
    echo "   • Frontend: ${BLUE}http://localhost:5173${NC}"
    echo "   • Test API: ${BLUE}http://localhost:5173/api-test${NC}"
else
    echo "   • Frontend: ${YELLOW}Non démarré (npm manquant)${NC}"
fi
echo ""
echo "📚 Documentation:"
echo "   • ${BLUE}CONNECTION_GUIDE.md${NC} - Guide de connexion BD/Backend/Frontend"
echo "   • ${BLUE}README.md${NC} - Documentation générale"
echo ""
echo -e "${YELLOW}Appuyez sur Ctrl+C pour arrêter tous les services${NC}"
echo ""

# Garder le script actif tant que les services tournent
wait
