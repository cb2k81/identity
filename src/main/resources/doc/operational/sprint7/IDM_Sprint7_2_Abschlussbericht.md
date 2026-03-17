# IDM Sprint 7 – Abschlussbericht

**Stand:** 2026-03-17
**Version:** 1.0

---

## 1. Zielsetzung des Sprints

Ziel von Sprint 7 war die **technisch saubere Entkopplung der generischen Security-/Authorization-Infrastruktur im `system`-Bereich von der IDM-spezifischen Berechtigungsauflösung**, sodass:

* der generische Security-Kernel keine direkte Abhängigkeit mehr zu IDM-Domain-Services enthält,
* die bestehende fachliche Berechtigungslogik unverändert erhalten bleibt,
* die Umsetzung regressionsfrei und deterministisch erfolgt,
* die gewählte Struktur später kontrolliert auf die Personnel App übertragen werden kann.

Der Sprint wurde ausdrücklich **zunächst nur innerhalb der IDM App** umgesetzt, um den technischen Schnitt sauber herzustellen und vor einer Übertragung auf weitere Anwendungen verlässlich zu validieren.

---

## 2. Zusammenfassung des Ergebnisses

### Gesamtbewertung

**Sprint 7 ist erfolgreich abgeschlossen.**

Das Sprint-Ziel wurde im Kern vollständig erreicht:

* Der generische Resolver-Pfad im `system`-Bereich wurde von der IDM-Domain entkoppelt.
* Die bisher im generischen Resolver enthaltene IDM-spezifische Datenauflösung wurde in eine IDM-spezifische Implementierung verlagert.
* Das fachliche Verhalten der Berechtigungsauflösung blieb unverändert.
* Die Anwendung ist buildbar und lauffähig.
* Relevante Integrationstests sowie die vollständige Testsuite wurden erfolgreich ausgeführt.

### Status

| Bereich                                 |                Status | Bewertung                                                                |
| --------------------------------------- | --------------------: | ------------------------------------------------------------------------ |
| Technischer Schnitt im `system`-Bereich |            ✅ Erledigt | Generische Security-Infrastruktur ist nicht mehr direkt an IDM gekoppelt |
| IDM-spezifische Berechtigungsauflösung  |            ✅ Erledigt | Fachlogik liegt jetzt in der IDM-Domain                                  |
| Verhaltensgleichheit zur Baseline       |            ✅ Erledigt | Keine beabsichtigte fachliche Änderung                                   |
| Build-/Spring-Kontext                   |            ✅ Erledigt | Nach Ergänzung der Bean wieder vollständig funktionsfähig                |
| Regressionsprüfung                      |            ✅ Erledigt | Relevante Security-/Auth-Tests grün                                      |
| Vollständige Testsuite                  |            ✅ Erledigt | Gesamtsuite grün                                                         |
| Übertragung auf Personnel               | ⏳ Offen / Folgearbeit | Bewusst nicht Teil dieses Projekts                                       |

---

## 3. Umgesetzte Architekturänderung

### Ausgangssituation vor Sprint 7

Vor Sprint 7 war die Klasse `DatabasePermissionResolver` im generischen `system`-Security-Bereich direkt an IDM-spezifische Domain-Services gekoppelt.

Dadurch war der generische Kernel nicht mehr domänenneutral und die spätere Wiederverwendung bzw. Übertragung auf weitere Anwendungen (insbesondere Personnel) unnötig erschwert.

### Umgesetzte Zielarchitektur

Es wurde ein technischer Schnitt eingeführt:

* Der generische Resolver im `system`-Bereich bleibt als technischer Einstiegspunkt bestehen.
* Die effektive Berechtigungsauflösung wird über eine **generische technische Abstraktion** bereitgestellt.
* Die IDM-spezifische Auflösung wird durch eine **IDM-spezifische Implementierung** dieser Abstraktion geliefert.

### Ergebnisstruktur

#### Generischer Bereich (`system`)

* `PermissionResolver` bleibt technischer Einstiegspunkt.
* `DatabasePermissionResolver` ist nun ein generischer Orchestrator.
* Neue technische Abstraktion: `PermissionAuthoritySource`

#### IDM-Bereich (`domain.idm`)

* Neue Implementierung: `IdmDatabasePermissionAuthoritySource`
* Diese Klasse kapselt die bisherige IDM-spezifische Datenbanklogik zur Ermittlung effektiver Berechtigungen.

---

## 4. Fachliches Verhalten nach der Umstellung

Die fachliche Berechtigungslogik wurde **nicht geändert**, sondern lediglich strukturell verlagert.

### Unverändert beibehalten

* Scope-Auflösung über `(applicationKey, stageKey)`
* Rollenauflösung im Scope
* Auflösung der Role-Permission-Assignments
* Rückgabe effektiver Permission-Namen
* finale Normalisierung der Authorities im Resolver:

    * Filterung leerer Werte
    * `trim`
    * `distinct`
    * Sortierung

### Wichtig

Der Sprint war **kein Fachsprint**, sondern ein **Architektur-/Struktursprint**.
Es wurden keine neuen Berechtigungsregeln eingeführt und keine bestehenden Regeln fachlich geändert.

---

## 5. Test- und Regressionsabsicherung

Nach der Umsetzung wurde der neue Zustand deterministisch validiert.

### Gezielt geprüfte relevante Integrationsbereiche

* Authorization-Verhalten / Method Security
* HTTP-Security-Pfade (`/public/**`, `/api/**`)
* Login-/JWT-Pfad
* Security-Fehlerverhalten / Informationsleck-Schutz
* Geschützte CRUD-/Assignment-Flows mit Admin-Token

### Ergebnis

* Relevante Einzeltests wurden erfolgreich ausgeführt.
* Anschließend wurde die **gesamte Testsuite erfolgreich** ausgeführt.
* Es wurden **keine Regressionen** festgestellt.

### Bewertung

Die durch Sprint 7 geänderte Security-/Authorization-Struktur gilt damit innerhalb der IDM App als **stabil und regressionsfrei verifiziert**.

---

## 6. Risiken und Restrisiken

### 6.1 Aktuell beherrschte Risiken

| Risiko                                  | Bewertung | Einordnung                                          |
| --------------------------------------- | --------: | --------------------------------------------------- |
| Security-Regression im Login-/JWT-Pfad  |   Niedrig | Durch Integrationstests und Gesamtsuite abgesichert |
| Security-Regression bei Method Security |   Niedrig | Relevante Autorisierungstests grün                  |
| Spring-Wiring / Bean-Auflösung          |   Niedrig | Nach Ergänzung der IDM-Implementierung stabil       |
| Fachliche Verhaltensabweichung          |   Niedrig | Logik wurde extrahiert, nicht neu entworfen         |

### 6.2 Verbleibende Restrisiken / Folgepunkte

| Thema                                                                              | Bewertung | Bedeutung                                                     |
| ---------------------------------------------------------------------------------- | --------: | ------------------------------------------------------------- |
| Mehrere `PermissionAuthoritySource`-Implementierungen in einer gemeinsamen Runtime |    Mittel | Bei späterer Integration weiterer Apps relevant               |
| Selektionsstrategie für mehrere Domänen                                            |    Mittel | Für Personnel bzw. gemeinsame Kontexte zu klären              |
| Vereinheitlichung von Package-/Namenskonventionen                                  |   Niedrig | Aktuell kein Blocker, nur späteres Ordnungs-/Governance-Thema |

### Wichtigster Folgepunkt

Sobald neben IDM eine weitere Anwendung (z. B. Personnel) eine eigene `PermissionAuthoritySource`-Implementierung in denselben Spring-Kontext einbringt, muss eine **explizite Selektionsstrategie** festgelegt werden.

Beispiele möglicher Strategien:

* `@Primary`
* Qualifier-basierte Auswahl
* Registry/Factory
* domänenspezifischer Delegator

**Diese Entscheidung wurde in Sprint 7 bewusst noch nicht umgesetzt**, da sie für die isolierte IDM App aktuell nicht erforderlich ist.

---

## 7. Abweichungen / Nicht-Ziele

### Keine fachlichen Erweiterungen

Nicht Bestandteil von Sprint 7 waren insbesondere:

* neue Berechtigungsmodelle,
* neue Permission-Regeln,
* Erweiterungen an JWT-Inhalten,
* Änderungen an `@PreAuthorize`-Regeln,
* Umstellung weiterer Anwendungen.

### Keine Personnel-Umsetzung in diesem Projekt

Die Übertragung des Musters auf die Personnel App ist **bewusst nicht Bestandteil des IDM-Projekts** und wurde hier nicht umgesetzt.

Dies ist kein offener Fehler, sondern eine **explizit geplante Folgearbeit außerhalb dieses Projekts**.

---

## 8. Empfehlung für die Folgearbeit in der Personnel App

Für die spätere Umsetzung in der Personnel App wird empfohlen:

1. **Dasselbe technische Muster übernehmen**

    * generischer Resolver im gemeinsamen/technischen Bereich
    * Personnel-spezifische Implementierung außerhalb des generischen Kernels

2. **Vor Umsetzung Selektionsstrategie definieren**

    * Falls IDM und Personnel in einem gemeinsamen Spring-Kontext laufen oder perspektivisch gemeinsam lauffähig sein sollen, muss vorab definiert werden, wie mehrere `PermissionAuthoritySource`-Implementierungen deterministisch aufgelöst werden.

3. **Keine Vorab-Verallgemeinerung ohne konkreten Bedarf**

    * Sprint 7 hat bewusst nur den notwendigen technischen Schnitt für IDM hergestellt.
    * Eine weitergehende Multi-Domain-Abstraktion sollte erst umgesetzt werden, wenn der reale Personnel-Kontext vollständig vorliegt.

---

## 9. Abschlussbewertung

### Fachlich

* Das bisherige Berechtigungsverhalten bleibt erhalten.
* Es wurden keine fachlichen Regressionen festgestellt.

### Technisch

* Die generische Security-Infrastruktur wurde sauber von IDM entkoppelt.
* Die Lösung ist klarer, wartbarer und für die nächste Ausbaustufe besser vorbereitbar.

### Projektbezogen

* Sprint 7 ist für die IDM App formal abgeschlossen.
* Die weitere Entwicklung wird auf Seite der Personnel App fortgesetzt.
* Für dieses Projekt ist damit **kein weiterer Sprint-7-Codebedarf** gegeben.

---

## 10. Formale Entscheidung

**Sprint 7 wird für das IDM-Projekt als abgeschlossen freigegeben.**

Die Umsetzung gilt als:

* **deterministisch durchgeführt**,
* **regressionsfrei verifiziert**,
* **technisch sauber abgeschlossen**.

Die nächste fachlich zusammenhängende Weiterentwicklung erfolgt **außerhalb dieses Projekts** in der Personnel App.
