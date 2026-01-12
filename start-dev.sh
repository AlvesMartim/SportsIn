#!/bin/bash

# ============================================
# SCRIPT DE DÉMARRAGE COMPLET DU PROJET
# ============================================
# Ce script démarre le backend et le frontend
# de manière coordonnée pour le développement

set -e

echo "🚀 Démarrage du projet SportsIn..."
echo ""

# Couleurs pour l'affichage
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Vérifier que nous sommes dans le bon répertoire
if [ ! -f "create_database.sh" ]; then
    echo "❌ Erreur: Veuillez exécuter ce script depuis la racine du projet"
    exit 1
fi

# ============================================
# ÉTAPE 1: Créer la base de données
# ============================================
echo -e "${BLUE}[1/4]${NC} Création de la base de données SQLite..."
if [ -f "sportsin.db" ]; then
    echo "    ℹ️  Base de données existante détectée"
    # Vérifier si les tables existent
    if ! sqlite3 "sportsin.db" ".tables" | grep -q "equipe"; then
        echo "    ⚠️  Tables manquantes, recréation de la base de données..."
        rm "sportsin.db"
        bash create_database.sh
    fi
else
    bash create_database.sh
    echo -e "${GREEN}    ✅ Base de données créée${NC}"
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
cd frontend
if [ ! -d "node_modules" ]; then
    npm install --silent
    echo -e "${GREEN}    ✅ Dépendances installées${NC}"
else
    echo "    ℹ️  node_modules détecté"
fi
cd ..
echo ""

# ============================================
# ÉTAPE 4: Démarrer le projet
# ============================================
echo -e "${BLUE}[4/4]${NC} Démarrage des services..."
echo ""

# Créer des fichiers temporaires pour les PIDs
BACKEND_PID_FILE="/tmp/sportsin_backend.pid"
FRONTEND_PID_FILE="/tmp/sportsin_frontend.pid"

# Fonction de nettoyage
cleanup() {
    echo ""
    echo -e "${YELLOW}⏹️  Arrêt des services...${NC}"
    
    if [ -f "$BACKEND_PID_FILE" ]; then
        kill $(cat "$BACKEND_PID_FILE") 2>/dev/null || true
        rm "$BACKEND_PID_FILE"
    fi
    
    if [ -f "$FRONTEND_PID_FILE" ]; then
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
./gradlew bootRun > /tmp/sportsin_backend.log 2>&1 &
echo $! > "$BACKEND_PID_FILE"

# Attendre que le backend soit prêt
echo "⏳ Attente du backend..."
for i in {1..60}; do
    if curl -s http://localhost:8080/api/equipes > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Backend démarré sur http://localhost:8080${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e "${RED}❌ Le backend n'a pas démarré à temps${NC}"
        echo ""
        echo "Logs du backend:"
        cat /tmp/sportsin_backend.log | tail -50
        cleanup
        exit 1
    fi
    sleep 1
done

# Démarrer le frontend en arrière-plan
echo ""
echo -e "${GREEN}📱 Démarrage du frontend...${NC}"
cd frontend
npm run dev > /tmp/sportsin_frontend.log 2>&1 &
echo $! > "$FRONTEND_PID_FILE"
cd ..

# Attendre que le frontend soit prêt
echo "⏳ Attente du frontend..."
sleep 5
echo -e "${GREEN}✅ Frontend démarré sur http://localhost:5173${NC}"

# Afficher le résumé
echo ""
echo -e "${GREEN}════════════════════════════════════════════${NC}"
echo -e "${GREEN}🎉 Projet SportsIn démarré avec succès !${NC}"
echo -e "${GREEN}════════════════════════════════════════════${NC}"
echo ""
echo "📍 Accès:"
echo "   • Backend:  ${BLUE}http://localhost:8080${NC}"
echo "   • Frontend: ${BLUE}http://localhost:5173${NC}"
echo "   • Test API: ${BLUE}http://localhost:5173/api-test${NC}"
echo ""
echo "📚 Documentation:"
echo "   • ${BLUE}CONNECTION_GUIDE.md${NC} - Guide de connexion BD/Backend/Frontend"
echo "   • ${BLUE}README.md${NC} - Documentation générale"
echo ""
echo -e "${YELLOW}Appuyez sur Ctrl+C pour arrêter tous les services${NC}"
echo ""

# Garder le script actif
wait
