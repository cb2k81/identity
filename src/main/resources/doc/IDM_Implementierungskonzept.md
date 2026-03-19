# Identity Management (IDM) Service – Technisches Implementierungskonzept
**Version:** 2
**Datum:** 19.03.2026

---

## 1. Einordnung und Zielsetzung

Dieses technische Implementierungskonzept definiert die verbindlichen Architektur-, Security- und Implementierungsregeln für den **Identity Management (IDM) Service**. Es ergänzt das Fachkonzept und bildet gemeinsam mit diesem die maßgebliche Grundlage für eine konsistente, deterministische Umsetzung.

Der IDM Service wird als **Spring-Boot-basiertes Backend-System** umgesetzt und stellt die im Fachkonzept definierten REST-Schnittstellen bereit. Ein Web-Client ist **nicht Bestandteil** dieses Projekts. Der **GWC** wird als eigenständiges, entkoppeltes Frontend-Projekt betrachtet und konsumiert die IDM-APIs.

Der aktuelle, belastbar nachweisbare Baseline-Stand des Projekts umfasst insbesondere:

* Spring Boot 3.x / Java 17+
* monolithische, schichtenbasierte Backend-Architektur
* JWT-basierte Authentifizierung
* persistentes Rollen-, Permission- und Scope-Modell
* deterministisches Bootstrap auf XML-Basis
* produktiv nutzbare Basis-Endpunkte für Login und Current Auth Context

Der Projekt-Scope dieses Dokuments umfasst:

* Implementierung der fachlichen Aggregate und Services
* Bereitstellung konsistenter REST-APIs
* Security (Authentifizierung, Autorisierung, JWT)
* Persistenz, Auditierung und Fehlermanagement
* deterministische Initialisierung sicherheitsrelevanter Stammdaten

Nicht Bestandteil:

* Frontend / Web-Client
* vollständiger OAuth2/OIDC Authorization Server
* externe IAM-Integrationen (z. B. LDAP, Keycloak, Entra), sofern nicht explizit beauftragt

---

## 2. Laufzeit- und Betriebsmodell

Das Laufzeit- und Betriebsmodell des IDM Service ist auf einen stabilen, skalierbaren und bewusst einfachen Betrieb ausgerichtet.

* Die Anwendung wird als **eigenständig lauffähiges Spring-Boot-JAR** ausgeliefert.
* Ein embedded Webserver wird verwendet; ein separater Applikationsserver ist nicht erforderlich.
* Die Zielumgebung ist containerfähig und Kubernetes-tauglich.
* Die Konfiguration erfolgt umgebungsgetrieben über `application.yml`, Profile, Environment-Variablen und Secrets.

### 2.1 Aktueller Baseline-Stand

Der aktuelle Projektstand umfasst unter anderem:

* konfigurierten Server-Port
* Health-/Info-Exposure via Actuator
* öffentlich erreichbaren Login-Endpunkt auf `/auth/login`
* JWT-Konfiguration über Secret und TTL
* Bootstrap-Konfiguration über `idm.bootstrap.*`
* testprofilbasierte H2-Nutzung
* konfigurierbaren Login-Schutz

Damit ist der technische Kern für einen leichtgewichtigen, produktiv nutzbaren Identity-Service bereits vorhanden.

### 2.2 Betriebsprinzip

* **Stateless im HTTP-Sinne**: keine serverseitigen HTTP-Sessions.
* **Stateful nur dort, wo fachlich nötig**: Persistenz von User-, Rollen-, Scope- und perspektivisch Session-/Refresh-Metadaten.
* **Horizontal skalierbar**: mehrere Instanzen sind möglich, sofern zustandsbehaftete Informationen nicht lokal im Prozess verbleiben.

### 2.3 Profile und Konfiguration

Es existieren klar abgegrenzte Profile für Entwicklung, Test und Produktion.

* `dev` für lokale Entwicklung
* `test` für automatisierte Tests und Integrationsumgebungen
* `prod` bzw. produktive Default-Konfiguration für den Realbetrieb

Verbindliche Konfigurationsregeln:

* `application.yml` enthält den produktionsnahen Default-Stand.
* Profilbezogene Abweichungen werden über ergänzende Profil-Dateien definiert.
* Secrets, Credentials und Schlüssel dürfen nicht im Repository hardcodiert werden.
* Sicherheitsrelevante Parameter wie JWT-Secret, TTL, Login-Protection und Bootstrap-Modus müssen konfigurationsgetrieben bleiben.

---

## 3. Technologie-Stack und technische Leitplanken

### 3.1 Verbindlicher Stack

* **Java 17+**
* **Spring Boot 3.x**
* **Spring Web / Spring MVC**
* **Spring Security**
* **Spring Data JPA / Hibernate**
* **MariaDB** als primäre Ziel-Datenbank
* **H2** für Tests
* **Maven** als Build-System
* **Lombok** zur Boilerplate-Reduktion
* **Liquibase** als Zielstandard für DB-Migrationen
* **Actuator** für Health/Info

### 3.2 Ergänzende Bibliotheken / Standards

* **Jakarta Bean Validation** für Request-Validierung
* **Jackson** für JSON-Serialisierung
* **MapStruct** oder funktional äquivalente Compile-Time-Mapping-Lösung
* **JJWT** oder äquivalente, aktiv gepflegte JWT-Bibliothek
* optional später: **Micrometer**, **Resilience4j**, zusätzliche Security-Scanner

### 3.3 Leichtgewichts-Prinzip

Technische Entscheidungen folgen dem Prinzip:

* produktiv belastbar,
* klar verständlich,
* deterministisch umsetzbar,
* ohne unnötigen IAM-Overhead.

Das bedeutet insbesondere:

* keine vorschnelle Einführung komplexer OAuth2-/OIDC-Provider-Mechanismen,
* keine verteilte Security-Infrastruktur ohne realen Bedarf,
* klare Trennung zwischen **Baseline/MVP-Kern** und **nächster produktiver Härtungsstufe**.

---

## 4. Architekturprinzipien

Die Architektur des IDM Service ist bewusst so gestaltet, dass Fachlichkeit, technische Stabilität und Erweiterbarkeit sauber getrennt bleiben.

Zentrales Leitprinzip ist die **Trennung von Verantwortlichkeiten entlang fachlicher und technischer Grenzen**. Jede Schicht besitzt eine klar definierte Aufgabe und darf ausschließlich mit den zulässigen Nachbarschichten interagieren.

### 4.1 Schichtenarchitektur und Zusammenspiel

#### Web-/Controller-Layer

* technische REST-Endpunkte
* Request-/Response-Verarbeitung
* Parameterbindung und formale Validierung
* keine fachlichen Entscheidungen

#### API-/Application-Layer

* Orchestrierung fachlicher Use Cases
* Transaktionsgrenzen
* Mapping zwischen DTOs und Domain-Objekten
* Durchsetzung von Endbenutzer-Autorisierung auf Use-Case-Ebene

#### Domain-Layer

* fachliche Aggregate
* fachliche Regeln und Konsistenz
* keine Kenntnis von Web/API-Details

#### Persistence-Layer

* Repositories und Persistenzadapter
* keine fachliche Orchestrierung

#### System-/Security-Layer

* technischer Shared Kernel
* JWT-Validierung
* Authentifizierungs- und Autorisierungs-Infrastruktur
* keine fachlichen Abhängigkeiten in Richtung Domain

### 4.2 Verbindliche Abhängigkeitsregeln

Die Architektur folgt der bereits etablierten Shared-Kernel-Regel:

```text
web         → domain
web         → system (nur technische Aspekte)

domain      → system

persistence → domain
persistence → system

system      → (keine Abhängigkeit zu domain)
```

**Kernregel:**

> Das `system`-Package darf niemals fachliche Domänenabhängigkeiten in Richtung IDM-Domain besitzen.

Diese Regel ist für die generische Rollen→Rechte-Auflösung und die Trennung von Core und Domäne verbindlich.

### 4.3 Self-Scope / Foreign-Scope als Architekturprinzip

Bereits im technischen Design zu berücksichtigen:

* **Self-Scope** = IDM verwaltet eigene Verwaltungsrechte
* **Foreign-Scopes** = IDM verwaltet zentrale Rollen-/Scope-Zuordnungen für Fachanwendungen
* **Foreign-Scopes liefern primär Rollen**, nicht zwingend finale Einzelrechte
* die **finale Rollen→Rechte-Auflösung** für Foreign-Scopes darf bewusst in der Fachanwendung erfolgen

Diese Trennung ist fachlich und sicherheitlich bindend.

---

## 5. Datenmodell und Persistenz

### 5.1 Kern-Entities des belastbaren Baseline-Stands

Der aktuelle Projektstand verwendet ein persistentes, generisches Rollen- und Berechtigungsmodell mit folgenden zentralen Entitäten:

* `UserAccount`
* `ApplicationScope`
* `PermissionGroup`
* `Permission`
* `Role`
* `UserApplicationScopeAssignment`
* `UserRoleAssignment`
* `RolePermissionAssignment`

### 5.2 Fachlich verbindliche Constraints

Verbindlich vorzusehen sind insbesondere:

* `UserAccount.username` → UNIQUE
* `ApplicationScope(applicationKey, stageKey)` → UNIQUE
* `PermissionGroup(applicationScope, name)` → UNIQUE
* `Permission(applicationScope, name)` → UNIQUE
* `Role(applicationScope, name)` → UNIQUE
* `UserApplicationScopeAssignment(userAccount, applicationScope)` → UNIQUE
* `UserRoleAssignment(userAccount, role)` → UNIQUE
* `RolePermissionAssignment(role, permission)` → UNIQUE

### 5.3 Optionale / nachgelagerte Domäne

* `Person` ist **nicht Bestandteil des belastbaren MVP-Kerns**.
* Falls fachlich später erforderlich, wird `Person` als optionale, nachgelagerte Domäne eingeführt.
* Das technische Konzept blockiert diese spätere Erweiterung nicht, priorisiert aber aktuell bewusst die **Identity- und Berechtigungsdomäne**.

### 5.4 Produktive Zielerweiterungen (noch nicht als Baseline-Kern voraussetzen)

Für die nächste produktive Härtungsstufe sind zusätzliche kontrollierbare Entitäten vorgesehen:

* `UserSession` / `RefreshSession` oder funktional äquivalente Session-Repräsentation
* `PasswordResetToken` oder funktional äquivalente Reset-Repräsentation
* `EmailVerificationToken` oder funktional äquivalente Verifikations-Repräsentation

Wichtig:

* Diese Entitäten sind **architektonisch vorgesehen**.
* Sie dürfen **nicht fälschlich als bereits vollständig im aktuellen Baseline-Kern umgesetzt** dokumentiert werden.

### 5.5 Migrationsstrategie

* **Liquibase ist Zielstandard** für produktive Schema-Migrationen.
* Der aktuelle Stand kann noch Übergangscharakter besitzen.
* Für die nächste Härtungsstufe ist die konsequente Aktivierung und Nutzung von Liquibase verbindlich vorzusehen.

---

## 6. Authentifizierung, Tokens und Sessions

### 6.1 Baseline-nachweisbarer Ist-Stand

Der aktuelle Baseline-Stand belegt:

* öffentlicher Login auf **`POST /auth/login`**
* Current-Context-Endpoint auf **`GET /auth/me`**
* JWT-Konfiguration über Secret und TTL
* tokenbasierte Absicherung geschützter Endpunkte

### 6.2 Verbindliche Trennung: Access Token vs. Session-/Refresh-Kontext

Für die erste echte Produktionsreife ist die Unterscheidung verbindlich zu präzisieren:

#### Access Token

* kurzlebig
* ausschließlich für API-Zugriffe
* JWT-basiert
* **wird nicht dauerhaft serverseitig persistiert**

#### Refresh Token / Session Token / kontrollierter Session-Kontext

* langlebiger als Access Token
* ausschließlich für kontrollierte Erneuerung und Session-Lifecycle
* serverseitig kontrollierbar, prüfbar und widerrufbar
* darf technisch als Token, Session-ID oder äquivalente Repräsentation modelliert werden

### 6.3 Aktuelle technische Leitentscheidung für Version 2

* Der **belastbar nachweisbare Baseline-Kern** ist aktuell **Access-Token-zentriert**.
* Ein produktionsreifer **Refresh-/Session-Mechanismus ist fachlich vorgesehen und technisch empfohlen**, aber **nicht als bereits vollständig umgesetzt zu behandeln**.

### 6.4 Login-Flow (Baseline + Zielbild)

#### Baseline-Kern

1. Client ruft `POST /auth/login` mit Credentials auf.
2. User wird gegen `UserAccount` validiert.
3. Benutzerstatus, Passwort-Hash, Fehlversuche und Sperren werden geprüft.
4. Bei Erfolg wird ein **JWT Access Token** ausgestellt.

#### Produktive Härtung (nächste Stufe)

Zusätzlich:

5. Es wird ein **Refresh-/Session-Kontext** erstellt.
6. Ein kontrollierter Refresh-Mechanismus wird aktiviert.
7. Session-Lifecycle wird explizit widerrufbar.

### 6.5 Access-Token-Inhalt

Das Token enthält mindestens:

* `sub`
* `iat`
* `exp`
* `iss`
* `userId`
* `username`
* Scope-Kontext / Scope-Referenz

Wichtige Klarstellung:

* die aktuelle Architektur sieht vor, dass **effektive Permissions serverseitig aufgelöst werden**
* Rollen und/oder finale Permissions sollen **nicht unkontrolliert redundant im Token als alleinige Autorisierungsquelle** fungieren
* das entspricht der dokumentierten Trennung von Authentication und serverseitiger Authorization-Resolution

### 6.6 Signaturstrategie

#### Aktueller realistischer Baseline-Stand

* symmetrische Signatur mit Shared Secret, z. B. **HS256**

#### Zielbild für produktive Härtung

* asymmetrische Signatur (**RS256** oder **ES256**) für Multi-Service-Betrieb

Damit gilt:

* **HS256** = zulässiger, realistischer Baseline-/MVP-Stand
* **RS256/ES256** = bevorzugte nächste produktive Härtungsstufe

### 6.7 Refresh / Logout / Invalidierung (Zielarchitektur)

Für die nächste produktive Stufe sind verbindlich vorzusehen:

* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`

Regeln:

* Access Tokens werden nicht blacklisted und nicht dauerhaft persistiert.
* Refresh-/Session-Kontexte sind widerrufbar.
* Passwortänderung, Passwort-Reset, Benutzer-Deaktivierung und sicherheitsrelevante Sperren müssen aktive Session-Kontexte invalidieren können.
* optional: Rotation von Refresh Tokens als nächster Härtungsschritt.

---

## 7. Autorisierung und Rechteauflösung

### 7.1 Grundmodell

Das System folgt technisch dem Modell:

**User → Roles → Permissions → GrantedAuthorities**

### 7.2 Verbindliche Baseline-Architektur

Die aktuelle Architekturentscheidung lautet:

* JWT dient primär der **Authentifizierung / Identität**.
* Rollen und Permissions werden **serverseitig datenbankbasiert** aufgelöst.
* Effektive `GrantedAuthority`-Instanzen entstehen zur Laufzeit.

### 7.3 Konsequenzen

Vorteile:

* keine stale Authorization bei Rollenänderungen
* Änderungen wirken sofort ohne Token-Neuausstellung
* keine überladene Token-Struktur
* zentrale Governance im IDM

### 7.4 Foreign-Scopes

Für Fachanwendungen gilt bewusst:

* IDM liefert zentrale Rollen-/Scope-Zuordnung.
* Fachanwendungen dürfen Foreign-Scope-Rollen in **eigene fachliche Rechte** auflösen.
* Dadurch bleibt Domänenhoheit in der Fachanwendung erhalten.

### 7.5 Method Security

* `@PreAuthorize` / `hasAuthority(...)` ist das Zielmuster.
* Interne IDM-Admin-APIs sind permission-basiert abzusichern.
* Keine hardcodierte „God Role“ ohne fachliche Auflösung.

---

## 8. API-Design und Endpunkt-Strategie

### 8.1 Baseline-nachweisbare Auth-Endpunkte

* `POST /auth/login`
* `GET /auth/me`

Diese Pfade sind im aktuellen Baseline-Stand konsistent zu referenzieren.

### 8.2 Produktive Härtung – nächste Stufe

Zusätzlich vorzusehen:

* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`
* optional intern: `POST /auth/introspect`

### 8.3 Verwaltungs-APIs

Konsistent vorzusehen:

* `/api/.../users`
* `/api/.../application-scopes`
* `/api/.../roles`
* `/api/.../permissions`
* `/api/.../permission-groups`
* `/api/.../user-application-scope-assignments`
* `/api/.../user-role-assignments`

### 8.4 Versionierung

Empfehlung:

* Zielbild: versionierte API, z. B. `/api/v1/...`
* baseline-naher pragmatischer Betrieb ohne vollständige Versionierung ist aktuell zulässig, solange die API konsistent dokumentiert bleibt

### 8.5 DTO-Strategie

* strikte Trennung von Entities und DTOs
* separate Create-/Update-/Response-DTOs
* keine sensitiven Felder in Responses
* eigener stabiler DTO-Typ für `/auth/me`

---

## 9. Bootstrap, Initialisierung und Konfigurationsmodell

### 9.1 Verbindliche Architektur

Der aktuelle Stand verwendet ein **deterministisches, XML-basiertes Bootstrap-Modell** für sicherheitsrelevante Stammdaten.

Konfigurierbar sind unter anderem:

* Admin-User
* Scopes
* Permission Groups
* Permissions
* Roles
* Role-Permission-Assignments
* User-Role-Assignments

### 9.2 Technische Regeln

* Definition (XML) und Deployment-Selektion (Properties) bleiben strikt getrennt.
* Self-Scope wird bewusst und deterministisch initialisiert.
* Bootstrap ist nicht nur Seed-Data, sondern Sicherheits- und Betriebsinitialisierung.
* Safe-/Force-Strategien bleiben zulässig, sofern deterministisch und testabgedeckt.

### 9.3 Nicht zulässig

* versteckte Hardcodings im Code statt deklarativer Initialisierung
* fachliche Sicherheitsdaten ausschließlich manuell per DB-Manipulation

---

## 10. Security-Hardening für die erste produktive Stufe

### 10.1 Passwort-Handling

* `DelegatingPasswordEncoder`
* Standard z. B. BCrypt / Argon2id
* konfigurierbare Mindestanforderungen
* keine Klartextspeicherung

### 10.2 Login-Schutz

Der Baseline-Stand sieht bereits konfigurierbare Login-Protection-Parameter vor:

* `max-failed-attempts`
* `lock-duration-seconds`

Diese Mechanik ist als Mindest-Hardening beizubehalten und auszubauen.

### 10.3 Reset- und Verifikations-Flows

Für die nächste produktive Härtungsstufe verbindlich vorzusehen:

* administrativer Passwort-Reset
* optional nutzerinitiierter Reset via E-Mail-Token
* E-Mail-Verification

Regeln:

* Tokens kurzlebig
* einmalig
* serverseitig kontrollierbar
* möglichst keine Klartextpersistenz solcher Tokens

### 10.4 Logging und Audit

* keine Passwörter oder Tokens in Logs
* Logging sicherheitsrelevanter Kernereignisse
* Audit-Felder auf Entity-Ebene
* bewusst leichtgewichtig starten, aber Kernereignisse zwingend erfassen

---

## 11. Tests, Qualitätssicherung und Deterministik

### 11.1 Verbindliche Testthemen

Mindestens abzudecken:

* erfolgreicher Login
* Login mit falschem Passwort
* Zugriff ohne Token
* Zugriff mit gültigem Token
* Current Auth Context (`/auth/me`)
* Rollen-/Permission-Auflösung
* Bootstrap (Disabled / Safe / Idempotenz / Fehlerszenarien)
* Login-Protection / Sperrverhalten
* später: Refresh / Logout / Logout-all

### 11.2 Testprinzipien

* profilgesteuert (`test`)
* deterministische Testdaten
* keine impliziten Umgebungsannahmen
* H2 oder äquivalente isolierte Test-Datenbank
* Baseline + bestätigte Chat-Änderungen als einzige Wahrheitsquelle

### 11.3 Projektregel

Für dieses Projekt gilt dauerhaft:

* ausschließlich deterministisches Arbeiten
* keine Annahmen über nicht nachgewiesenen Code
* keine Erfindung von Klassen, Methoden oder Pfaden
* Dokumentation und Umsetzung müssen mit Textexport und bestätigten Änderungen konsistent bleiben

---

## 12. Konsolidierte Leitentscheidung für Version 2

Dieses technische Konzept in **Version 2** konsolidiert den aktuellen MVP-Stand mit der nächsten klar definierten produktiven Härtungsstufe.

### 12.1 Verbindlicher Baseline-Kern

* Spring-Boot-Monolith mit klarer Schichtung
* persistentes Rollen-/Permission-/Scope-Modell
* deterministisches XML-Bootstrap
* `POST /auth/login`
* `GET /auth/me`
* JWT-basierte Authentifizierung
* serverseitige Rollen-/Permission-Auflösung
* konfigurierbarer Login-Schutz

### 12.2 Verbindliche nächste produktive Härtung

* klare Trennung **Access Token vs. Refresh-/Session-Kontext**
* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`
* kontrollierbare Session-Invalidierung ohne Persistenz von Access Tokens
* Passwort-Reset
* E-Mail-Verification
* perspektivisch asymmetrische JWT-Signatur
* Aktivierung echter Liquibase-Migrationsführung

### 12.3 Wichtige Korrekturen gegenüber früheren, zu groben Annahmen

* **Person** ist aktuell **nicht MVP-Kern**.
* **Refresh-/Logout-Flows** sind **fachlich erforderlich**, aber **nicht als bereits vollständig umgesetzt zu dokumentieren**.
* **Auth-Pfade** sind baseline-konsistent derzeit auf **`/auth/...`** zu referenzieren.
* **HS256** ist als realistischer Baseline-Stand zulässig; **RS256/ES256** ist Zielbild.
* **Autorisierung** bleibt serverseitig datenbankbasiert, nicht tokenzentriert.

---

## 13. Zusammenfassung

Das IDM ist im aktuellen Stand ein **leichtgewichtiger, aber bereits ernsthaft produktionsnaher Identity- und Authorization-Service**.

Der belastbare MVP-Kern ist erreicht.

Für echte erste Produktivreife fehlen technisch vor allem noch die klar definierte und sauber implementierte **Session-/Refresh-Strategie**, die **kontrollierte Invalidierung aktiver Session-Kontexte** sowie die flankierenden **Reset-/Verifikations-Flows**.

Genau diese Punkte sind in diesem Dokument nun bewusst als **nächste produktive Härtungsstufe** definiert – ohne den aktuellen Baseline-Stand zu überzeichnen.

Damit ist das technische Konzept konsistent zum überarbeiteten Fachkonzept V2 und zum aktuellen IDM-Projektstand.
