# IDM – Sprint 4.1 Planung

Stand: 2026-02-23

---

## 1. Ausgangssituation nach Sprint 3

Nach Abschluss von Sprint 3 besitzt der IDM-Service:

* Eine minimal funktionsfähige UserAccount-Domäne
* Einen Login-Endpoint mit JWT-Ausgabe
* JWT-Validierung auf HTTP-Ebene (`/api/**` geschützt)
* Konfigurationsgetriebenes Secret- und TTL-Handling (ADR-004)
* Keine Rollen oder Permissions im Token
* Keine Method Security (`@PreAuthorize` wird aktuell nicht genutzt)

Die Architektur ist schichtensauber, aber Autorisierung ist noch rein technisch ("authenticated or not").

Sprint 4 führt nun das fachliche Autorisierungsmodell ein.

---

## 2. Zielsetzung Sprint 4

Sprint 4 implementiert das in ADR-003 definierte Autorisierungsmodell.

Kernziele:

1. Aktivierung von Method Security
2. Einführung eines Rollenmodells
3. Einführung eines Permission-Modells
4. Role → Permission Resolution
5. Erweiterung des JWT um Rollen-Claims
6. Integration von `@PreAuthorize` in ausgewählten Use Cases

Nicht Bestandteil von Sprint 4:

* Key-Rotation
* Asymmetrische JWT-Signatur
* Refresh Tokens
* Mandantenfähigkeit
* UI/Admin-Oberfläche

---

## 3. Architekturgrundlage (Referenz Textexport)

Gemäß Implementierungskonzept gilt:

* Domain Layer enthält fachliche Regeln
* API Layer orchestriert Use Cases
* Security Layer ist querschnittlich
* JWT enthält Rollen, keine Permissions
* Permissions werden zur Laufzeit aufgelöst

Diese Struktur bleibt unverändert.

---

## 4. Zielarchitektur Sprint 4

### 4.1 EnableMethodSecurity

* Aktivierung von `@EnableMethodSecurity`
* Keine Verwendung veralteter GlobalMethodSecurity-Ansätze
* Autorisierung erfolgt über `hasAuthority(...)`

---

### 4.2 Rollenmodell

Einführung stabiler Rollen-Identifier, z. B.:

* `IDM_ADMIN`
* `IDM_USER_MANAGER`

Rollen sind:

* Statische, codebasierte Definitionen (MVP)
* Nicht datenbankgetrieben in Sprint 4

---

### 4.3 Permission-Modell

Permissions sind feingranulare Authorities, z. B.:

* `IDM_USER_READ`
* `IDM_USER_CREATE`
* `IDM_USER_DELETE`

Permissions werden nicht im Token gespeichert.

---

### 4.4 RolePermissionResolver

Ein technischer Resolver übernimmt:

Input:

* Rollen aus JWT-Claim `roles`

Output:

* Effektive Permission-Authorities

Ablauf im Request:

1. `JwtAuthenticationFilter` validiert Token
2. Rollen werden extrahiert
3. Resolver ermittelt Permissions
4. Authentication wird mit berechneten Authorities gesetzt
5. `@PreAuthorize` wertet Authorities aus

---

### 4.5 JWT-Erweiterung

Sprint 4 ergänzt das Token um:

* `roles` (Array von Rollen-Identifiern)

Keine Permissions im Token.

---

## 5. Arbeitspakete

### EPIC 1 – Security-Basis

* E1.1 `@EnableMethodSecurity` Konfiguration
* E1.2 Sicherstellen, dass NON-DEV-Konfiguration kompatibel bleibt

---

### EPIC 2 – Rollen- & Permission-Definition

* E2.1 `IdmRoles` Konstante-Klasse
* E2.2 `IdmPermissions` Konstante-Klasse
* E2.3 Mapping Role → Permission (codebasiert)

---

### EPIC 3 – Resolver & Filter-Integration

* E3.1 Implementierung `RolePermissionResolver`
* E3.2 Erweiterung `JwtAuthenticationFilter`
* E3.3 Authority-Befüllung im SecurityContext

---

### EPIC 4 – JWT Claim Erweiterung

* E4.1 `IdmTokenService` erweitert Claims um `roles`
* E4.2 Integrationstests anpassen

---

### EPIC 5 – Method Security Integration

* E5.1 Dummy-geschützter Endpoint
* E5.2 Verwendung von `@PreAuthorize("hasAuthority(...)")`
* E5.3 Negative/Positive Tests

---

## 6. Definition of Done

Sprint 4 gilt als abgeschlossen, wenn:

* `@PreAuthorize` technisch aktiv ist
* Rollen im JWT enthalten sind
* Permissions korrekt aufgelöst werden
* Zugriff ohne erforderliche Permission → 403
* Zugriff mit Permission → 200
* Tests grün

---

## 7. Risiken

* Fehlende Trennung zwischen Domain- und Security-Logik
* Zu frühe Datenbankmodellierung für Rollen
* Vermischung von ADR-003 und ADR-004 Themen

---

## 8. Abschlussbedingung

Sprint 4 endet mit einem klar dokumentierten Autorisierungsmodell gemäß ADR-003 und testabgedeckter Method Security.
