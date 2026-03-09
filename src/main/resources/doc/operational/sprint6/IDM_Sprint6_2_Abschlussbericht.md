# IDM – Sprint 6 Abschlussbericht

Stand: 09.03.2026
Version: 1.0

---

# 1. Überblick

Dieser Bericht dokumentiert den Abschluss von **Sprint 6** des Identity‑Management‑Systems (IDM). Der Sprint hatte das Ziel, die bereits funktionale MVP‑Basis des Systems sicherheitstechnisch zu härten und die vorhandene Testabdeckung systematisch zu vervollständigen.

Sprint 6 stellt den letzten Schritt dar, um das IDM in einen **sauberen, sicherheitstechnisch abgesicherten MVP‑Zustand** zu überführen.

Der Fokus lag bewusst **nicht auf Funktionsausbau**, sondern auf:

* Absicherung des Login‑Mechanismus
* Durchsetzung einer minimalen Passwort‑Policy
* vollständiger Security‑Testabdeckung der Management‑Endpoints

Alle Arbeiten wurden strikt auf Basis der bestehenden Architektur umgesetzt.

---

# 2. Ausgangssituation vor Sprint 6

Nach Abschluss von Sprint 5 verfügte das System bereits über folgende Fähigkeiten:

* JWT‑basierte Authentifizierung
* Rollen‑ und Permission‑Modell
* method‑basierte Autorisierung
* deterministischen System‑Bootstrap
* grundlegende Integrationstests

Der Login‑Mechanismus funktionierte korrekt, besaß jedoch noch keine Schutzmechanismen gegen automatisierte Fehlversuche.

Ebenso existierte keine verbindliche Regel zur Mindestqualität von Passwörtern.

Darüber hinaus war die Security‑Testabdeckung der Management‑Endpoints funktional vorhanden, jedoch noch nicht vollständig systematisch strukturiert.

Sprint 6 adressierte gezielt diese Punkte.

---

# 3. Ziele von Sprint 6

Der Scope von Sprint 6 war bewusst klar begrenzt und bestand aus drei Themenbereichen:

1. Fehlversuchs‑Handling und Account‑Locking
2. Einführung einer minimalen Passwort‑Policy
3. Systematische Security‑Testmatrix für Management‑Endpoints

Ziel war eine **minimal notwendige Sicherheitsabsicherung für einen MVP‑Release**, ohne zusätzliche Infrastruktur oder komplexe Sicherheitsmechanismen einzuführen.

---

# 4. Umgesetzte Maßnahmen

## 4.1 Login‑Protection (Brute‑Force‑Schutz)

Der Login‑Mechanismus wurde um eine Schutzstrategie gegen wiederholte Fehlanmeldungen erweitert.

### Funktionsweise

Bei jedem fehlgeschlagenen Login‑Versuch:

* wird der Fehlversuchszähler des Benutzerkontos erhöht
* wird geprüft, ob die maximale Anzahl erlaubter Fehlversuche erreicht ist

Wird der konfigurierte Schwellenwert überschritten:

* wird das Konto temporär gesperrt
* es wird ein Zeitpunkt festgelegt, bis zu dem die Sperre aktiv bleibt

Während der Sperrzeit sind keine erfolgreichen Logins möglich.

### Entsperrlogik

Ein Konto wird automatisch wieder freigegeben, sobald:

* die konfigurierte Sperrdauer abgelaufen ist
* oder ein erfolgreicher Login erfolgt

Bei erfolgreicher Authentifizierung werden:

* der Fehlversuchszähler
* sowie ein eventuell gesetzter Sperrzeitpunkt

vollständig zurückgesetzt.

### Konfiguration

Die Login‑Protection ist vollständig konfigurierbar:

* maximale Fehlversuche
* Dauer der temporären Sperre

Damit kann das Verhalten an unterschiedliche Betriebsanforderungen angepasst werden.

---

## 4.2 Minimale Passwort‑Policy

Für den MVP‑Release wurde eine **deterministische Mindestanforderung für Passwörter** eingeführt.

### Policy

Die aktuelle Policy definiert:

* eine konfigurierbare Mindestlänge für Passwörter

Weitere Regeln (z. B. Zeichensatzanforderungen, Passwort‑Historien oder Blacklists) sind bewusst **nicht Bestandteil dieses Sprints**.

### Zentrale Implementierung

Die Passwortprüfung erfolgt über eine zentrale Policy‑Komponente innerhalb der Domain.

Diese wird angewendet bei:

* Erstellung neuer Benutzerkonten
* Änderung bestehender Passwörter

Verstößt ein Passwort gegen die Mindestanforderungen, wird die Operation deterministisch abgelehnt.

Diese Architektur stellt sicher, dass Passwortregeln **konsistent und zentral durchgesetzt werden**.

---

## 4.3 Systematische Security‑Testmatrix

Ein zentrales Ziel von Sprint 6 war der systematische Nachweis der Zugriffssicherheit aller Management‑Endpoints.

Für relevante REST‑Operationen wurden Integrationstests ergänzt oder vervollständigt.

Die Testmatrix deckt mindestens folgende Fälle ab:

| Szenario                             | Erwartetes Ergebnis    |
| ------------------------------------ | ---------------------- |
| Zugriff ohne Token                   | 401 Unauthorized       |
| Zugriff ohne erforderliche Authority | 403 Forbidden          |
| Zugriff mit korrekter Authority      | Erfolgreiche Operation |

Zusätzlich werden dort, wo fachlich sinnvoll, auch negative fachliche Fälle geprüft.

Diese Tests stellen sicher, dass:

* die HTTP‑Security korrekt greift
* die Method‑Security zuverlässig funktioniert
* die API deterministisch auf fehlende Berechtigungen reagiert

Alle Tests werden automatisiert im Build ausgeführt.

---

# 5. Technischer Zustand nach Sprint 6

Nach Abschluss des Sprints befindet sich das IDM‑System in einem stabilen MVP‑Zustand mit folgenden Eigenschaften:

### Authentifizierung

* JWT‑basierte Authentifizierung
* signierte Tokens
* serverseitige Validierung

### Autorisierung

* Rollen‑ und Permission‑Modell
* Authority‑basierte Method‑Security

### Login‑Sicherheit

* Schutz gegen automatisierte Fehlversuche
* temporäre Account‑Sperren
* konfigurierbare Parameter

### Passwortregeln

* zentrale Passwort‑Policy
* Mindestlänge konfigurierbar

### Systeminitialisierung

* deterministischer Bootstrap
* reproduzierbare Initialdaten

### Testabdeckung

* Integrationstests für zentrale Auth‑Flows
* Security‑Testmatrix für Management‑Endpoints

Alle Tests des Projekts sind aktuell **grün**.

---

# 6. Architekturkonformität

Die Umsetzung erfolgte vollständig innerhalb der bestehenden Architekturprinzipien.

Insbesondere wurden folgende Regeln eingehalten:

* klare Trennung zwischen Domain‑Logik und Security‑Konfiguration
* Controller enthalten keine Authentifizierungslogik
* Passwort‑Hashing bleibt Infrastruktur
* JWT‑Erzeugung erfolgt im technischen System‑Layer

Es wurden **keine Architekturverletzungen gegenüber der bestehenden Baseline eingeführt**.

---

# 7. Abgleich mit Konzept, Architektur und ADR

Im Rahmen der Abschlussanalyse wurde der aktuelle Implementierungsstand systematisch mit den folgenden Referenzdokumenten abgeglichen:

* Fachkonzept: `IDM_Fachkonzept.md`
* Technisches Konzept: `IDM_Implementierungskonzept.md`
* Architekturentscheidungen (ADR)

Ziel dieses Abschnitts ist eine transparente Darstellung des aktuellen Umsetzungsgrades sowie möglicher Abweichungen oder bewusst zurückgestellter Funktionen.

---

## 7.1 Architektur‑ und Systemanforderungen

| Bereich                       | Konzept / Anforderung            | Implementierung              | Status |
| ----------------------------- | -------------------------------- | ---------------------------- | ------ |
| Systemarchitektur             | Spring Boot Service              | Spring Boot 3.x Service      | ✔      |
| Stateless Architektur         | Token‑basierte Authentifizierung | JWT ohne Server‑Session      | ✔      |
| Containerfähigkeit            | Kubernetes‑tauglicher Betrieb    | Spring Boot JAR, stateless   | ✔      |
| Profile (dev/test/prod)       | getrennte Laufzeitprofile        | vorhanden                    | ✔      |
| Liquibase Migrationen         | versionierte DB Migrationen      | vorhanden                    | ✔      |
| Maven Build                   | deterministischer Build          | Maven + automatisierte Tests | ✔      |
| SBOM / Dependency Transparenz | dokumentierte Abhängigkeiten     | teilweise vorbereitet        | ◐      |

---

## 7.2 Domain‑Modell

| Bereich                        | Konzept                     | Implementierung     | Status |
| ------------------------------ | --------------------------- | ------------------- | ------ |
| UserAccount                    | Benutzerkonto verwalten     | Entity vorhanden    | ✔      |
| Username Constraint            | eindeutiger Username        | vorhanden           | ✔      |
| Passwort‑Hash                  | sichere Speicherung         | BCrypt              | ✔      |
| Account‑Status                 | Aktiv/Deaktiviert/Lock      | vorhanden           | ✔      |
| ApplicationScope               | Scope pro Anwendung + Stage | vorhanden           | ✔      |
| PermissionGroup                | Gruppierung von Permissions | vorhanden           | ✔      |
| Permission                     | feingranulare Rechte        | vorhanden           | ✔      |
| Role                           | Rollenmodell                | vorhanden           | ✔      |
| RolePermissionAssignment       | Rollen → Permissions        | vorhanden           | ✔      |
| UserRoleAssignment             | User → Rollen               | vorhanden           | ✔      |
| UserApplicationScopeAssignment | User → Scope                | vorhanden           | ✔      |
| Person‑Entität                 | Verwaltung von Personen     | nicht implementiert | ○      |

Die aktuelle Implementierung entspricht damit dem im Konzept beschriebenen generischen Rollen‑ und Berechtigungsmodell.

---

## 7.3 Security‑Architektur

| Bereich               | Konzept                        | Implementierung             | Status |
| --------------------- | ------------------------------ | --------------------------- | ------ |
| Login Endpoint        | Authentifizierung über REST    | `/auth/login`               | ✔      |
| JWT Tokens            | signierte Tokens               | vorhanden                   | ✔      |
| JWT Secret            | konfigurierbar                 | vorhanden                   | ✔      |
| Token TTL             | konfigurierbar                 | vorhanden                   | ✔      |
| JWT Validation        | Filter / Resource Server       | vorhanden                   | ✔      |
| Rollen im Token       | nicht vorgesehen               | korrekt nicht implementiert | ✔      |
| Permission Resolution | serverseitige Auflösung        | PermissionResolver          | ✔      |
| Method Security       | `@PreAuthorize`                | vorhanden                   | ✔      |
| Password Hashing      | Infrastruktur                  | BCrypt                      | ✔      |
| Password Policy       | Mindestanforderungen           | implementiert               | ✔      |
| Login Protection      | Account Lock bei Fehlversuchen | implementiert               | ✔      |
| Refresh Token         | Erweiterungsoption             | nicht implementiert         | ○      |
| Key Rotation          | Erweiterungsoption             | nicht implementiert         | ○      |

Die implementierte Security‑Architektur entspricht vollständig der in den ADRs definierten Strategie, insbesondere der serverseitigen Autorisierungsauflösung.

---

## 7.4 Bootstrap und Initialisierung

| Bereich                     | Konzept                        | Implementierung | Status |
| --------------------------- | ------------------------------ | --------------- | ------ |
| deterministischer Bootstrap | XML‑basierte Initialisierung   | vorhanden       | ✔      |
| Admin‑User                  | Initialisierung über Bootstrap | vorhanden       | ✔      |
| Scopes                      | Bootstrap XML                  | vorhanden       | ✔      |
| Permissions                 | Bootstrap XML                  | vorhanden       | ✔      |
| Rollen                      | Bootstrap XML                  | vorhanden       | ✔      |
| Rollen‑Berechtigungen       | Bootstrap XML                  | vorhanden       | ✔      |
| User‑Rollen                 | Bootstrap XML                  | vorhanden       | ✔      |

Damit ist eine reproduzierbare Initialisierung des Systems gewährleistet.

---

## 7.5 Layer‑Architektur

| Bereich                  | Konzept                 | Implementierung                     | Status |
| ------------------------ | ----------------------- | ----------------------------------- | ------ |
| Controller Layer         | REST Endpunkte          | vorhanden                           | ✔      |
| API / Application Facade | Use‑Case Orchestrierung | aktuell nicht separat implementiert | ⚠      |
| Domain Services          | fachliche Logik         | vorhanden                           | ✔      |
| Entity Services          | Persistenzoperationen   | vorhanden                           | ✔      |
| Repository Layer         | Datenzugriff            | vorhanden                           | ✔      |

Im aktuellen Projekt existiert keine separate Application‑Facade‑Schicht. Domain‑Services übernehmen aktuell diese Rolle. Für den Umfang des MVP stellt dies jedoch keine funktionale Einschränkung dar.

---

## 7.6 Teststrategie

| Bereich           | Konzept              | Implementierung | Status |
| ----------------- | -------------------- | --------------- | ------ |
| Domain Tests      | isolierte Logiktests | vorhanden       | ✔      |
| Integration Tests | Spring Boot Tests    | vorhanden       | ✔      |
| Security Tests    | Login + Permissions  | vorhanden       | ✔      |
| Bootstrap Tests   | Konfigurationstests  | vorhanden       | ✔      |

Die Teststrategie entspricht damit vollständig den im Implementierungskonzept definierten Qualitätsanforderungen.

---

# 8. Offene Punkte und bewusst nicht umgesetzte Features

Im Rahmen von Sprint 6 bestehen **keine offenen Punkte innerhalb des definierten Scopes**.

Alle Ziele des Sprints wurden erreicht:

* Login‑Protection implementiert
* Passwort‑Policy eingeführt
* Security‑Tests vervollständigt

Das System erfüllt damit die Sicherheitsanforderungen für den vorgesehenen MVP‑Release.

---

## 8.1 Nicht Bestandteil des MVP

Die folgenden Funktionen sind im Fach‑ oder Implementierungskonzept vorgesehen, wurden jedoch bewusst nicht im MVP umgesetzt:

| Feature                 | Status                        |
| ----------------------- | ----------------------------- |
| Person‑Domäne           | nicht implementiert           |
| Refresh‑Token‑Strategie | nicht implementiert           |
| Key‑Rotation für JWT    | nicht implementiert           |
| Record‑Level‑Security   | nicht implementiert           |
| UI‑Administration       | separates zukünftiges Projekt |

Diese Funktionen sind Erweiterungen für spätere Ausbaustufen und stellen keine Einschränkung für den MVP‑Release dar.

---

# 9. Ausblick

Sprint 6 markiert den Abschluss der minimal notwendigen Sicherheitsmaßnahmen für das IDM‑MVP.

Mögliche zukünftige Erweiterungen außerhalb des aktuellen Scopes können sein:

* komplexere Passwort‑Policies
* Passwort‑Historie
* Token‑Revocation
* Key‑Rotation für JWT
* Audit‑Logging sicherheitsrelevanter Aktionen

Diese Themen sind jedoch **nicht erforderlich für den aktuellen MVP‑Release**.

---

# 10. Zusammenfassung

Sprint 6 schließt die letzten sicherheitsrelevanten Lücken der IDM‑MVP‑Implementierung.

Der Login‑Mechanismus ist nun gegen wiederholte Fehlversuche abgesichert, eine minimale Passwort‑Policy wird konsequent durchgesetzt, und die Zugriffssicherheit der Management‑API ist durch Integrationstests nachgewiesen.

Das Identity‑Management‑System befindet sich damit in einem **stabilen, sicherheitsseitig abgesicherten MVP‑Zustand** und ist bereit für den nächsten Entwicklungsabschnitt oder eine erste produktive Nutzung im vorgesehenen Einsatzkontext.
