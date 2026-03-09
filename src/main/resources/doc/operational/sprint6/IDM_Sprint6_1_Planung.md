# IDM – Sprint 6 Planung (IDM_Sprint6_1)

Stand: 2026-03-06

## 1. Baseline / Verbindlichkeit

* **Verbindliche Baseline:** `idm_code-export_2026-03-06_09-39-47.txt` (Textexport).
* Diese Planung ist ausschließlich aus der aktuellen Baseline und den darin enthaltenen Code-, Test- und Dokumentationsständen abgeleitet.
* Der Arbeitsvertrag gilt unverändert: **deterministische Umsetzung, keine Annahmen/Erfindungen, baseline-basiert, regressionsarm, testbar pro Zwischenzustand**.

---

## 2. Ausgangslage

Nach Abschluss von Sprint 5 befindet sich das Identity‑Management‑System in einem funktionalen MVP‑Zustand. Die zentralen Sicherheitsmechanismen sind implementiert und durch Integrationstests abgesichert. Der Login‑Endpunkt stellt über `/auth/login` ein signiertes JWT bereit, geschützte Endpunkte unter `/api/idm/**` verlangen ein gültiges Token und die Autorisierung erfolgt über das persistente Rollen‑ und Berechtigungsmodell.

Die Bootstrap‑Initialisierung erzeugt deterministisch die erforderlichen Basisdaten des Systems. Sie arbeitet idempotent und ist durch Integrationstests abgesichert. Ebenso existieren Integrationstests für zentrale Authentifizierungs‑, Autorisierungs‑ und CRUD‑Flows der vorhandenen REST‑Endpunkte.

Damit erfüllt das System die funktionalen Anforderungen eines MVP. Aus der Baseline und den vorhandenen Arbeits‑ und Abschlussdokumenten ergibt sich jedoch, dass noch mehrere sicherheitsrelevante Aspekte nicht vollständig abgedeckt sind. Diese betreffen insbesondere den Umgang mit wiederholten Fehlanmeldungen, eine minimale Qualitätssicherung für Passwörter sowie eine systematische Absicherung der vorhandenen Management‑Endpunkte durch eine vollständige Authentifizierungs‑ und Autorisierungstestmatrix.

Diese Punkte stellen keine funktionale Erweiterung des IDM dar, sondern schließen ausschließlich Lücken in der bestehenden Sicherheits‑ und Testabdeckung.

---

## 3. Ziel des Sprints

Der Zweck von Sprint 6 besteht darin, ausschließlich diejenigen Maßnahmen umzusetzen, die für einen stabilen und nachvollziehbaren MVP‑Release notwendig sind. Grundlage dieser Planung ist ausschließlich der Zustand der aktuellen Baseline.

Der Sprint erweitert das System nicht um zusätzliche Funktionen oder Architekturelemente. Insbesondere werden keine neuen Authentifizierungsmodelle, keine zusätzlichen Token‑Mechanismen und keine erweiterten Betriebsfunktionen eingeführt.

Nach Abschluss des Sprints soll das System folgende Eigenschaften besitzen:

Wiederholte Fehlanmeldungen werden kontrolliert erkannt und führen nach einem definierten Grenzwert zu einer temporären Sperrung des betroffenen Benutzerkontos. Gleichzeitig wird sichergestellt, dass Passwörter eine minimale Qualitätsanforderung erfüllen. Darüber hinaus ist die Sicherheitsabsicherung der vorhandenen REST‑Management‑Endpunkte vollständig und systematisch über Integrationstests nachgewiesen. Schließlich wird überprüft, dass sicherheitsrelevante Konfigurationsfehler bereits beim Start der Anwendung erkannt werden.

Jede Änderung innerhalb dieses Sprints führt zu einem klar definierten und testbaren Zwischenzustand. Dadurch bleibt der Systemzustand jederzeit überprüfbar und regressionsfrei.

## 4. Scope von Sprint 6

Sprint 6 umfasst ausschließlich die folgenden Themen.

### 4.1 Fehlversuchs‑Handling / Brute‑Force‑Schutz

Der Login‑Endpunkt ist aktuell funktional korrekt implementiert, reagiert jedoch nicht auf wiederholte Fehlversuche eines Benutzers. Dadurch besteht theoretisch die Möglichkeit, den Login‑Mechanismus durch automatisierte Passwortversuche zu missbrauchen.

Um dieses Risiko zu reduzieren, wird eine minimal notwendige Schutzstrategie implementiert. Das System erfasst künftig die Anzahl fehlgeschlagener Login‑Versuche pro Benutzerkonto. Sobald eine konfigurierbare Anzahl von Fehlversuchen überschritten wird, wird das betroffene Konto temporär gesperrt.

Die Sperrung erfolgt ausschließlich auf Ebene des Benutzerkontos und ist zeitlich begrenzt. Nach Ablauf der konfigurierten Sperrdauer wird der Account automatisch wieder freigegeben. Erfolgt zwischenzeitlich ein erfolgreicher Login, werden alle Fehlversuchs‑Informationen zurückgesetzt.

Die Implementierung erfolgt innerhalb der bestehenden Authentifizierungslogik. Die Fachzustände eines Kontos verbleiben weiterhin im Aggregate `UserAccount`. Die vorhandene Schichtenstruktur bleibt unverändert erhalten.

Diese Maßnahme stellt sicher, dass der Login‑Mechanismus minimal gegen automatisierte Fehlversuche abgesichert ist, ohne zusätzliche Infrastruktur oder externe Komponenten einzuführen.

---

### 4.2 Minimale Passwort‑Policy

In der aktuellen Baseline existiert keine konsistente Regel zur minimalen Qualität eines Passworts. Für einen MVP‑Release ist eine einfache, deterministische Regel ausreichend.

Es wird daher eine minimale Passwort‑Policy eingeführt, die ausschließlich eine Mindestlänge für Passwörter definiert. Diese Regel wird zentral implementiert und an allen Stellen angewendet, an denen Passwörter erzeugt oder geändert werden.

Die Policy wird insbesondere bei der Erstellung neuer Benutzerkonten sowie bei Passwortänderungen angewendet. Wird die Mindestlänge unterschritten, lehnt das System die Operation deterministisch ab und liefert eine konsistente Fehlermeldung zurück.

Komplexere Regeln wie Zeichensatzanforderungen, Passwort‑Historien oder Blacklists sind ausdrücklich nicht Bestandteil dieses Sprints.

---

### 4.3 Systematische AuthZ-Testmatrix für Management-Endpunkte

#### Ziel

Die vorhandenen REST-Endpunkte müssen nicht nur funktional, sondern systematisch sicherheitstechnisch abgesichert nachgewiesen sein.

#### Verbindlicher Minimalumfang

Für alle bereits vorhandenen Management-Endpunkte wird die Testmatrix vervollständigt.

Mindestens je relevanter Operation:

* **401** ohne Token
* **403** ohne Authority
* **200 / 201 / 204** mit korrekter Authority

Zusätzlich dort, wo fachlich naheliegend und bereits durch Baseline-Struktur gedeckt:

* fachlicher Fehlerfall (z. B. ungültige IDs, Duplikate, Abhängigkeiten)

#### Begründung

Die Baseline nennt genau diese Testvervollständigung als wesentliche MVP-Lücke. Dieser Punkt ist für Sprint 6 verbindlich.

---

### 4.4 Startvalidierung sicherheitskritischer Konfiguration

#### Ziel

Sicherheitskritische Fehlkonfigurationen dürfen nicht erst zur Laufzeit in Requests sichtbar werden.

#### Verbindlicher Minimalumfang

Beim Start werden die bereits vorhandenen, sicherheitsrelevanten Konfigurationswerte konsistent validiert:

* JWT-Konfiguration vorhanden und verwendbar
* Self-Scope-Konfiguration konsistent
* sicherheitsrelevante Sprint-6-Konfigurationen vollständig und plausibel

Fehler führen zu **Fail-Fast beim Start**.

#### Begründung

Dieser Punkt ist mit der vorhandenen Bootstrap-/Security-Konfiguration deterministisch anschließbar und erhöht die Betriebssicherheit ohne funktionale Ausweitung.

---

## 5. Nicht Bestandteil von Sprint 6

Folgende Themen wurden gegen die Baseline abgeglichen und sind **bewusst nicht** Bestandteil dieses Sprints, da sie für das MVP entweder nicht zwingend oder nicht minimal genug sind:

* JWT Key Rotation
* Refresh Tokens
* Permission-Resolution-Cache
* erweiterter Security-Audit-Trail für alle Änderungsereignisse
* OAuth2 / OpenID Connect
* externe Identity Provider
* Multi-Tenant-Modell
* UI-Administration
* komplexe Passwort-Policy
* IP-basiertes verteiltes Rate-Limiting

Diese Themen sind fachlich sinnvoll, würden den Sprint aber über den minimalen MVP-Bedarf hinaus erweitern.

---

## 6. Umsetzungsstrategie

Sprint 6 wird in **klar abgegrenzten, testbaren Phasen** umgesetzt. Jede Phase endet in einem stabilen Zwischenzustand.

---

### Phase 1 – Sicherheitskonfiguration vorbereiten

In der ersten Phase wird die Konfigurationsbasis für die neuen Sicherheitsfunktionen geschaffen. Ziel dieser Phase ist es, alle notwendigen Konfigurationswerte zu definieren und ihre Konsistenz beim Start der Anwendung sicherzustellen.

Hierzu werden Konfigurationsparameter für die maximale Anzahl fehlgeschlagener Login‑Versuche, die Dauer einer Kontosperrung sowie die minimale Passwortlänge eingeführt oder erweitert. Diese Werte werden in die bestehende Konfigurationsstruktur integriert.

Zusätzlich wird eine zentrale Validierung dieser Parameter implementiert. Die Anwendung prüft beim Start, ob alle sicherheitsrelevanten Konfigurationswerte vorhanden und plausibel sind. Werden unzulässige Werte erkannt, beendet sich die Anwendung kontrolliert mit einer klaren Fehlermeldung.

Nach Abschluss dieser Phase verändert sich das Laufzeitverhalten der Anwendung noch nicht. Alle bestehenden Funktionen bleiben unverändert und sämtliche bisherigen Integrationstests müssen weiterhin erfolgreich sein.

---

### Phase 2 – Minimale Passwort‑Policy implementieren

In der zweiten Phase wird die minimale Passwort‑Policy in die bestehenden Änderungs- und Erzeugungsprozesse für Benutzerkonten eingebunden. Ziel ist, dass alle relevanten Passwortoperationen künftig dieselbe fachliche Regel anwenden.

Hierzu wird ein zentraler Policy‑Baustein eingeführt, der die Mindestlänge eines Passworts überprüft. Diese Prüfung wird sowohl bei der Erstellung eines Benutzerkontos als auch bei Passwortänderungen verwendet. Die Fehlerbehandlung wird so angepasst, dass Verstöße gegen die Passwortregel in allen betroffenen Use Cases konsistent behandelt werden.

Nach Abschluss dieser Phase akzeptiert das System weiterhin alle fachlich gültigen Passwörter, lehnt jedoch zu kurze Passwörter deterministisch ab. Der fachliche Umfang bleibt dabei bewusst auf die Mindestlängenprüfung begrenzt.

---

### Phase 3 – Fehlversuchs‑Handling / Account Locking – Fehlversuchs‑Handling / Account Locking

In dieser Phase wird das zuvor definierte Fehlversuchs‑Handling in die Authentifizierungslogik integriert. Der Login‑Prozess wird so erweitert, dass jeder fehlgeschlagene Login‑Versuch erfasst und dem entsprechenden Benutzerkonto zugeordnet wird.

Überschreitet die Anzahl der Fehlversuche den konfigurierten Grenzwert, wird das Benutzerkonto temporär gesperrt. Während der Sperrzeit sind weitere Login‑Versuche nicht möglich. Die Sperre wird automatisch aufgehoben, sobald die konfigurierte Sperrdauer abgelaufen ist.

Ein erfolgreicher Login setzt den Fehlversuchszähler vollständig zurück. Dadurch wird sichergestellt, dass einzelne Fehlversuche eines legitimen Benutzers nicht dauerhaft zu einer Sperre führen.

Nach Abschluss dieser Phase ist der Login‑Endpunkt funktional unverändert nutzbar, besitzt jedoch einen minimalen Schutzmechanismus gegen wiederholte Fehlanmeldungen.

---

### Phase 4 – AuthZ‑Testmatrix vervollständigen

In der vierten Phase wird die Sicherheitsabsicherung der bereits vorhandenen REST‑Management‑Endpunkte systematisch über Integrationstests vervollständigt. Diese Phase erweitert keine Fachlogik, sondern schließt Nachweislücken der bestehenden Implementierung.

Für jede relevante Operation der vorhandenen Management‑API wird mindestens geprüft, dass ein Zugriff ohne Token mit `401 Unauthorized` beantwortet wird, ein Zugriff mit gültigem Token aber ohne erforderliche Authority `403 Forbidden` liefert und ein korrekt autorisierter Zugriff den erwarteten Erfolgsstatus zurückgibt.

Zusätzlich werden dort, wo es sich direkt aus der Baseline und aus der vorhandenen Endpunktlogik ergibt, fachliche Negativfälle ergänzt, etwa ungültige Identifikatoren, doppelte Benutzernamen oder Löschversuche bei bestehenden Abhängigkeiten.

Nach Abschluss dieser Phase ist die Sicherheitswirkung der vorhandenen REST‑Schnittstellen nicht nur implementiert, sondern für den MVP‑Release systematisch und reproduzierbar nachgewiesen.

---

## 7. Umsetzungsreihenfolge

Die Reihenfolge ist verbindlich, da sie die geringste Regressionsgefahr und die höchste Testbarkeit erzeugt:

1. **Konfiguration + Startvalidierung**
2. **Minimale Passwort-Policy**
3. **Fehlversuchs-Handling / Account Locking**
4. **Systematische AuthZ-Testmatrix**

Begründung:

* Phase 1 schafft die stabile Konfigurationsbasis.
* Phase 2 ist lokal und regressionsarm.
* Phase 3 verändert das Login-Verhalten fachlich und baut auf der Konfiguration auf.
* Phase 4 fixiert den finalen Sicherheitsnachweis des MVP-Zustands.

---

## 8. Teststrategie für Sprint 6

Die bestehende verbindliche Teststrategie bleibt erhalten:

* ausschließlich Integrationstests
* vollständige Spring-Konfiguration
* keine isolierten Unit-Tests als Sprint-Pflicht
* Prüfung der kompletten Kette

Für Sprint 6 werden die Tests entlang der Phasen erweitert.

### 8.1 Konfiguration

* gültige Konfiguration -> Start erfolgreich
* ungültige Sicherheitskonfiguration -> Start schlägt kontrolliert fehl

### 8.2 Passwort-Policy

* gültige Passwörter -> Erfolg
* zu kurze Passwörter -> fachlicher Fehler

### 8.3 Fehlversuchs-Handling

* Fehlversuche zählen hoch
* Account wird gesperrt
* gesperrter Account bleibt bis Ablauf gesperrt
* erfolgreicher Login setzt Zustand zurück

### 8.4 AuthZ-Matrix

* 401 ohne Token
* 403 ohne Authority
* Erfolg mit Authority
* wo passend: fachliche Negativfälle

---

## 9. Risiken

### 9.1 Risiko: zu breiter Sprint

Wenn zusätzliche Themen wie Key-Rotation, Caching oder Voll-Auditierung aufgenommen werden, verliert Sprint 6 den MVP-Fokus.

**Gegenmaßnahme:** strikte Begrenzung auf den in Abschnitt 4 definierten Scope.

### 9.2 Risiko: inkonsistente Passwortprüfung

Wenn Passwortregeln nur an einzelnen Stellen geprüft werden, entstehen Regelbrüche.

**Gegenmaßnahme:** zentrale Policy-Komponente, die an allen relevanten Änderungsstellen verwendet wird.

### 9.3 Risiko: Sperrlogik erzeugt Nebeneffekte

Fehlversuchs-Handling darf das bestehende Login-Verhalten nicht unkontrolliert verändern.

**Gegenmaßnahme:** phasenweise Umsetzung mit Integrationstests nach jedem Zwischenzustand.

---

## 10. Definition of Done Sprint 6

Sprint 6 ist abgeschlossen, wenn:

* eine minimale Passwort-Policy konsistent durchgesetzt wird
* wiederholte Fehlanmeldungen zu temporärem Account-Locking führen
* die sicherheitsrelevante Konfiguration beim Start validiert wird
* die vorhandenen Management-Endpunkte systematisch mit **401 / 403 / Erfolgsfall** testabgedeckt sind
* alle bestehenden und neuen Integrationstests grün sind
* keine Architekturverletzungen gegenüber der Baseline eingeführt wurden

---

## 11. Ergebnis

Sprint 6 schließt die **minimal notwendigen Sicherheits- und Testlücken** des IDM-MVP.

Nach Sprint 6 ist das IDM nicht „maximal ausgebaut“, aber in dem Umfang abgesichert, der für einen **sauberen MVP-Release** nach Baseline, aktueller Teststrategie und vorhandener Architektur wirklich erforderlich ist.
