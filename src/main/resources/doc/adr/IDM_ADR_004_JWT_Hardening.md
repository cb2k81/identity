# IDM – ADR 004: JWT Hardening (Secret, TTL, Key Management)

Stand: 2026-02-23

## 1. Kontext

Der IDM Service stellt im MVP (Sprint 3) einen Login-Endpoint bereit und gibt signierte JWTs aus. JWTs werden serverseitig validiert und schützen technische API-Endpunkte.

Im Sprint-3-Stand wurden folgende Hardening-Maßnahmen technisch umgesetzt:

* JWT Secret ist extern konfiguriert (keine Hardcodes im Code)
* Token-TTL ist konfigurierbar und wird als Single Source of Truth verwendet
* JWT-Claims im MVP sind minimal (keine Rollen/Permissions)

Dieses ADR dokumentiert verbindlich, wie JWT-Secret, TTL und das Key-Management (Hardening) im IDM behandelt werden.

## 2. Problem

Ohne klare Hardening-Regeln entstehen typischerweise folgende Risiken:

* Secrets werden in Quelltext oder Artefakten hart kodiert und damit unkontrolliert verteilt.
* Token-Laufzeiten sind inkonsistent (z. B. doppelt berechnet) oder schwer auditierbar.
* Key-Rotation und Umgebungs-Trennung (DEV/TEST/PROD) sind nicht definiert.
* Spätere Umstellung auf asymmetrische Signaturen (Microservice-Landschaft) wird erschwert.

## 3. Entscheidung

### 3.1 Secret Externalisierung (verbindlich)

* Das JWT-Secret darf **nicht** im Code stehen.
* Das JWT-Secret wird **ausschließlich** über Konfiguration bereitgestellt.

Verbindliche Property:

* `idm.security.jwt.secret`

Bereitstellung:

* bevorzugt über Environment-Variablen / Secret-Management (z. B. Kubernetes Secret, Vault)
* lokal (DEV) kann ein Secret in `application-dev.yml` stehen, sofern es **nicht** produktiv genutzt wird

### 3.2 TTL Policy (verbindlich)

* Die Token-Laufzeit wird konfiguriert.
* Es gibt genau **eine** Quelle der Wahrheit für den Ablaufzeitpunkt.

Verbindliche Property:

* `idm.security.jwt.ttl-ms`

Regel:

* Der Ablaufzeitpunkt (`exp`) im Token und das Response-Feld `expiresAt` müssen aus derselben Berechnung stammen.

### 3.3 MVP Claim Set (Sprint 3)

Im Sprint-3-MVP enthält das Token ausschließlich minimale Identitätsinformationen.

Verbindliche Claims (MVP):

* `sub` (technische Identität)
* `username`
* `iat` (automatisch durch JWT-Build)
* `exp`

Nicht enthalten (Sprint 3):

* Rollen
* Permissions

> Hinweis: Rollen/Permissions und Method Security werden in ADR-003 behandelt und in Sprint 4 umgesetzt.

### 3.4 Key Management & Rotation (Zielbild)

Für produktive Umgebungen ist Key-Rotation erforderlich.

MVP (Sprint 3):

* Ein einzelnes aktives Secret (symmetrisch, HS256).

Zielbild (nach MVP):

* definierte Rotation-Strategie
* optional: parallele Validierung mehrerer Keys (Key-ID / `kid`)
* perspektivisch Umstellung auf asymmetrische Signaturen (RS256), falls Token von anderen Services validiert werden sollen

## 4. Konsequenzen

### 4.1 Positive Konsequenzen

* Keine Secret-Leaks durch Quelltext oder Artefakte.
* Deterministische Token-Laufzeiten (auditierbar).
* Saubere Trennung: Auth-Modell (ADR-003) vs. Hardening/Key-Management (ADR-004).
* Umstellung auf professionelles Key-Management ist vorbereitet.

### 4.2 Negative Konsequenzen / Trade-offs

* Konfigurationsmanagement wird verpflichtend (Secrets müssen in jeder Umgebung bereitgestellt werden).
* Rotation erfordert zusätzliche Betriebsprozesse.

## 5. Umsetzung (Sprint 3 – Referenz)

Verbindliche Referenzregeln:

* Secret wird über `idm.security.jwt.secret` injiziert.
* TTL wird über `idm.security.jwt.ttl-ms` injiziert.
* Token-Ausstellung liefert Token + `expiresAt` aus derselben Berechnung.

## 6. Abgrenzung

Nicht Bestandteil dieses ADR:

* Rollenmodell
* Permission-Mapping
* Method Security (`@PreAuthorize`)

Diese Themen sind in ADR-003 definiert und werden in Sprint 4 umgesetzt.

## 7. Status

* **Accepted** (Sprint 3 Abschlussbedingung)
