#!/usr/bin/env bash
set -e

echo "[INFO] Initialisiere DEV-Datenbank"

PROJECT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
echo "[INFO] Project directory: $PROJECT_DIR"

ENV_FILE="$PROJECT_DIR/.env"
if [ ! -f "$ENV_FILE" ]; then
  echo "[ERROR] .env file not found!"
  exit 1
fi

echo "[INFO] Loading environment variables from $ENV_FILE"
source "$ENV_FILE"

source "$PROJECT_DIR/bin/fn/fn.inc.sh"

DB_NAME="${APP_DEV_DB_NAME}"
DB_USER="${APP_DEV_DB_USER}"
DB_PASS="${APP_DEV_DB_PASS}"
DB_HOST="${APP_DB_HOST}"

SQL=$(cat <<EOF
DROP DATABASE IF EXISTS \`${DB_NAME}\`;
CREATE DATABASE \`${DB_NAME}\`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.*
  TO '${DB_USER}'@'${DB_HOST}'
  IDENTIFIED BY '${DB_PASS}';

FLUSH PRIVILEGES;
EOF
)

echo "[INFO] Executing SQL:"
echo "$SQL"

echo "$SQL" | sudo mysql -u root

echo "[OK] Database ${DB_NAME} recreated."

echo "[INFO] Applying Liquibase schema to DEV database (${DB_NAME})..."

mvn \
  -Dliquibase.url="jdbc:mariadb://${DB_HOST}:3306/${DB_NAME}" \
  -Dliquibase.username="${DB_USER}" \
  -Dliquibase.password="${DB_PASS}" \
  -Dliquibase.changeLogFile="src/main/resources/db/changelog/db.changelog-master.xml" \
  liquibase:update

echo "[OK] DEV database ${DB_NAME} initialized successfully."
