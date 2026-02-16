#!/bin/bash
set -euo pipefail
# bin/build.sh
# Builds the application, runs Liquibase, packages and deploys the ZIP

# Load environment and logging
source "$(dirname "${BASH_SOURCE[0]}")/init.env.sh"

# Settings from .env
WORK_DIR="$PROJECT_DIR"
TEMP_DIR="/tmp"
REMOTE_HOST="$DEPL_REMOTE_HOST"
REMOTE_DIR="$DEPL_REMOTE_DIR"
REMOTE_SSH_USER="root"
PORT="$APP_PORT"
PROFILE="${APP_PROFILE:-dev}"

cd "$WORK_DIR" || { log_error "Failed to cd to $WORK_DIR"; exit 1; }

# Build application zuerst
log_info "Building package"
mvn clean package || { log_error "Maven build failed"; exit 1; }

# JAR suchen NACH dem Build
log_info "Locating ${APP_NAME}-*.jar in target/…"
shopt -s nullglob
JARS=(target/${APP_NAME}-*.jar)
if [ ${#JARS[@]} -eq 0 ]; then
  log_error "No JAR matching target/${APP_NAME}-*.jar found"
  exit 1
elif [ ${#JARS[@]} -gt 1 ]; then
  log_warn "Multiple JARs found; using first: ${JARS[0]}"
fi
JAR_FILE="${WORK_DIR}/${JARS[0]}"
VERSION="$(basename "$JAR_FILE" .jar | sed -E "s/^${APP_NAME}-//")"
TARGET_FILE="${APP_NAME}.jar"
TMP_ZIP="${TEMP_DIR}/${APP_NAME}-${VERSION}.zip"
TARGET_ZIP="${APP_NAME}.zip"
log_info "Version: $VERSION"
log_debug "JAR_FILE=$JAR_FILE"
log_debug "TARGET_FILE=$TARGET_FILE"
log_debug "TMP_ZIP=$TMP_ZIP"

# Liquibase: diff & update
log_info "Generating Liquibase diff…"
if ! "$WORK_DIR/bin/db/dbdiff.sh"; then
  log_error "dbdiff.sh failed"
  exit 1
fi
log_info "Liquibase diff generated"

log_info "Updating build database…"
if ! "$WORK_DIR/bin/db/dbupdate.sh"; then
  log_error "dbupdate.sh failed"
  exit 1
fi
log_info "Build database updated"

# Commit schema changes
log_info "Committing and pushing schema changes"
git add .
git commit -m "auto-commit: schema diff $VERSION" || log_warn "Nothing to commit"
git push origin main || { log_error "Git push failed"; exit 1; }

# Stop any running instance
is_port_in_use() { lsof -i:"$PORT" | grep LISTEN &> /dev/null; }
stop_service() {
  if is_port_in_use; then
    log_info "Stopping service on port $PORT"
    kill -9 "$(lsof -t -i:"$PORT")"
    log_info "Service stopped"
  else
    log_debug "No service to stop on port $PORT"
  fi
}
stop_service

# Start service to export API docs
log_info "Starting service (profile=$PROFILE)"
java -Dspring.profiles.active="$PROFILE" -jar "$JAR_FILE" &
PID=$!
log_info "Waiting for service on port $PORT"
until is_port_in_use; do sleep 1; done
log_info "Service is running"

# Download OpenAPI spec
API_URL="http://localhost:$PORT/api-docs"
API_FILE="target/apidocs/openapi.json"
log_info "Downloading OpenAPI spec"
if ! wget -qO "$API_FILE" "$API_URL"; then
  log_error "Failed to download OpenAPI spec"
  stop_service
  exit 1
fi

# Stop service before packaging
stop_service

# Package ZIP
log_info "Creating ZIP archive"
rm -f "$TMP_ZIP"
if ! zip -r "$TMP_ZIP" "$JAR_FILE" target/apidocs target/site/*.spdx.json README.md bin src/main/resources; then
  log_error "ZIP creation failed"
  exit 1
fi

# Deploy
log_info "Uploading $TMP_ZIP → $REMOTE_HOST:$REMOTE_DIR/$TARGET_ZIP"
if ! scp "$TMP_ZIP" "$REMOTE_SSH_USER@$REMOTE_HOST:$REMOTE_DIR/$TARGET_ZIP"; then
  log_error "SCP upload failed"
  exit 1
fi

log_info "Build and deployment of $APP_NAME v$VERSION completed successfully"
