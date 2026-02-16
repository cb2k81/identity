# IDM – Sprint 1 Abschlussbericht (Sprint 1.2)

## 1. Ziel des Sprints

Sprint 1 hatte das Ziel, ein **stabileres technisches Fundament (Bootstrap-Sprint)** für den IDM Service zu schaffen. Der Fokus lag bewusst **nicht auf Fachlogik**, sondern auf:

* reproduzierbarem Build
* sauberer Projektstruktur
* klaren Architekturleitplanken
* dokumentierten Grundsatzentscheidungen

Der Sprint sollte einen Zustand herstellen, in dem alle folgenden Sprints **risikoarm, deterministisch und konsistent** aufbauen können.

---

## 2. Geplanter Scope (Rückblick)

Der geplante Scope von Sprint 1 umfasste:

* Abstimmung und Aufbau der Projekt- und Verzeichnisstruktur
* Erstellung einer tragfähigen `pom.xml`
* Basis-Konfiguration (`application.yml`)
* technische Startfähigkeit der Anwendung
* Festhalten grundlegender Architekturentscheidungen

Nicht Bestandteil des Sprints waren bewusst:

* fachliche Use Cases
* produktive Security-Implementierungen
* vollständige Persistenz- oder Domain-Modelle

---

## 3. Umgesetzte Ergebnisse

### 3.1 Projekt- & Build-Basis

* Maven-Projekt erfolgreich initialisiert
* `pom.xml` erstellt und stabilisiert
* Spring Boot JAR Build funktionsfähig
* Reproduzierbarer Build mit `mvn clean install`

**Ergebnis:**

* Der Build läuft fehlerfrei durch
* Artefakte werden korrekt erzeugt und installiert

---

### 3.2 SBOM & Build-Transparenz

* CycloneDX Maven Plugin integriert
* Automatische Erzeugung einer SBOM (XML & JSON) im Build-Lifecycle (`verify`)

**Ergebnis:**

* Transparenz über verwendete Abhängigkeiten
* Grundlage für spätere Security-Scans und Compliance-Anforderungen

---

### 3.3 Basis-Konfiguration

* Zentrale `application.yml` erstellt
* Profile vorbereitet (`dev`, `test`)
* Actuator aktiviert (Health, Probes)
* Logging-Basis definiert
* JPA & Liquibase technisch aktiviert (ohne Fachschema)

**Ergebnis:**

* Spring Context ist startfähig
* Keine produktiven Secrets oder Annahmen enthalten

---

### 3.4 Datenbank-Grundlage

* SQL-Skripte zur Anlage der Datenbank `idm`
* Einrichtung eines minimal privilegierten DB-Users `idm`
* Rechte passend für JPA und Liquibase vergeben

**Ergebnis:**

* Technische Persistenz-Grundlage vorhanden
* Trennung zwischen Infrastruktur- und Fachschema möglich

---

### 3.5 Zentrale Application-Klassen

* Review und Anpassung der zentralen Start- und Konfigurationsklassen
* Entfernung statischer fachlicher Zustände
* saubere Initialisierung über Spring Lifecycle
* präzisiertes Component- und Entity-Scanning

**Ergebnis:**

* klarer, wartbarer Einstiegspunkt der Anwendung
* bessere Testbarkeit und Lifecycle-Kontrolle

---

### 3.6 Architekturentscheidungen (ADR)

* Erstellung von **ADR-001: Basisarchitektur und Schichtentrennung**
* Dokumentation u. a. folgender Entscheidungen:

    * Trennung zwischen `domain` und `system`
    * feste Package-Struktur
    * Aggregates als reine Entities
    * Marker-Interface für DTOs
    * klare DTO-Typisierung
    * verbindliche Klassenkommentare

**Ergebnis:**

* Architekturentscheidungen sind nachvollziehbar und versioniert dokumentiert
* Grundlage für konsistente Weiterentwicklung

---

## 4. Review: Zielerreichung

| Ziel                                | Status   |
| ----------------------------------- | -------- |
| Reproduzierbarer Build              | erreicht |
| SBOM-Erstellung                     | erreicht |
| Basis-Konfiguration                 | erreicht |
| Startfähigkeit der Anwendung        | erreicht |
| Architekturleitplanken dokumentiert | erreicht |

**Gesamtbewertung:**

Sprint 1 ist **vollständig und erfolgreich abgeschlossen**.

---

## 5. Abweichungen & Risiken

* Keine fachlichen Abweichungen vom geplanten Scope
* Keine Architekturverletzungen
* Keine technischen Blocker für Folgesprints

Bewusst akzeptierte Einschränkungen:

* Keine fachlichen Entities oder Use Cases
* Keine produktive Security-Konfiguration

Diese Einschränkungen sind **Sprint-1-konform** und gewollt.

---

## 6. Ergebniszustand nach Sprint 1

Nach Abschluss von Sprint 1 existiert:

* ein lauffähiges, sauberes Projektgerüst
* ein stabiler Build- und Konfigurationsstand
* eine dokumentierte Basisarchitektur

Alle folgenden Sprints können **ohne strukturelle Nacharbeiten** auf diesem Stand aufsetzen.

---

## 7. Freigabe

Sprint 1 wird hiermit formal abgeschlossen.

Die Ergebnisse sind:

* geprüft
* dokumentiert
* konsistent

Der Projektzustand ist bereit für den Übergang zu Sprint 2.
