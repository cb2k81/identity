# IDM – Sprint 5 Zwischenanalyse (UseCases & Architektur)

Stand: 2026-02-27
Baseline: idm_code-export_2026-02-27_11-05-59

---

# 1. Zielbild Sprint 5

Am Ende von Sprint 5 sollen alle IDM-UseCases:

* vollständig implementiert sein
* über alle Schichten sauber geführt werden
* einem einheitlichen Architekturstandard folgen
* konsistente Security-Absicherung besitzen
* konsistent über Facade → Handler → EntityService → Repository laufen

Zielarchitektur:

Controller
→ DomainService / Facade
→ UseCase-Handler
→ EntityService
→ Repository

Keine Businesslogik im Controller.
Keine Security-Logik in Entities.
Method-Security ausschließlich auf UseCase-/Domain-Service-Ebene.

---

# 2. Aktuell vollständig umgesetzte UseCases

## 2.1 UserAccount

Status: Architektonisch konsistent

Implementiert:

* Create User
* Get User
* List Users
* Activate / Deactivate
* Change Password
* Delete User

Architektur:

* Controller delegiert an UserAccountDomainService
* Rechteprüfung auf Domain-Service-Ebene
* Keine direkte Repository-Nutzung im Controller

Bewertung: ✔ sauber umgesetzt

---

## 2.2 ApplicationScope

Implementierte Handler (UseCase-Ebene vorhanden):

* CreateApplicationScopeHandler
* ReadApplicationScopeHandler
* ListApplicationScopesHandler
* UpdateApplicationScopeHandler
* DeleteApplicationScopeHandler

Facade-Integration:

* `de.cocondo.app.domain.idm.permission.PermissionManagementDomainService` stellt bereits die Methoden `create/list/read/update/deleteApplicationScope(...)` bereit und delegiert an die Handler.

REST-API-Status (Ist-Zustand Baseline):

* `de.cocondo.app.domain.idm.scope.ApplicationScopeController` exponiert aktuell **nur** `GET /api/idm/scopes`.
* Der Controller ruft dabei **direkt** den `ApplicationScopeEntityService` auf (DTO-Mapping im Controller) und enthält **@PreAuthorize**.

Bewertung:

* UseCase-Stack ist für ApplicationScope grundsätzlich vorhanden.
* Die REST-Schicht ist **noch nicht** architekturkonform (Controller muss über Facade/Handler laufen).

---

## 2.3 Role ↔ Permission Assignment

Implementiert:

* AssignPermissionToRoleHandler
* UnassignPermissionFromRoleHandler

Enthält:

* Scope-Konsistenzprüfung
* Duplikatsprüfung

Bewertung: ✔ korrekt auf UseCase-Ebene umgesetzt

---

## 2.4 User ↔ Role Assignment

Implementiert:

* AssignRoleToUserHandler
* UnassignRoleFromUserHandler

Bewertung: ✔ funktional vorhanden

---

# 3. Teilweise vorbereitete UseCases

## 3.1 Role

Vorhanden:

* CreateRoleHandler
* DeleteRoleHandler

Fehlend:

* Read Role (by id)
* List Roles (je Scope)
* Update Role

Architekturstatus:

* CRUD nicht vollständig
* Nicht alle Authorities werden aktiv genutzt

---

## 3.2 Permission

Vorhanden:

* CreatePermissionHandler
* DeletePermissionHandler

Fehlend:

* Read Permission
* List Permissions
* Update Permission

---

## 3.3 PermissionGroup

Vorhanden:

* CreatePermissionGroupHandler

Fehlend:

* Read
* List
* Update
* Delete

---

## 3.4 User ↔ ApplicationScope Assignment

Vorhanden:

* Entity
* Repository
* EntityService
* Bootstrap-Verwendung

Fehlend:

* AssignApplicationScopeToUserHandler
* UnassignApplicationScopeFromUserHandler
* REST-Endpunkte

Bewertung:
Funktional wichtig für MVP, aktuell nicht als UseCase implementiert.

---

# 4. Architekturanalyse

## 4.1 Einheitlichkeit

Nicht vollständig gegeben.

Inkonsistenzen:

* ApplicationScope List läuft noch nicht über Handler/Fassade.
* Einige Aggregate haben nur Teil-CRUD.

## 4.2 Security-Verortung

Ist-Zustand (Baseline):

* Viele UseCase-Handler sind per `@PreAuthorize` abgesichert.
* Der `UserAccountController` selbst ist *nicht* per `@PreAuthorize` gesichert; die Absicherung erfolgt im `UserAccountDomainService` (wie im Kommentar im Controller beschrieben).
* Der `ApplicationScopeController` ist hingegen *direkt* per `@PreAuthorize` gesichert (Abweichung vom UserAccount-Pattern).

Ziel (einheitlicher Standard Sprint 5):

* **Keine** `@PreAuthorize`-Annotationen in Controllern.
* Method-Security ausschließlich in DomainServices/Facades oder UseCase-Handlern (ein konsistentes Pattern wählen und überall identisch anwenden).

---

# 5. Offene funktionale Punkte für Sprint 5

## 5.1 Vollständige CRUD-Umsetzung

ApplicationScope

* Controller vollständig auf Facade umstellen

Role

* Read
* List
* Update

Permission

* Read
* List
* Update

PermissionGroup

* Read
* List
* Update
* Delete

User ↔ ApplicationScope

* Assign
* Unassign

---

# 6. Offene architektonische Punkte

1. Einheitlicher Query-Standard (Handler vs Controller)
2. Vollständige Delegation über Facade
3. Konsistentes Logging auf UseCase-Ebene
4. Konsistentes Error-Handling (IllegalArgument vs DomainException)
5. Prüfung, ob alle Authorities im Bootstrap konfiguriert sind

---

# 7. Bewertung des aktuellen Reifegrads

Domain-Design: stabil
Security-Konzept: konsistent
UseCase-Architektur: teilweise inkonsistent
CRUD-Vollständigkeit: unvollständig
Produktions-MVP: noch nicht vollständig erreicht

---

# 8. Zielzustand am Ende von Sprint 5

Alle IDM-Aggregate besitzen:

* Vollständige CRUD-UseCases
* REST-API-Endpunkte
* Delegation über Facade
* Method-Security auf UseCase-Ebene
* Keine direkten EntityService-Aufrufe im Controller
* Konsistentes Error-Handling

Erst dann gilt Sprint 5 Phase 1 als architektonisch abgeschlossen.

---

# 9. Verbindliche Umsetzungsreihenfolge (deterministisch)

Die folgende Reihenfolge ist so gewählt, dass:

* Architekturinkonsistenzen früh bereinigt werden
* Abhängigkeiten zwischen Aggregaten berücksichtigt sind
* Jeder Schritt einen abgeschlossenen, testbaren Zustand erzeugt
* Keine parallelen Architekturvarianten entstehen

## Phase 1 – Architektur-Stabilisierung (Technische Konsolidierung)

1. ApplicationScope-Controller vollständig auf Facade umstellen

    * List über ListApplicationScopesHandler
    * Read / Create / Update / Delete ausschließlich über Facade
    * Keine direkte EntityService-Nutzung im Controller

2. Einheitlicher Security-Standard

    * Entfernen von @PreAuthorize aus Controllern
    * Sicherstellen, dass jede Operation ausschließlich im Handler abgesichert ist

3. Logging-Standard festlegen

    * Logging ausschließlich im UseCase-Handler
    * Keine fachlichen Logs im Controller

Abschlusskriterium Phase 1:
ApplicationScope ist vollständig und architekturkonform über alle Schichten geführt.

---

## Phase 2 – Vollständige CRUD-Umsetzung je Aggregate (Stackweise)

Reihenfolge nach Abhängigkeitsgrad:

### 2.1 Role

* ReadRoleHandler
* ListRolesHandler (je ApplicationScope)
* UpdateRoleHandler
* Facade-Erweiterung
* REST-Endpunkte
* Tests

### 2.2 Permission

* ReadPermissionHandler
* ListPermissionsHandler (je ApplicationScope)
* UpdatePermissionHandler
* Facade-Erweiterung
* REST-Endpunkte
* Tests

### 2.3 PermissionGroup

* ReadPermissionGroupHandler
* ListPermissionGroupsHandler
* UpdatePermissionGroupHandler
* DeletePermissionGroupHandler (mit Schutzlogik, falls enthaltene Permissions existieren)
* Facade-Erweiterung
* REST-Endpunkte
* Tests

Abschlusskriterium Phase 2:
Alle Aggregate besitzen vollständige CRUD-UseCases mit einheitlicher Schichtenführung.

---

## Phase 3 – Fehlende Kern-UseCases für MVP

### 3.1 User ↔ ApplicationScope Assignment

* AssignApplicationScopeToUserHandler
* UnassignApplicationScopeFromUserHandler
* Facade-Erweiterung
* REST-Endpunkte
* Tests

Begründung:
Dieser UseCase ist funktional essenziell für produktiven Betrieb (User-Scope-Zuordnung), existiert aber aktuell nur auf Entity-Ebene.

Abschlusskriterium Phase 3:
ApplicationScope-Zuordnungen sind vollständig administrierbar.

---

## Phase 4 – Architektur-Härtung und Konsistenzprüfung

1. Einheitlicher Query-Standard final überprüfen
2. Exception-Strategie vereinheitlichen (DomainException vs IllegalArgumentException)
3. Bootstrap-Authorities mit implementierten UseCases abgleichen
4. Prüfung aller @PreAuthorize-Annotationen auf Konsistenz
5. Integrations-Tests für alle CRUD-Operationen

Abschlusskriterium Phase 4:

* Keine Architekturabweichungen
* Keine inkonsistenten Sicherheitsprüfungen
* Keine "halben" UseCases
* Tests vollständig grün

---

# 10. Definition of Done Sprint 5

Sprint 5 ist abgeschlossen, wenn:

* Alle Aggregate CRUD-fähig sind
* Jeder REST-Endpunkt über Facade und Handler läuft
* Security ausschließlich auf UseCase-Ebene durchgesetzt wird
* Kein Controller direkten Persistenzzugriff besitzt
* Alle Authorities im Bootstrap konfiguriert und genutzt werden
* Für jeden REST-Endpunkt existieren Controller-Tests

## Test-Strategie (verbindlich)

Für Sprint 5 gelten folgende Testanforderungen:

1. Es werden ausschließlich Tests gegen die REST-Controller implementiert.
2. Die Tests laufen gegen die vollständige Spring-Konfiguration (Integrationsebene).
3. Es werden keine isolierten Unit-Tests für Handler oder EntityServices benötigt.
4. Jeder Test prüft die gesamte Kette:
   Controller → Facade → Handler → EntityService → Repository → Datenbank
5. Pro CRUD-Operation mindestens:

    * Erfolgsfall (200 / 201 / 204)
    * Fehlende Authority (403)
    * Fachlicher Fehlerfall (z. B. Delete mit Abhängigkeiten → 400/409)
6. Alle Tests müssen grün sein.

---

Ende des Arbeitsdokuments.
