# IDM – Sprint 5 Planung

Stand: 2026-02-26
Sprint: 5
Ziel: Produktivfähiger MVP des IDM-Services
Status: Geplant

---

# 1. Zielsetzung des Sprints

Sprint 5 verfolgt das Ziel, den IDM-Service von einer architektonisch stabilen Sicherheitsbasis (Sprint 3–4) zu einem produktiv einsetzbaren MVP weiterzuentwickeln.

Produktiv-MVP bedeutet:

* Vollständige CRUD-Interaktion mit allen relevanten Aggregaten
* Konsistente, REST-basierte Management-API
* Architekturreine Umsetzung gemäß ADR-003, ADR-004, ADR-006
* Spring Boot Best Practices (Validation, Exception Handling, Security-Konfiguration)
* Minimale Security-Härtung für produktiven Betrieb
* Testabdeckung auf Integrationsniveau

Nicht Ziel dieses Sprints:

* UI-Entwicklung
* Multi-Tenant-Architektur
* Asymmetrische JWT-Signaturen
* Performance-Optimierung (Caching etc.)
* Record-Level-Permissions

---

# 2. Architektonische Leitplanken (verbindlich)

Alle Umsetzungen in Sprint 5 müssen:

1. Die ADRs einhalten:

    * ADR-003 – Authentication & Authorization
    * ADR-004 – JWT Hardening
    * ADR-006 – Bootstrap

2. DDD-Schichten strikt respektieren:

    * Controller → Application/Entity Services → Repositories
    * Keine Business-Logik in Controllern
    * Keine Repository-Nutzung in Security-Filtern
    * Keine Security-Logik in Domain-Entities

3. Spring Boot Best Practices berücksichtigen:

    * Bean Validation (jakarta.validation)
    * Global Exception Handling
    * Klare HTTP-Statuscodes
    * Saubere DTO-Trennung
    * OpenAPI-Dokumentation konsistent

---

# 3. Phasenmodell Sprint 5

Sprint 5 wird in drei Phasen strukturiert.

---

# Phase 1 – Vollständige IDM Management API

## Ziel

Bereitstellung einer vollständigen REST-API zur Verwaltung aller IDM-Aggregate.

## 1.1 UserAccount Use-Cases

Pflicht-Operationen:

* Create User
* Read User (Single)
* List Users
* Update User (State, ggf. Metadaten)
* Change Password
* Enable / Disable User
* Delete User
* Assign Role
* Remove Role
* Assign ApplicationScope
* Remove ApplicationScope

API-Pfad (Beispiel):

```
/api/idm/users
```

---

## 1.2 Role Use-Cases

* Create Role
* Read Role
* List Roles
* Update Role (Description)
* Delete Role
* Assign Permission
* Remove Permission

```
/api/idm/roles
```

---

## 1.3 Permission Use-Cases

* Create Permission
* Read Permission
* List Permissions
* Update Permission (Description)
* Delete Permission

```
/api/idm/permissions
```

---

## 1.4 PermissionGroup Use-Cases

* CRUD

---

## 1.5 ApplicationScope Use-Cases

* Create Scope
* Read Scope
* List Scopes
* Update Description
* Delete Scope (mit Validierungsregeln)

---

## 1.6 Security-Anforderungen Phase 1 (korrigiert, baseline-konform)

* Endpunkte ausschließlich über Permissions (Authorities) geschützt.
* Keine codebasierte Sonderrolle („God-Role“) im Sinne von Sonderlogik.
  *Hinweis:* Eine Rolle wie `IDM_ADMIN` kann im System existieren, ist aber nur Datenkonfiguration (Role → Permission Assignments), keine Sonderbehandlung im Code.

### 1.6.1 Verbindliche Authority-Namen für IDM-Administration

Für Method-Security (`@PreAuthorize`) werden die Authority-Strings aus `de.cocondo.app.domain.idm.authorities.IdmManagementAuthorities` verwendet.

**ApplicationScope**

* `IDM_SCOPE_CREATE`
* `IDM_SCOPE_READ`
* `IDM_SCOPE_UPDATE`
* `IDM_SCOPE_DELETE`

**Role**

* `IDM_ROLE_CREATE`
* `IDM_ROLE_READ`
* `IDM_ROLE_UPDATE`
* `IDM_ROLE_DELETE`

**Permission**

* `IDM_PERMISSION_CREATE`
* `IDM_PERMISSION_READ`
* `IDM_PERMISSION_UPDATE`
* `IDM_PERMISSION_DELETE`

**Assignments**

* `IDM_ROLE_PERMISSION_ASSIGN`
* `IDM_ROLE_PERMISSION_UNASSIGN`
* `IDM_USER_ROLE_ASSIGN`
* `IDM_USER_ROLE_UNASSIGN`

**UserAccount**

* `IDM_USER_CREATE`
* `IDM_USER_READ`
* `IDM_USER_UPDATE`
* `IDM_USER_DELETE`

> Wichtig: Authority-Namen der Form `IDM_*_MANAGE` werden im Code nicht verwendet und sind daher **kein** Planungsziel.

### 1.6.2 Bootstrap-Stand (Status quo)

Der Bootstrap legt systemProtected Permissions und eine Rolle `IDM_ADMIN` an und weist dieser Rolle die aktuell im Bootstrap definierten IDM-Management-Permissions zu.

Der Status quo (Bootstrap) umfasst u. a.:

* `IDM_SCOPE_CREATE`, `IDM_SCOPE_READ`, `IDM_SCOPE_UPDATE`, `IDM_SCOPE_DELETE`
* `IDM_PERMISSION_CREATE`, `IDM_PERMISSION_DELETE`
* `IDM_ROLE_CREATE`, `IDM_ROLE_DELETE`
* `IDM_ROLE_PERMISSION_ASSIGN`, `IDM_ROLE_PERMISSION_UNASSIGN`
* `IDM_USER_CREATE`, `IDM_USER_READ`, `IDM_USER_UPDATE`, `IDM_USER_DELETE`
* `IDM_USER_ROLE_ASSIGN`, `IDM_USER_ROLE_UNASSIGN`

**Hinweis:** Falls in Phase 1 Endpunkte umgesetzt werden, die auf Authorities basieren, die im Bootstrap noch nicht als Permission-Daten vorhanden sind (z. B. `IDM_ROLE_READ/UPDATE`, `IDM_PERMISSION_READ/UPDATE`), wird die Datenkonfiguration (Bootstrap) entsprechend ergänzt – ohne Änderung der Authority-Namen.

---

## Definition of Done – Phase 1

* Alle CRUD-Endpunkte implementiert
* DTO-Schicht sauber getrennt
* Bean Validation aktiv
* Integrationstests für alle Use-Cases
* Keine Architekturverletzungen

---

# Phase 2 – Security Hardening (Produktiv-MVP Minimum)

## Ziel

Erhöhung der Betriebssicherheit.

---

## 2.1 Bean Validation vollständig aktivieren

* Hibernate Validator integrieren
* DTO-Validierungen für:

    * Username
    * Passwort (Mindestlänge etc.)
    * Pflichtfelder

---

## 2.2 Passwort-Policy (Minimal)

* Mindestlänge
* Konfigurierbar
* Serverseitige Durchsetzung

---

## 2.3 Account-Lock-Mechanismus

* Zähler für Fehlversuche
* Temporäre Sperre nach X Fehlversuchen
* Konfigurierbare Parameter

---

## 2.4 Fehler- und Exception-Strategie

* Einheitliches Error-Response-Format
* Domain-Exceptions → HTTP Mapping
* Keine technischen Stacktraces nach außen

---

## 2.5 OpenAPI-Konsolidierung

* Vollständige Dokumentation aller Management-Endpunkte
* Security-Schema korrekt definiert

---

## Definition of Done – Phase 2

* Validation Provider aktiv
* Passwort-Policy getestet
* Locking getestet
* Einheitliche Fehlerstruktur
* Security-Integrationstests erweitert

---

# Phase 3 – Minimal Audit & Betriebsfähigkeit

## Ziel

Sicherstellen, dass produktiv relevante Sicherheitsereignisse nachvollziehbar sind.

---

## 3.1 Audit von Sicherheitsereignissen

Mindestens:

* Login erfolgreich
* Login fehlgeschlagen
* User gesperrt
* Role-Zuweisung geändert
* Permission-Zuweisung geändert

Integration mit bestehender DomainEvent-Infrastruktur.

---

## 3.2 Technische Betriebsstabilität

* Konfigurationsprüfung beim Start
* Validierung der Self-Scope-Konfiguration
* Fail-Fast bei inkonsistenter Security-Konfiguration

---

## 3.3 Dokumentationsabgleich

* Fachkonzept aktualisieren
* Implementierungskonzept aktualisieren
* ADR-Kohärenz prüfen

---

## Definition of Done – Phase 3

* Audit-Ereignisse persistiert
* Audit-Integrationstests vorhanden
* Dokumentation vollständig konsistent

---

# 4. Risiken

* Komplexitätsanstieg durch vollständige Management-API
* Unbeabsichtigte Architekturverletzungen bei CRUD-Implementierung
* Performance bei Permission-Resolution

---

# 5. Erfolgskriterien Sprint 5

Sprint 5 gilt als erfolgreich abgeschlossen, wenn:

* Alle IDM-Aggregate über REST vollständig verwaltbar sind
* Sicherheitsmechanismen produktionsreif sind
* Keine Architekturverletzungen vorliegen
* Alle Tests grün sind
* Dokumentation konsistent zur Code-Baseline ist

---

# 6. Zusammenfassung

Sprint 5 transformiert das IDM von einer architektonisch stabilen Sicherheitsbasis zu einem produktiv einsetzbaren Identity-Management-MVP.

Die Umsetzung erfolgt strikt ADR-konform, DDD-konform und gemäß Spring Boot Best Practices.
