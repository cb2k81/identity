# IDM Sprint 7.1 – Planung: Überarbeitung der Berechtigungsprüfung im system Package

**Stand:** 2026-03-16
**Version:** 1.0

---

## 1. Arbeitsvertrag / Verbindliche Rahmenbedingungen

**Bestätigung:** Der Arbeitsvertrag ist für diese Planung bestätigt und wird verbindlich eingehalten.

Für diese Planungsphase gelten insbesondere:

* Es wird **ausschließlich deterministisch** auf Basis der aktuellen Baseline gearbeitet.
* Baseline ist der zuletzt bereitgestellte Textexport:

    * `idm_code-export_2026-03-16_14-25-02.txt`
* Zusätzlich wurde zur Architekturprüfung der Stand der Fachanwendung als Referenz betrachtet:

    * `personnel_code-export_2026-03-16_14-29-41.txt`
* Für **konkrete Änderungen am `system` Package** wird in **Sprint 7.1 ausschließlich innerhalb der IDM-App** gearbeitet.
* Die spätere Übertragung / Validierung in Personnel ist **explizit nicht Bestandteil der Umsetzung in Sprint 7.1**, sondern wird als eigener definierter Folgepunkt geplant.
* Es werden in diesem Dokument **keine Annahmen über nicht nachgewiesene Klassen oder Implementierungen** getroffen.

---

## 2. Ziel dieses Dokuments

Dieses Dokument hält **vor jeder Code-Änderung** die für Sprint 7 relevanten Planungsinformationen fest.

Ziele:

1. Den **Ist-Zustand** der Berechtigungsprüfung im gemeinsamen `system` Package dokumentieren.
2. Die **architektonische Zielrichtung** für die Überarbeitung verbindlich festhalten.
3. Den **Umfang von Sprint 7.1** eindeutig begrenzen.
4. Den **späteren Übertragungs- und Prüfpunkt für Personnel** klar definieren.
5. Eine **regressionsarme, phasenweise Umsetzung** vorbereiten.

Dieses Dokument ist ein **operatives Planungsdokument**. Die eigentliche Architekturentscheidung wird ergänzend im ADR-Dokument `IDM_Sprint7_2_DOMAIN_PERMISSIONS.md` festgehalten.

---

## 3. Ausgangslage (Ist-Zustand, verifiziert)

### 3.1 Aktuelle technische Kette im system Package

Im aktuellen Stand der IDM-App ist die JWT-basierte Berechtigungsprüfung im `system` Package technisch wie folgt aufgebaut:

* `system.security.jwt.JwtAuthenticationFilter`

    * liest den JWT
    * extrahiert Claims
    * delegiert die Authority-Ermittlung an `PermissionResolver`
* `system.security.authorization.PermissionResolver`

    * technisches Interface zur Ermittlung effektiver Authorities
* `system.security.authorization.DatabasePermissionResolver`

    * konkrete Standardimplementierung
    * löst Rollen aus dem Token über IDM-Domainservices in Rechte auf

### 3.2 Architekturelles Problem im Ist-Zustand

Die aktuelle Implementierung `DatabasePermissionResolver` liegt im gemeinsamen `system` Package, referenziert jedoch direkt IDM-Domainservices:

* `ApplicationScopeEntityService`
* `RoleEntityService`
* `RolePermissionAssignmentEntityService`

Dadurch ist der gemeinsame Anwendungskernel **fachlich an die IDM-Domain gekoppelt**.

**Folge:**

Das `system` Package ist in seiner aktuellen Form **nicht übertragbar**, obwohl es als gemeinsamer Kernel für mehrere Anwendungen gedacht ist.

### 3.3 Bestätigung durch Referenzprüfung in Personnel

Im bereitgestellten Personnel-Textexport befindet sich dieselbe technische Struktur erneut, einschließlich einer `DatabasePermissionResolver`-Implementierung mit IDM-Domainbezug.

**Bedeutung:**

Das bestätigt, dass die aktuelle Struktur nicht nur theoretisch problematisch ist, sondern sich bereits als unerwünschte Kopplung in einer Fachanwendung fortgesetzt hat.

---

## 4. Fachliche Leitplanken (verbindlich)

Für Sprint 7 gelten folgende fachliche Leitplanken als verbindlich:

1. **Im JWT kommen nur Rollen an.**

    * Das Token transportiert keine finalen Einzelrechte.

2. **Die auswertende Anwendung ist für das Mapping verantwortlich.**

    * Jede Anwendung muss Rollen auf ihre eigenen Rechte auflösen.

3. **Die konkreten Rechte bleiben im Code verankert.**

    * Beispiel: Authorities / Permission-Konstanten, die in `@PreAuthorize` genutzt werden.

4. **Das Rollen→Rechte-Mapping soll unabhängig vom Fachcode änderbar sein.**

    * z. B. über XML / Konfigurationsdateien oder andere austauschbare Quellen.

5. **Das Auflösen von Rollen auf Rechte darf im gemeinsamen Core verbleiben.**

    * Aber nur generisch, ohne direkte Abhängigkeit zu einer Fachdomäne.

6. **Rechtegruppen sind fachlich zulässig und sollen unterstützt werden.**

    * Rolle → Einzelrechte
    * Rolle → Rechtegruppen
    * Rechtegruppe → Einzelrechte

---

## 5. Zielbild Sprint 7 (fachlich + technisch)

### 5.1 Zielbild für den gemeinsamen Kernel (system Package)

Das `system` Package soll weiterhin die **generische technische Berechtigungsauflösung** enthalten.

Das bedeutet:

* JWT-Verarbeitung bleibt im `system` Package.
* `PermissionResolver` bleibt im `system` Package.
* Die eigentliche Rollen→Authorities-Auflösung bleibt ebenfalls im `system` Package.

**Aber:**

Der Kernel darf dabei **keine direkte Kenntnis der IDM-Domain** oder einer anderen Fachdomäne haben.

### 5.2 Zielbild für die Datenquelle des Mappings

Die konkrete Rollen-/Rechte-/Rechtegruppen-Zuordnung soll **nicht hart im Core kodiert** sein, sondern über eine abstrahierte Quelle bereitgestellt werden.

Mögliche Quellen:

* IDM-Datenbank (im IDM-Kontext)
* XML-basierte Bootstrap-Konfiguration (insb. für Fachanwendungen)
* später ggf. weitere Konfigurationsquellen

### 5.3 Zielbild für Fachanwendungen

Eine Fachanwendung (z. B. Personnel) soll **nicht zwingend** Rollen, Rechte und Rechtegruppen als JPA-Entities besitzen müssen.

Für Fachanwendungen ist ein zulässiges Zielmodell:

* Rechte als Code-Konstanten
* Rollen-/Rechtegruppen-/Rechte-Mapping aus XML / Konfiguration
* Generische Auflösung im `system` Package

**Konsequenz:**

Eine Fachanwendung braucht für die lokale Token-Auswertung nicht zwingend ein eigenes „Mini-IDM“.

---

## 6. Umfang Sprint 7.1 (bewusst begrenzt)

### 6.1 Was Sprint 7.1 umfasst

Sprint 7.1 umfasst die **Planung und die anschließende technische Überarbeitung innerhalb der IDM-App**, um das `system` Package in der IDM-Codebasis architektonisch korrekt weiterzuentwickeln.

Das umfasst:

* Entkopplung des `system` Packages von direkter IDM-Domainabhängigkeit
* Vorbereitung eines generischen Rollen→Authorities-Resolvers
* Definition einer abstrahierten Mapping-Quelle
* Sicherstellung, dass alle bestehenden IDM-Tests nach der Änderung weiterhin grün bleiben

### 6.2 Was Sprint 7.1 ausdrücklich nicht umfasst

Sprint 7.1 umfasst **nicht**:

* die sofortige Übertragung der Änderungen in die Personnel-App
* die vollständige Implementierung einer produktiven XML-basierten Fachanwendungs-Bootstrap-Logik in Personnel
* eine vollständige Fachanwendungs-Migration außerhalb des IDM-Projekts

Diese Punkte werden bewusst in einen **späteren, definierten Folgepunkt** ausgelagert.

---

## 7. Klar definierter Folgepunkt: Personnel-Validierung

Die Weiterentwicklung des `system` Packages erfolgt zunächst **nur in der IDM-App**.

Damit die Architektur nicht nur theoretisch, sondern praktisch als gemeinsamer Kernel tragfähig ist, wird ein **verbindlicher Folgepunkt** definiert:

### Geplanter Folgepunkt (verbindlich)

**Sprint 7.x / nach Abschluss und Stabilisierung von Sprint 7.1 in IDM:**

> Die in der IDM-App weiterentwickelte `system`-Security-Architektur wird in der Personnel-App gezielt übernommen und dort unter realen Fachanwendungsbedingungen geprüft.

### Ziel dieses Folgepunkts

Nachweis, dass die neue Architektur in einer Fachanwendung funktioniert, in der:

* keine IDM-Domainabhängigkeit im `system` Package zulässig ist
* die Fachrechte app-spezifisch sind
* das Rollen→Rechte-Mapping lokal (z. B. per XML) bereitgestellt werden kann

### Ergebnisdefinition

Erst nach dieser Übertragung / Validierung kann die neue `system`-Security-Architektur als **praktisch übertragbarer Anwendungskernel** gelten.

---

## 8. Geplante Umsetzungsstrategie (phasenweise)

Die Umsetzung soll regressionsarm und in klaren, in sich abgeschlossenen Schritten erfolgen.

### Phase 1 – Architektur-Schnitt im IDM-Code sauber ziehen

Ziel:

* Problemstelle präzise lokalisieren
* neue generische Verantwortungsgrenzen definieren
* keine funktionale Ausweitung, nur architektonische Entkopplung vorbereiten

Erwartetes Ergebnis:

* Klare Trennung zwischen

    * generischer Resolver-Logik im `system` Package
    * fachlicher / konfigurativer Mapping-Quelle außerhalb harter Domainkopplung

### Phase 2 – Generische Resolver-Struktur im system Package

Ziel:

* Der gemeinsame Kernel behält die Rollen→Authorities-Auflösung
* direkte IDM-Domain-Imports verschwinden aus dem generischen Resolverpfad

Erwartetes Ergebnis:

* `system` Package ist fachlich neutraler und übertragbar

### Phase 3 – IDM-spezifische Mapping-Anbindung

Ziel:

* Das IDM bleibt als bestehende fachliche Quelle nutzbar
* vorhandene IDM-Daten (Scopes, Rollen, Rechte, Rechtegruppen, Zuordnungen) bleiben fachlich verwertbar

Erwartetes Ergebnis:

* bestehendes IDM-Verhalten bleibt funktional stabil
* keine Regression in Login/JWT/`@PreAuthorize`

### Phase 4 – Tests / Stabilisierung in IDM

Ziel:

* Alle bestehenden Tests in IDM bleiben grün
* JWT-basierte Autorisierung bleibt unverändert funktionsfähig

Erwartetes Ergebnis:

* Architektur korrigiert, Verhalten stabil

### Phase 5 – Geplanter Folgepunkt: Übertragung nach Personnel

Ziel:

* Nachweis der Übertragbarkeit in einer echten Fachanwendung
* Prüfung, ob XML-/Konfigurationsmodell ohne IDM-Entities genügt

Erwartetes Ergebnis:

* Validierte Kernel-Übertragbarkeit
* belastbare Grundlage für generische Fachanwendungs-Bootstrap-Strategie

---

## 9. Vorläufige Architekturentscheidung für Sprint 7.1

Für Sprint 7.1 wird **noch keine vollständige Endausprägung für alle Fachanwendungen** umgesetzt.

Stattdessen gilt:

* In der IDM-App wird das `system` Package **so weiterentwickelt**, dass es fachlich generischer wird.
* Die Änderungen werden **zuerst innerhalb der IDM-App stabilisiert**.
* Die **vollständige fachanwendungsseitige Bewährungsprobe** erfolgt **erst in einem definierten Folgepunkt innerhalb Personnel**.

Das ist bewusst gewählt, um:

* die Änderung kontrolliert einzuführen
* Regressionen im IDM gering zu halten
* die Übertragbarkeit später real und nicht nur theoretisch zu verifizieren

---

## 10. Risiken / offene Punkte vor Code-Änderung

Vor der eigentlichen Umsetzung sind folgende Punkte bewusst festgehalten:

1. **Rechtegruppen-Unterstützung muss architektonisch berücksichtigt werden**, auch wenn die erste technische Stufe intern ggf. bereits auf Endrechte expandiert.
2. **Der Core darf keine Fach-Entities kennen**, aber die konkrete Datenquelle kann app-spezifisch sein.
3. **Personnel ist die entscheidende Validierungsinstanz** für die Übertragbarkeit des Kernels.
4. **Sprint 7.1 endet nicht mit „nur theoretisch sauber“**, sondern mit einer explizit geplanten Folgevalidierung in Personnel.
5. **Keine überhastete Paralleländerung in zwei Projekten**: zuerst IDM stabilisieren, dann kontrolliert übertragen.

---

## 11. Akzeptanzkriterien für Sprint 7.1 (Planungssicht)

Sprint 7.1 ist aus Planungssicht erfolgreich vorbereitet, wenn:

1. Die Problemursache im Ist-Zustand eindeutig dokumentiert ist.
2. Der Ziel-Schnitt zwischen Core und Domäne klar definiert ist.
3. Der Umfang von Sprint 7.1 bewusst auf die IDM-App begrenzt ist.
4. Der spätere Personnel-Übertragungszeitpunkt explizit festgelegt ist.
5. Das ADR-Dokument als architektonische Referenz ergänzend erstellt wird.

---

## 12. Nächster Schritt

Nach Freigabe dieses Planungsdokuments folgt als nächster Dokumentationsschritt:

* `src/main/resources/doc/operational/sprint7/IDM_Sprint7_2_DOMAIN_PERMISSIONS.md`

Dieses zweite Dokument dient als **ADR** und hält die Architekturentscheidung zur generischen Rollen→Rechte-Auflösung, zur Trennung von Core und Domäne sowie zur Rolle von IDM vs. Fachanwendungen verbindlich fest.
