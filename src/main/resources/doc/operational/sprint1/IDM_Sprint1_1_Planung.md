tbd...# IDM Service – Sprint 1 Planungsdokument

## 1. Zielsetzung des Sprints

Sprint 1 dient ausschließlich dem **technischen Fundament (Bootstrap-Sprint)** des IDM Services. Ziel ist es, eine **stabile, deterministische und konsistente Ausgangsbasis** zu schaffen, auf der alle fachlichen Implementierungen der folgenden Sprints ohne strukturelle oder konzeptionelle Risiken aufbauen können.

Der Sprint enthält **keine fachliche Logik**, **keine Domain-Implementierung** und **keine produktiven Security-Flows**. Alle Ergebnisse dieses Sprints sind bewusst generisch, aber architekturkonform.

---

## 2. Geltungsbereich (Scope)

### 2.1 Im Scope

Der Sprint umfasst ausschließlich folgende Themen:

* Finalisierung und Bereinigung der Projekt- und Verzeichnisstruktur
* Aufbau einer konsistenten Maven-Basis (`pom.xml`)
* Definition der zentralen Konfiguration (`application.yml` + Profile)
* Technische Basis-Konfiguration (Spring Boot, Actuator, JPA, Liquibase, Logging)
* Sicherstellung der lokalen Start- und Build-Fähigkeit

### 2.2 Explizit **nicht** im Scope

Nicht Bestandteil dieses Sprints sind:

* Fachliche Entities (z. B. UserAccount, Person, Role, …)
* REST-Endpunkte mit fachlicher Semantik
* Security-Flows (Login, JWT, Rollen, Permissions)
* Liquibase-Fach-Changelogs
* Fachliche Services oder Business-Logik

---

## 3. Architekturleitplanken für Sprint 1

Die im Fach- und Implementierungskonzept definierten Architekturentscheidungen gelten verbindlich. Für Sprint 1 werden sie jedoch **nur strukturell vorbereitet**, nicht funktional ausgeschöpft.

Zentrale Leitlinien:

* Monolithischer Spring-Boot-Service (Single Module)
* Klare Schichtung (keine fachliche Vermischung)
* Stateless-Grundannahme
* Konfigurationsgetriebene Architektur (keine Hardcodings)

---

## 4. Verzeichnis- und Paketstruktur (Sprint-1-Stand)

Die bestehende Struktur wird für Sprint 1 **bereinigt und vereinheitlicht**, ohne fachliche Tiefe einzuführen.

### 4.1 Zielstruktur (Java)

```text
src/main/java
└── de.<org>.idm
    ├── IdmApplication
    ├── config
    ├── security
    ├── web
    ├── application
    ├── domain
    ├── persistence
    └── common
```

**Erläuterungen:**

* `config`: Zentrale Spring-Konfiguration (Beans, Properties-Binding, Basiskonfig)
* `security`: Platzhalter für spätere Security-Konfiguration (noch ohne Fachlogik)
* `web`: Platzhalter für Controller (leer oder minimal)
* `application`: Platzhalter für Application-/Use-Case-Services
* `domain`: Platzhalter für Domain-Modell (leer)
* `persistence`: Platzhalter für Repositories & JPA-Basis
* `common`: Querschnittliches (Errors, Utils, Base-Classes)

➡️ **Hinweis:** Leere Packages sind in Sprint 1 ausdrücklich erlaubt und gewünscht.

---

## 5. Maven-Basis (`pom.xml`)

### 5.1 Ziel

Die `pom.xml` stellt:

* reproduzierbaren Build
* konsistente Dependency-Versionen
* klare Trennung zwischen Runtime, Test und optionalen Erweiterungen

sicher.

### 5.2 Inhaltliche Leitplanken

Sprint 1 umfasst u. a.:

* Spring Boot BOM
* Java-Version (konfiguriert, nicht verteilt)
* Dependencies:

    * spring-boot-starter-web
    * spring-boot-starter-security
    * spring-boot-starter-data-jpa
    * spring-boot-starter-actuator
    * liquibase-core
    * lombok
    * springdoc-openapi (Basis)
    * Test-Stack

Optionale, aber vorbereitete Bausteine:

* SBOM (CycloneDX Plugin)
* spätere JWT-Libraries **noch ohne Nutzung**

---

## 6. Konfiguration (`application.yml`)

### 6.1 Profile

* `default` / `prod`
* `dev`
* `test`

### 6.2 Inhaltliche Schwerpunkte

Sprint 1 definiert ausschließlich:

* Application-Metadaten
* Server-Port
* Logging-Grundstruktur
* Actuator-Endpunkte
* Datasource-Grundkonfiguration (lokal lauffähig)
* Liquibase-Aktivierung

Keine Secrets, keine produktiven Werte.

---

## 7. Basis-Konfiguration

### 7.1 Spring Boot

* Startfähige `@SpringBootApplication`
* Sauberer Context-Start ohne Warnungen

### 7.2 Actuator

* `/actuator/health`
* Readiness-/Liveness-Vorbereitung

### 7.3 Logging

* Einheitliches Log-Level-Konzept
* Keine sensiblen Daten
* Vorbereitung für strukturiertes Logging

### 7.4 Security (minimal)

* Default: deny-all oder noop
* Keine Authentifizierungslogik
* Keine Filterketten mit Fachbezug

---

## 8. Definition of Done (Sprint 1)

Der Sprint gilt als abgeschlossen, wenn:

* Projekt lokal startbar ist
* Maven Build erfolgreich durchläuft
* Actuator Health erreichbar ist
* Keine fachlichen Abhängigkeiten existieren
* Struktur konform zu den Konzepten ist
* Keine impliziten Annahmen getroffen wurden

---

## 9. Ergebnis & Übergang zu Sprint 2

Nach Sprint 1 existiert ein **stabiles technisches Fundament**.

Sprint 2 kann darauf aufbauen mit:

* Einführung erster Domain-Entities
* Liquibase-Fachschema
* Basale Security-Implementierung

Ohne strukturelle oder konzeptionelle Anpassungen an Sprint 1-Ergebnissen vornehmen zu müssen.
