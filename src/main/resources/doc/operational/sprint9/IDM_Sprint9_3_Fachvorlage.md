# IDM User UI – fachliche Vorlage für GWC

## 1. Ziel

Diese Vorlage definiert die fachlich korrekte UI-Grundlage für die Darstellung von **User Accounts** im GWC-Projekt auf Basis der aktuellen IDM-Baseline.

Fokus dieses ersten Standes ist die **rein visuelle/konzeptionelle User-Detailseite**. Die Vorlage ist zugleich die fachliche Grundlage für die nachfolgende Listen- und Formularansicht.

---

## 2. Fachliches Leitbild

Im aktuellen IDM ist ein User **kein Personenstammsatz**, sondern ein **technischer UserAccount**.

Der User besteht fachlich aus:

* technischem UserAccount
* Zuordnung zu ApplicationScopes
* Zuordnung von Rollen innerhalb von Scopes
* technischem Sicherheits- und Lebenszyklusstatus
* technischen Metadaten/Auditdaten
* optionaler Session-/Anmeldekontextdarstellung

Nicht Bestandteil des aktuellen IDM-User-Modells sind insbesondere:

* Vorname
* Nachname
* Telefonnummer
* Abteilung
* Titel / Funktion
* Sprache
* Zeitzone
* Employee Number

Solche Felder dürfen daher in der User-Detailseite **nicht als fachliche Stammdaten des IDM-Users** modelliert werden.

---

## 3. Relevante IDM-Datenobjekte

### 3.1 UserAccount

Primäres Objekt der Seite.

Relevante Felder aus dem Modell:

* `id`
* `username`
* `displayName`
* `email`
* `state`
* `failedLoginAttempts`
* `lockedUntil`
* `createdAt`
* `createdBy`
* `lastModifiedAt`
* `lastModifiedBy`
* `tags`
* `keyValuePairs`

Nicht anzeigen:

* `passwordHash`

### 3.2 UserApplicationScopeAssignment

Definiert, in welchen ApplicationScopes der User grundsätzlich vorhanden ist.

Darstellung als Liste/Tabelle der zugeordneten Scopes:

* Scope-ID
* `applicationKey`
* `stageKey`
* `description`

### 3.3 UserRoleAssignment

Definiert die Rollen des Users.

Da Rollen scopegebunden sind, sollen Rollen **immer mit Scope-Kontext** dargestellt werden.

Darstellung:

* Rollenname
* Rollenbeschreibung
* systemProtected
* Zugehöriger Scope (`applicationKey`, `stageKey`)

### 3.4 ApplicationScope

Scope ist fachlich relevant, weil das IDM zwischen Anwendung und Stage unterscheidet.

Ein Scope ist fachlich:

* `applicationKey`
* `stageKey`
* `description`

Beispiel:

* IDM / DEV
* IDM / PROD
* PERSONNEL / TEST

### 3.5 AuthSession / Sicherheitskontext

Für eine Admin-Detailseite fachlich sinnvoll, sofern verfügbar:

* aktive Sessions
* Session-Status
* Ablaufzeitpunkt
* Revocation-Informationen

Da dies ein separates Modell ist, sollte dieser Block als **technischer Sicherheits-/Sessionbereich** gestaltet werden und nicht als Stammdatenbereich.

---

## 4. Empfohlene Struktur der User-Detailseite

## Seitenkopf

### Kopfbereich links

* Seitentitel: **User Account**
* Primäre Kennung: `displayName` oder alternativ `username`
* Sekundäre Kennung: `username`
* Status-Badge aus `state`

### Kopfbereich rechts

Primäre Seitenaktionen:

* Bearbeiten
* Passwort ändern
* Aktivieren / Deaktivieren
* Historie / Audit
* Sessions verwalten (optional, wenn vorgesehen)

### Kopf-Metriken

Empfohlen:

* Letzter Login
* Fehlversuche
* Anzahl aktiver Sessions

Hinweis:

`Letzter Login` ist aktuell im gezeigten `UserAccountDTO` noch nicht enthalten und muss daher für eine finale UI gezielt bereitgestellt werden.

---

## 5. Fachliche Informationsblöcke der Detailseite

## Block A – Stammdaten

Pflichtblock.

Anzuzeigende Felder:

* ID
* Benutzername
* Anzeigename
* E-Mail
* Status

Optional im selben Block oder als eigener Technikblock:

* Fehlgeschlagene Login-Versuche
* Temporär gesperrt bis (`lockedUntil`)

### Fachliche Bewertung

Dieser Block beschreibt den technischen Account selbst.

---

## Block B – Scope-Zuordnungen

Pflichtblock.

Darstellung als Liste oder kleines Grid.

Pro Eintrag:

* Application Key
* Stage Key
* Beschreibung
* optional Scope-ID

### Fachliche Bedeutung

Dieser Block beantwortet: **In welchen Anwendungskontexten existiert bzw. gilt dieser User?**

---

## Block C – Rollen je Scope

Pflichtblock.

Rollen sollen **nicht scope-los** als einfache Tag-Liste erscheinen, sondern gruppiert dargestellt werden.

### Empfohlene Darstellung

Accordion oder Karten pro Scope:

* Scope: `applicationKey` / `stageKey`
* darunter Rollenliste

Pro Rolle:

* Rollenname
* Beschreibung
* systemProtected

### Fachliche Bedeutung

Dieser Block beantwortet: **Welche Rollen hat der User in welchem Scope?**

Das ist fachlich deutlich präziser als eine einfache globale Rollenliste.

---

## Block D – Sicherheitsstatus

Pflichtblock für Admin-Oberfläche.

Anzuzeigen:

* Account-Status (`ACTIVE`, `DISABLED`, `EXPIRED`, `LOCKED_TEMPORARY`, `LOCKED_PERMANENT`)
* Fehlversuche (`failedLoginAttempts`)
* Sperrzeitpunkt bis (`lockedUntil`), falls gesetzt

Optional:

* Passwort geändert am
* Passwort-Reset angefordert am

Hinweis:

Die letzten beiden Felder sind aktuell nicht Bestandteil des gezeigten Modells und daher derzeit nur als zukünftige Erweiterung zu sehen.

---

## Block E – Audit / technische Metadaten

Sehr sinnvoll für die Detailseite.

Anzuzeigen:

* Angelegt am
* Angelegt von
* Zuletzt geändert am
* Zuletzt geändert von

Optional zusätzlich:

* Tags
* Key-Value-Metadaten

### Fachliche Bedeutung

Dieser Block beschreibt Herkunft und Änderungsverlauf des Accounts.

---

## Block F – Anmelde- und Session-Kontext

Optional, aber fachlich sehr wertvoll.

Geeignet als Seitenleiste oder separater Card-Block.

Anzuzeigen, sofern Daten geliefert werden:

* Letzter Login
* aktive Sessions
* Session-Abläufe
* Revoked/Expired Sessions

Wichtig:

Dieser Block ist **nicht** Teil der User-Stammdaten, sondern ein technischer Betriebsblock.

---

## 6. Was aus dem aktuellen GWC-Dummy entfernt oder ersetzt werden sollte

Die aktuellen Dummies enthalten mehrere Felder, die in der IDM-Baseline für User derzeit fachlich nicht belegt sind.

### Aus Detailseite entfernen

* Vorname
* Nachname
* Telefon
* Abteilung
* Titel / Funktion
* Sprache
* Zeitzone
* Employee Number

### Aus Edit-Seite entfernen

* Vorname
* Nachname
* Telefon
* Abteilung
* Titel / Funktion
* Sprache
* Zeitzone
* Scope-Profil
* interne Notizen, sofern diese nicht explizit über Metadata/KeyValuePairs modelliert werden

### Ersetzen durch echte IDM-Felder

Stattdessen verwenden:

* Benutzername
* Anzeigename
* E-Mail
* Status
* Fehlversuche
* Locked-until
* Scope-Zuordnungen
* Rollen je Scope
* Audit/Metadaten

---

## 7. Konkrete fachliche Zielstruktur für die Detailseite

## Hauptbereich

### Card 1 – Account-Kopf

* DisplayName oder Username als Haupttitel
* Username als Sekundärinfo
* Status-Badge
* Metriken:

    * letzter Login
    * Fehlversuche
    * aktive Sessions

### Card 2 – Account-Stammdaten

* ID
* Benutzername
* Anzeigename
* E-Mail
* Status

### Card 3 – Scope-Zuordnungen

Liste aller zugewiesenen ApplicationScopes.

### Card 4 – Rollen je Scope

Gruppierte Rollenliste pro Scope.

### Card 5 – Sicherheitsstatus

* Fehlversuche
* lockedUntil
* optional Session-Status-Zusammenfassung

## Seitenleiste

### Card 6 – Audit / Meta

* Angelegt am / von
* Zuletzt geändert am / von
* Tags
* Key-Value-Metadaten

### Card 7 – Historie

Nur wenn echte Auditdaten vorhanden sind.

Bis dahin besser als Platzhalter/disabled Entry statt frei erfundener Timeline-Daten.

---

## 8. Fachliche Empfehlung für die spätere Listenansicht

Die spätere finale Listenansicht sollte mindestens diese Spalten berücksichtigen:

* ID
* Benutzername
* Anzeigename
* E-Mail
* Status
* Letzter Login
* Fehlversuche
* Locked until
* Zugewiesene Scopes (kompakt)
* Rollenanzahl oder Rollenindikator
* Zuletzt geändert am

Hinweis:

Einige dieser Spalten sind aktuell fachlich sinnvoll, aber noch nicht in der einfachen User-Liste-DTO enthalten. Für die finale Listenansicht wird daher voraussichtlich ein erweitertes Listen-DTO oder eine dedizierte Query benötigt.

---

## 9. Fachliche Empfehlung für die spätere Formularansicht

Die spätere Formularansicht sollte sich auf tatsächlich änderbare IDM-User-Felder konzentrieren.

### Stammdaten editierbar

* Anzeigename
* E-Mail
* Status (fachlich gesteuert)

### Separat oder in Unterbereichen

* Scope-Zuordnungen verwalten
* Rollen-Zuordnungen verwalten
* Passwort ändern

### Read-only

* ID
* Benutzername (je nach Fachregel eher read-only)
* Auditfelder
* Sicherheits-/Lock-Informationen

---

## 10. Zusammenfassung

Für das aktuelle IDM ist die User-Detailseite fachlich korrekt, wenn sie den **technischen Account**, dessen **Scope-Zuordnungen**, dessen **Rollen pro Scope**, den **Sicherheitsstatus** und die **Audit-/Metadaten** darstellt.

Nicht korrekt wäre eine UI, die den User bereits wie einen vollständigen Personenstammsatz modelliert.

Die aktuelle GWC-Dummy-Struktur ist als Seitenlayout brauchbar, muss aber fachlich auf das IDM-Modell zurückgeschnitten werden.

---

## 11. Konzeptionelle Kopplung zwischen GWC und IDM-API

Dieses Kapitel definiert, welche bestehenden IDM-Endpunkte für welche User-Ansicht verwendet werden sollen und ob die aktuelle API die benötigten Daten bereits ausreichend liefert.

### 11.1 Grundsatz

Für die User-Oberflächen soll der Client **nicht** aus Entitätsannahmen arbeiten, sondern aus klaren API-Verträgen.

Das bedeutet:

* Listenansicht nutzt primär den paginierten User-Listenendpunkt
* Detailseite lädt den User-Kern separat von Scope-/Rolleninformationen
* Formularansicht trennt Stammdatenbearbeitung, Passwortänderung, Scope-Zuordnung und Rollen-Zuordnung in eigene API-Operationen
* Mehrere Requests pro Seite sind fachlich zulässig und hier teilweise erforderlich

---

### 11.2 Bestehende User-Kernendpunkte

#### User-Liste

`GET /api/idm/users/list`

Parameter:

* `page`
* `size`
* `sortBy`
* `sortDir`
* `username`
* `displayName`
* `email`
* `state`

Antwort:

* `PagedResponseDTO<UserAccountDTO>`

Aktuell geliefertes DTO:

* `id`
* `username`
* `displayName`
* `email`
* `state`

#### User-Detail

`GET /api/idm/users/{id}`

Antwort:

* `UserAccountDTO`

Aktuell geliefertes DTO:

* `id`
* `username`
* `displayName`
* `email`
* `state`

#### User anlegen

`POST /api/idm/users`

Request:

* `username`
* `displayName`
* `email`
* `password`

Antwort:

* `UserAccountDTO`

#### User aktivieren / deaktivieren

`PUT /api/idm/users/{id}/activate`

`PUT /api/idm/users/{id}/deactivate`

Antwort jeweils:

* `UserAccountDTO`

#### Passwort ändern

`PUT /api/idm/users/{id}/password`

Request:

* `newPassword`

Antwort:

* `UserAccountDTO`

#### User löschen

`DELETE /api/idm/users/{id}`

---

### 11.3 Bestehende Endpunkte für Scope-Zuordnungen des Users

#### Zugewiesene Scopes eines Users lesen

`GET /api/idm/assignments/user-scope/users/{userAccountId}/scopes`

Antwort:

* Liste von `ApplicationScopeDTO`

Felder:

* `id`
* `applicationKey`
* `stageKey`
* `description`

#### Zugewiesene Scopes eines Users paginiert lesen

`GET /api/idm/assignments/user-scope/users/{userAccountId}/scopes/list`

Parameter:

* `page`
* `size`
* `sortBy`
* `sortDir`

Antwort:

* `PagedResponseDTO<ApplicationScopeDTO>`

#### Scope zu User zuordnen

`POST /api/idm/assignments/user-scope`

Request:

* `userAccountId`
* `applicationScopeId`

#### Scope von User entfernen

`DELETE /api/idm/assignments/user-scope`

Request:

* `userAccountId`
* `applicationScopeId`

---

### 11.4 Bestehende Endpunkte für Rollen des Users

#### Rollen eines Users innerhalb eines Scopes lesen

`GET /api/idm/assignments/user-role/users/{userAccountId}/roles`

Pflichtparameter:

* `applicationKey`
* `stageKey`

Antwort:

* Liste von `RoleDTO`

#### Rollen eines Users innerhalb eines Scopes paginiert lesen

`GET /api/idm/assignments/user-role/users/{userAccountId}/roles/list`

Pflichtparameter:

* `applicationKey`
* `stageKey`

Weitere Parameter:

* `page`
* `size`
* `sortBy`
* `sortDir`

Antwort:

* `PagedResponseDTO<RoleDTO>`

#### Rolle zu User zuordnen

`POST /api/idm/assignments/user-role`

Request:

* `userAccountId`
* `roleId`

#### Rolle von User entfernen

`DELETE /api/idm/assignments/user-role`

Request:

* `userAccountId`
* `roleId`

---

### 11.5 Auth-/Session-Endpunkte mit UI-Relevanz

Vorhanden sind Auth-Endpunkte für Login- und Refresh-Lifecycle:

* `POST /auth/login`
* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`
* `GET /auth/me`

Diese Endpunkte sind jedoch **keine Admin-Read-API für User-Sessions**.

Damit gilt:

* Für die Login-Maske und Session-Steuerung des aktuellen Clients sind sie relevant.
* Für eine Admin-User-Detailseite liefern sie **nicht** die benötigte Sessionübersicht eines beliebigen Users.

---

### 11.6 Empfohlene API-Nutzung pro Ansicht

## A. Finale Listenansicht

### Primäre Datenquelle

`GET /api/idm/users/list`

### Bereits fachlich gut abdeckbar

* ID
* Benutzername
* Anzeigename
* E-Mail
* Status
* Paging
* Sortierung innerhalb der heute unterstützten Felder
* Filter auf Username, DisplayName, E-Mail, Status

### Mit aktueller API nicht ausreichend abdeckbar

* letzter Login
* Login-Zähler / erfolgreiche Logins gesamt
* Fehlversuche
* lockedUntil
* Scopes kompakt in der Liste
* Rollenanzahl / Rollenindikator
* Zuletzt geändert am

### Fazit Listenansicht

Für eine **MVP-Listenansicht** reicht die aktuelle API.

Für die **fachlich gewünschte finale Listenansicht** reicht sie **nicht vollständig**. Dafür ist ein erweitertes Listen-DTO oder ein dedizierter Query-Endpunkt erforderlich.

---

## B. Detailseite

### Empfohlene Requests

#### Request 1 – User-Kern

`GET /api/idm/users/{id}`

#### Request 2 – Scope-Zuordnungen

`GET /api/idm/assignments/user-scope/users/{userAccountId}/scopes`

#### Request 3..n – Rollen je Scope

Für jeden Scope:

`GET /api/idm/assignments/user-role/users/{userAccountId}/roles?applicationKey=...&stageKey=...`

### Damit bereits darstellbar

* ID
* Benutzername
* Anzeigename
* E-Mail
* Status
* Zugewiesene Scopes
* Rollen pro Scope

### Mit aktueller API nicht ausreichend abdeckbar

* Fehlversuche (`failedLoginAttempts`)
* `lockedUntil`
* Auditdaten (`createdAt`, `createdBy`, `lastModifiedAt`, `lastModifiedBy`)
* Tags
* Key-Value-Metadaten
* letzter Login
* Sessionübersicht
* echte Audit-Timeline

### Fazit Detailseite

Die Detailseite ist mit **mehreren bestehenden Requests** bereits in einer fachlich sinnvollen Grundform umsetzbar.

Das aktuelle Backend liefert aber nur den **Kernaccount plus Zuordnungen**. Für Sicherheits-, Audit- und Metadatenblöcke fehlen heute passende Read-Verträge.

---

## C. Formularansicht

### 1. Initiales Laden

Für die Edit-Seite sollte der Client laden:

* `GET /api/idm/users/{id}`
* `GET /api/idm/assignments/user-scope/users/{userAccountId}/scopes`
* je Scope: `GET /api/idm/assignments/user-role/users/{userAccountId}/roles?...`

### 2. Speichern der Stammdaten

Hier besteht aktuell eine zentrale Lücke:

Es gibt derzeit **keinen allgemeinen Update-Endpunkt** für User-Stammdaten wie:

* `displayName`
* `email`
* allgemeine Statuspflege in einer einheitlichen Request-Struktur

Vorhanden sind nur:

* Aktivieren
* Deaktivieren
* Passwort ändern

### 3. Scope-Verwaltung

Vorhanden und sauber trennbar:

* `POST /api/idm/assignments/user-scope`
* `DELETE /api/idm/assignments/user-scope`

### 4. Rollen-Verwaltung

Vorhanden und sauber trennbar:

* `POST /api/idm/assignments/user-role`
* `DELETE /api/idm/assignments/user-role`

### 5. Passwort-Verwaltung

Vorhanden:

* `PUT /api/idm/users/{id}/password`

### Fazit Formularansicht

Die Formularansicht kann konzeptionell sauber in **mehrere Teiloperationen** zerlegt werden.

Was aktuell fehlt, ist jedoch der **allgemeine Update-Use-Case für User-Stammdaten**.

---

### 11.7 Bewertung der Backend-Abdeckung je UI-Bereich

#### Bereits ausreichend vorhanden

* User anlegen
* User laden (Basisdaten)
* User-Liste (Basisdaten)
* User aktivieren / deaktivieren
* Passwort ändern
* Scopes eines Users lesen
* Scopes zuordnen / entfernen
* Rollen eines Users je Scope lesen
* Rollen zuordnen / entfernen

#### Fachlich sinnvoll, aber aktuell nicht ausreichend geliefert

* letzter Login
* Login-Zähler
* Fehlversuche
* lockedUntil in der Read-API
* Auditfelder in der User-Read-API
* Tags / Key-Value-Metadaten in der User-Read-API
* Sessionübersicht für Admin-Sicht
* konsolidierte Detailprojektion für eine komplette User-Detailseite
* erweiterte Listenprojektion für finale DataGrid-Spalten
* allgemeiner Update-Endpoint für User-Stammdaten

---

### 11.8 Konzeptionelle Empfehlung für die Client-Backend-Kopplung

## Für die aktuelle Umsetzung ohne Backend-Änderung

#### Listenansicht

Kopplung an:

* `GET /api/idm/users/list`

Anzeigbar ohne fachliche Fakes:

* ID
* Benutzername
* Anzeigename
* E-Mail
* Status

#### Detailseite

Kopplung an:

* `GET /api/idm/users/{id}`
* `GET /api/idm/assignments/user-scope/users/{userAccountId}/scopes`
* pro Scope: `GET /api/idm/assignments/user-role/users/{userAccountId}/roles?...`

#### Formularansicht

Kopplung an:

* `POST /api/idm/users`
* `PUT /api/idm/users/{id}/activate`
* `PUT /api/idm/users/{id}/deactivate`
* `PUT /api/idm/users/{id}/password`
* `POST /api/idm/assignments/user-scope`
* `DELETE /api/idm/assignments/user-scope`
* `POST /api/idm/assignments/user-role`
* `DELETE /api/idm/assignments/user-role`

## Für die fachlich vollständige Ziel-UI

Das Backend sollte zusätzlich eigene, UI-taugliche Query-/Command-Verträge erhalten:

* erweitertes `UserListItemDTO`
* erweitertes `UserDetailDTO`
* `UpdateUserRequestDTO` plus allgemeiner Update-Endpunkt
* optional Session-Read-API für Admin-Sicht
* optional Audit-/History-API
* optional Metadata-API für Tags / KeyValuePairs

---

### 11.9 Klare Schlussfolgerung

Die bestehende IDM-API erlaubt bereits eine **saubere Grundkopplung** für User-Liste, Detailseite und Formularlogik, wenn der Client mehrere spezialisierte Requests verwendet.

Für die gewünschte **finale** UI reichen die vorhandenen Verträge jedoch noch nicht vollständig aus.

Insbesondere fehlen aktuell:

* ein allgemeiner User-Update-Endpunkt
* Read-DTOs für Security- und Auditdaten
* UI-taugliche Detail-/Listenprojektionen für letzte Logins, Fehlversuche, Lock-Informationen und Metadaten

Die konzeptionelle Kopplung ist damit bereits definierbar, aber die finale Zieloberfläche erfordert zusätzliche Backend-Verträge.
