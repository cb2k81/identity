# IDM – Sprint 5 – Abschlussbericht (IDM_Sprint5_3)

Stand: 2026-03-02

## 1. Baseline / Verbindlichkeit

* **Verbindliche Baseline:** `idm_code-export_2026-03-02_10-47-56.txt` (Textexport).
* Diese Baseline wird für alle Aussagen in diesem Bericht als Quelle verwendet.
* Der **Arbeitsvertrag (deterministische Umsetzung, keine Annahmen/Erfindungen, baseline-basiert, Selbstprüfung, Importpfade/Fragmente beibehalten)** wird weiterhin eingehalten.

## 2. Sprint-Ziele – Reifegrad und Vollständigkeit

### 2.1 Ergebnisübersicht

Der Sprint-Zielzustand ("Test ist grün") ist erreicht. Die zentrale MVP-Fähigkeit für **IDM-Authentifizierung + Autorisierung im Kontext ApplicationScope/Rollen/Rechte** ist im Projektzustand nachweisbar durch:

* Login-Endpoint `/api/auth/login` liefert JWT.
* Zugriffsschutz auf Management-Endpunkte unter `/api/idm/**`.
* Autorisierung über **Authorities/Permissions** (Spring Security `hasAuthority(...)`) ist implementiert und wird in End-to-End-Integrationstests geprüft.
* Verwaltung/CRUD für ApplicationScope und UserAccount ist im Stack **Controller → DomainService/Facade → Handler → EntityService → Repository → DB** umgesetzt.

### 2.2 Reifegrad-Einschätzung (MVP)

**Reifegrad: MVP-fähig (funktional, testbar, baseline-konform), aber nicht „produktionshart“ in allen Randbereichen.**

Begründung (rein aus Code/Tests ableitbar):

* Die Kernprozesse sind als Integrationstests abgedeckt (Login, Schutz ohne Token, Schutz ohne Scope/Rolle, Admin-Aktionen, CRUD ApplicationScope).
* Die Architektur-Schichten sind eingehalten (keine direkten Repository-Zugriffe aus Controllern; Security i. d. R. in Handlern/Domain Services).
* Es gibt jedoch noch typische MVP-Randthemen (siehe Abschnitt 5 Technische Schulden / MVP-Pflichten).

## 3. Umsetzungsstatus / Vollständigkeitsmatrix

Legende:

* ✅ Fertig / nachweisbar im Code + Tests
* 🟡 Teilweise / im Code vorhanden, aber nicht vollständig abgesichert
* ❌ Offen / nicht nachweisbar

### 3.1 Sprint-Deliverables (funktional)

| Themenblock                | Erwartetes Ergebnis                     | Status | Nachweis (Konzeptuell)                        |
| -------------------------- | --------------------------------------- | -----: | --------------------------------------------- |
| Auth: Login                | `/api/auth/login` liefert JWT           |      ✅ | Login-Integrationstests vorhanden             |
| AuthZ: Token erforderlich  | Ohne Token 401/Unauthorized             |      ✅ | Integrationstests vorhanden                   |
| AuthZ: ApplicationScope    | User ohne Scope wird abgewiesen         |      ✅ | Integrationstests vorhanden                   |
| AuthZ: Rollen/Permissions  | hasAuthority(…) schützt Use-Cases       |      ✅ | Handler/DomainServices mit `@PreAuthorize`    |
| IDM User Management        | `/api/idm/users` create (Admin)         |      ✅ | UserAccountManagementIntegrationTest          |
| IDM Scope Management       | `/api/idm/scopes` CRUD                  |      ✅ | ApplicationScopeControllerCrudIntegrationTest |
| Bootstrapping (safe/force) | deterministisches Setup für IDM-Objekte |      ✅ | Bootstrap-Integrationstests vorhanden         |

### 3.2 Sprint-Deliverables (architektonisch)

| Themenblock          | Erwartetes Ergebnis                                                    | Status | Bewertung                                                     |
| -------------------- | ---------------------------------------------------------------------- | -----: | ------------------------------------------------------------- |
| Layering             | Controller → Facade/Domain → Handler → EntityService → Repo            |      ✅ | In Controllern dokumentiert + in Code umgesetzt               |
| Security-Platzierung | Keine `@PreAuthorize` in Controllern; Enforcement in Handlern/Services |      ✅ | Controller-Kommentare + PreAuthorize in Handlern              |
| DTO-Regel            | Controller/Domain liefern DTOs, EntityServices keine DTOs              |      ✅ | Pattern ist konsistent (EntityServices arbeiten auf Entities) |
| Transaktionen        | Use-Case Grenzen über Services/Handler                                 |      ✅ | `@Transactional` / readOnly vorhanden                         |

### 3.3 MVP-Gesamtanforderungen (über Sprint-Ziele hinaus)

| MVP-Anforderung               | Status | Hinweis                                                                                                                            |
| ----------------------------- | -----: | ---------------------------------------------------------------------------------------------------------------------------------- |
| Minimale Nutzerverwaltung     |      ✅ | Create User per API + AuthZ geprüft                                                                                                |
| Minimaler Rollen/Rechte-Stack |      ✅ | Rollen, Permissions, Assignments vorhanden + Tests                                                                                 |
| Scope-basierte Trennung       |      ✅ | Scope Entity + Assignments + Testfälle                                                                                             |
| Auditability (Basis)          |     🟡 | Audit-Felder in DomainEntity vorhanden; Security-Event-Audit (Login/Denied) nicht als eigenes Audit-Subsystem nachgewiesen         |
| Input-Validierung (API)       |     🟡 | Manuelle Checks in Services; flächendeckende Bean Validation (jakarta.validation) ist nicht ersichtlich/erzwingt nicht automatisch |
| Negative/Edge-Cases           |     🟡 | Einige vorhanden (401/403, falsches Passwort), aber keine systematische Abdeckung aller Management-Cases                           |

## 4. Architektur-Compliance (explizite Prüfung)

### 4.1 Positivbefunde

* **Controller sind schlank** und delegieren in DomainService/Facade (Beispiel: ApplicationScopeController).
* **Security-Checks sind in Use-Case/Handler-Ebene** über `@PreAuthorize("hasAuthority('…')")` umgesetzt.
* **EntityService kapselt Persistenz** und enthält keine DTO/Security/Use-Case-Orchestrierung.
* **Integrationstests testen den Stack Ende-zu-Ende** (inkl. DB).

### 4.2 Auffälligkeiten / potentielle Architektur-Schulden

* In einzelnen Bereichen existieren noch **manuelle Validierungen** (Argument-Checks) in DomainServices statt konsistenter Bean-Validation auf DTO + Controller. Das ist für MVP okay, ist aber mittelfristig Inkonsistenz-Risiko.
* Test-Setup arbeitet teils mit **JdbcTemplate Inserts** für Baseline-konformes Seeding. Das ist für Integrationstests legitim, erhöht aber Wartungsaufwand bei Schemaänderungen.

## 5. Technische Schulden – müssen sie im MVP behoben werden?

### 5.1 Technische Schulden (feststellbar)

1. **Validierungsstrategie nicht einheitlich**

    * Aktuell: viele manuelle Checks (null/blank) in Services.
    * Risiko: inkonsistente Fehlermeldungen/HTTP-Codes, doppelte Logik.

2. **Testabdeckung fokussiert auf „Happy Path“ + wenige Negativfälle**

    * Positiv: Auth (401/403), falsches Passwort, Scope/Role-Konstellationen sind teilweise abgedeckt.
    * Offen: systematisches Negativ-Testing für alle CRUD-Operationen (z. B. fehlende Authorities, Invalid DTO, Duplicate username, Löschszenarien, Update-Szenarien).

3. **Security-Hardening-Themen nicht als abgeschlossen nachweisbar**

    * Beispiele (nicht zwingend Sprint-Ziel, aber MVP-Risiko): Rate-Limit/Brute-Force Schutz, Account Locking nach Fehlversuchen, Passwort-Policy über Mindestlänge/Komplexität, Security Event Audit.

### 5.2 MVP-Pflicht: Was MUSS vor einem „MVP Release“ rein?

Empfehlung – minimal, aber sicherheitsrelevant:

* **Brute-Force/Fehlversuchs-Handling** (mindestens: temporäres Locking oder Delay) – wenn IDM extern erreichbar ist.
* **Passwort-Policy minimal** (z. B. Mindestlänge) konsistent enforced.
* **Systematische AuthZ-Tests** für mindestens: Create/Read/Update/Delete pro Resource (User, Role, Permission, Scope) mit jeweils: 401 (no token), 403 (no authority), 200/201 (authorized).

Wenn das IDM aktuell nur intern/abgesichert betrieben wird, kann man diese Punkte (mit Risikoakzeptanz) in einen Folge-Sprint verschieben.

## 6. Testabdeckung – Bewertung

### 6.1 Was ist gut abgedeckt?

* **Login/JWT**: Token kommt zurück, falsches Passwort liefert 401.
* **Access Control**:

    * Ohne Token: 401.
    * Ohne Scope: 401.
    * Ohne Rolle/Permission: 403.
* **Management-Flows**:

    * User create als Admin.
    * Scope CRUD als Vollintegration.
* **Bootstrapping**:

    * Safe/Force Bootstrapping wird per Integrationstest abgesichert.

### 6.2 Was fehlt für „ausreichend“ im Sinne MVP?

* Für alle Management-Endpunkte mindestens ein Satz Tests:

    * **401** ohne Token
    * **403** ohne Authority
    * **200/201/204** mit Authority
* Mehr negative Datenfälle:

    * Duplicate-Username
    * Ungültige IDs
    * Invalid/Blank Felder in Requests

## 7. Zusammenfassung / Sprint-Status

* **Sprint 5 Status:** **ABGESCHLOSSEN (grün, Kernziele erfüllt)**.
* **MVP-Status:** **MVP-fähig** für Kern-Auth/AuthZ + Management-Basics.
* **Haupt-Risiken vor MVP-Release:** Security-Hardening (Brute-Force/Locking), konsistente Validierung, breitere AuthZ-Testmatrix.

## 8. Nächste Schritte (konkret)

1. Entscheidung: **Ist das IDM im MVP extern erreichbar?**

    * Ja → Security-Hardening als MVP-Pflicht.
    * Nein → Hardening kann nach MVP (mit dokumentierter Risikoakzeptanz).

2. Testmatrix vervollständigen (systematisch je Resource/Operation).

3. Optional: Bean Validation auf Request DTOs + konsistentes Error-Handling (ControllerAdvice), um die Service-Argument-Checks zu reduzieren.
