# IDM_ADR_003_Auth

## Status

Proposed

---

## Kontext

Der IDM-Service benötigt eine konsistente, sichere und erweiterbare Architektur für Authentication und Authorization.

Die Anforderungen ergeben sich aus den bisherigen Diskussionen:

1. Path-Security-Trennung:

    * `/public/**` muss anonym erreichbar sein (HTTP 200).
    * `/api/**` muss Authentication erfordern (HTTP 401 ohne Token).

2. Authentication:

    * Nur definierte User-Accounts dürfen sich authentifizieren.
    * Login stellt ein signiertes JWT aus.
    * Ungültige Credentials müssen zu HTTP 401 führen.

3. Authorization:

    * Method-Level-Security mittels `@PreAuthorize("hasAuthority(...)")`.
    * Permissions müssen zentral definiert sein.
    * User besitzen Roles.
    * Roles aggregieren Permissions.
    * Das JWT muss eine roles-Claim enthalten.
    * Effektive Permissions werden zur Laufzeit aus den Roles ermittelt.

4. Architektonische Randbedingungen:

    * Keine Business-Logik in Controllern.
    * Keine Password-Hashing-Logik in Controllern.
    * Keine JWT-Signing-Logik in Controllern.
    * Domain-Services definieren Permissions, nicht Controller.

---

## Entscheidung

Authentication und Authorization werden klar getrennt behandelt:

* Authentication = Identitätsprüfung + JWT-Issuance
* Authorization = rollenbasierte Permission-Auswertung mittels Method Security

Das System folgt dem Modell:

Role → Permission → Authority

---

## Architekturüberblick

### 1. Authentication-Flow

1. Der Client ruft `POST /api/auth/login` auf.
2. `AuthController` delegiert an `UserAccountDomainService.authenticate(...)`.
3. Der Domain-Service:

    * Lädt den User aus dem Repository.
    * Prüft das Passwort mittels `PasswordEncoder`.
    * Wirft bei Fehlern eine `IllegalArgumentException`.
4. Der Controller mappt die Domain-Exception auf `BadCredentialsException`.
5. `IdmTokenService` stellt ein JWT aus.
6. Das JWT enthält:

    * `sub` (username oder userId)
    * `roles` (Array von Role-Identifiern)
    * `iat`
    * `exp`
7. Das Token wird an den Client zurückgegeben.

Authentication-Fehler werden behandelt durch:

* `RestAuthenticationEntryPoint` (401)
* `GlobalExceptionHandler` für `AuthenticationException`

---

### 2. Path Security

Konfiguriert in `HttpSecurityJwtConfig`:

* `permitAll()` für:

    * `/api/auth/login`
    * `/public/**`
    * Dokumentationsendpunkte
    * statische Ressourcen

* `authenticated()` für:

    * `/api/**`

Resultierendes Verhalten:

| Szenario                      | Ergebnis |
| ----------------------------- | -------- |
| Kein Token → `/public/**`     | 200      |
| Kein Token → `/api/**`        | 401      |
| Gültiges Token → `/public/**` | 200      |
| Gültiges Token → `/api/**`    | 200      |

---

### 3. Authorization-Modell

Authorization ist rollenbasiert, wird jedoch über Permissions technisch durchgesetzt.

#### 3.1 Permissions

Permissions werden als stabile Authority-Strings definiert.

Beispiel:

```
IDM_USER_READ
IDM_USER_CREATE
IDM_USER_DELETE
```

Permissions werden in Domain-Services verwendet mittels:

```
@PreAuthorize("hasAuthority(IdmPermissions.USER_READ)")
```

Permissions sind zentral in der Domain-Schicht definiert.

---

#### 3.2 Roles

Roles sind übergeordnete Gruppierungen von Permissions.

Beispiel:

* `IDM_ADMIN`
* `IDM_USER_MANAGER`

Role-Definitionen mappen Roles auf Permissions.

Beispiel-Mapping:

```
IDM_ADMIN → { alle IDM_* Permissions }
IDM_USER_MANAGER → { IDM_USER_READ, IDM_USER_CREATE }
```

Die Role-Definitionen sind deterministisch und initial code-basiert implementiert.

---

#### 3.3 Role → Permission Resolution

Ein `RolePermissionResolver` ermittelt die effektiven Authorities.

Input:

* Roles aus der JWT-Claim

Output:

* Menge von Permission-Authority-Strings

Der Resolver:

* Liest die roles-Claim aus dem JWT
* Schlägt die Role-Definitionen nach
* Aggregiert die Permissions
* Liefert die daraus resultierenden Granted Authorities zurück

---

### 4. JWT-Integration

Das JWT enthält ausschließlich Roles, keine Permissions.

Begründung:

* Kompakteres Token
* Zentrale Permission-Policy
* Änderungen an Permissions erfordern keine Token-Strukturänderung

Zur Laufzeit eines Requests:

1. `JwtAuthenticationFilter` validiert das Token.
2. Extrahiert:

    * subject
    * roles-Claim
3. Ruft `RolePermissionResolver` auf.
4. Erstellt ein `Authentication`-Objekt mit:

    * principal
    * aufgelösten Permission-Authorities
5. Setzt den `SecurityContext`.

Die Method Security von Spring Security wertet anschließend die Authorities aus.

---

### 5. Method Security

Method Security ist global aktiviert.

Permissions werden auf Service-Ebene durchgesetzt, nicht auf Controller-Ebene.

Beispiel:

```
@PreAuthorize("hasAuthority(IdmPermissions.USER_READ)")
public UserDto getUser(...)
```

Falls die Authority fehlt:

* `AccessDeniedException`
* Behandlung durch `RestAccessDeniedHandler`
* HTTP 403

---

## Teststrategie

### 1. Authentication-Tests

* Gültiger Login → 200 + Token
* Ungültiges Passwort → 401
* `/api/auth/me` ohne Token → 401
* `/api/auth/me` mit Token → 200

Dies sind Integration-Tests mit:

* echtem `UserAccount`
* H2-Datenbank
* echtem `PasswordEncoder`
* echter JWT-Erzeugung

Es wird kein Mocking von `UserAccount` verwendet.

---

### 2. Path-Security-Tests

Eine dedizierte Integration-Testklasse verifiziert:

* Kein Token → `/public/**` = 200
* Kein Token → `/api/**` = 401
* Gültiges Token → `/public/**` = 200
* Gültiges Token → `/api/**` = 200

---

### 3. Authorization-Tests (zukünftiger Schritt)

Nach Implementierung der Roles:

* Token mit unzureichender Role → 403
* Token mit ausreichender Role → 200
* Test auf Vorhandensein der roles-Claim im JWT
* Test der Resolver-Korrektheit

Authorization-Tests sind strikt von Authentication-Tests getrennt.

---

## Konsequenzen

### Positive Effekte

* Klare Trennung zwischen Authentication und Authorization
* Domain-getriebenes Permission-Modell
* Kompatibel mit anderen IDM-integrierten Anwendungen
* Deterministische Role → Permission Resolution
* Isoliert testbar

### Trade-offs

* Zentrale Pflege der Role-Definitionen erforderlich
* Änderungen an Permissions erfordern Deployment (bei code-basierter Definition)
* Token-TTL muss kontrolliert werden, wenn Role-Änderungen sofort wirksam sein sollen

---

## Zukünftige Erweiterungen

* Token-Versionierung pro User
* Refresh-Token-Unterstützung
* Datenbankbasierte Role-Permission-Konfiguration
* Multi-Tenant-Role-Isolation

---

## Fazit

Authentication und Authorization im IDM werden umgesetzt mittels:

* Stateless JWT Authentication
* rollenbasierter Permission-Aggregation
* authority-basierter Method Security
* strikter Path-Trennung zwischen `/public` und `/api`

Authentication wird vollständig abgeschlossen, bevor Method-Level-Authorization durchgesetzt wird.
