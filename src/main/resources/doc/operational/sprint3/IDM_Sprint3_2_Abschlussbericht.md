# IDM – Sprint 3.2 Abschlussbericht

Stand: 2026-02-23

---

## 1. Zielsetzung Sprint 3

Sprint 3 hatte das Ziel, den IDM-Service von einem rein strukturellen Projektzustand in einen lauffähigen Authentifizierungs-MVP zu überführen.

Kernziele:

1. Entfernung der Person-Domäne (MVP-Fokus: reine User-Authentifizierung)
2. Einführung einer minimalen UserAccount-Domäne
3. Implementierung eines Login-Endpoints (`/api/auth/login`)
4. JWT-Erzeugung und -Validierung
5. HTTP-Security-Schutz für `/api/**`
6. DEV- und NON-DEV-Security-Konfiguration
7. Technisches Hardening (Secret & TTL) als Abschlussbedingung

---

## 2. Umgesetzter Funktionsumfang

### 2.1 Domain – UserAccount

Umgesetzt wurde ein minimales Aggregat:

* `UserAccount`

    * `username`
    * `passwordHash`
* `UserAccountRepository`
* `UserAccountEntityService`
* `UserAccountDomainService`

Die Domain ist:

* frei von HTTP- oder JWT-Logik
* frei von Security-Framework-Abhängigkeiten
* ausschließlich für Passwort-Hashing und Credential-Prüfung zuständig

Zusätzlich wurde eine typisierte DomainException eingeführt:

* `InvalidCredentialsException`

Diese ersetzt generische `IllegalArgumentException` und verbessert die fachliche Trennung.

---

### 2.2 Auth API

Implementiert:

* `POST /api/auth/login`
* `GET /api/auth/me`

Login-Flow:

1. Controller delegiert an DomainService
2. DomainService validiert Credentials
3. `IdmTokenService` erzeugt signiertes JWT
4. Response enthält:

    * `token`
    * `tokenType`
    * `expiresAt`

`expiresAt` und Token-`exp` stammen aus derselben Berechnung (Single Source of Truth).

---

### 2.3 JWT-Technik

Implementiert:

* `JwtService` (technische Signatur + Validierung)
* `JwtAuthenticationFilter`
* `HttpSecurityJwtConfig` (NON-DEV)
* `HttpSecurityConfig` (DEV)

Pfad-Schutz:

| Pfad              | Verhalten        |
| ----------------- | ---------------- |
| `/api/auth/login` | permitAll        |
| `/public/**`      | permitAll        |
| `/api/**`         | JWT erforderlich |

Im MVP enthält das JWT ausschließlich Identitätsclaims:

* `sub`
* `username`
* `iat`
* `exp`

Keine Rollen oder Permissions (werden in Sprint 4 umgesetzt).

---

## 3. Technisches Hardening (Sprint 3.2)

Folgende Punkte wurden vor Sprint-Abschluss verpflichtend umgesetzt:

### 3.1 Secret Externalisierung

* Kein Hardcoded Secret im Code
* Konfiguration über:

    * `idm.security.jwt.secret`
* Mindestlängenprüfung für HS256

Dokumentiert in:

* ADR-004 – JWT Hardening

---

### 3.2 TTL Konfigurationsgetrieben

* Property: `idm.security.jwt.ttl-ms`
* Keine TTL-Duplikation mehr im Controller
* Ablaufzeitpunkt wird zentral berechnet

---

### 3.3 DomainException

* Einführung von `InvalidCredentialsException`
* Keine generischen Exceptions mehr für Auth-Fehler
* Saubere Schichtentrennung

---

### 3.4 Entfernte TODOs

Alle zuvor offenen TODO-/TODO-ARCH-Kommentare zu:

* Secret-Externalisierung
* TTL-Externalisierung
* Ablaufzeitpunkt-Delegation

wurden entfernt bzw. in deklarative Beschreibung überführt.

---

## 4. Tests

Integrationstests decken ab:

* Erfolgreicher Login
* Login mit falschem Passwort → 401
* Zugriff ohne Token → 401
* Zugriff mit gültigem Token → 200

Tests laufen unverändert erfolgreich.

---

## 5. Nicht Bestandteil von Sprint 3

Explizit nicht umgesetzt:

* Rollenmodell
* Permission-Mapping
* `@PreAuthorize` Method Security
* RolePermissionResolver
* Key-Rotation
* Asymmetrische Signatur (RS256)
* Liquibase-Aktivierung

Diese Themen werden in Sprint 4 bzw. späteren ADRs behandelt.

---

## 6. Architekturstatus nach Sprint 3

Der IDM-Service besitzt nun:

* Klare Schichtentrennung (Controller / Domain / System)
* Deterministische JWT-Sicherheitsarchitektur
* Konfigurationsgetriebenes Secret-Handling
* Testabgedeckte Authentifizierung
* Keine fachlich überflüssige Person-Domäne

Technisch ist das System ein stabiler, minimaler Auth-MVP.

---

## 7. Status

**Sprint 3 – Abgeschlossen**

Abschlussbedingung erfüllt:

* Funktional lauffähig
* Tests grün
* Keine offenen sicherheitsrelevanten TODOs
* Hardening dokumentiert (ADR-004)

---

## 8. Ausblick – Sprint 4

Sprint 4 wird behandeln:

* EnableMethodSecurity
* Rollen- und Permission-Modell
* RolePermissionResolver
* JWT-Claim-Erweiterung (roles)
* `@PreAuthorize` Integration

Bezug: ADR-003
