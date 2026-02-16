# Identity Management (IDM) Service – Konzept (Version 1)

---

## 1. Projekt-Kontext

Der Auftraggeber entwickelt eine neue Anwendung namens **PersonnelApp**, die besonders sensible personenbezogene Daten verarbeitet. Für diese Anwendung – und perspektivisch weitere Fachanwendungen – wird ein **eigener, leichtgewichtiger, aber sicherer Authentifizierungs- und Berechtigungsdienst** benötigt.

Ziele des Auftraggebers:

* **PK-1**: Der IDM-Service soll vollständig **entkoppelt** von bestehenden zentralen IT-Mechanismen sein (z. B. Active Directory, Keycloak). Die zentralen IT-Administratoren sollen **keinen Einfluss auf die Berechtigungen oder den Datenzugriff** in PersonnelApp haben.
* **PK-2**: Der Dienst soll **autark** und **eigenständig administrierbar** sein, um dedizierte Verantwortlichkeiten sicherzustellen.
* **PK-3**: Die Lösung muss sich **präzise an fachliche Anforderungen** anpassen lassen, ohne die Komplexität eines vollwertigen Identity Providers (z. B. Keycloak).
* **PK-4**: Gleichzeitig sollen **moderne Sicherheitsstandards** gewährleistet sein (Passwort-Hashing, JWT, Berechtigungsmodell, sichere Konfiguration, k8s-Tauglichkeit).
* **PK-5**: Der Dienst soll eine **langfristige Basis** sein, die später extern angebunden werden kann (z. B. AD/LDAP/Keycloak) – jedoch erst in späteren Ausbaustufen.
* **PK-6**: Die Architektur muss den Betrieb in einer **Kubernetes-Umgebung** ermöglichen.

Damit bildet der IDM-Service eine **spezialisierte, sicherheitssensible Kernkomponente**, die sowohl unabhängig von der IT-Landschaft, als auch zukunftsfähig gestaltet ist.

---

## 1. Anforderungen (erste produktive Ausbaustufe)

In diesem Kapitel werden alle bekannten Anforderungen aus dem bisherigen Austausch strukturiert, hierarchisch und nummeriert erfasst. Fokus ist die **erste produktionsreife Ausbaustufe** (MVP+, nicht Spielwiese).

### 1.1 Fachliche Anforderungen (Functional Requirements, FR)

**FR-1 Benutzerkonten verwalten**
Der IDM-Service muss Benutzerkonten (User Accounts) verwalten können.

* FR-1.1: Anlegen, Lesen, Aktualisieren, Deaktivieren von Benutzerkonten.
* FR-1.2: Benutzerkonten besitzen Attribute wie: ID, Username, Passwort-Hash, Account-Status, optionale Verfallsdaten, Timestamps.
* FR-1.3: Benutzerkonten können ohne zugehörige Person existieren (z. B. technische Accounts / Service Accounts).

**FR-2 Personen verwalten**
Der IDM-Service muss Personen als eigenständige Entitäten verwalten.

* FR-2.1: Anlegen, Lesen, Aktualisieren, Deaktivieren von Personen.
* FR-2.2: Attribute einer Person u. a.: ID, Vorname, Nachname, Anzeige-Name, E-Mail, optionale Telefonnummer, optionale organisatorische Zuordnung.
* FR-2.3: Beziehungen Person ↔ Benutzerkonto: Eine Person kann mit **0..n** Benutzerkonten verknüpft sein; ein Benutzerkonto kann mit **0..1** Person verknüpft sein.

**FR-3 Rollen & Scopes**
Der IDM-Service verwaltet Rollen in unterschiedlichen Scopes.

* FR-3.1: Rollen repräsentieren fachliche Berechtigungen, z. B. `ADMIN`, `READ_ONLY`, `IDM_ADMIN`, etc.
* FR-3.2: Ein Scope beschreibt den Kontext einer Rolle (z. B. `GLOBAL`, `APP_ORDERS`, `APP_FINANCE`).
* FR-3.3: Rollen sind einer Scope-Entität zugeordnet (Role → Scope: N:1).
* FR-3.4: Rollen können mehrfach verschiedenen Benutzerkonten zugewiesen werden (UserRoleAssignments).
* FR-3.5: Verwaltung von Rollen und Scopes über eigene Endpoints (CRUD, begrenzt in der ersten Ausbaustufe auf das Notwendige).

**FR-4 Rollen-Zuweisungen (Role Assignments)**
Zuweisung von Rollen zu Benutzerkonten.

* FR-4.1: Ein Benutzerkonto kann mehrere Rollen besitzen – auch in unterschiedlichen Scopes.
* FR-4.2: Rollen-Zuweisungen werden als eigene Entität `UserRoleAssignment` modelliert.
* FR-4.3: Optional können Gültigkeitszeiträume (`validFrom`, `validUntil`) und Metadaten (`grantedBy`, Kommentar) gepflegt werden – in der ersten Ausbaustufe mindestens das Grundmodell ohne komplexe Zeitlogik.

**FR-5 Interne IDM-Rollen & Berechtigungen**
Der IDM-Service benötigt ein eigenes internes Berechtigungsmodell für seine Verwaltungs-APIs.

* FR-5.1: Es existieren interne IDM-Rollen (z. B. `IDM_ADMIN`, `IDM_USER_MANAGER`, `IDM_READONLY`).
* FR-5.2: Diese Rollen werden in Permissions (feingranulare Berechtigungen, z. B. `USER_READ`, `USER_MANAGE`, `ROLE_MANAGE`) aufgelöst.
* FR-5.3: Interne Admin-Endpoints sind über diese Permissions gesichert (keine „God“-Superrolle ohne Einschränkungen; aber in der ersten Ausbaustufe mindestens eine starke Admin-Rolle).

**FR-6 Authentifizierung (Login) mit JWT**
Der IDM-Service stellt Endpoints zur Benutzer-Authentifizierung bereit.

* FR-6.1: Authentifizierung erfolgt typischerweise über Username/Passwort gegen interne User Accounts.
* FR-6.2: Bei erfolgreicher Authentifizierung wird ein JWT Access Token zurückgegeben.
* FR-6.3: Optional wird ein Refresh Token zurückgegeben (erste Ausbaustufe: einfache, aber sichere Variante).
* FR-6.4: Das JWT enthält Claims zu User, Person und zugewiesenen Rollen/Scopes.

**FR-7 Token-Validierung & -Verlängerung**
Der IDM-Service stellt Endpoints bereit, um Tokens zu prüfen und ggf. zu erneuern.

* FR-7.1: Endpoint zur Validierung eines Tokens (Introspection) für interne Services.
* FR-7.2: Endpoint zur Erneuerung eines Access Tokens über ein Refresh Token.
* FR-7.3: Fehlerhafte oder abgelaufene Tokens werden klar erkennbar zurückgewiesen.

**FR-8 Bereitstellung für andere Fachanwendungen**
Der IDM-Service ist als zentraler Dienst für andere Anwendungen konzipiert.

* FR-8.1: Andere Fachanwendungen nutzen den IDM-Service zur Authentifizierung von Nutzern.
* FR-8.2: Andere Anwendungen lesen keine Passwörter, sondern vertrauen auf das vom IDM ausgestellte JWT.
* FR-8.3: Der IDM-Service fungiert als zentrale Stelle für Rollenverwaltung, Rollen-Zuweisungen und User-/Person-Informationen.

**FR-9 Service-Accounts / technische Nutzer**
Neben menschlichen Nutzern muss der Service technische Accounts verwalten können.

* FR-9.1: Service-Accounts sind Benutzerkonten ohne verknüpfte Person.
* FR-9.2: Service-Accounts können eigene Rollen/Scopes erhalten (z. B. `APP_XYZ_SERVICE`).

**FR-10 API-Dokumentation**
Die bereitgestellten Endpoints sollen dokumentiert sein.

* FR-10.1: Verwendung von OpenAPI/Swagger zur Generierung einer maschinen- und menschenlesbaren API-Dokumentation.

### 1.2 Nicht-funktionale Anforderungen (NFR)

**NFR-1 Stateless Service**
Der IDM-Service ist vollständig stateless.

* NFR-1.1: Keine Verwendung von HTTP-Sessions; Authentisierung ausschließlich über JWT.
* NFR-1.2: Zustand (z. B. Tokens, Userdaten, Rollen) wird ausschließlich in persistenten Systemen (DB, optional Cache) gehalten.

**NFR-2 Deployment auf Kubernetes**
Der Service soll später (oder direkt) auf Kubernetes laufen.

* NFR-2.1: Container-fähige Spring-Boot-Anwendung.
* NFR-2.2: Bereitstellung von Health Endpoints (Liveness/Readiness) für k8s Probes.
* NFR-2.3: Konfiguration über Environment-Variablen/ConfigMaps/Secrets.

**NFR-3 Code-Qualität & Standards**
Hohe Code-Qualität hat Priorität.

* NFR-3.1: Klare Schichtung (Controller, Service, Domain, Persistence, Security).
* NFR-3.2: Unit- und Integrationstests für wesentliche Komponenten (insb. Auth, Security, Persistence).
* NFR-3.3: Code, Klassennamen, Methoden, Log-Meldungen und Fehlermeldungen in Englisch.
* NFR-3.4: Kommentare zur Erläuterung in Deutsch erlaubt.

**NFR-4 Performance & Skalierbarkeit**
Der Service muss mit wachsenden Nutzerzahlen skalieren.

* NFR-4.1: Horizontale Skalierung über mehrere Instanzen im Cluster.
* NFR-4.2: Caching für häufig gelesene Metadaten (z. B. Rollen/Scopes) ist optional einsetzbar.

**NFR-5 Erweiterbarkeit**
Das Design muss spätere Integrationen unterstützen (Keycloak, LDAP, AD, MS Entra).

* NFR-5.1: Trennung zwischen Authentifizierungs-Logik und externen Identity Providern.
* NFR-5.2: Möglichkeit, später einen externen Provider anstelle des internen User-Stores zu nutzen (Interface-basierter Ansatz).

**NFR-6 Dokumentation**

* NFR-6.1: Basis-Architektur und APIs werden dokumentiert (z. B. in einem README/Architecture Overview).

### 1.3 Sicherheitsanforderungen (SEC)

**SEC-1 Passwort-Handling**

* SEC-1.1: Passwörter werden niemals im Klartext gespeichert.
* SEC-1.2: Verwendung eines sicheren Passwort-Hashing-Algorithmus (z. B. Argon2id, BCrypt über Spring Security DelegatingPasswordEncoder).
* SEC-1.3: Möglichkeit, Hash-Algorithmen in Zukunft zu migrieren (präfix-basiertes Schema `{bcrypt}`, `{argon2}`, ...).

**SEC-2 JWT-Sicherheit**

* SEC-2.1: Signierte JWTs (bevorzugt asymmetrisch, z. B. RS256) zur Entkopplung von Signatur und Validierung.
* SEC-2.2: Access Tokens mit kurzer Laufzeit (z. B. 5–15 Minuten).
* SEC-2.3: Refresh Tokens mit längerer Laufzeit und Möglichkeit zur Revokation.
* SEC-2.4: Tokens werden über HTTPS übertragen; keine Speicherung sensibler Daten im Token.

**SEC-3 Rechtemanagement & Autorisierung**

* SEC-3.1: Autorisierung auf Basis von Rollen und Permissions.
* SEC-3.2: Nutzung von Methoden- oder Endpoint-basierten Sicherheitsannotationen (z. B. `@PreAuthorize`).
* SEC-3.3: Interne Admin-Endpunkte sind nur mit speziellen IDM-Rollen/Permissions erreichbar.

**SEC-4 Logging & Audit**

* SEC-4.1: Keine Passwörter, Tokens oder andere hochsensible Daten in Logs.
* SEC-4.2: Logging sicherheitsrelevanter Ereignisse (Logins, Fehl-Logins, Rollenänderungen, Konto-Sperrungen) in angemessener Tiefe.
* SEC-4.3: Basis-Audit-Informationen in Entities (Created/Modified By/Date).

**SEC-5 Rate Limiting / Bruteforce-Schutz (Basis)**

* SEC-5.1: Basis-Schutz vor Bruteforce-Angriffen auf Login-Endpunkt (z. B. Rate Limiting pro IP/User in der ersten Ausbaustufe als einfacher Mechanismus).

### 1.4 Technische Anforderungen & Stack (TECH)

**TECH-1 Technologie-Stack**

* TECH-1.1: Java 17+.
* TECH-1.2: Spring Boot 3.x.
* TECH-1.3: Spring Web / Spring MVC.
* TECH-1.4: Spring Security (JWT-basiert, stateless).
* TECH-1.5: Spring Data JPA mit Hibernate.
* TECH-1.6: Datenbank: z. B. PostgreSQL oder MariaDB (konfigurierbar).
* TECH-1.7: Maven als Build-System.
* TECH-1.8: Lombok zur Reduktion von Boilerplate.
* TECH-1.9: Liquibase für DB-Migrationen.

**TECH-2 DTO-Mapping**

* TECH-2.1: Einführung einer dedizierten Mapping-Lösung (z. B. MapStruct) für DTO↔Entity.
* TECH-2.2: Trennung von Request/Response-DTOs und Entities.

**TECH-3 Observability & Actuator**

* TECH-3.1: Einsatz von Spring Boot Actuator (Health, Info, ggf. Metrics).
* TECH-3.2: Health Endpoints für k8s (Liveness/Readiness).

### 1.5 Betriebsanforderungen (OPS)

**OPS-1 Containerisierung & k8s**

* OPS-1.1: Erzeugung eines lauffähigen Container-Images.
* OPS-1.2: Bereitstellung von Konfiguration über Environment-Variablen.
* OPS-1.3: Unterstützung typischer k8s Patterns (Probes, ConfigMaps, Secrets).

**OPS-2 SBOM & Security-Scanning**

* OPS-2.1: Erzeugung einer Software Bill of Materials (SBOM), z. B. mit CycloneDX Maven Plugin.
* OPS-2.2: Optionaler Einsatz von Dependency-Check/OWASP oder ähnlichen Tools im Build-Prozess.

### 1.6 Zukunftsanforderungen / Integrationsanforderungen (nur referenziert, nicht Teil der ersten Ausbaustufe)

Diese Anforderungen dienen als Orientierung, fließen aber in der ersten Ausbaustufe nur in die Architektur ein, nicht in die Umsetzung.

**FUT-1 Integration externer Identity Provider**

* FUT-1.1: Anbindung an Keycloak, Active Directory, LDAP, MS Entra.

**FUT-2 Erweiterte IDM-Funktionen**

* FUT-2.1: Self-Service (Passwort ändern, Profil bearbeiten).
* FUT-2.2: Mandantenfähigkeit (Tenants).
* FUT-2.3: Objektbezogene Berechtigungen (z. B. auf Organisationseinheitsebene).

---

## 2. Umsetzungsplan (Abbildung der Anforderungen auf die Lösung)

In diesem Kapitel wird beschrieben, wie die Anforderungen der ersten produktionsreifen Ausbaustufe umgesetzt werden sollen. Wo sinnvoll, werden Optionen aufgezeigt; Entscheidungen können später getroffen werden.

### 2.1 Architektur & Schichtung

**Abdeckung:** NFR-1, NFR-3, NFR-5, TECH-1

Vorgeschlagene Schichten:

* **API Layer (Web/REST)**: Spring Web Controller, Request-/Response-DTOs.
* **Application/Service Layer**: Fachlogik, Transaktionen, Security-bezogene Checks.
* **Domain/Persistence Layer**: JPA-Entities & Repositories.
* **Security Layer**: Konfiguration von Authentifizierung und Autorisierung (JWT, Filter, Method Security).

**Option A (klassisch):** Monolithischer Spring-Boot-Service mit klarer Package-Struktur (z. B. `auth`, `user`, `person`, `role`, `scope`, `security`, `config`, `common`).
**Option B:** Frühzeitige Aufteilung in Module (Maven Multi-Module, z. B. `idm-core`, `idm-web`, `idm-adapters`).
→ Für die erste Ausbaustufe bietet sich **Option A** an (einfacher Build, weniger Overhead), aber mit einem Architektur-Design, das Option B später ermöglicht.

### 2.2 Datenmodell & Persistenz

**Abdeckung:** FR-1 bis FR-4, FR-5, TECH-1, TECH-2, NFR-4

**Kern-Entities (erste Ausbaustufe):**

* `Person`
* `UserAccount`
* `Scope`
* `Role`
* `UserRoleAssignment`
* `Permission` (internes IDM-Rechtemodell)

**Umsetzung:**

* Verwendung von UUID als Primärschlüssel (entweder Applikations-generiert oder DB-generiert).
* Beziehungen:

  * `Person` 1:n `UserAccount` (optional verknüpft).
  * `Role` n:1 `Scope`.
  * `UserRoleAssignment` n:1 `UserAccount`, n:1 `Role`.
  * `Role` n:m `Permission`.

**Liquibase:**

* Erstellung von Changelogs pro Entität/Änderung (z. B. `01-create-person.xml`, `02-create-user-account.xml`, ...).
* Bootstrap-Daten für Basis-Rollen, Permissions und ggf. initialen Admin-User.

**Optionen:**

* **Option A:** Permissions sofort als eigene Entität einführen (saubere RBAC-Architektur).
* **Option B:** Zunächst nur Rollen, Permissions später ergänzen.
  Empfehlung: **Option A**, da die interne IDM-Absicherung ein Kernfeature ist.

### 2.3 Authentifizierung & JWT

**Abdeckung:** FR-6, FR-7, SEC-1, SEC-2, SEC-3, NFR-1

**Login-Flow (erste Ausbaustufe):**

1. Client sendet `POST /auth/login` mit Username/Passwort.
2. Service validiert Benutzer gegen `UserAccount` (inkl. Status, Passwort-Hash, ggf. Sperren).
3. Bei Erfolg: Erstellung eines Access Tokens (+ optional Refresh Token).

**Token-Inhalt (Access Token):**

* Standard-Claims: `sub`, `iat`, `exp`, `iss`, `typ`.
* Custom Claims:

  * `userId`, `username`.
  * `person` (reduzierter Satz an Person-Daten: ID, name, email).
  * `roles`: Liste der Rollen inkl. Scope.
  * `permissions`: Liste der effektiven Berechtigungen.

**Optionen für Signaturverfahren:**

* **Option A – HS256 (symmetrisch):**

  * Einfacher Start (Shared Secret in k8s Secret).
  * Nachteile: Alle validierenden Services benötigen dasselbe Secret.
* **Option B – RS256 (asymmetrisch, Public/Private Key):**

  * Private Key im IDM, Public Key in anderen Services.
  * Bessere Trennung und Sicherheit, besonders in größeren Landschaften.

Empfehlung: **Option B** für langfristige Sicherheit und k8s-Microservice-Landschaften, aber Option A kann als temporäre Lösung genutzt werden, wenn die Umgebung stark vereinfacht ist.

**Token-Refresh:**

* Erste Ausbaustufe: Einführung eines Refresh Tokens mit einer einfachen, aber sicheren Strategie:

  * Speicherung von Refresh Token-IDs in der Datenbank.
  * Endpoint `/auth/refresh` prüft Token, ID und Gültigkeit und stellt neuen Access Token aus.
  * Token Rotation kann als optionaler Schritt vorbereitet werden (Architektur berücksichtigen, ggf. Implementierung später erweitern).

### 2.4 Autorisierung & internes Rechtemanagement

**Abdeckung:** FR-5, SEC-3, SEC-4, NFR-3, NFR-5

**Ansatz:**

* Rollen und Permissions werden im IDM als Entities geführt.
* Beim Login werden effektive Permissions ermittelt (Rollen → Permissions) und ins JWT geschrieben.
* In Spring Security werden Permissions zu `GrantedAuthority`s gemappt (z. B. Präfix `PERM_`).

**Beispiele:**

* Admin-Endpunkt zum Anlegen eines Users: `@PreAuthorize("hasAuthority('PERM_USER_MANAGE')")`.
* Lese-Endpunkte: `@PreAuthorize("hasAuthority('PERM_USER_READ')")`.

**Optionen für Granularität:**

* **Option A:** Grobe Permissions (User Read/Manage, Person Read/Manage, Role Manage).
* **Option B:** Feinere Permissions (z. B. getrennt für „Create“, „Update“, „Delete“).
  Für die erste Ausbaustufe ist **Option A** ausreichend; das Modell sollte jedoch so gestaltet sein, dass Option B später möglich ist.

### 2.5 API-Design & DTOs

**Abdeckung:** FR-1 bis FR-4, FR-6 bis FR-8, FR-10, TECH-2

**Grundprinzipien:**

* Klare Trennung von Entities und DTOs.
* Versionierte REST-API (z. B. `/api/v1/...`).
* MapStruct für Mapping DTO ↔ Entity.

**Zentrale Endpoints (erste Ausbaustufe):**

* `/auth/login`, `/auth/refresh`, `/auth/introspect`.
* `/api/v1/users` (CRUD, Rollen zuweisen/entziehen).
* `/api/v1/persons` (CRUD, Verknüpfungen zu User Accounts auslesen).
* `/api/v1/roles`, `/api/v1/scopes` (mindestens Read/Basic CRUD für Admins).

**DTO-Strategie:**

* Eigene DTOs für Create/Update (z. B. `UserAccountCreateDto`, `UserAccountUpdateDto`).
* Response-DTOs, die nur relevante Felder enthalten und keine sensitiven Daten (z. B. kein Passwort-Hash).

### 2.6 Security-Implementierung & Password Handling

**Abdeckung:** SEC-1, SEC-2, SEC-5

**Passwort-Handling:**

* Einsatz des `DelegatingPasswordEncoder` in Spring Security.
* Standard-Algorithmus: z. B. BCrypt oder Argon2 (konfigurierbar).
* Beim Anlegen/Ändern von Passwörtern wird das Passwort validiert (Länge etc.) und gehasht.

**Bruteforce-Schutz:**

* Einfache Implementierung in erster Ausbaustufe:

  * Zählung von Fehlversuchen pro User/IP in-memory oder in DB.
  * Temporäre Sperrung nach X Fehlversuchen.
* Optional: Integration von Rate-Limiting-Library (z. B. Bucket4j).

### 2.7 Observability, Logging, Audit

**Abdeckung:** SEC-4, TECH-3, OPS-1

**Actuator:**

* Aktivierung von `/actuator/health`, `/actuator/info`.
* Nutzung von dedizierten Health-Indikatoren (DB-Check).

**Logging:**

* Standard-SLF4J/Logback-Konfiguration.
* Keine sensiblen Daten in Logs.
* Logging besonders für Login-Vorgänge und Admin-Aktionen (Rollenänderungen, Sperrungen).

**Audit-Felder:**

* Verwendung von Spring Data JPA Auditing für `createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate`.

### 2.8 Build Setup, SBOM, CI/CD

**Abdeckung:** OPS-2, TECH-1, NFR-3

**Build:**

* Maven-Projekt mit klaren Dependencies.
* Verwendung von Maven-Plugins:

  * CycloneDX Maven Plugin für SBOM-Erzeugung.
  * (Optional) OWASP Dependency-Check oder ähnliche Tools für Sicherheitsanalyse.

**Optionen:**

* **Option A:** SBOM nur im „verify“-Lifecycle generieren.
* **Option B:** SBOM zusätzlich als Artefakt in CI/CD speichern und automatisiert auswerten.

**Container:**

* Erstellung eines Docker/OCI-Images (z. B. per Jib oder Buildpacks).
* Image wird in Container Registry bereitgestellt und kann auf k8s ausgeliefert werden.

### 2.9 Kubernetes-Integration

**Abdeckung:** NFR-2, OPS-1

**Deployment-Grundlagen:**

* k8s Deployment mit Replikas (z. B. 2–3) für Hochverfügbarkeit.
* Service (ClusterIP/Ingress) zur Erreichbarkeit.

**Health Probes:**

* Liveness: `/actuator/health/liveness` (oder `.../health` mit Konfiguration).
* Readiness: `/actuator/health/readiness`.

**Konfiguration:**

* DB-Zugangsdaten, JWT-Keys und sonstige Secrets in k8s Secrets.
* Nicht-sensible Konfiguration (z. B. Token-Laufzeiten) über ConfigMaps.

### 2.10 Projektorganisation & erste Milestones

Vorschlag für eine erste Umsetzung in Iterationen:

1. **Iteration 1:**

   * Projekt-Setup, Basiskonfiguration, DB-Anbindung, Liquibase-Basis.
   * Entities `Person`, `UserAccount`, `Scope`, `Role`, `UserRoleAssignment`.
   * Einfache CRUD-Endpunkte für User & Person.

2. **Iteration 2:**

   * Implementierung von Auth (`/auth/login`) mit JWT-Erzeugung.
   * Integration von Spring Security, Stateless-Konfiguration.
   * Basic internes Rechtemanagement mit Rollen (ohne sehr feine Permissions).

3. **Iteration 3:**

   * Ausbau des Rollen/Scope-Modells, Einführung von Permissions.
   * Absicherung der Admin-APIs.
   * SBOM-Integration, erste k8s Deployment-Definition, Health-Endpoints.

4. **Iteration 4:**

   * Token-Refresh-Flow.
   * Logging/Auditing ausbauen.
   * Hardenings (Rate Limiting, zusätzliche Tests).

---

## 3. Ausblick: Weitere Ausbaustufen & Features

Dieses Kapitel beschreibt mögliche Erweiterungen über die erste produktive Ausbaustufe hinaus. Sie sind wichtig für die langfristige Roadmap, aber **nicht** Teil des initialen Scopes.

### 3.1 Integration externer Identity Provider

* **Keycloak-Integration:**

  * IDM-Service als „Fach-User- & Rollen-Registry“; Authentifizierung über Keycloak.
  * Synchronisation von Rollen/Benutzern zwischen IDM und Keycloak.

* **Active Directory / LDAP:**

  * Anbindung an bestehende Verzeichnisdienste für Benutzer-Authentifizierung.
  * Mapping von AD-Gruppen auf IDM-Rollen.

* **MS Entra (Azure AD):**

  * Nutzung von OIDC/OAuth2-Flows.
  * Externe Auth, interne Rollen- und Rechtevergabe im IDM.

### 3.2 Erweiterte IDM-Funktionalitäten

* **Self-Service Portal:**

  * Nutzer können ihre Profil-Daten und Passwörter selbst verwalten.
  * Password-Reset via E-Mail.

* **Mandantenfähigkeit (Tenants):**

  * Einführung einer Tenant-Entität.
  * Tenant-spezifische Rollen und Scopes.

* **Feingranulare objektbezogene Berechtigungen:**

  * z. B. Zugriff nur auf bestimmte Organisationseinheiten, Projekte oder Datenbereiche.
  * Custom `PermissionEvaluator` für Domain-Objekte.

* **Erweiterte Audit- und Compliance-Funktionen:**

  * Vollständige Audit Trails aller ändernden Aktionen mit Historisierung.

### 3.3 Security & Convenience Features

* **Passwortlose Authentifizierung:**

  * Magic Links, Einmal-Codes, WebAuthn/FIDO2.

* **Erweiterte Policies:**

  * Dynamische Policies (z. B. zeit- oder ortsabhängig).

### 3.4 Technische Erweiterungen

* **Refactoring in Module/Hexagonal Architecture:**

  * Aufteilung in Core, Adapters, Ports für noch bessere Erweiterbarkeit.

* **Mehr Observability:**

  * Metriken (Prometheus/Micrometer), verteiltes Tracing (OpenTelemetry).

* **Automatisierte Security-Checks:**

  * Integration mit Sicherheitsplattformen (Snyk, GitHub Dependabot, etc.).

---

Dieses Konzept bildet die Grundlage für die erste produktionsreife Ausbaustufe des IDM-Services. Die Anforderungen sind strukturiert und nummeriert, der Umsetzungsplan beschreibt, wie diese Anforderungen technisch umgesetzt werden können, und der Ausblick skizziert sinnvolle Erweiterungen für spätere Phasen.

