#!/bin/bash
set -euo pipefail
# bin/db/init-build-db.sh
# Setzt Build-DB zurück und wendet Baseline (001) an

# Umgebungsvariablen laden
source "$(dirname "${BASH_SOURCE[0]}")/../init.env.sh"

BUILD_DB_NAME="$APP_BUILD_DB_NAME"
BUILD_DBUSER="$APP_BUILD_DB_USER"
BUILD_DBPASS="$APP_BUILD_DB_PASS"

# --- Datenbank zurücksetzen + Rechte ---
SQL_CMD="
DROP DATABASE IF EXISTS \`${BUILD_DB_NAME}\`;
CREATE DATABASE \`${BUILD_DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON \`${BUILD_DB_NAME}\`.* TO '${BUILD_DBUSER}'@'localhost' IDENTIFIED BY '${BUILD_DBPASS}';
FLUSH PRIVILEGES;
"
echo "Auszuführendes SQL:"
printf "%s\n" "$SQL_CMD"
sudo mysql -e "$SQL_CMD"
echo "Datenbank »${BUILD_DB_NAME}« neu erstellt und Rechte gesetzt."

# --- Master-Changelog prüfen ---
MASTER_REL="src/main/resources/db/changelog/db.changelog-master.xml"
MASTER_FILE="${PROJECT_DIR}/${MASTER_REL}"
if [ ! -f "$MASTER_FILE" ]; then
  echo "FEHLER: Master-Changelog nicht gefunden: $MASTER_FILE" >&2
  exit 1
fi

# --- Ins Projekt-Root wechseln und Liquibase ausführen ---
cd "$PROJECT_DIR"

mvn liquibase:update \
  -Dliquibase.url="jdbc:mariadb://${DB_HOST:-localhost}:3306/${BUILD_DB_NAME}" \
  -Dliquibase.username="${BUILD_DBUSER}" \
  -Dliquibase.password="${BUILD_DBPASS}" \
  -Dliquibase.changeLogFile="${MASTER_REL}"

echo "Baseline angewendet auf »${BUILD_DB_NAME}«."
