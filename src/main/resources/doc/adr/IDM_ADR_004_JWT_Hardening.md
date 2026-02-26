# IDM – ADR 004: JWT Hardening (Secret, TTL, Key Management)

Stand: 2026-02-26
Status: Accepted (Sprint 3), bestätigt und präzisiert in Sprint 4

---

## 1. Kontext

Der IDM Service stellt einen Login-Endpoint bereit und gibt signierte JWTs aus. JWTs werden serverseitig validiert und schützen technische API-Endpunkte.

Seit Sprint 4 existiert ein vollständiges Rollen- und Berechtigungsmodell. Die Autorisierung erfolgt jedoch weiterhin serverseitig datenbankbasiert und nicht über im Token gespeicherte Rollen.

Im aktuellen Stand gelten folgende Hardening-Maßnahmen verbindlich:

* JWT Secret ist extern konfiguriert (keine Hardcodes im Code)
* Token-TTL ist konfigurierbar und wird als Single Source of Truth verwendet
* JWT-Claims bleiben bewusst minimal (keine Rollen/Permissions im Token)

Dieses ADR dokumentiert verbindlich, wie JWT-Secret, TTL und das Key-Management im IDM behandelt werden.

---

## 2. Problem

Ohne klare Hardening-Regeln entstehen typischerweise folgende Risiken:

* Secrets werden in Quelltext oder Artefakten hart kodiert und damit unkontrolliert verteilt.
* Token-Laufzeiten sind inkonsistent oder schwer auditierbar.
* Key-Rotation und Umgebungs-Trennung (DEV/TEST/PROD) sind nicht definiert.
* Rollen im Token führen zu Stale-Authorization bei Rollenänderungen.
* Spätere Umstellung auf asymmetrische Signaturen wird erschwert.

---

## 3. Entscheidung

### 3.1 Secret Externalisierung (verbindlich)

* Das JWT-Secret darf **nicht** im Code stehen.
* Das JWT-Secret wird ausschließlich über Konfiguration bereitgestellt.

Verbindliche Property:

* `idm.security.jwt.secret`

Bereitstellung:

* bevorzugt über Environment-Variablen / Secret-Management
* lokal (DEV) darf ein Secret in einer Profil-Konfiguration stehen, sofern es nicht produktiv genutzt wird

---

### 3.2 TTL Policy (verbindlich)

* Die Token-Laufzeit wird konfiguriert.
* Es gibt genau eine Quelle der Wahrheit für den Ablaufzeitpunkt.

Verbindliche Property:

* `idm.security.jwt.ttl-ms`

Regel:

* Der Ablaufzeitpunkt (`exp`) im Token und das Response-Feld `expiresAt` stammen aus derselben Berechnung.

---

### 3.3 Claim-Strategie (verbindlich, auch nach Sprint 4)

Das Token enthält ausschließlich minimale Identitätsinformationen.

Verbindliche Claims:

* `sub`
* `username`
* `iat`
* `exp`

Nicht enthalten (auch nach Einführung des Rollenmodells in Sprint 4):

* Rollen
* Permissions
* Scope-spezifische Berechtigungen

Begründung:

* Rollen können sich während der Token-Laufzeit ändern.
* Serverseitige Auflösung verhindert Stale-Authorization.
* Kein Risiko durch manipulierte oder veraltete Rollen-Claims.

Die effektiven Berechtigungen werden serverseitig über das Rollen- und Permission-Modell sowie entsprechende Resolver bestimmt.

---

### 3.4 Key Management & Rotation (Zielbild)

Aktueller Stand:

* Ein einzelnes aktives Secret (symmetrisch, HS256).

Zielbild:

* definierte Rotation-Strategie
* optional parallele Validierung mehrerer Keys (z. B. via `kid`)
* perspektivisch Umstellung auf asymmetrische Signaturen (RS256), falls Token von anderen Services validiert werden

---

## 4. Konsequenzen

### 4.1 Positive Konsequenzen

* Keine Secret-Leaks durch Quelltext oder Artefakte.
* Deterministische Token-Laufzeiten.
* Saubere Trennung zwischen Authentifizierung (JWT) und Autorisierung (Rollen-/Permission-Modell).
* Keine Stale-Rollen durch Token-Caching.

### 4.2 Trade-offs

* Jede Anfrage benötigt serverseitige Rollenauflösung.
* Konfigurationsmanagement für Secrets ist verpflichtend.
* Rotation erfordert Betriebsprozesse.

---

## 5. Umsetzung (Baseline Sprint 4)

Verbindliche Referenzregeln:

* Secret wird über `idm.security.jwt.secret` injiziert.
* TTL wird über `idm.security.jwt.ttl-ms` injiziert.
* Token-Ausstellung liefert Token + `expiresAt` aus derselben Berechnung.
* Autorisierung erfolgt nicht über Token-Claims, sondern über serverseitige Rollen-/Permission-Auflösung.

---

## 6. Abgrenzung

Nicht Bestandteil dieses ADR:

* Rollenmodell
* Permission-Mapping
* Method Security
* Record-Level-Permissions

Diese Themen sind in separaten ADRs definiert.

---

## 7. Status

* Accepted (Sprint 3)
* Bestätigt und präzisiert gemäß Architekturstand Sprint 4
