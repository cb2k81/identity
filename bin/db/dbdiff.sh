#!/bin/bash
# bin/db/dbdiff.sh
# Generiert neues ChangeSet 00n-schema-update.xml und ergänzt Master-Changelog
# (inkl. Version & Zeitstempel als Kommentar oberhalb der root-Elemente)

set -euo pipefail

# Umgebung laden
source "$(dirname "$0")/../init.env.sh"
APP_DB_HOST="localhost"

# Pfade
CHANGELOG_DIR="$PROJECT_DIR/src/main/resources/db/changelog"
MASTER_FILE="$CHANGELOG_DIR/db.changelog-master.xml"
VERSION_FILE="$CHANGELOG_DIR/.last_version"

# 1) Basis-Checks
if ! command -v liquibase &> /dev/null; then
  echo "Fehler: Liquibase CLI nicht im PATH." >&2
  exit 1
fi
if [ ! -f "$MASTER_FILE" ]; then
  echo "Fehler: Master-Changelog $MASTER_FILE fehlt." >&2
  exit 2
fi

# 2) Fehlende Includes ergänzen
for f in "$CHANGELOG_DIR"/[0-9][0-9][0-9]-schema-update.xml; do
  [ -e "$f" ] || break
  rel="$(basename "$f")"
  if ! grep -q "<include file=\"$rel\"" "$MASTER_FILE"; then
    echo "Füge fehlenden Eintrag für $rel ins Master-Changelog ein..."
    sed -i.bak "/<\/databaseChangeLog>/i \
    <include file=\"$rel\" relativeToChangelogFile=\"true\"/>" "$MASTER_FILE"
    echo "✔️ Include für $rel hinzugefügt."
    exit 0
  fi
done

# 3) Nächste Versionsnummer ermitteln
MAX=$(find "$CHANGELOG_DIR" -maxdepth 1 -type f -name '[0-9][0-9][0-9]-schema-update.xml' \
      | sed -E 's#.*/([0-9]{3})-.*#\1#' \
      | sort -n \
      | tail -n1 || true)
if [ -z "$MAX" ]; then
  NEXT="002"
else
  NEXT=$(printf "%03d" $((10#$MAX + 1)))
fi
NEW_FILE="$CHANGELOG_DIR/${NEXT}-schema-update.xml"
echo "→ Erzeuge neue Diff-Datei: $NEW_FILE"

# 4) diffChangeLog ausführen
liquibase \
  --logLevel=info \
  --referenceUrl="jdbc:mariadb://${APP_DB_HOST}:3306/${APP_DEV_DB_NAME}" \
  --referenceUsername="${APP_DEV_DB_USER}" \
  --referencePassword="${APP_DEV_DB_PASS}" \
  --url="jdbc:mariadb://${APP_DB_HOST}:3306/${APP_BUILD_DB_NAME}" \
  --username="${APP_BUILD_DB_USER}" \
  --password="${APP_BUILD_DB_PASS}" \
  --changeLogFile="$NEW_FILE" \
  diffChangeLog

# 5) Datei prüfen
if [ ! -s "$NEW_FILE" ]; then
  echo "⚠️ Keine Änderungen erkannt – Datei $NEW_FILE ist leer oder fehlt."
  rm -f "$NEW_FILE"
  exit 0
fi

# 5b) Fix defaultValue="null"
if grep -q 'defaultValue="null"' "$NEW_FILE"; then
  sed -i 's/defaultValue="null"/defaultValueComputed="NULL"/g' "$NEW_FILE"
  echo "[INFO] Ersetze defaultValue=\"null\" durch defaultValueComputed=\"NULL\" in $NEW_FILE"
fi

# 6) Datei-Header-Kommentar mit Version und Zeitstempel einfügen
TIMESTAMP="$(date +'%Y-%m-%dT%H:%M:%S%z')"
# Kommentar nach XML-Deklaration (Zeile 1)
sed -i "2i<!-- schema-update Version: $NEXT  |  Generated: $TIMESTAMP -->" "$NEW_FILE"

# 7) .last_version aktualisieren
echo "$NEXT" > "$VERSION_FILE"
echo "Versionsdatei auf $NEXT gesetzt."

# 8) Master-Changelog ergänzen
echo "Füge Eintrag für $(basename "$NEW_FILE") ins Master-Changelog ein..."
sed -i "/<\/databaseChangeLog>/i \
    <include file=\"$(basename "$NEW_FILE")\" relativeToChangelogFile=\"true\"/>" "$MASTER_FILE"
echo "Include für $(basename "$NEW_FILE") hinzugefügt."

exit 0
