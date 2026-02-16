#!/bin/bash
# bin/init.env.sh
# Load and export environment variables for DB and project

# Default values (can be overridden in .env)
APP_DEV_DB_NAME="myapp"
APP_DEV_DB_USER="dbuser"
APP_DEV_DB_PASS="dbpass"

APP_BUILD_DB_NAME="myapp_build"
APP_BUILD_DB_USER="dbuser"
APP_BUILD_DB_PASS="dbpass"

DB_HOST="localhost"
PROJECT_DIR="${PROJECT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"
echo "[INFO] Project directory: $PROJECT_DIR"

if [ -f "$PROJECT_DIR/.env" ]; then
  echo "[INFO] Loading environment variables from $PROJECT_DIR/.env"
  source "$PROJECT_DIR/.env"
else
  echo "[WARN] .env file not found in $PROJECT_DIR"
fi

echo "[OK] Environment variables loaded."

FN_INC_FILE="$PROJECT_DIR/bin/fn/fn.inc.sh"
if [ -f "$FN_INC_FILE" ]; then
  echo "Loading functions from $FN_INC_FILE"
  source "$FN_INC_FILE"
else
  echo "Unable to load functions from $FN_INC_FILE"
  exit 1
fi
