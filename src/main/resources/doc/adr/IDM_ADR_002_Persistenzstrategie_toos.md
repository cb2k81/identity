# ADR-002: Persistenzstrategie und Tooling (Hibernate ↔ Liquibase)

## Status

**Accepted**

---

## Kontext

Für den IDM Service wird eine relationale Datenbank mit JPA/Hibernate als ORM eingesetzt. Gleichzeitig ist Liquibase als langfristiges Werkzeug für Schema-Migrationen und Versionskontrolle vorgesehen.

In einer sehr frühen Projektphase (vor externer Nutzung oder produktivem Betrieb) besteht das Bedürfnis,:

* Entity-Modelle schnell zu validieren
* Schema-Details iterativ zu entwickeln
* initiale Liquibase-Changelogs effizient zu erzeugen

Zusätzlich existiert ein projektübergreifend standardisiertes Tooling (`bin/`-Skripte), das Datenbank- und Liquibase-Aktionen kapselt und vereinheitlicht.

Diese Situation erfordert eine **bewusst geregelte Übergangsstrategie** zwischen Hibernate und Liquibase.

---

## Entscheidung

### 1. Temporäre Nutzung von Hibernate zur Schema-Erzeugung

Hibernate darf in einer frühen Projektphase zur **initialen Schema-Erzeugung** verwendet werden.

Regeln:

* Hibernate-Schema-Generierung ist **zeitlich begrenzt**
* sie dient ausschließlich:

    * der Validierung von Entity-Mappings
    * der Erzeugung eines initialen Referenzschemas
* sie ist **kein dauerhaftes Betriebsmodell**

---

### 2. Übergabepunkt zu Liquibase

Es existiert ein **klar definierter Übergabepunkt**:

1. Hibernate erzeugt das initiale Schema
2. Das Schema wird über standardisierte `bin/`-Skripte exportiert
3. Daraus wird ein initiales Liquibase-Changelog erzeugt
4. Ab diesem Zeitpunkt ist Liquibase die **alleinige Quelle der Wahrheit** für das Schema

Nach diesem Übergang:

* Hibernate darf **keine** Schema-Änderungen mehr durchführen
* Hibernate dient nur noch der **Validierung** gegen das bestehende Schema

---

### 3. Hibernate-Konfiguration nach dem Übergang

Nach Erstellung des initialen Liquibase-Changelogs gilt verbindlich:

* `hibernate.hbm2ddl.auto = validate`
* Schema-Änderungen erfolgen ausschließlich über Liquibase

Ein Mischbetrieb (Hibernate erzeugt oder ändert Schema parallel zu Liquibase) ist **unzulässig**.

---

### 4. Ablageort der Changelogs

Alle Liquibase-Changelogs liegen verbindlich unter:

```
src/main/resources/db/changelog
```

Abweichende Pfade sind nicht zulässig.

---

### 5. Tooling als Teil der Architektur

Die im Projekt vorhandenen `bin/`-Skripte gelten als **architekturelles Tooling**.

Regeln:

* Skripte kapseln Datenbank- und Liquibase-Operationen
* Skripte sind projektübergreifend einheitlich
* Änderungen an den Skripten sind architektur-relevant
* individuelle lokale Anpassungen sind unzulässig

---

## Konsequenzen

**Positive Auswirkungen:**

* schneller, sicherer Projektstart
* frühe Validierung des Domain-Modells
* saubere Übergabe in eine versionierte Migrationsstrategie
* einheitliche Bedienung über alle Projekte hinweg

**Negative Auswirkungen / Trade-offs:**

* erfordert Disziplin beim Übergangspunkt
* initial etwas mehr Koordinationsaufwand

Diese Trade-offs werden bewusst akzeptiert, um langfristige Konsistenz und Wartbarkeit sicherzustellen.

---

## Gültigkeit

Diese Entscheidung gilt für alle Projekte und Module des IDM Services.

Abweichungen sind nur zulässig, wenn sie durch ein weiteres ADR explizit dokumentiert und begründet werden.
