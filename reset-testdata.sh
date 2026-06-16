#!/bin/bash
# ============================================================
# reset-testdata.sh — Réinitialise la base de données de test
# Usage : bash reset-testdata.sh   (depuis la racine du projet)
# ============================================================
# Ce script supprime sportsin.db. Au prochain démarrage via
# ./start-dev.sh, Spring Boot recrée la base depuis schema.sql
# et data.sql (jeu de données de test complet).
# ============================================================

DB_PATH="./sportsin.db"

echo "🗑️  Suppression de $DB_PATH..."
if [ -f "$DB_PATH" ]; then
    rm "$DB_PATH"
    echo "✅ Base de données supprimée."
else
    echo "ℹ️  Aucune base de données trouvée (déjà vide)."
fi

echo ""
echo "✅ Prêt. Lancez ./start-dev.sh pour recharger le jeu de données complet."
echo ""
echo "   Comptes de test (tous avec mot de passe : Sportsin1)"
echo "   ───────────────────────────────────────────────────"
echo "   BenYedder    / benyedder@sportsin.test    → AS Monaco (niv.5)"
echo "   Mbappe       / mbappe@sportsin.test        → PSG (niv.5)"
echo "   Lacazette    / lacazette@sportsin.test     → OL (niv.4)"
echo "   KoloMuani    / kolomuani@sportsin.test     → FC Nantes (niv.3)"
echo "   Camara       / camara@sportsin.test        → Stade Brest (niv.2)"
echo ""
echo "   ⚠️  Note badges météo (Feature 1) :"
echo "   Les badges sont calculés sur les sessions jouées en DIRECT."
echo "   Pour débloquer RAIN_WARRIOR : gagnez une session sous la pluie"
echo "   (les conditions météo réelles s'appliquent à l'arène choisie)."
