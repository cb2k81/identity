# README für das /bin Verzeichnis

Dieses Verzeichnis enthält zentrale Skripte und Funktionen, die in allen Java Spring Boot Service-Anwendungen genutzt werden. Es gliedert sich in:

* **Root-Skripte** im Verzeichnis `/bin`
* **Funktionsbibliothek** im Unterordner `/bin/fn`
* **Datenbankskripte** im Unterordner `/bin/db`

---

## 1. Root-Skripte

### init.env.sh

Lädt Standard-Umgebungsvariablen, überschreibt sie optional mit projektspezifischen Einstellungen aus `../.env` und bindet die Funktionsbibliothek ein.

* Setzt Default-Werte für Datenbank- und Projektparameter
* Prüft auf eine `.env`-Datei im Projekt-Root und lädt diese
* Definiert und lädt Funktionsdateien aus `/bin/fn/fn.inc.sh` oder bricht bei Fehler ab

### build.sh

Hauptautomatisierung für Build, Datenbank-Update, Paketierung und Deployment.

Ablauf:

1. Lädt Umgebung und Logging-Funktionen
2. Führt Liquibase-Diff und Datenbank-Update durch (`dbdiff.sh`, `dbupdate.sh`) – diese Schritte werden auch beim Deployment automatisch ausgeführt, damit keine Schema-Änderungen unbeachtet bleiben
3. Commit/Push generierter Changelogs
4. Stoppt laufende Service-Instanz (Port-basiert)
5. Baut das JAR mit Maven
6. Startet Dienst temporär, um OpenAPI-Spezifikation zu exportieren
7. Paketiert JAR, API-Dokus & Zusatzdateien zu einer ZIP
8. Überträgt das ZIP via `scp` auf den Remote-Host

Parameter und Pfade werden über Umgebungsvariablen aus der `.env` gesteuert.

---

## 2. Funktionsbibliothek (`/bin/fn`)

Kleinere Shell-Bibliotheken, die von Skripten zentral eingebunden werden.

### fn.inc.sh

Wrapper, der alle einzelnen Funktionsskripte lädt:

* `log.fn.sh`   – Logging-Funktionen (DEBUG–ERROR)
* `jq.fn.sh`    – JSON-Verarbeitung mit jq
* `jlogin.fn.sh` – (Projektinterne) Login-/Authentifizierungsfunktionen

### log.fn.sh

Bietet standardisierte Log-Ausgaben mit konfigurierbarem `LOG_LEVEL`.

* Definiert numerische Level für DEBUG, INFO, WARN, ERROR
* Prüft gegen gesetztes `LOG_LEVEL`
* Schreibt WARN/ERROR auf stderr, andere nach stdout

### jq.fn.sh

(als `jg.fn.sh` im Repository): Funktionen zur JSON-Ausgabe und Validierung mittels `jq`.

* `checkjq()`: Prüft jq-Installation
* `out()`: Formatiert Eingabe (String oder Datei) als JSON, falls valide, sonst plain Text

### jlogin.fn.sh

Shell-Funktion zum Authentifizieren an einem Service-Endpunkt und Speichern des Tokens

* **Funktion**: `jlogin <username> <password> [targetUrl]`
* **Parameter**:

  * `username`: Benutzername für die Authentifizierung
  * `password`: Passwort für die Authentifizierung
  * `targetUrl` (optional): Basis-URL des Services (Standard: `BASE_URL`)
* **Ablauf**:

  1. Sendet eine POST-Anfrage an `$targetUrl/auth/login` mit JSON-Payload `{"username":"<username>","password":"<password>"}`
  2. Liest das von der API zurückgegebene Token (`TOKEN`)
  3. Prüft, ob ein gültiges Token empfangen wurde, andernfalls Abbruch mit Fehler
  4. Exportiert das Token in die Umgebungsvariable `AUTH_TOKEN`
  5. Gibt das Token auf stdout aus

---

## 3. Datenbankskripte (`/bin/db`)

Skripte zur Initialisierung und Pflege von Datenbankschemata via Liquibase.

### init-db-schema.sh

Erzeugt die Basis (Baseline) des Dev-DB-Schemas und legt eine Master-Changelog-Datei an.

* **Zu Beginn manuell ausführen**: Entwickler erzeugt initiale Baseline mit dem Skript:

  ```bash
  bin/db/init-db-schema.sh
  ```
* Generiert XML-Baseline mit `mvn liquibase:generateChangeLog`
* Ersetzt `defaultValue="null"` durch `defaultValueComputed="NULL"`
* Legt `db.changelog-master.xml` an, falls nicht vorhanden

### init-build-db.sh

Setzt die Build-Datenbank zurück und wendet die Baseline (`001-initial-schema.xml`) an.

* **Zu Beginn manuell ausführen**: Entwickler erstellt Build-Instanz mit:

  ```bash
  bin/db/init-build-db.sh
  ```
* Lädt Umgebungsvariablen
* Droppt und erstellt das Build-DB-Schema mit korrekten Rechten
* Führt `mvn liquibase:update` gegen die neue Build-DB aus

### dbupdate.sh

Wendet Änderungen aus dem Master-Changelog auf die Build-Datenbank an.

* Lädt Umgebungsvariablen
* Prüft auf installierte Liquibase-CLI
* Führt `liquibase update` gegen `db.changelog-master.xml` aus

### dbdiff.sh

Erzeugt eine Liquibase-Differenz (Changelog) zwischen der Dev- und der Build-Datenbank.

* **Manuelle Nutzung**: Führen Sie bei Schemaänderungen aus, um Erkenntnisse in Changelog-Dateien zu übernehmen:

  ```bash
  bin/db/dbdiff.sh
  ```
* Nutzt Maven-Liquibase unter der Haube, um eine Änderungsdatei (z.B. `src/main/resources/db/changelog/00X-<description>.xml`) zu generieren.
* In `build.sh` automatisch aufgerufen vor dem Update, um neue Schema-Änderungen festzuhalten.

### 4. Erforderliche `.env`-Variablen

Um alle Skripte korrekt zu betreiben, müssen in der Projekt-Root-Datei `.env` mindestens folgende Variablen definiert werden:

| Variable                 | Beschreibung                                             | Beispiel                  |
| ------------------------ | -------------------------------------------------------- | ------------------------- |
| `APP_NAME`               | Basisname der Anwendung (z.B. für JAR- und ZIP-Namen)    | `invoices`                |
| `APP_DEV_DB_NAME`        | Name der Entwicklungsdatenbank                           | `invoices`                |
| `APP_DEV_DB_USER`        | DB-Benutzer für die Entwicklungsdatenbank                | `invoices`                |
| `APP_DEV_DB_PASS`        | Passwort für den Dev-DB-User                             | `invoices`                |
| `APP_BUILD_DB_NAME`      | Name der Build-Datenbank                                 | `invoices_build`          |
| `APP_BUILD_DB_USER`      | DB-Benutzer für die Build-Datenbank                      | `invoices`                |
| `APP_BUILD_DB_PASS`      | Passwort für den Build-DB-User                           | `invoices`                |
| `APP_DB_HOST`            | Host der Datenbank (z.B. `localhost`)                    | `localhost`               |
| `APP_PORT`               | Port, auf dem der Service läuft (für Start/Stopp-Checks) | `8090`                    |
| `PROJECT_DIR`            | Pfad zum Projektverzeichnis (nur wenn abweichend)        | `/opt/cocondo/invoices`   |
| `LOG_LEVEL`              | Log-Level (`DEBUG`, `INFO`, `WARN`, `ERROR`)             | `WARN`                    |
| `DEPL_REMOTE_HOST`       | Remote-Host für das Deployment (scp)                     | `www.callstack.de`        |
| `DEPL_REMOTE_DIR`        | Zielverzeichnis auf dem Remote-Host                      | `/var/www/vhosts/...`     |
| `BASE_URL`               | Basis-URL für `jlogin` (falls abweichend)                | `https://api.example.com` |
| `APP_PROFILE` (optional) | Spring-Profil für den Start (Standard: `dev`)            | `prod`                    |

---

## Nutzung

1. **.env anlegen**: Kopieren Sie `init.env.sh` als Template oder erstellen Sie eine `.env` im Projekt-Root mit projektspezifischen Werten.
2. **Permissions**: Stellen Sie sicher, dass alle Skripte ausführbar sind (`chmod +x bin/**/*.sh`).
3. **Build & Deployment**: Starten Sie im Projekt-Root:

   ```bash
   bin/build.sh
   ```

---

*Ende der Dokumentation*
