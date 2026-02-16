#!/bin/bash

# Projektverzeichnis ermitteln (relativ zum Ort des Skripts)
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SRC_DIR="$PROJECT_DIR/src/main/java"

# Export-Verzeichnis
TMP_DIR="$PROJECT_DIR/tmp/textexports"
mkdir -p "$TMP_DIR"

# Zeitstempel (Format: yyyy-mm-dd_hh:mm:ss)
TIMESTAMP="$(date +"%Y-%m-%d_%H-%M-%S")"

# KORREKT: Variable direkt verwenden
OUT_FILE="$TMP_DIR/idm_code-export_${TIMESTAMP}.txt"


# SOURCES: Liste von Quellen, die verarbeitet werden sollen.
# Mögliche Einträge:
#   - Vollqualifizierte Klassennamen (z. B. de.cocondo.app.system.entity.DomainEntity)
#   - Paketnamen (z. B. de.cocondo.app.domain.contact.company.location)
#   - Paketnamen mit ** für Subpackages
#   - Beliebige Dateien (z. B. pom.xml oder src/main/resources/foo.xml)

SOURCES=(
  "pom.xml"
  "de.cocondo.app.**"
  "README.md"
)


# Funktion zur Anzeige der Hilfe
print_usage() {
    echo "Verwendung: $0 -s \"quelle1 quelle2 ...\""
    echo "  -s \"...\"     Liste von Quellen (Pakete, Klassen, Dateien),"
    echo "               optional mit \".**\" für Subpackages"
}

# Argumente parsen
while [[ $# -gt 0 ]]; do
  case $1 in
    -s)
      IFS=' ' read -r -a SOURCES <<< "$2"
      shift 2
      ;;
    -h|--help)
      print_usage
      exit 0
      ;;
    *)
      echo "Unbekannter Parameter: $1"
      print_usage
      exit 1
      ;;
  esac
done

# Prüfen ob Quellen angegeben wurden
if [ ${#SOURCES[@]} -eq 0 ]; then
  echo "Fehler: Keine Quellen angegeben."
  print_usage
  exit 1
fi

# tmp-Verzeichnis anlegen
mkdir -p "$TMP_DIR"
> "$OUT_FILE"

echo "Beginne Verarbeitung..."

for ENTRY in "${SOURCES[@]}"; do
  INCLUDE_SUBPACKAGES=false
  CLEAN_ENTRY="$ENTRY"

  # Prüfen auf rekursive Verarbeitung
  if [[ "$ENTRY" == *".**" ]]; then
    INCLUDE_SUBPACKAGES=true
    CLEAN_ENTRY="${ENTRY%".**"}"
  fi

  # Wenn ENTRY eine existierende Datei ist
  if [ -f "$PROJECT_DIR/$CLEAN_ENTRY" ]; then
    ABS_PATH="$PROJECT_DIR/$CLEAN_ENTRY"
    echo ">>> Datei: $ABS_PATH" >> "$OUT_FILE"
    echo "--------------------------------------------------------------------------------" >> "$OUT_FILE"
    cat "$ABS_PATH" >> "$OUT_FILE"
    echo -e "\n\n" >> "$OUT_FILE"
    echo "✓ Datei eingefügt: $CLEAN_ENTRY"
    continue
  fi

  # Prüfen auf Java-Klasse (z. B. de.foo.Bar → de/foo/Bar.java)
  CLASS_PATH="$SRC_DIR/$(echo "$CLEAN_ENTRY" | tr '.' '/')".java
  if [ -f "$CLASS_PATH" ]; then
    echo ">>> Datei: $CLASS_PATH" >> "$OUT_FILE"
    echo "--------------------------------------------------------------------------------" >> "$OUT_FILE"
    cat "$CLASS_PATH" >> "$OUT_FILE"
    echo -e "\n\n" >> "$OUT_FILE"
    echo "✓ Klasse eingefügt: $CLEAN_ENTRY"
    continue
  fi

  # Prüfen auf Package (Verzeichnis)
  PACKAGE_DIR="$SRC_DIR/$(echo "$CLEAN_ENTRY" | tr '.' '/')"
  if [ -d "$PACKAGE_DIR" ]; then
    echo "✓ Paket: $CLEAN_ENTRY (rekursiv=$INCLUDE_SUBPACKAGES)"
    if [ "$INCLUDE_SUBPACKAGES" = true ]; then
      FIND_CMD=(find "$PACKAGE_DIR" -type f -name "*.java")
    else
      FIND_CMD=(find "$PACKAGE_DIR" -maxdepth 1 -type f -name "*.java")
    fi
    for JAVA_FILE in $("${FIND_CMD[@]}"); do
      echo ">>> Datei: $JAVA_FILE" >> "$OUT_FILE"
      echo "--------------------------------------------------------------------------------" >> "$OUT_FILE"
      cat "$JAVA_FILE" >> "$OUT_FILE"
      echo -e "\n\n" >> "$OUT_FILE"
    done
    continue
  fi

  echo "⚠ Warnung: Quelle nicht gefunden oder ungültig: $ENTRY"
done

echo "Fertig. Ausgabe gespeichert in $OUT_FILE"
