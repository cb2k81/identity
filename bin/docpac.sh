#!/usr/bin/env bash
source "$(dirname "$0")/env.sh"
# ==============================================================================
# Personnel Service – Documentation Package Generator
# ------------------------------------------------------------------------------
# Erzeugt für ein Set von Quellen (Dateien, Ordner oder Java Klassen) einen
# Code-Export als Dokument.
#
# Erweiterung:
#   - Binärdateien (z. B. Bilder) werden erkannt und nicht als Zeichensalat
#     eingefügt. Stattdessen wird ein Kommentar eingefügt.
#
# Skript-Status: experimentell
# ==============================================================================

set -euo pipefail

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

# SOURCES: Liste von Quellen, die verarbeitet werden sollen
SOURCES=(
  "pom.xml"
  ".emv"
  ".gitignore"
  "src/main/java/**"
  "README.md"
  "bin/**"
  "src/main/resources/application.yml"
  "src/main/resources/application-dev.yml"
  "src/main/resources/application-test.yml"
  "src/main/resources/db/**"
  "src/main/resources/doc/**"
  "src/test/java/**"
)


print_usage() {
    echo "Verwendung: $0 -s \"quelle1 quelle2 ...\""
    echo "  -s \"...\"     Liste von Quellen (Pakete, Klassen, Dateien, Verzeichnisse),"
    echo "               optional mit \".**\" für Subpackages/rekursive Verarbeitung"
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

if [ ${#SOURCES[@]} -eq 0 ]; then
  echo "Fehler: Keine Quellen angegeben."
  print_usage
  exit 1
fi

mkdir -p "$TMP_DIR"
> "$OUT_FILE"

echo "Beginne Verarbeitung..."

# ------------------------------------------------------------------------------
# Hilfsfunktion: Prüft, ob eine Datei binär ist
# ------------------------------------------------------------------------------
is_binary_file() {
  local file="$1"
  if file "$file" | grep -qE 'binary|image|audio|video|compressed'; then
    return 0
  else
    return 1
  fi
}

# ------------------------------------------------------------------------------
# Hauptverarbeitung
# ------------------------------------------------------------------------------
for ENTRY in "${SOURCES[@]}"; do
  INCLUDE_SUBPACKAGES=false
  CLEAN_ENTRY="$ENTRY"

  if [[ "$ENTRY" == *".**" ]] || [[ "$ENTRY" == *"/**" ]]; then
    INCLUDE_SUBPACKAGES=true
    CLEAN_ENTRY="${ENTRY%"/**"}"
    CLEAN_ENTRY="${CLEAN_ENTRY%".**"}"
  fi

  # -------------------------------------------
  # 1. Einzelne Datei
  # -------------------------------------------
  if [ -f "$PROJECT_DIR/$CLEAN_ENTRY" ]; then
    ABS_PATH="$PROJECT_DIR/$CLEAN_ENTRY"
    echo ">>> Datei: $ABS_PATH" >> "$OUT_FILE"
    echo "--------------------------------------------------------------------------------" >> "$OUT_FILE"
    if is_binary_file "$ABS_PATH"; then
      echo "# [Binärdatei, Inhalt ausgelassen]" >> "$OUT_FILE"
    else
      cat "$ABS_PATH" >> "$OUT_FILE"
    fi
    echo -e "\n\n" >> "$OUT_FILE"
    echo "✓ Datei eingefügt: $CLEAN_ENTRY"
    continue
  fi

  # -------------------------------------------
  # 2. Java-Klasse anhand Klassennamen
  # -------------------------------------------
  CLASS_PATH="$SRC_DIR/$(echo "$CLEAN_ENTRY" | tr '.' '/')".java
  if [ -f "$CLASS_PATH" ]; then
    echo ">>> Datei: $CLASS_PATH" >> "$OUT_FILE"
    echo "--------------------------------------------------------------------------------" >> "$OUT_FILE"
    cat "$CLASS_PATH" >> "$OUT_FILE"
    echo -e "\n\n" >> "$OUT_FILE"
    echo "✓ Klasse eingefügt: $CLEAN_ENTRY"
    continue
  fi

  # -------------------------------------------
  # 3. Java-Package (Verzeichnis)
  # -------------------------------------------
  PACKAGE_DIR="$SRC_DIR/$(echo "$CLEAN_ENTRY" | tr '.' '/')"
  if [ -d "$PACKAGE_DIR" ]; then
    echo "✓ Paket: $CLEAN_ENTRY (rekursiv=$INCLUDE_SUBPACKAGES)"
    if [ "$INCLUDE_SUBPACKAGES" = true ]; then
      FIND_CMD=(find "$PACKAGE_DIR" -type f -name "*.java")
    else
      FIND_CMD=(find "$PACKAGE_DIR" -maxdepth 1 -type f -name "*.java")
    fi
    while IFS= read -r JAVA_FILE; do
      echo ">>> Datei: $JAVA_FILE" >> "$OUT_FILE"
      echo "--------------------------------------------------------------------------------" >> "$OUT_FILE"
      cat "$JAVA_FILE" >> "$OUT_FILE"
      echo -e "\n\n" >> "$OUT_FILE"
    done < <("${FIND_CMD[@]}")
    continue
  fi

  # -------------------------------------------
  # 4. Beliebiges Verzeichnis
  # -------------------------------------------
  ABS_DIR="$PROJECT_DIR/$CLEAN_ENTRY"
  if [ -d "$ABS_DIR" ]; then
    echo "✓ Verzeichnis: $CLEAN_ENTRY (rekursiv=$INCLUDE_SUBPACKAGES)"
    if [ "$INCLUDE_SUBPACKAGES" = true ]; then
      FIND_CMD=(find "$ABS_DIR" -type f)
    else
      FIND_CMD=(find "$ABS_DIR" -maxdepth 1 -type f)
    fi
    while IFS= read -r FILE; do
      echo ">>> Datei: $FILE" >> "$OUT_FILE"
      echo "--------------------------------------------------------------------------------" >> "$OUT_FILE"
      if is_binary_file "$FILE"; then
        echo "# [Binärdatei, Inhalt ausgelassen]" >> "$OUT_FILE"
      else
        cat "$FILE" >> "$OUT_FILE"
      fi
      echo -e "\n\n" >> "$OUT_FILE"
    done < <("${FIND_CMD[@]}")
    continue
  fi

  echo "⚠ Warnung: Quelle nicht gefunden oder ungültig: $ENTRY"
done

echo "Fertig. Ausgabe gespeichert in $OUT_FILE"
