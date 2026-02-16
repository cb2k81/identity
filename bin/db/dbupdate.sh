#!/bin/bash
# file: bin/db/dbupdate.sh
# aktualisiert die BUILD Datenbank auf Basis des Changelog.
set -e

source "$(dirname "$0")/../init.env.sh"

DBHOST="localhost"
DBNAME=$APP_BUILD_DB_NAME
DBUSER=$APP_BUILD_DB_USER
DBPASS=$APP_BUILD_DB_PASS

# Liquibase CLI prüfen
if ! command -v liquibase &> /dev/null; then
  echo "Fehler: Liquibase CLI ist nicht installiert oder nicht im PATH."
  exit 1
fi

# ChangeLog-Datei relativ zum Projektstamm
MASTER_FILE="src/main/resources/db/changelog/db.changelog-master.xml"

# Liquibase ausführen
liquibase \
  --changeLogFile="$MASTER_FILE" \
  --url="jdbc:mariadb://$DBHOST:3306/$DBNAME" \
  --username="$DBUSER" \
  --password="$DBPASS" \
  update

# Ergebnis prüfen
if [ $? -eq 0 ]; then
  echo "Datenbank invoices_build wurde erfolgreich aktualisiert."
else
  echo "Fehler beim Aktualisieren der Datenbank."
  exit 1
fi