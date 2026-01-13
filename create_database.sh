#!/bin/bash

# Script pour créer la base de données SQLite du projet SportsIn

DB_FILE="sportsin.db"
SCHEMA_FILE="app/src/main/resources/schema.sql"

# Vérifier si sqlite3 est installé
if ! command -v sqlite3 &> /dev/null; then
    echo "❌ Erreur: La commande 'sqlite3' n'est pas trouvée."
    echo "   Veuillez installer SQLite3 ou laisser le backend créer la base de données au démarrage."
    exit 1
fi

echo "Création de la base de données SQLite: $DB_FILE"

# Supprimer la base de données existante si elle existe
if [ -f "$DB_FILE" ]; then
    echo "Suppression de l'ancienne base de données..."
    rm "$DB_FILE"
fi

# Créer la base de données en exécutant le script SQL
if [ -f "$SCHEMA_FILE" ]; then
    sqlite3 "$DB_FILE" < "$SCHEMA_FILE"
    echo "Base de données créée avec succès: $DB_FILE"
    echo ""
    echo "Tables créées:"
    sqlite3 "$DB_FILE" ".tables"
    echo ""
    echo "Pour vérifier le schéma, exécutez: sqlite3 $DB_FILE '.schema'"
else
    echo "Erreur: Le fichier $SCHEMA_FILE n'existe pas!"
    exit 1
fi
