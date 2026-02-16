# ADR-001: Basisarchitektur und Schichtentrennung

## Status

**Accepted**

---

## Kontext

Für den IDM Service wird eine eigenständige, langlebige Anwendung mit sensiblen Daten aufgebaut. Die Architektur muss daher:

* fachliche und technische Aspekte klar trennen
* unbeabsichtigte Kopplungen vermeiden
* langfristig wartbar und erweiterbar bleiben
* spätere Architekturabweichungen erkennbar machen

Im Zuge des Aufbaus der Basisstrukturen (Sprint 1) wurden mehrere grundlegende Architekturentscheidungen getroffen, die verbindlich dokumentiert werden müssen, um Konsistenz und Nachvollziehbarkeit sicherzustellen.

---

## Entscheidung

### 1. Trennung zwischen Domain und System

Die Anwendung unterscheidet explizit zwischen fachlichen und technischen Konzepten:

* `domain` enthält ausschließlich fachliche Modelle (Aggregate, Entities, Value Objects)
* `system` enthält ausschließlich technische und infrastrukturelle Konzepte

**Abhängigkeitsregel:**

* `domain` darf keine Abhängigkeiten zu `system` besitzen
* `system` darf auf `domain` referenzieren (z. B. für Mapping oder Transport)

Diese Trennung stellt sicher, dass die Domäne unabhängig von technischen Transport- oder Framework-Entscheidungen bleibt.

---

### 2. Package-Struktur

Die oberste Package-Struktur der Anwendung ist verbindlich festgelegt:

```
de.cocondo.app
├── domain
├── system
├── application
├── persistence
├── web
├── config
├── security
```

Regeln:

* Fachliche Untergliederung erfolgt innerhalb von `domain`
* Technische Querschnitte liegen nicht im `domain`-Package
* Zyklische Abhängigkeiten zwischen Top-Level-Packages sind unzulässig

---

### 3. Aggregate und Entities

Aggregate Roots werden als normale JPA-Entities umgesetzt.

Regeln:

* Aggregate Roots erhalten kein Marker-Interface
* Die Rolle als Aggregate Root ergibt sich aus fachlicher Bedeutung, Package-Kontext und Nutzung im Application Layer

Begründung:

Aggregate sind fachliche Konzepte. Eine technische Markierung würde keinen zusätzlichen fachlichen Nutzen bringen und die Persistenz unnötig verkomplizieren.

---

### 4. Marker-Interface für DTOs

Alle Data Transfer Objects implementieren ein zentrales Marker-Interface:

```
de.cocondo.app.system.dto.DataTransferObject
```

Regeln:

* DTOs enthalten keine JPA-Annotationen
* DTOs enthalten keine Business-Logik
* DTOs dienen ausschließlich dem Transport zwischen Schichten

Das Marker-Interface dient der klaren Erkennbarkeit von DTOs und ermöglicht spätere Architektur- und Qualitätsprüfungen.

---

### 5. DTO-Typen und Verantwortlichkeiten

Es wird zwischen verschiedenen DTO-Arten unterschieden:

1. **Payload DTOs**

    * enthalten fachliche Daten ohne Kontext
    * können für Create- oder Update-Vorgänge genutzt werden

2. **DTOs mit ID**

    * repräsentieren bestehende Aggregate
    * enthalten zusätzlich die technische Identität

3. **Use-Case-spezifische DTOs**

    * explizit für konkrete Aktionen (z. B. Create, Partial Update)
    * werden nicht wiederverwendet, wenn sich die Semantik unterscheidet

Diese Differenzierung verhindert überladene DTOs und macht API-Semantik explizit.

---

### 6. Klassenkommentare

Alle Klassen müssen einen Klassenkommentar besitzen.

Der Kommentar beschreibt:

* die Rolle der Klasse
* ihre Verantwortung im System

Implementierungsdetails werden nicht im Klassenkommentar dokumentiert.

---

### 7. Schichtentrennung

Die Anwendung folgt einer klaren Schichtenarchitektur:

* Web Layer kommuniziert ausschließlich mit dem Application Layer
* Der Application Layer ist der einzige Zugriffspunkt auf Aggregate
* Der Persistence Layer ist rein technisch und enthält keine Business-Logik

---

### 8. Umgang mit statischem Zustand

Regeln:

* Keine fachlichen Informationen in statischen Feldern
* Konfiguration erfolgt ausschließlich über Dependency Injection und Properties

Dies stellt einen kontrollierten Lifecycle und eine saubere Testbarkeit sicher.

---

### 9. Konventionsbasierte Architektur

Die Architektur wird primär durch Konventionen umgesetzt:

* Package-Struktur
* Namenskonventionen
* klare Verantwortlichkeiten

Framework-spezifische Marker werden nur dort eingesetzt, wo sie fachlich oder technisch notwendig sind.

---

## Konsequenzen

**Positive Auswirkungen:**

* klare fachlich-technische Trennung
* hohe Wartbarkeit und Erweiterbarkeit
* geringe Gefahr unbeabsichtigter Architekturabweichungen
* gute Grundlage für spätere Architekturprüfungen (z. B. ArchUnit)

**Negative Auswirkungen / Trade-offs:**

* höherer initialer Disziplinbedarf
* mehr explizite Klassen (z. B. spezifische DTOs)

Diese Trade-offs werden bewusst akzeptiert zugunsten langfristiger Qualität.

---

## Gültigkeit

Diese Architekturentscheidungen gelten ab Sprint 1 für den gesamten IDM Service.

Abweichungen sind nur zulässig, wenn sie durch ein neues ADR explizit dokumentiert und begründet werden.
