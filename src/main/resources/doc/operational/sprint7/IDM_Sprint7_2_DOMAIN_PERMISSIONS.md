# ADR – Domain Permissions / Rollen-zu-Rechte-Auflösung im gemeinsamen Security-Kernel

**Titel:** Domain Permissions – Generische Rollen-zu-Rechte-Auflösung im `system` Package bei domänenseitiger Mapping-Definition
**Stand:** 2026-03-16
**Version:** 1.0

---

## 1. Kontext

Im aktuellen Stand der IDM-App wird die JWT-basierte Berechtigungsprüfung technisch über das gemeinsame `system` Package abgewickelt.

Der relevante Ablauf ist:

* `system.security.jwt.JwtAuthenticationFilter`

    * validiert den JWT
    * extrahiert Claims
    * delegiert die Ermittlung effektiver Authorities an `PermissionResolver`
* `system.security.authorization.PermissionResolver`

    * technisches Interface für die Berechtigungsauflösung
* `system.security.authorization.DatabasePermissionResolver`

    * aktuelle Standardimplementierung
    * löst Rollen aus dem Token über IDM-Domainservices in Rechte auf

Der gemeinsame Anwendungskernel (`system` Package) ist für mehrere Anwendungen vorgesehen, nicht nur für das IDM.

Im aktuellen Ist-Zustand referenziert die konkrete Resolver-Implementierung jedoch direkt IDM-Domainservices und liegt trotzdem im `system` Package.

Gleichzeitig ist fachlich vorgegeben:

* Im JWT kommen Benutzer mit **Rollen**, nicht mit finalen Einzelrechten.
* Die auswertende Anwendung muss Rollen auf **eigene Rechte** auflösen.
* Die **konkreten Rechte** bleiben im Code der Anwendung verankert (Authorities / Permission-Konstanten).
* Das Rollen→Rechte-Mapping soll **änderbar und nicht hart im Fachcode verdrahtet** sein.
* Rechtegruppen sind fachlich zulässig und sollen unterstützt werden.

---

## 2. Problembeschreibung

### 2.1 Architekturelles Problem im Ist-Zustand

Die aktuelle Implementierung `DatabasePermissionResolver` liegt im gemeinsamen `system` Package, importiert jedoch direkt IDM-Domainservices.

Dadurch entsteht eine **verbotene fachliche Abhängigkeit** vom gemeinsamen technischen Kernel zur IDM-Domäne.

**Folgen:**

1. Das `system` Package ist in seiner aktuellen Form **nicht übertragbar**.
2. Die Security-Logik ist technisch zwar generisch gemeint, praktisch aber **IDM-gebunden**.
3. Fachanwendungen (z. B. Personnel) übernehmen damit ungewollt IDM-Kopplungen.

### 2.2 Fachliches Spannungsfeld

Es gibt zwei gleichzeitig gültige Anforderungen:

1. **Die Rollen→Rechte-Auflösung soll zentral/generisch im Core bleiben**, weil jede Anwendung diese Aufgabe benötigt.
2. **Die konkrete Rollen-/Rechte-/Rechtegruppen-Definition ist domänenspezifisch** und darf nicht im Core fest verdrahtet sein.

Das bedeutet:

* Die Auflösungslogik ist **technische Kernel-Verantwortung**.
* Die Mapping-Daten sind **domänenseitige / konfigurationsseitige Verantwortung**.

### 2.3 Unterschied zwischen IDM und Fachanwendungen

Das IDM ist die führende Identitäts- und Berechtigungsdomäne und verwaltet bereits fachlich:

* Scopes
  n- Rollen
* Rechtegruppen
* Rechte
* Rollen→Rechte-Zuordnungen
* User→Rollen-Zuordnungen

Fachanwendungen wie Personnel müssen dagegen **nicht zwingend** dieselben Konzepte als persistente Domain-Entities abbilden, solange sie:

* ihre Rechte im Code definieren,
* Rollen aus dem JWT lesen,
* das lokale Rollen→Rechte-Mapping konfigurationsgetrieben bereitstellen,
* und damit `@PreAuthorize` korrekt bedienen können.

---

## 3. Entscheidung

### 3.1 Grundsatzentscheidung

Die Rollen-zu-Rechte-Auflösung bleibt **im gemeinsamen `system` Package**.

**Aber:**

Die konkrete Rollen-/Rechte-/Rechtegruppen-Zuordnung wird **nicht mehr direkt aus einer Fachdomäne im Core gelesen**.

Stattdessen wird der gemeinsame Kernel so weiterentwickelt, dass er:

* eine **generische Rollen→Authorities-Auflösung** bereitstellt,
* aber die konkreten Mapping-Daten nur über eine **abstrahierte Quelle** bezieht.

### 3.2 Verbindlicher Ziel-Schnitt

Es gilt künftig folgende Trennung:

#### Im `system` Package verbleiben

* JWT-Technik (`JwtService`, `JwtAuthenticationFilter`)
* Security-Filterchain
* `PermissionResolver`
* generische Rollen→Authorities-Auflösung
* generische Abstraktion für die Bereitstellung des Rollen-/Rechte-Mappings

#### Nicht im `system` Package zulässig

* direkte Abhängigkeiten auf `de.cocondo.app.domain.idm...`
* direkte Abhängigkeiten auf Fachdomänen anderer Anwendungen
* fest verdrahtete Datenzugriffe auf konkrete Domänenmodelle innerhalb des generischen Resolver-Kerns

### 3.3 Fachliche Modellierung der Berechtigungen

Fachlich wird folgendes Modell unterstützt:

* **Rolle → Einzelrechte**
* **Rolle → Rechtegruppen**
* **Rechtegruppe → Einzelrechte**

Die finalen Authorities, die an Spring Security übergeben werden, sind immer **atomare Einzelrechte**.

### 3.4 Technische Zielsemantik im Resolver

Der generische Resolver arbeitet technisch idealerweise auf einer **bereits expandierten Endmenge von Einzelrechten**.

Das bedeutet:

* Die Quelle des Mappings darf intern Rechtegruppen kennen.
* Vor oder bei der Bereitstellung für den Resolver wird aufgelöst zu:

    * `roleName -> Set<permissionName>`
* Der Resolver selbst arbeitet dann nur noch mit finalen Einzelrechten.

**Begründung:**

Das hält den gemeinsamen Kernel klein, stabil und unabhängig von fachlichen Detailvarianten.

### 3.5 Rechte bleiben im Fachcode verankert

Die konkreten Rechte (Authorities) bleiben weiterhin in der jeweiligen Anwendung im Code verankert, z. B. als:

* Authority-Konstanten
* Permission-Klassen / Permission-Namespaces

Diese Rechte sind die verbindlichen Strings für `@PreAuthorize("hasAuthority(...)")`.

### 3.6 Rollen bleiben tokenseitig erhalten

Im JWT werden weiterhin **Rollen** transportiert, nicht fertige Einzelrechte.

Die Anwendung wertet den Token lokal aus und löst die Rollen anhand ihres eigenen Mappings in Einzelrechte auf.

### 3.7 User-zu-Rollen-Zuordnung gehört nicht in die Fachanwendung

Die lokale Fachanwendung benötigt für die reine Token-Auswertung **keine** eigene persistente User→Role-Zuordnung.

Die User-Rollen-Zuordnung ist Sache des ausstellenden Systems / Login-Kontexts.

Die Fachanwendung benötigt lokal nur:

* Token mit Rollen
* lokales Rollen→Rechte-Mapping
* Rechtekonstanten im Code

---

## 4. Entscheidung für IDM vs. Fachanwendungen

### 4.1 IDM-Perspektive

Das IDM bleibt die **führende Identitäts- und Berechtigungsdomäne**.

Für das IDM sind persistente Domain-Entities für Rollen, Rechte, Rechtegruppen und Zuordnungen weiterhin fachlich sinnvoll und zulässig.

Das IDM darf insbesondere weiterhin fachlich modellieren und verwalten:

* Application Scopes
* Rollen
* Rechtegruppen
* Rechte
* Rollen→Rechte-Zuordnungen
* User→Rollen-Zuordnungen

### 4.2 Fachanwendungs-Perspektive

Eine Fachanwendung (z. B. Personnel) **muss nicht** zwingend Rollen, Rechte und Rechtegruppen als JPA-Entities besitzen.

Für Fachanwendungen ist ein zulässiges Zielmodell:

* Rechte als Code-Konstanten
* Rollen-/Rechtegruppen-/Rechte-Mapping aus XML / Konfiguration
* lokale Token-Auswertung mit generischem Resolver aus dem `system` Package

### 4.3 XML-/Konfigurationsmodell als bevorzugte Fachanwendungsstrategie

Für Fachanwendungen wird explizit als zulässige und bevorzugte Strategie festgehalten:

* Laden des Rollen-/Rechte-Mappings aus externen Konfigurationsdateien (z. B. XML)
* kein Zwang zur Replikation des IDM-Domainmodells
* keine Pflicht, ein eigenes „Mini-IDM“ in jeder Fachanwendung zu bauen

---

## 5. Konsequenzen

### 5.1 Positive Konsequenzen

1. Das `system` Package wird wieder als **wirklich generischer Anwendungskernel** nutzbar.
2. Die zentrale technische Security-Logik bleibt an einer Stelle.
3. Fachanwendungen können ihre Rechte weiterhin im Code definieren.
4. Das Rollen→Rechte-Mapping bleibt änderbar und kann konfigurationsgetrieben gepflegt werden.
5. Rechtegruppen bleiben fachlich möglich, ohne den Core unnötig zu verkomplizieren.
6. Fachanwendungen werden nicht gezwungen, das IDM-Domainmodell nachzubauen.

### 5.2 Negative / zu beachtende Konsequenzen

1. Es ist eine **klare Abstraktion für die Mapping-Quelle** erforderlich.
2. Der bisherige `DatabasePermissionResolver` in seiner aktuellen Form ist architektonisch nicht mehr zulässig.
3. Die erste Umstellung muss regressionsarm erfolgen, damit bestehende Tests grün bleiben.
4. Die tatsächliche Übertragbarkeit kann nicht allein in IDM bewiesen werden; sie muss in einer Fachanwendung validiert werden.

---

## 6. Umsetzungsstrategie (verbindliche Reihenfolge)

### 6.1 Stufe 1 – Umsetzung zunächst ausschließlich in IDM

Die Weiterentwicklung des `system` Packages erfolgt **zunächst nur innerhalb der IDM-App**.

**Begründung:**

* kontrollierte Einführung
* geringeres Regressionsrisiko
* vorhandene Tests können direkt als Sicherheitsnetz dienen
* technische Reifung des generischen Kerns vor Übertragung in andere Apps

### 6.2 Stufe 2 – Verbindlicher Folgepunkt in Personnel

Nach Stabilisierung der Änderung in IDM wird die neue Architektur **gezielt in der Personnel-App** überprüft.

Dieser Folgepunkt ist **verbindlich** und nicht optional.

**Ziel der Personnel-Validierung:**

* Nachweis, dass das `system` Package ohne IDM-Domainkopplung funktioniert
* Prüfung, ob XML-/Konfigurationsmodell für Fachanwendungen ausreicht
* Nachweis, dass keine persistente Rollen-/Rechte-Domäne in der Fachanwendung erforderlich ist

### 6.3 Abschlusskriterium der Architekturentscheidung

Die Architekturentscheidung gilt erst dann als **praktisch bewährt**, wenn sie:

1. in IDM technisch korrekt umgesetzt ist und
2. in Personnel unter realen Fachanwendungsbedingungen erfolgreich übernommen und geprüft wurde.

---

## 7. Abgeleitete Leitlinien für die weitere Umsetzung

1. **Keine direkte Fachdomänen-Abhängigkeit im `system` Package.**
2. **Rollen→Rechte-Auflösung bleibt Kernel-Verantwortung.**
3. **Mapping-Daten kommen über eine abstrahierte, austauschbare Quelle.**
4. **Rechtegruppen sind fachlich zulässig, final ausgewertet werden Einzelrechte.**
5. **Rechte bleiben im Anwendungscode als Authorities / Permission-Konstanten definiert.**
6. **Fachanwendungen brauchen für lokale Token-Auswertung keine User→Role-Persistenz.**
7. **IDM darf führende Verwaltungsdomäne bleiben, ohne dass andere Apps dessen Domainmodell kopieren müssen.**

---

## 8. Status

**Entschieden.**

Diese ADR ist für Sprint 7 die verbindliche architektonische Grundlage für:

* die Planung in `IDM_Sprint7_1_Planung.md`
* die folgende technische Umsetzung im IDM-Projekt
* die spätere, verpflichtende Übertragungs- und Validierungsphase in Personnel
