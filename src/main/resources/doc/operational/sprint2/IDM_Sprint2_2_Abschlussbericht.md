# IDM – Sprint 2 Abschlussbericht (Sprint 2.2)

## 1. Ziel des Sprints

Sprint 2 hatte das Ziel, die in Sprint 1 definierten Architekturleitplanken durch einen vollständigen Architekturdurchstich zu validieren.

Im Fokus stand nicht die Breite der Domäne, sondern die saubere Umsetzung eines Referenz-Aggregats (`Person`) über alle relevanten Schichten hinweg.

Der Sprint sollte insbesondere klären:

* Funktioniert die zweistufige Service-Architektur in der Praxis?
* Sind Domain-, DTO- und Mapping-Grenzen klar und belastbar?
* Ist die Persistenzstrategie (Hibernate → Liquibase) technisch umsetzbar?

---

## 2. Geplanter Scope (Rückblick)

Sprint 2 umfasste ausschließlich den Architekturdurchstich für das Aggregate `Person`:

* Domain-Entity `Person`
* Repository im Aggregate-Package
* `PersonEntityService` (Stufe 1)
* `PersonDomainService` (Stufe 2)
* DTO-Struktur inkl. Marker-Interface
* MapStruct-basierter `PersonMapper`
* Integration von MapStruct in die Build-Konfiguration

Nicht Bestandteil des Sprints waren:

* weitere Aggregate
* Security-Implementierungen
* komplexe Validierungsregeln
* Update-/Delete-Use-Cases
* Pagination-Use-Cases

---

## 3. Umgesetzte Ergebnisse

### 3.1 Domain

* Implementierung des Aggregate Roots `Person`
* Saubere JPA-Entity ohne DTO- oder Infrastruktur-Abhängigkeiten
* Klare Trennung zwischen Domain-Objekt und Transportobjekten

---

### 3.2 Repository

* `PersonRepository` im Package des Aggregats
* Nutzung von `JpaRepository<Person, String>`
* Pagination- und Sorting-Funktionalität technisch verfügbar
* Keine Business-Logik im Repository

---

### 3.3 Zweistufige Service-Architektur

#### Stufe 1 – `PersonEntityService`

* Kapselt Persistenzzugriffe
* Arbeitet ausschließlich mit `Person`-Entities
* Kennt keine DTOs
* Kennt keine Berechtigungen

#### Stufe 2 – `PersonDomainService`

* Orchestriert Use Cases (`create`, `getById`)
* Arbeitet nach außen mit DTOs
* Arbeitet nach innen mit Entities
* Definiert Transaktionsgrenzen
* Nutzt Mapper und EntityService

Die Trennung wurde konsequent eingehalten und praktisch validiert.

---

### 3.4 DTO-Strategie

* `PersonPayloadDTO`
* `PersonDTO` mit `@EqualsAndHashCode(callSuper = true)`
* Implementierung des Marker-Interfaces `DataTransferObject`
* Keine JPA-Annotationen oder Business-Logik in DTOs

Die DTO-Struktur ist klar, erweiterbar und use-case-fähig.

---

### 3.5 MapStruct-Integration

* Einführung von MapStruct 1.5.5.Final
* Integration in den Maven-Compiler mit Annotation Processor
* `PersonMapper` im Aggregate-Kontext
* Keine ID-Erzeugung im Mapper

Build erfolgreich mit generierten Implementierungen unter `target/generated-sources`.

---

### 3.6 Persistenzstrategie (Übergangsphase)

* Hibernate temporär zur Schema-Generierung genutzt (`ddl-auto=create`)
* Liquibase bewusst deaktiviert
* Persistenzstrategie durch ADR-002 geregelt
* Klar definierter Übergabepunkt zu Liquibase vorgesehen

Die Strategie ist konsistent dokumentiert und technisch vorbereitet.

---

## 4. Architekturvalidierung

Sprint 2 bestätigt folgende Architekturentscheidungen:

* Domain/System-Trennung ist tragfähig
* Aggregate-nahe Bausteine (Repository, Services, Mapper) können konsistent im Kontext des Aggregats organisiert werden
* Zweistufige Service-Architektur funktioniert ohne Overengineering
* MapStruct integriert sich sauber in den Build-Prozess
* Tooling-Ansatz (`bin/`, `.env`, Liquibase-Vorbereitung) ist kompatibel

Es wurden keine Architekturverletzungen festgestellt.

---

## 5. Offene Punkte (bewusst verschoben)

* Liquibase-Initialchangelog erzeugen und Hibernate auf `validate` umstellen
* REST-Controller für End-to-End-Test
* Pagination-Use-Case
* Security-Integration

Diese Punkte sind bewusst Bestandteil zukünftiger Sprints.

---

## 6. Definition of Done – Bewertung

| Ziel                                | Status   |
| ----------------------------------- | -------- |
| Architekturdurchstich abgeschlossen | erreicht |
| Zweistufige Services validiert      | erreicht |
| MapStruct integriert                | erreicht |
| Persistenzstrategie geregelt        | erreicht |
| Keine Architekturverletzungen       | erreicht |

Sprint 2 ist vollständig abgeschlossen.

---

## 7. Ergebniszustand

Nach Abschluss von Sprint 2 existiert:

* ein vollständig validiertes Referenz-Aggregat (`Person`)
* eine geprüfte Service-Architektur
* eine saubere DTO- und Mapping-Strategie
* eine vorbereitete Persistenz-Migrationsstrategie

Die Architektur ist belastbar genug, um in Sprint 3 kontrolliert in die Breite zu gehen.

---

## 8. Freigabe

Sprint 2 wird hiermit formal abgeschlossen.

Die Ergebnisse sind konsistent, dokumentiert und architekturkonform umgese
