# IDM Sprint 7 – Abschlussbericht

**Stand:** 2026-03-19
**Version:** 1.1

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
* Die Testarchitektur wurde in der Abschlussphase zusätzlich stabilisiert, sodass Maven- und IDE-Ausführung wieder konsistent deterministisch grün sind.

### Status

| Bereich                                 |                Status | Bewertung                                                                              |
| --------------------------------------- | --------------------: | -------------------------------------------------------------------------------------- |
| Technischer Schnitt im `system`-Bereich |            ✅ Erledigt | Generische Security-Infrastruktur ist nicht mehr direkt an IDM gekoppelt               |
| IDM-spezifische Berechtigungsauflösung  |            ✅ Erledigt | Fachlogik liegt jetzt in der IDM-Domain                                                |
| Verhaltensgleichheit zur Baseline       |            ✅ Erledigt | Keine beabsichtigte fachliche Änderung                                                 |
| Build-/Spring-Kontext                   |            ✅ Erledigt | Nach Ergänzung der Bean wieder vollständig funktionsfähig                              |
| Regressionsprüfung                      |            ✅ Erledigt | Relevante Security-/Auth-Tests grün                                                    |
| Vollständige Testsuite                  |            ✅ Erledigt | Gesamtsuite grün                                                                       |
| Testarchitektur / IDE-Kompatibilität    |            ✅ Erledigt | Testprofil/Datasource deterministisch stabilisiert; Maven und IntelliJ konsistent grün |
| Übertragung auf Personnel               | ⏳ Offen / Folgearbeit | Bewusst nicht Teil dieses Projekts                                                     |

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

### 5.1 Gezielt geprüfte relevante Integrationsbereiche

* Authorization-Verhalten / Method Security
* HTTP-Security-Pfade (`/public/**`, `/api/**`)
* Login-/JWT-Pfad
* Security-Fehlerverhalten / Informationsleck-Schutz
* Geschützte CRUD-/Assignment-Flows mit Admin-Token

### 5.2 Ergebnis der Regressionsprüfung

* Relevante Einzeltests wurden erfolgreich ausgeführt.
* Anschließend wurde die **gesamte Testsuite erfolgreich** ausgeführt.
* Es wurden **keine Regressionen** festgestellt.

### 5.3 Zusätzliche Stabilisierung in der Abschlussphase

Im Anschluss an die eigentliche Sprint-7-Umsetzung wurde in derselben Abschlussphase noch eine **abschließende Testarchitektur-Stabilisierung** durchgeführt.

Auslöser war, dass die Tests zwar grundsätzlich grün waren, aber in der IDE (insbesondere bei vollständiger Ausführung über IntelliJ) zeitweise nicht mehr deterministisch denselben Zustand erreichten wie im Maven-Lauf.

#### Identifizierte Ursache

* Das `test`-Profil war im Ergebnis nicht mehr strikt genug von der Default-Konfiguration getrennt.
* Dadurch konnte es zu einer **hybriden bzw. nicht vollständig überschriebenen Datasource-Konfiguration** kommen.
* Zusätzlich waren einzelne Startup-/Bootstrap-Testfälle durch Spring-Context-Wiederverwendung empfindlich gegenüber Reihenfolgeeffekten.

#### Durchgeführte Korrektur

* `application-test.yml` wurde so korrigiert, dass die Test-Datasource wieder **vollständig und eindeutig** im Testprofil definiert ist.
* Kritische Startup-/Bootstrap-Tests wurden so abgesichert, dass ihre Kontexte nicht ungewollt in nachfolgende Testklassen hineinwirken.
* Die Testausführung wurde anschließend erneut sowohl einzeln als auch als Gesamtsuite validiert.

#### Ergebnis

* Maven-Lauf und IDE-Lauf verhalten sich wieder konsistent.
* Die Tests sind im aktuellen Projektstand **deterministisch ausführbar**.
* Der Abschlusszustand von Sprint 7 ist damit nicht nur funktional, sondern auch testtechnisch belastbar abgesichert.

### 5.4 Bewertung

Die durch Sprint 7 geänderte Security-/Authorization-Struktur gilt damit innerhalb der IDM App als:

* **stabil**,
* **regressionsfrei verifiziert**,
* **deterministisch testbar**,
* **für den Projektstand Sprint 7 formal freigabefähig**.

---

## 6. Fachlicher Leistungsumfang des IDM nach Sprint 7 (MVP-Zusammenfassung)

Sprint 7 war selbst kein Fachsprint. Für die formale Abschlussphase wurde jedoch der aktuelle, nachweisbare MVP-Leistungsumfang des IDM nochmals überprüft.

### 6.1 Als umgesetzt und MVP-relevant zu bewerten

Im aktuellen Projektstand sind für das IDM-MVP nachweisbar:

1. **Benutzer-Authentifizierung**

    * Login über `/api/auth/login`
    * gültiger Benutzer erhält JWT
    * ungültige Zugangsdaten führen zu `401`

2. **JWT-basierte Zugriffssicherung**

    * geschützte Endpunkte erfordern gültige Authentifizierung
    * ohne Token: `401`

3. **IDM-interne Autorisierung im Self-Scope**

    * `ApplicationScope` als Sicherheitskontext
    * Rollen, Rechtegruppen und Einzelrechte
    * Authority-/Permission-basierte Prüfung für IDM-eigene Funktionen

4. **Deterministisches Bootstrapping**

    * SAFE / FORCE / DISABLED
    * Anlage von Admin-User, Self-Scope, Rollen-/Rechte-Basisbestand und Zuordnungen

5. **Basis-Management-Funktionen**

    * `ApplicationScope`-Verwaltung (CRUD)
    * `UserAccount`-Basisverwaltung / mindestens Create-Flow im Management-Kontext

### 6.2 Nicht Ziel dieses Abschlusses / bewusst noch nicht als vollständig breit ausgebaut

Nicht als vollständig ausgebauter Fachumfang dieses MVP-Abschlusses zu werten sind insbesondere:

* vollständige CRUD-Matrix für `Role`
* vollständige CRUD-Matrix für `Permission`
* vollständige CRUD-Matrix für `PermissionGroup`
* vollständige API-seitige Breitenabsicherung aller Assignment-Ressourcen
* Security-Hardening (z. B. Brute-Force-/Locking-Strategien)
* vollständige Negativfall-Matrix für Validierungs- und Konfliktfälle

### Einordnung

Der Projektstand ist damit **MVP-fähig für Kern-Auth/AuthZ + Management-Basics**, aber bewusst noch **nicht produktionshart in allen Randbereichen**.

---

## 7. Bewertung der Testabdeckung (fachlich)

### 7.1 Gut abgesicherte Kernbereiche

Im aktuellen Projektstand sind für den MVP-Kern belastbar abgesichert:

* positiver Login (`JWT` wird zurückgegeben)
* negativer Login (`401` bei falschen Zugangsdaten)
* Zugriff ohne Token (`401`)
* Zugriff ohne Scope (`401`)
* Zugriff ohne Rolle/Permission (`403`)
* `ApplicationScope`-CRUD als Integrationsfluss
* mindestens ein administrativer `UserAccount`-Create-Flow
* Bootstrap-Verhalten (insbesondere SAFE/FORCE-Kontexte)

### 7.2 Fachlich sinnvolle noch offene Testergänzungen

Für einen breiter abgesicherten Folge-Stand werden empfohlen:

1. **Systematische AuthZ-Testmatrix je Resource/Operation**

    * je API mindestens `401` ohne Token
    * `403` ohne Authority
    * `200/201/204` mit Authority

2. **Assignment-spezifische Fachtests**

    * `UserApplicationScopeAssignment`
    * `UserRoleAssignment`
    * insbesondere Scope-Konsistenz und Cross-Scope-Verhalten

3. **Negative Datenfälle**

    * Duplicate Username
    * Duplicate Scope-Schlüssel
    * ungültige IDs
    * Blank-/Null-Pflichtfelder
    * Konfliktfälle bei Updates

4. **Optionale Security-Randfälle**

    * abgelaufenes Token
    * manipuliertes Token
    * ungültiger Bearer-Header

### Einordnung

Diese Lücken stellen **keinen Widerspruch zum Sprint-7-Abschluss** dar, sondern beschreiben den sachlich korrekten Zustand:

* **MVP-Kern ausreichend abgesichert:** Ja
* **vollständige breite Fach-Testmatrix bereits vorhanden:** Nein

---

## 8. Risiken und Restrisiken

### 8.1 Aktuell beherrschte Risiken

| Risiko                                  | Bewertung | Einordnung                                                                       |
| --------------------------------------- | --------: | -------------------------------------------------------------------------------- |
| Security-Regression im Login-/JWT-Pfad  |   Niedrig | Durch Integrationstests, Gesamtsuite und finalen IDE-/Maven-Abgleich abgesichert |
| Security-Regression bei Method Security |   Niedrig | Relevante Autorisierungstests grün                                               |
| Spring-Wiring / Bean-Auflösung          |   Niedrig | Nach Ergänzung der IDM-Implementierung stabil                                    |
| Fachliche Verhaltensabweichung          |   Niedrig | Logik wurde extrahiert, nicht neu entworfen                                      |
| Testinstabilität durch Profil-/DB-Mix   |   Niedrig | Durch Korrektur des `test`-Profils und erneute Validierung behoben               |

### 8.2 Verbleibende Restrisiken / Folgepunkte

| Thema                                                                              | Bewertung | Bedeutung                                                     |
| ---------------------------------------------------------------------------------- | --------: | ------------------------------------------------------------- |
| Mehrere `PermissionAuthoritySource`-Implementierungen in einer gemeinsamen Runtime |    Mittel | Bei späterer Integration weiterer Apps relevant               |
| Selektionsstrategie für mehrere Domänen                                            |    Mittel | Für Personnel bzw. gemeinsame Kontexte zu klären              |
| Vereinheitlichung von Package-/Namenskonventionen                                  |   Niedrig | Aktuell kein Blocker, nur späteres Ordnungs-/Governance-Thema |
| Noch nicht vollständige AuthZ-Testmatrix über alle Ressourcen                      |   Niedrig | Kein Sprint-7-Blocker, aber sinnvoller Folgepunkt             |
| Security-Hardening über MVP-Kern hinaus                                            |    Mittel | Vor externer Freigabe gesondert zu bewerten                   |

### Wichtigster Folgepunkt

Sobald neben IDM eine weitere Anwendung (z. B. Personnel) eine eigene `PermissionAuthoritySource`-Implementierung in denselben Spring-Kontext einbringt, muss eine **explizite Selektionsstrategie** festgelegt werden.

Beispiele möglicher Strategien:

* `@Primary`
* Qualifier-basierte Auswahl
* Registry/Factory
* domänenspezifischer Delegator

**Diese Entscheidung wurde in Sprint 7 bewusst noch nicht umgesetzt**, da sie für die isolierte IDM App aktuell nicht erforderlich ist.

---

## 9. Abweichungen / Nicht-Ziele

### Keine fachlichen Erweiterungen durch Sprint 7 selbst

Nicht Bestandteil von Sprint 7 waren insbesondere:

* neue Berechtigungsmodelle,
* neue Permission-Regeln,
* Erweiterungen an JWT-Inhalten,
* Änderungen an `@PreAuthorize`-Regeln,
* Umstellung weiterer Anwendungen.

### Keine Personnel-Umsetzung in diesem Projekt

Die Übertragung des Musters auf die Personnel App ist **bewusst nicht Bestandteil des IDM-Projekts** und wurde hier nicht umgesetzt.

Dies ist kein offener Fehler, sondern eine **explizit geplante Folgearbeit außerhalb dieses Projekts**.

### Kein neuer Fachsprint innerhalb des IDM-Projekts

Die in der Abschlussphase ergänzten Aussagen zum MVP-Leistungsumfang und zur Testabdeckung sind **kein neuer Sprintinhalt**, sondern eine **abschließende Review- und Dokumentationsverdichtung** des bereits vorhandenen Projektzustands.

---

## 10. Empfehlung für die Folgearbeit in der Personnel App

Für die spätere Umsetzung in der Personnel App wird empfohlen:

1. **Dasselbe technische Muster übernehmen**

    * generischer Resolver im gemeinsamen/technischen Bereich
    * Personnel-spezifische Implementierung außerhalb des generischen Kernels

2. **Vor Umsetzung Selektionsstrategie definieren**

    * Falls IDM und Personnel in einem gemeinsamen Spring-Kontext laufen oder perspektivisch gemeinsam lauffähig sein sollen, muss vorab definiert werden, wie mehrere `PermissionAuthoritySource`-Implementierungen deterministisch aufgelöst werden.

3. **Keine Vorab-Verallgemeinerung ohne konkreten Bedarf**

    * Sprint 7 hat bewusst nur den notwendigen technischen Schnitt für IDM hergestellt.
    * Eine weitergehende Multi-Domain-Abstraktion sollte erst umgesetzt werden, wenn der reale Personnel-Kontext vollständig vorliegt.

4. **Die im IDM identifizierten Testmuster früh systematisieren**

    * AuthZ-Matrix pro Resource/Operation früh festlegen
    * Assignment- und Cross-Scope-Fälle explizit absichern
    * Testprofil strikt vollständig von Default-Konfiguration trennen

---

## 11. Abschlussbewertung

### Fachlich

* Das bisherige Berechtigungsverhalten bleibt erhalten.
* Es wurden keine fachlichen Regressionen festgestellt.
* Der aktuelle IDM-Stand ist **MVP-fähig für Kern-Auth/AuthZ + Management-Basics**.

### Technisch

* Die generische Security-Infrastruktur wurde sauber von IDM entkoppelt.
* Die Lösung ist klarer, wartbarer und für die nächste Ausbaustufe besser vorbereitbar.
* Die Testarchitektur wurde in der Abschlussphase nochmals stabilisiert und ist im aktuellen Zustand deterministisch belastbar.

### Projektbezogen

* Sprint 7 ist für die IDM App formal abgeschlossen.
* Die weitere Entwicklung wird auf Seite der Personnel App fortgesetzt.
* Für dieses Projekt ist damit **kein weiterer Sprint-7-Codebedarf** gegeben.

---

## 12. Formale Entscheidung

**Sprint 7 wird für das IDM-Projekt als abgeschlossen freigegeben.**

Die Umsetzung gilt als:

* **deterministisch durchgeführt**,
* **regressionsfrei verifiziert**,
* **testarchitektonisch stabilisiert**,
* **technisch sauber abgeschlossen**.

Die nächste fachlich zusammenhängende Weiterentwicklung erfolgt **außerhalb dieses Projekts** in der Personnel App.
