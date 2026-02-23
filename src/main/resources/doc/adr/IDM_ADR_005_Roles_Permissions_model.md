# IDM – ADR 005: Rollen- und Berechtigungsmodell

Stand: 2026-02-23
Status: Accepted (Sprint 4 Grundlage)

---

## 1. Kontext

Mit Sprint 4 wird das Autorisierungsmodell des IDM vollständig persistiert umgesetzt. Das IDM dient nicht nur der eigenen Benutzer- und Rollenverwaltung, sondern stellt zentral Rollen und Berechtigungen für mehrere Fachanwendungen bereit.

Das Berechtigungsmodell muss daher:

* sowohl IDM-eigene Rechte
* als auch Rechte externer Fachanwendungen

mit derselben Struktur abbilden können.

Eine Sonderlogik ausschließlich für das IDM ist ausdrücklich nicht vorgesehen.

---

## 2. Problemstellung

Folgende Anforderungen müssen erfüllt werden:

1. Rollen und Berechtigungen müssen persistiert sein.
2. Rollen müssen Benutzern zugewiesen werden können.
3. Rollen müssen Berechtigungen aggregieren.
4. Berechtigungen müssen logisch gruppierbar sein.
5. Das Modell muss für beliebige Fachanwendungen erweiterbar sein.
6. Die technische Rechteprüfung muss generisch im `system`-Layer erfolgen.
7. Das IDM selbst muss exakt dieselbe Struktur verwenden wie externe Anwendungen.

Zusätzlich müssen Performance und Skalierbarkeit berücksichtigt werden (potenziell viele Berechtigungen pro Rolle).

---

## 3. Architekturentscheidung

### 3.1 Einheitliches Modell für alle Anwendungen

Es existiert kein separates "IDM-internes" Rollenmodell.

Das IDM ist lediglich eine Anwendung mit:

```
applicationKey = "IDM"
```

Fachanwendungen verwenden eigene `applicationKey`-Werte, z. B.:

* `PERSONNEL`
* `FINANCE`
* `CRM`

Damit ist das Modell vollständig generisch.

---

### 3.2 Domain Entities (finales Modell mit Scope-Unterstützung)

Mit Einführung von `ApplicationScope` wird das bisherige `applicationKey`-Feld durch eine explizite Scope-Relation ersetzt.

---

#### ApplicationScope

Repräsentiert eine konkrete Anwendung in einer konkreten Stage.

Attribute:

* id
* applicationKey
* stageKey
* description

Unique Constraint:

* (applicationKey, stageKey)

Beispiele:

* (IDM, DEV)
* (IDM, PROD)
* (PERSONNEL, TEST)

Alle Rollen und Berechtigungen sind einem ApplicationScope eindeutig zugeordnet.

---

#### PermissionGroup

Strukturiert logisch zusammengehörige Berechtigungen.

Attribute:

* id
* applicationScope (ManyToOne, LAZY)
* name
* description

Unique Constraint:

* (application_scope_id, name)

---

#### Permission

Repräsentiert eine konkrete fachliche Berechtigung.

Attribute:

* id
* applicationScope (ManyToOne, LAZY)
* name
* description
* permissionGroup (ManyToOne, LAZY)
* systemProtected (boolean)

Unique Constraint:

* (application_scope_id, name)

---

#### Role

Aggregiert Berechtigungen.

Attribute:

* id
* applicationScope (ManyToOne, LAZY)
* name
* description
* systemProtected (boolean)

Unique Constraint:

* (application_scope_id, name)

---

#### RolePermissionAssignment

Verknüpft Rollen mit Berechtigungen (n:m).

Attribute:

* id
* role (ManyToOne, LAZY)
* permission (ManyToOne, LAZY)

Unique Constraint:

* (role_id, permission_id)

---

#### UserRoleAssignment

Verknüpft Benutzer mit Rollen (n:m).

Attribute:

* id
* userAccount (ManyToOne, LAZY)
* role (ManyToOne, LAZY)

Unique Constraint:

* (user_account_id, role_id)

---

### 3.3 Modellübersicht (Relationen)

Strukturelle Beziehung:

ApplicationScope
↓
PermissionGroup
↓
Permission

ApplicationScope
↓
Role
↓
RolePermissionAssignment
↓
Permission

UserAccount
↓
UserRoleAssignment
↓
Role

Alle Relationen sind unidirektional und LAZY.

---

### 3.4 Fetch-Strat

Alle Relationen werden LAZY geladen.

Begründung:

* Rollen können viele Berechtigungen enthalten.
* Benutzer können mehrere Rollen besitzen.
* Die Rechteauflösung erfolgt explizit im Resolver.

Es werden keine bidirektionalen Relationen modelliert.

---

### 3.3 Scope- und Stage-Modell

Neben dem `applicationKey` benötigt das Modell eine weitere Dimension, um unterschiedliche Betriebsumgebungen (Stages) sowie ggf. mehrere Scope-Definitionen pro Anwendung sauber abzubilden.

#### Ziel

* Jede Anwendung besitzt einen eigenen Rechte-Scope.
* Innerhalb einer Anwendung können mehrere Stages existieren (z. B. DEV, TEST, PROD).
* Rollen- und Berechtigungszuordnungen können je Stage variieren.
* IDM-eigene Rechte werden exakt gleich behandelt, jedoch durch Domain-Regeln geschützt.

---

#### Einführung: ApplicationScope

Neue Entity:

* id
* applicationKey
* stageKey (z. B. DEV, TEST, PROD oder frei definierbar)
* description

Unique Constraint:

* (applicationKey, stageKey)

Bedeutung:

Ein `ApplicationScope` repräsentiert eine konkrete Anwendung in einer konkreten Stage.

Beispiele:

* (IDM, DEV)
* (IDM, PROD)
* (PERSONNEL, TEST)

---

#### Anpassung bestehender Entities

`Role`, `PermissionGroup` und `Permission` werden einem `ApplicationScope` zugeordnet.

Dadurch gilt:

* Rechte sind nicht nur application-spezifisch, sondern auch stage-spezifisch.
* Stage-Unterschiede werden vollständig isoliert.

`UserRoleAssignment` bleibt stage-spezifisch, da Rollen stagegebunden sind.

---

#### Warum kein Sondermodell für IDM?

Das IDM selbst nutzt ebenfalls `ApplicationScope` mit:

* applicationKey = "IDM"
* stageKey = z. B. "DEV" oder "PROD"

Es existiert keinerlei Sonderbehandlung im Datenmodell.

Der Schutz der IDM-eigenen Basisrollen und -rechte erfolgt ausschließlich im Domain-Service (z. B. Verbot des Löschens bestimmter systemrelevanter Rollen).

---

#### Domain-Regel: Systemgeschützte Rollen und Rechte

Bestimmte Rollen oder Berechtigungen können als "systemgeschützt" markiert werden.

Empfohlene Ergänzung:

Boolean-Feld:

* `systemProtected`

für `Role` und ggf. `Permission`.

Regel im DomainService:

* Systemgeschützte Objekte dürfen nicht gelöscht werden.
* Änderungen können eingeschränkt werden.

Diese Logik gehört ausschließlich in den Domain-Service und nicht ins Entity-Modell.

---

## 4. Verantwortlichkeiten

### 4.1 Domain Layer (IDM)

Verantwortlich für:

* Persistenz von Rollen, Berechtigungen und Gruppen
* Fachliche Definition der Berechtigungen
* Bootstrap von IDM-eigenen Rollen und Berechtigungen

Der Domain Layer kennt keine technische Security-Implementierung.

---

### 4.2 System Layer

Im Package:

```
de.cocondo.app.system.security.authorization
```

Wird implementiert:

* RolePermissionResolver

Verantwortlich für:

* Laden der Rollen anhand von Role-IDs aus dem JWT
* Auflösen der zugehörigen Berechtigungen
* Erzeugen von `GrantedAuthority`
* Integration in Spring Security

Der System Layer enthält keine fachlichen Rollen- oder Berechtigungsdefinitionen.

---

## 5. JWT-Design

Das JWT enthält ausschließlich:

* `sub` (User-ID)
* `roles` (Liste von Role-IDs)
* `iat`
* `exp`

Es enthält keine:

* Rollen-Namen
* Berechtigungs-Namen
* PermissionGroups

Begründung:

* Rename-sicher
* Keine Token-Invalidierung bei Namensänderung
* Keine fachliche Logik im Token
* Berechtigungsauflösung bleibt serverseitig

---

## 6. Autorisierungsfluss

1. JWT wird validiert.
2. Role-IDs werden extrahiert.
3. RolePermissionResolver lädt Rollen.
4. Zugehörige Berechtigungen werden aggregiert.
5. Authorities werden in den SecurityContext gesetzt.
6. `@PreAuthorize` prüft Authorities.

IDM-eigene Rechte werden identisch behandelt wie externe Fachanwendungsrechte.

---

## 7. Bootstrap-Strategie

Da Liquibase derzeit deaktiviert ist, erfolgt die Initialbefüllung über einen Initializer-Service.

Für das IDM werden beim Start erzeugt:

* PermissionGroups (z. B. USER_MANAGEMENT)
* Permissions (z. B. USER_READ)
* Rollen (z. B. IDM_ADMIN)
* Zuordnungen Role → Permission

Alle mit `applicationKey = "IDM"`.

---

## 8. Konsequenzen

### Positive

* Einheitliches, generisches Modell
* Skalierbar auf beliebig viele Anwendungen
* Keine Sonderlogik für IDM
* Saubere Trennung von Domain und technischer Security
* Zukunftsfähig für Caching und Mandantenfähigkeit

### Trade-offs

* Mehr Entities und Persistenzaufwand
* Resolver benötigt optimierte Queries
* Initialer Implementierungsaufwand höher als codebasiertes Mapping

---

## 9. Abgrenzung

Nicht Bestandteil dieses ADR:

* Key-Rotation (ADR-004)
* Mandantenmodell
* UI-Administration
* Refresh-Token-Strategie

---

## 10. Zusammenfassung

Das IDM verwendet ein vollständig persistiertes, generisches Rollen- und Berechtigungsmodell.

Die eigene Rechteprüfung des IDM funktioniert exakt analog zu der von externen Fachanwendungen, für die das IDM konzipiert ist.

Es existiert kein Sondermodell und keine codebasierte Rollen-Logik.

Die technische Rechteauflösung erfolgt im System-Layer, während die fachliche Definition vollständig in der IDM-Domain verbleibt.
