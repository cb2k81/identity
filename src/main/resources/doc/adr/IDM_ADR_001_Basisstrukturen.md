# ADR-001 – Basisstrukturen und Abhängigkeitsregeln (Version 3)

## Status

Accepted

---

## Kontext

Der personnel Service folgt einer klar strukturierten, schichtenbasierten Architektur mit fachlicher Trennung nach Domänen sowie technischen Basisdiensten.

In der ersten Fassung dieses ADRs war definiert, dass das `domain`-Package keine Abhängigkeiten zum `system`-Package besitzen darf. Diese Regel ist nicht mit der tatsächlichen Architektur vereinbar, da zentrale technische Basiskomponenten (z. B. `DomainEntity`, ID-Generierung, Auditierung, Metadaten) im `system`-Package verortet sind und von fachlichen Entitäten verwendet werden.

Zur Sicherstellung einer konsistenten und stabilen Architektur wird die Abhängigkeitsregel präzisiert.

Zusätzlich wurde im Projektverlauf sichtbar, dass für technische Basisklassen und persistente Entities verbindliche Regeln benötigt werden, um:

* Proxy-sichere Equality zu gewährleisten (Hibernate/JPA)
* Lombok gezielt und risikominimiert einzusetzen
* technische Metadaten (Tags, Key-Value) konsistent zu modellieren, ohne redundante Persistenzattribute

---

## Entscheidung

### 1. Paketstruktur

Der personnel Service ist in folgende Hauptpakete gegliedert:

* `web` – REST-Controller, API-Endpunkte
* `domain` – Fachliche Aggregate, Value Objects, Domain Services
* `persistence` – Repositories und Persistenzadapter
* `system` – Technische Basiskomponenten (Shared Kernel)
* `config` – Konfiguration
* `security` – Sicherheitsintegration

---

### 2. Rolle des `system`-Packages

Das `system`-Package stellt einen **Shared Kernel für technische Infrastruktur** dar.

Es enthält ausschließlich technische Basiskomponenten, z. B.:

* `DomainEntity`
* ID-Generierungsmechanismen
* Auditierungsinfrastruktur
* Metadaten-Mechanismen (Tags, Key-Value-Paare)
* technische Hilfsklassen

Das `system`-Package enthält **keine fachlichen Konzepte oder Domänenlogik**.

---

### 3. Abhängigkeitsregeln

Die zulässigen Abhängigkeiten sind wie folgt definiert:

```
web        → domain
web        → system (nur technische Aspekte wie Error-Handling)

domain     → system

persistence → domain
persistence → system

system     → (keine Abhängigkeit zu domain)
```

Zentrale Regel:

> Das `domain`-Package darf auf `system` zugreifen.
> Das `system`-Package darf niemals auf `domain` zugreifen.

Damit ist die Abhängigkeitsrichtung eindeutig von fachlich zu technisch gerichtet.

---

### 4. Architekturprinzipien

1. **Fachliche Isolation**
   Sämtliche Geschäftslogik (Workflow-Regeln, Mutability-Prüfungen, Invarianten, Budget- und Kapazitätslogik, Ledger-Erzeugung etc.) befindet sich ausschließlich im `domain`-Package.

2. **Technische Kapselung**
   Technische Infrastruktur (ID, Audit, Version-Feld, Basisklassen, generische Utilities) liegt ausschließlich im `system`-Package.

3. **Keine Zyklen**
   Zyklische Paketabhängigkeiten sind unzulässig.

4. **DomainEntity ist rein technisch**
   Alle fachlichen Aggregate Roots erben von `system.entity.DomainEntity`.
   `DomainEntity` enthält ausschließlich technische Basisfunktionalität (z. B. technische ID, Audit-Felder, Versionsfeld).
   Sie enthält keine fachliche Logik, keine Workflow-Regeln und keine planbezogenen Konzepte.

5. **Workflow- und Mutability-Logik verbleiben in der Domäne**
   Statusübergänge, Mutability-Prüfungen (z. B. DRAFT/APPROVED-Regeln) sowie Append-Only-Regeln werden ausschließlich im `domain`-Layer implementiert.
   Das `system`-Package darf keine generischen Mechanismen enthalten, die fachliche Statuslogik erzwingen oder umgehen.

6. **Ledger ist fachliches Konzept**
   Ein fachliches Ledger (z. B. StaffingLedgerEntry) ist Bestandteil der Domäne.
   Das `system`-Package stellt lediglich technische Audit-Infrastruktur bereit und darf kein fachliches Ereignisprotokoll enthalten.

7. **Repositories umgehen keine Fachregeln**
   Persistenzkomponenten dürfen fachliche Mutability- oder Workflow-Regeln nicht umgehen.
   Die Einhaltung von Lifecycle- und Append-Only-Regeln wird im Domain-Layer geprüft und ist vor Persistenzoperationen sicherzustellen.

8. **Technik kennt keine Fachlichkeit**
   Das `system`-Package darf keinerlei Referenzen auf fachliche Klassen oder planbezogene Aggregate enthalten.

---

### 5. Verbindliche Regeln für `DomainEntity` und persistente Entities

Dieser Abschnitt konkretisiert technische Mindeststandards für Entities (Shared Kernel und fachliche Entities), um Stabilität, JPA-Kompatibilität und deterministisches Verhalten sicherzustellen.

#### 5.1 Equality-/HashCode-Strategie (Proxy-sicher)

* Entities müssen eine **proxy-sichere** Equality-Strategie verwenden.
* `equals()` und `hashCode()` dürfen **keine** Collections, Relationen (z. B. `@OneToMany`, `@ManyToOne`) oder mutable Fachattribute einbeziehen.
* Equality basiert auf:

   * **technischer Identität** (ID) sobald gesetzt, und
   * einer proxy-sicheren Klassenprüfung (Hibernate Proxy / `Hibernate.getClass(this)`-Strategie oder äquivalent).

**Normative Regel:**

> In Entities ist `@Data` nicht zulässig, wenn dadurch automatisch `equals()/hashCode()/toString()` generiert wird, die Relationen oder Collections einbeziehen könnten.

#### 5.2 Lombok-Regeln (Entities und DTOs)

**Für Entities (JPA):**

* `@Data` ist **nicht** zulässig.
* Zulässig sind zielgerichtete Lombok-Annotationen, z. B.:

   * `@Getter`, `@Setter`
   * `@NoArgsConstructor` (sofern JPA benötigt)
   * `@ToString(onlyExplicitlyIncluded = true)` bzw. selektive `@ToString.Include`
   * `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` oder projektweit definierte, explizite Strategy
* Relationen müssen aus `toString()` und `equals()/hashCode()` ausgeschlossen werden (z. B. `@ToString.Exclude`, `@EqualsAndHashCode.Exclude`).

**Für DTOs:**

* DTOs dürfen `@Data` verwenden, **sofern** keine Vererbung betroffen ist.
* Bei DTO-Vererbung ist `@EqualsAndHashCode(callSuper = false/true)` explizit zu setzen, um Warnungen und implizite Entscheidungen zu vermeiden.

#### 5.3 `toString()`-Regeln

* `toString()` darf keine Relationen traversieren (Lazy-Loading/Proxy-Fallen).
* Es sollen nur stabile Identifikatoren/Schlüsselfelder enthalten sein.

#### 5.4 ID, Version und Audit als technische Querschnittsthemen

* ID, Persistence-Version und Audit-Felder sind technische Basiseigenschaften.
* Fachliche Invarianten dürfen nicht von Audit-/Technikfeldern abhängen.

---

### 6. Metadaten (Tags / Key-Value) als Teil des technischen Shared Kernel

Metadaten dienen der technischen, dynamischen Erweiterbarkeit von DomainEntities.

#### 6.1 Grundprinzip

* Metadaten sind **technisch**.
* Metadaten enthalten **keine** fachliche Logik, keine fachlichen Invarianten und keine Workflow-Regeln.

#### 6.2 Modellierungsregeln für Key-Value-Paare

* Ein Key-Value-Paar referenziert genau eine `DomainEntity` (Many-to-One).
* Es muss eine fachlich sinnvolle Eindeutigkeitsregel geben, z. B. `(domain_entity_id, key_name)`.

**Redundanzregel (verbindlich):**

> Relationale Beziehungen dürfen nicht redundant als zusätzliches Persistenzfeld (z. B. nochmals als `entityId`) modelliert werden.

Das bedeutet:

* Wenn eine Relation `domainEntity` existiert, ist sie die **einzige Quelle der Wahrheit** für die Zuordnung.
* Es darf kein zusätzliches FK-Spiegel-Feld in derselben Entity existieren.
* Unique-Constraints und Indizes sind ausschließlich auf Basis der tatsächlichen FK-Spalte (`domain_entity_id`) zu definieren.

Diese Regel dient der Vermeidung von Inkonsistenzen, Nullability-Fehlern und Synchronisationslogik zwischen doppelten Attributen.

#### 6.3 SQL/DB-Kompatibilität

* Spaltennamen müssen so gewählt werden, dass keine Konflikte mit SQL-Keywords entstehen.

* Falls erforderlich, sind explizite Spaltennamen zu definie

* Spaltennamen müssen so gewählt werden, dass keine Konflikte mit SQL-Keywords entstehen.

* Falls erforderlich, sind explizite Spaltennamen zu definieren.

---

## Konsequenzen

* Die Verwendung von `DomainEntity` innerhalb fachlicher Entitäten ist explizit zulässig.
* Gemeinsame technische Mechanismen (ID, Audit, Versionierung, Metadaten) werden zentral im `system`-Package gehalten.
* Änderungen an Basiskomponenten erfolgen ausschließlich im `system`-Package.
* ArchUnit-Tests oder vergleichbare Prüfmechanismen sollten die Abhängigkeitsrichtung absichern.

Zusätzliche Konsequenzen aus Version 3:

* Für Entities gilt eine verbindliche Lombok- und Equality-Policy.
* `@Data` ist in JPA-Entities verboten.
* Metadaten-Entities müssen redundant-frei bzw. deterministisch synchronisiert modelliert sein.

---

## Begründung

Diese Regelung ermöglicht:

* klare Trennung von Fachlichkeit und Technik
* Wiederverwendbarkeit technischer Basiskomponenten
* konsistente ID-, Audit- und Versionierungsstrategien
* stabile Erweiterbarkeit der Domäne ohne technische Streuung
* proxy-sicheres Verhalten von Entities unter Hibernate/JPA
* deterministische Persistenz von Metadaten ohne Inkonsistenzrisiken

Die Architektur entspricht damit einer klar definierten Schichtenarchitektur mit einem technischen Shared Kernel und einer strikt eingehaltenen Abhängigkeitsrichtung.
