# IDM – ADR 008: List API Standard für UI-fähige Management-Listen

Stand: 2026-03-27
Status: Accepted (Sprint 8 – verbindliche Architekturentscheidung)
Version: 1.0

---

## 1. Kontext

Der IDM-Service besitzt nach dem aktuellen Baseline-Stand eine fachlich nutzbare Management-API für die zentralen Aggregate:

* UserAccount
* Role
* ApplicationScope
* Zuordnungen / Assignments

Die Baseline enthält bereits REST-Endpunkte zum Lesen von Listen, jedoch aktuell in einer einfachen Form:

* rohe `List<DTO>`-Antworten
* keine garantierte serverseitige Pagination
* keine standardisierte Sortierung
* keine standardisierte Filterung
* keine explizit UI-stabile Listenstruktur für generische Admin-Komponenten

Für Sprint 8 ist das Zielbild verbindlich:

* Ein Admin-UI muss unmittelbar auf dem IDM aufsetzen können
* Listen müssen generisch in UI-Komponenten nutzbar sein
* Filter, Sortierung und Pagination müssen API-seitig unterstützt werden
* Die Lösung muss regressionsarm auf der bestehenden Baseline aufbauen

Gleichzeitig ist die bestehende API bereits vorhanden und soll nicht unnötig durch Breaking Changes destabilisiert werden.

---

## 2. Problembeschreibung

### 2.1 Fachlich-technisches Problem

Die aktuellen Listen-Endpunkte sind für eine generische UI-Anbindung nicht ausreichend belastbar, weil:

1. ein UI keine stabile Paged-Response-Struktur erwarten kann,
2. serverseitige Pagination fehlt bzw. nicht als verbindlicher API-Vertrag existiert,
3. Filterung und Sortierung nicht standardisiert sind,
4. relationale Listen (z. B. Zuordnungen) denselben Bedarf haben,
5. ein direkter Umbau bestehender Endpunkte von `List<DTO>` auf einen Page-Wrapper unnötige Regressionen erzeugen kann.

### 2.2 Risiko eines direkten Umbaus

Ein harter Umbau bestehender Collection-GET-Endpunkte würde:

* bestehende Tests gefährden,
* potenzielle Consumer brechen,
* unnötige API-Inkompatibilitäten erzeugen,
* Sprint 8 unnötig verbreitern.

Für Sprint 8 ist daher eine **minimal-invasive, aber verbindliche Standardisierung** erforderlich.

---

## 3. Entscheidung

### 3.1 Grundsatzentscheidung

Für Sprint 8 wird ein **verbindlicher, UI-fähiger Listenstandard** eingeführt.

Dieser Standard gilt für:

* Hauptlisten der IDM-Management-API
* relationale Listen / Zuordnungslisten
* alle neuen UI-orientierten Listenendpunkte

Der Standard ist **nicht optional**, sondern bildet die verbindliche Grundlage für das nutzbare MVP nach Sprint 8.

---

### 3.2 Bestehende Legacy-Listen bleiben stabil

Bestehende Collection-GET-Endpunkte mit `List<DTO>`-Vertrag bleiben in Sprint 8 **zunächst stabil**, sofern ihr Umbau ein unnötiges Breaking Change Risiko erzeugen würde.

Beispiele:

* `GET /api/idm/users`
* `GET /api/idm/roles`
* `GET /api/idm/scopes`

Verbindliche Regel:

> Bestehende Listen-Endpunkte dürfen in Sprint 8 nicht ohne zwingenden Grund hart auf ein neues Response-Format umgestellt werden, wenn dadurch Regressionen oder Consumer-Breaks entstehen könnten.

---

### 3.3 Neue dedizierte UI-Listenendpunkte

Für UI-fähige Listen werden **dedizierte zusätzliche Listenendpunkte** eingeführt.

Empfohlene Zielstruktur:

* bestehender Collection-GET-Endpunkt bleibt erhalten
* zusätzlicher UI-Listenendpunkt wird ergänzt

Beispielhafte Form (Pfadprinzip, nicht als bereits implementiert zu verstehen):

* `/api/idm/users/list`
* `/api/idm/roles/list`
* `/api/idm/scopes/list`

Verbindliche Regel:

> Neue UI-fähige Listen werden bevorzugt als zusätzliche, dedizierte Listenendpunkte eingeführt, statt bestehende Collection-GET-Endpunkte mit hohem Breaking-Change-Risiko umzubauen.

---

### 3.4 Einheitlicher Query-Parameter-Standard

Alle UI-Listenendpunkte verwenden einen gemeinsamen, schlanken Query-Parameter-Standard.

Verbindliche Mindestparameter:

* `page`
* `size`
* `sortBy`
* `sortDir`

Zusätzliche Filter:

* nur **explizit pro Endpoint freigegebene Felder**
* keine freie Query-DSL
* keine dynamische Reflection-basierte Filterengine

Verbindliche Regel:

> Filterung erfolgt nur über explizit definierte, whitelist-basierte Parameter je Endpoint.

---

### 3.5 Einheitliche Listen-Response-Struktur

UI-Listen liefern eine **eigene, stabile API-Response-Struktur**.

Mindestbestandteile:

* `items`
* `page`
* `size`
* `totalElements`
* `totalPages`

Optional zulässig:

* `sortBy`
* `sortDir`
* weitere Meta-Felder, sofern sie konsistent und stabil dokumentiert sind

Verbindliche Regel:

> Öffentliche Listen-APIs geben keinen rohen Spring-`Page<T>`-Vertrag als API-Standard aus, sondern eine bewusst kontrollierte, schlanke API-Response-Struktur.

---

### 3.6 Keine übergenerische Query-Architektur in Sprint 8

Sprint 8 führt **keine** allgemeine Query-DSL und **kein** komplexes generisches Search-Framework ein.

Explizit nicht im Scope:

* freie Suchsprache
* beliebige Feldkombinationen ohne Freigabe
* Reflection- oder Annotation-Magie als Primärmodell
* generische Cross-Domain-Meta-Abstraktion als Selbstzweck

Stattdessen gilt:

* kleiner gemeinsamer Listenstandard
* pro Endpoint definierte Filter-/Sort-Felder
* deterministische, testbare Implementierung

---

### 3.7 Geltung auch für relationale Listen

Der Listenstandard gilt **nicht nur** für Hauptaggregate, sondern auch für relationale / zuordnungsbezogene Listen, insbesondere dort, wo ein Admin-UI Relationen anzeigen muss.

Beispiele:

* User ↔ Role
* User ↔ ApplicationScope
* Role ↔ Permission

Verbindliche Regel:

> Relation-Reads verwenden denselben Listenstandard wie Hauptlisten, sofern sie als UI-relevante Listen bereitgestellt werden.

---

## 4. Konsequenzen

### 4.1 Positive Konsequenzen

* Sofort UI-fähige Listenverträge für generische Admin-Komponenten
* Keine unnötigen Breaking Changes an bestehenden Legacy-Listen
* Einheitliche API-Nutzung für Pagination / Sort / Filter
* Saubere Grundlage für Relation-Read-APIs
* Gute Erweiterbarkeit für spätere Ausbaustufen

### 4.2 Trade-offs

* Vorübergehend koexistieren Legacy-Listen und UI-Listen
* Zusätzliche Endpunkte erhöhen die API-Oberfläche leicht
* Es entsteht bewusst ein kleiner zusätzlicher API-Standard, der dokumentiert und getestet werden muss

---

## 5. Umsetzungsvorgaben für Sprint 8

Sprint 8 setzt diesen ADR in folgender Reihenfolge um:

1. Hauptlisten:
    * Users
    * Roles
    * ApplicationScopes

2. Danach relationale Listen:
    * User ↔ Role
    * User ↔ ApplicationScope
    * Role ↔ Permission

3. Erst danach:
    * Fehlerbild-Härtung für Listenparameter und Konfliktfälle

Verbindliche Regel:

> Der Listenstandard wird zuerst auf den Hauptaggregaten stabilisiert und erst anschließend auf Relation-Reads ausgerollt.

---

## 6. Abgrenzung

Nicht Bestandteil dieses ADR:

* konkrete Java-Klassennamen
* konkrete DTO-Klassennamen
* konkrete Repository-Schnittdetails
* konkrete technische Implementierungsdetails (z. B. `JpaSpecificationExecutor` vs. alternative interne Strategie)
* Volltextsuche / Fuzzy-Suche
* Record-Level-Security / Sichtbarkeitsfilter
* UI-Implementierung

Diese Punkte werden erst in der Code-Umsetzung deterministisch anhand der realen Baseline finalisiert.

---

## 7. Beziehung zu bestehenden ADRs

Dieser ADR ergänzt und konkretisiert insbesondere:

* **ADR-001** – Basisstrukturen / Abhängigkeitsregeln
* **ADR-003** – Authentication & Authorization Architecture
* **ADR-006** – Bootstrap / Initial Data Strategy
* **ADR-007** – Domain Permissions / Rollen-zu-Rechte-Auflösung

Wichtig:

* ADR-008 verändert nicht die Authentifizierungs- oder Autorisierungsarchitektur.
* ADR-008 definiert ausschließlich den verbindlichen Standard für UI-fähige Listen in der Management-API.

---

## 8. Status / Verbindlichkeit

* Accepted (Sprint 8)
* Verbindliche Grundlage für alle UI-fähigen Listenendpunkte im IDM
* Muss vor der Code-Umsetzung von Sprint 8 berücksichtigt werden

---

## 9. Zusammenfassung

Sprint 8 benötigt für ein nutzbares MVP einen verbindlichen Listenstandard.

Die Lösung lautet:

* bestehende Legacy-Listen stabil halten,
* neue dedizierte UI-Listenendpunkte ergänzen,
* ein einheitliches Query-Modell für Pagination / Sort / Filter einführen,
* eine stabile, schlanke Listen-Response-Struktur verwenden,
* Relation-Reads auf denselben Standard aufbauen.

Damit entsteht eine regressionsarme, deterministische und UI-taugliche Basis für die Management-API des IDM.