#!/bin/bash
# file: dbreset.sh

###############################
# Nur für Entwicklungszwecke! #
###############################

DB_NAME="invoices_build"
DB_USER="invoices"
DB_PASS="invoices"
DB_HOST="localhost"
DB_PORT="3306"
JDBC_URL="jdbc:mariadb://$DB_HOST:$DB_PORT/$DB_NAME"

BASE_DIR="./src/main/resources"
MASTER_FILE="db/changelog/db.changelog-master.xml"

cd "$BASE_DIR" || exit 1

# Liquibase CLI prüfen
if ! command -v liquibase &> /dev/null; then
  echo "Fehler: Liquibase CLI ist nicht installiert oder nicht im PATH."
  exit 1
fi

echo "❗️Datenbank $DB_NAME wird zurückgesetzt (nur Liquibase-Tabellen)..."

# DATABASECHANGELOG und DATABASECHANGELOGLOCK löschen
echo "DROP TABLE IF EXISTS DATABASECHANGELOG;" | mysql -u"$DB_USER" -p"$DB_PASS" -h "$DB_HOST" "$DB_NAME"
echo "DROP TABLE IF EXISTS DATABASECHANGELOGLOCK;" | mysql -u"$DB_USER" -p"$DB_PASS" -h "$DB_HOST" "$DB_NAME"

# Erneut changelogSync ausführen
liquibase \
  --changeLogFile="$MASTER_FILE" \
  --url="$JDBC_URL" \
  --username="$DB_USER" \
  --password="$DB_PASS" \
  changelogSync

if [ $? -eq 0 ]; then
  echo "✅ Reset und Sync erfolgreich durchgeführt."
else
  echo "❌ Fehler beim Zurücksetzen."
fi

exit 0
