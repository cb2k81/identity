#!/bin/bash
# file: bin/db/dbsync.sh

set -euo pipefail

# Umgebung initialisieren
source "$(dirname "$0")/../init.env.sh"

# DB-Verbindungsdaten aus Umgebungsvariablen
DBHOST="${APP_DB_HOST:-localhost}"
DBNAME="${APP_BUILD_DB_NAME}"
DBUSER="${APP_BUILD_DB_USER}"
DBPASS="${APP_BUILD_DB_PASS}"

# Verzeichnis, in dem sich das Master-Changelog befindet (relativ zum Projektroot)
BASE_DIR="src/main/resources"
MASTER_FILE="db/changelog/db.changelog-master.xml"

# Ins Changelog-Verzeichnis wechseln
cd "$PROJECT_DIR/$BASE_DIR" || {
  echo "Fehler: Verzeichnis $PROJECT_DIR/$BASE_DIR nicht gefunden." >&2
  exit 1
}

# Liquibase CLI prüfen
if ! command -v liquibase &> /dev/null; then
  echo "Fehler: Liquibase CLI ist nicht installiert oder nicht im PATH." >&2
  exit 1
fi

# Changelog-Synchronisation durchführen
liquibase \
  --changeLogFile="$MASTER_FILE" \
  --url="jdbc:mariadb://$DBHOST:3306/$DBNAME" \
  --username="$DBUSER" \
  --password="$DBPASS" \
  changelogSync

# Ergebnis prüfen und ausgeben
if [ $? -eq 0 ]; then
  echo "Datenbank $DBNAME wurde erfolgreich synchronisiert."
else
  echo "Fehler beim Synchronisieren der Datenbank $DBNAME." >&2
  exit 1
fi
