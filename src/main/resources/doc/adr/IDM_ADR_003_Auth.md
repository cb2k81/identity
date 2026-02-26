# IDM – ADR 003: Authentication & Authorization Architecture

Stand: 2026-02-26
Status: Accepted (Sprint 4 – aktualisiert gemäß Baseline)

---

## 1. Kontext

Der IDM-Service benötigt eine konsistente, sichere und erweiterbare Architektur für Authentication und Authorization.

Die Anforderungen ergeben sich aus der umgesetzten Architektur in Sprint 3 und 4:

1. Path-Security-Trennung
2. JWT-basierte Authentication
3. Rollen- und Permission-basiertes Authorization-Modell
4. Strikte Schichtenkonformität (DDD)

Wesentliche Korrektur gegenüber früheren Annahmen:

> JWT enthält **keine Roles-Claim**.
> Rollen und Permissions werden serverseitig datenbankbasiert aufgelöst.

---

## 2. Grundprinzip

Authentication und Authorization sind strikt getrennt:

* Authentication = Identitätsprüfung + JWT-Issuance
* Authorization = serverseitige Rollen-/Permission-Auswertung mittels Method Security

Das System folgt dem Modell:

User → Roles → Permissions → GrantedAuthority

Die effektiven Authorities entstehen ausschließlich serverseitig.

---

## 3. Path Security

Konfiguriert in `HttpSecurityJwtConfig`.

### Erlaubt ohne Authentication:

* `/public/**`
* `/api/auth/login`
* Dokumentationsendpunkte
* statische Ressourcen

### Authentication erforderlich:

* `/api/**`

Resultierendes Verhalten:

| Szenario                      | Ergebnis |
| ----------------------------- | -------- |
| Kein Token → `/public/**`     | 200      |
| Kein Token → `/api/**`        | 401      |
| Gültiges Token → `/public/**` | 200      |
| Gültiges Token → `/api/**`    | 200      |

---

## 4. Authentication-Architektur

### 4.1 Login-Flow

1. Client ruft `POST /api/auth/login` auf.
2. Controller delegiert an einen Authentication-Service.
3. Der Service:

   * Lädt den User aus dem Repository.
   * Prüft Passwort mittels `PasswordEncoder`.
   * Prüft User-State.
4. Bei Fehler → `AuthenticationException`.
5. `IdmTokenService` erstellt JWT.
6. Token wird zurückgegeben.

### 4.2 JWT-Inhalt

Das Token enthält ausschließlich minimale Identitätsinformationen:

* `sub`
* `username`
* `iat`
* `exp`

Nicht enthalten:

* Rollen
* Permissions
* Scope-Zuordnungen

Begründung:

* Rollen können sich während Token-Laufzeit ändern.
* Keine Stale-Authorization.
* Token bleibt kompakt.
* Autorisierung ist zentral steuerbar.

Diese Entscheidung ist verbindlich und konsistent mit ADR-004.

---

## 5. Authorization-Modell

Authorization ist datenbankbasiert und rollengetrieben.

### 5.1 Persistenzmodell

* Roles sind ApplicationScope-gebunden.
* Permissions sind ApplicationScope-gebunden.
* RolePermissionAssignments definieren Role → Permission.
* UserRoleAssignments definieren User → Role.

Alle Zuordnungen sind Bestandteil des Bootstrap-Modells (ADR-006).

---

### 5.2 Runtime-Resolution

Bei jedem Request mit gültigem JWT:

1. `JwtAuthenticationFilter` validiert das Token.
2. Subject wird extrahiert.
3. User wird aus Datenbank geladen.
4. Rollen des Users werden geladen.
5. Permissions der Rollen werden geladen.
6. Permissions werden in `GrantedAuthority` gemappt.
7. `Authentication` wird im `SecurityContext` gesetzt.

Es erfolgt **keine Auswertung von Rollen aus dem Token**.

---

## 6. Method Security

Method Security ist global aktiviert.

Permissions werden ausschließlich auf Service-Ebene geprüft.

Beispiel:

```
@PreAuthorize("hasAuthority('IDM_USER_READ')")
```

Bei fehlender Authority:

* `AccessDeniedException`
* HTTP 403

Controller enthalten keine Business- oder Security-Logik.

---

## 7. Teststrategie

### 7.1 Authentication-Tests

* Gültiger Login → 200 + Token
* Ungültige Credentials → 401
* `/api/**` ohne Token → 401

Integrationstests mit:

* echter H2-Datenbank
* echtem PasswordEncoder
* echter JWT-Erzeugung

Kein Mocking von UserAccount.

---

### 7.2 Path-Security-Tests

Separate Integration-Testklasse verifiziert:

* `/public/**` ohne Token → 200
* `/api/**` ohne Token → 401
* `/api/**` mit Token → 200

---

### 7.3 Authorization-Tests

* User ohne Role → 403
* User mit Role → 200
* Prüfung effektiver Permission-Auflösung

Authorization-Tests sind strikt getrennt von Authentication-Tests.

---

## 8. Architektonische Leitplanken

* Keine Business-Logik in Controllern.
* Keine JWT-Signing-Logik in Controllern.
* Keine Password-Hashing-Logik in Controllern.
* Keine Repository-Nutzung in Security-Filtern.
* Rollen und Permissions sind Domain-Objekte, keine Security-Konstanten.

---

## 9. Konsequenzen

### Positive Effekte

* Klare Trennung Authentication vs. Authorization
* Kein Rollen-Drift durch Token-Caching
* Vollständig serverseitige Autorisierung
* Stage-isoliertes Rollenmodell
* Testbar und deterministisch

### Trade-offs

* Jede Anfrage benötigt Datenbankzugriffe zur Rollenauflösung
* Kein rein stateless Role-Claim-Modell

---

## 10. Zukunft

* Caching von Permission-Resolution
* Token-Versionierung pro User
* Refresh-Token-Unterstützung
* Record-Level-Permissions
* Multi-Tenant-Isolation

---

## Fazit

Authentication im IDM ist JWT-basiert und minimalistisch.

Authorization erfolgt vollständig serverseitig über das persistierte Rollen- und Permission-Modell.

JWT enthält bewusst keine Rollen oder Permissions.

Die Architektur ist konsistent mit ADR-004 (JWT Hardening) und ADR-006 (Bootstrap).
