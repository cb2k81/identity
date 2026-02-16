#!/bin/bash
set -e
# bin/db/init-db-schema.sh
# Creates initial schema changelog and master changelog file

source "$(dirname "$0")/../init.env.sh"

DBHOST="${APP_DB_HOST:-localhost}"
DBNAME="$APP_DEV_DB_NAME"
DBUSER="$APP_DEV_DB_USER"
DBPASS="$APP_DEV_DB_PASS"

CHANGELOG_DIR="$PROJECT_DIR/src/main/resources/db/changelog"
MASTER_CHANGELOG="$CHANGELOG_DIR/db.changelog-master.xml"
BASE_FILE="$CHANGELOG_DIR/001-initial-schema.xml"

# Ensure changelog directory exists
mkdir -p "$CHANGELOG_DIR"
echo "[INFO] Changelog directory ensured: $CHANGELOG_DIR"

# Generate changelog from current DB
echo "[INFO] Generating initial changelog..."
if ! mvn liquibase:generateChangeLog \
  -Dliquibase.url="jdbc:mariadb://${DBHOST}:3306/${DBNAME}" \
  -Dliquibase.username="${DBUSER}" \
  -Dliquibase.password="${DBPASS}" \
  -Dliquibase.outputChangeLogFile="$BASE_FILE"; then
  echo "[ERROR] Failed to generate changelog." >&2
  exit 1
fi

# Fix null defaults
if grep -q 'defaultValue="null"' "$BASE_FILE"; then
  sed -i 's/defaultValue="null"/defaultValueComputed="NULL"/g' "$BASE_FILE"
  echo "[INFO] Replaced defaultValue=\"null\" with defaultValueComputed=\"NULL\""
fi

# Create master changelog if it doesn't exist
if [ ! -f "$MASTER_CHANGELOG" ]; then
  cat > "$MASTER_CHANGELOG" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.5.xsd">
    <include file="001-initial-schema.xml" relativeToChangelogFile="true"/>
</databaseChangeLog>
EOF
  echo "[OK] Created master changelog."
else
  echo "[INFO] Master changelog already exists."
fi

exit 0
