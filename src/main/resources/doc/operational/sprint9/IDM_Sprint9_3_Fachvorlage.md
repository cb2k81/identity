# IDM Sprint 9.3 – Fachvorlage User-UI / API-Kopplung für GWC

## 1. Ziel

Diese Vorlage definiert die fachlich korrekte Grundlage für die Darstellung von **User Accounts** im GWC-Projekt auf Basis der aktuellen IDM-Baseline und des aktuellen Sprint-9-Zuschnitts.

Sprint 9 dient aktuell der **UI-Reifmachung der IDM-API**. Diese Vorlage beschreibt daher nicht nur die visuelle/fachliche Zielstruktur der User-Oberflächen, sondern auch die **reale fachliche Kopplung** zwischen GWC und IDM-API im aktuellen Stand.

Das Dokument ist eine **Fachvorlage für die GWC-Umsetzung** und zugleich eine Referenz für die weitere API-Abstimmung.

---

## 2. Fachliches Leitbild

Im aktuellen IDM ist ein User **kein Personenstammsatz**, sondern ein **technischer UserAccount**.

Der User besteht fachlich aus:

* technischem UserAccount
* Zuordnung zu ApplicationScopes
* Zuordnung von Rollen innerhalb von Scopes
* technischem Sicherheits- und Lebenszyklusstatus
* technischen Auditdaten
* technischem Login-/Anmeldekontext

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

Relevante Felder im aktuellen fachlichen/API-nahen Stand:

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
* `loginCount`
* `lastLogin`

Nicht anzeigen:

* `passwordHash`

Hinweis:

`tags` und `keyValuePairs` sind im generischen Metadatenmodell grundsätzlich vorhanden, sind aber **vorerst nicht MVP-relevant für Sprint 9** und daher **kein aktiver Bestandteil des minimalen UI-Zuschnitts**.

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

### 3.5 AuthSession / Login-Kontext

Für die User-Verwaltung fachlich relevant:

* `lastLogin`
* `loginCount`

Diese Informationen werden aktuell als Login-/Nutzungsmetriken im User-Kontext betrachtet.

Eine dedizierte **Admin-Session-Read-API** für aktive Sessions, Revocation-Informationen oder Session-Listen ist **vorerst nicht MVP-relevant** und daher aktuell **nicht Bestandteil des Sprint-9-Minimalumfangs**.

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

### Kopf-Metriken

Empfohlen:

* Letzter Login
* Fehlversuche
* Login-Zähler

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

Pflichtblock für die Admin-Oberfläche.

Anzuzeigen:

* Account-Status (`ACTIVE`, `DISABLED`, `EXPIRED`, `LOCKED_TEMPORARY`, `LOCKED_PERMANENT`)
* Fehlversuche (`failedLoginAttempts`)
* Sperrzeitpunkt bis (`lockedUntil`), falls gesetzt

Optional später:

* Passwort geändert am
* Passwort-Reset angefordert am

Hinweis:

Die letzten beiden Felder sind aktuell nicht Bestandteil des minimalen Sprint-9-Umfangs.

---

## Block E – Audit

Sehr sinnvoll für die Detailseite.

Anzuzeigen:

* Angelegt am
* Angelegt von
* Zuletzt geändert am
* Zuletzt geändert von

### Fachliche Bedeutung

Dieser Block beschreibt Herkunft und Änderungsverlauf des Accounts.

---

## Block F – Login-Kontext

Optionaler, aber fachlich wertvoller Block.

Anzuzeigen, sofern geliefert:

* Letzter Login
* Login-Zähler

Wichtig:

Dieser Block ist **kein Personen-/Stammdatenblock**, sondern ein technischer Kontextblock.

Nicht Bestandteil des aktuellen Sprint-9-MVP-Zuschnitts sind:

* aktive Sessions als Detail-Liste
* Session-Abläufe
* Revoked/Expired Sessions

---

## 6. Was aus den aktuellen GWC-Dummies entfernt oder ersetzt werden sollte

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
* interne Notizen

### Ersetzen durch echte IDM-Felder

Stattdessen verwenden:

* Benutzername
* Anzeigename
* E-Mail
* Status
* Fehlversuche
* Locked-until
* Login-Zähler
* letzter Login
* Scope-Zuordnungen
* Rollen je Scope
* Auditdaten

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
  * Login-Zähler

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

## Seitenleiste

### Card 6 – Audit

* Angelegt am / von
* Zuletzt geändert am / von

### Card 7 – Login-Kontext

* letzter Login
* Login-Zähler

---

## 8. Fachliche Empfehlung für die finale Listenansicht

Die finale Listenansicht sollte mindestens diese Spalten berücksichtigen:

* ID
* Benutzername
* Anzeigename
* E-Mail
* Status
* Letzter Login
* Login-Zähler
* Fehlversuche
* Locked until
* Zugewiesene Scopes (kompakt)
* Rollenanzahl oder Rollenindikator
* Zuletzt geändert am

Hinweis:

Nicht alle dieser Informationen sind heute bereits über alle User-bezogenen Endpunkte in gleicher Vollständigkeit konsistent geliefert. Für Sprint 9 ist jedoch die User-Liste fachlich deutlich weiter reif als zu Beginn der Analyse.

Die verbleibenden Konsistenz-/Listenfähigkeits-Themen werden separat als **Technische Schulden** geführt.

---

## 9. Fachliche Empfehlung für die Formularansicht

Die Formularansicht sollte sich auf tatsächlich änderbare IDM-User-Felder konzentrieren.

### Stammdaten editierbar

* Anzeigename
* E-Mail

### Statuspflege fachlich separat

* Aktivieren
* Deaktivieren

### Separat oder in Unterbereichen

* Scope-Zuordnungen verwalten
* Rollen-Zuordnungen verwalten
* Passwort ändern

### Read-only

* ID
* Benutzername
* Auditfelder
* Sicherheits-/Lock-Informationen
* Login-Metriken

---

## 10. Konzeptionelle Kopplung zwischen GWC und IDM-API

Dieses Kapitel definiert, welche bestehenden IDM-Endpunkte für welche User-Ansicht verwendet werden sollen.

### 10.1 Grundsatz

Für die User-Oberflächen soll der Client **nicht** aus Entitätsannahmen arbeiten, sondern aus klaren API-Verträgen.

Das bedeutet:

* Listenansicht nutzt primär den paginierten User-Listenendpunkt
* Detailseite lädt den User-Kern separat von Scope-/Rolleninformationen
* Formularansicht trennt Stammdatenbearbeitung, Passwortänderung, Scope-Zuordnung und Rollen-Zuordnung in eigene API-Operationen
* Mehrere Requests pro Seite sind fachlich zulässig und hier teilweise erforderlich

---

### 10.2 User-Kernendpunkte

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

#### User-Detail

`GET /api/idm/users/{id}`

Antwort:

* `UserAccountDTO`

#### User anlegen

`POST /api/idm/users`

#### User-Stammdaten aktualisieren

`PUT /api/idm/users/{id}`

Request:

* `displayName`
* `email`

#### User aktivieren / deaktivieren

`PUT /api/idm/users/{id}/activate`

`PUT /api/idm/users/{id}/deactivate`

#### Passwort ändern

`PUT /api/idm/users/{id}/password`

#### User löschen

`DELETE /api/idm/users/{id}`

---

### 10.3 Endpunkte für Scope-Zuordnungen des Users

#### Zugewiesene Scopes eines Users lesen

`GET /api/idm/assignments/user-scope/users/{userAccountId}/scopes`

#### Zugewiesene Scopes eines Users paginiert lesen

`GET /api/idm/assignments/user-scope/users/{userAccountId}/scopes/list`

#### Scope zu User zuordnen

`POST /api/idm/assignments/user-scope`

#### Scope von User entfernen

`DELETE /api/idm/assignments/user-scope`

---

### 10.4 Endpunkte für Rollen des Users

#### Rollen eines Users innerhalb eines Scopes lesen

`GET /api/idm/assignments/user-role/users/{userAccountId}/roles`

Pflichtparameter:

* `applicationKey`
* `stageKey`

#### Rollen eines Users innerhalb eines Scopes paginiert lesen

`GET /api/idm/assignments/user-role/users/{userAccountId}/roles/list`

Pflichtparameter:

* `applicationKey`
* `stageKey`

#### Rolle zu User zuordnen

`POST /api/idm/assignments/user-role`

#### Rolle von User entfernen

`DELETE /api/idm/assignments/user-role`

---

## 11. API-Nutzung pro Ansicht

## A. Listenansicht

### Primäre Datenquelle

`GET /api/idm/users/list`

### Fachlich direkt nutzbar

* ID
* Benutzername
* Anzeigename
* E-Mail
* Status
* Fehlversuche
* lockedUntil
* letzter Login
* Login-Zähler
* Auditfelder aus dem User-Kontext

### Noch als technische Schulden / Folgepunkte zu betrachten

* serverseitige Listenfähigkeit aller neuen Spalten
* Scopes kompakt in der Liste
* Rollenindikatoren in der Liste
* vollständige DTO-Konsistenz über alle User-bezogenen Read-Pfade

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

### Damit darstellbar

* ID
* Benutzername
* Anzeigename
* E-Mail
* Status
* Fehlversuche
* `lockedUntil`
* Auditdaten
* `lastLogin`
* `loginCount`
* Zugewiesene Scopes
* Rollen pro Scope

### Aktuell bewusst nicht MVP-relevant

* Tags
* KeyValuePairs
* Sessionlisten / aktive Sessions / Revocation-Übersichten
* echte Audit-Timeline

---

## C. Formularansicht

### Initiales Laden

* `GET /api/idm/users/{id}`
* `GET /api/idm/assignments/user-scope/users/{userAccountId}/scopes`
* je Scope: `GET /api/idm/assignments/user-role/users/{userAccountId}/roles?...`

### Speichern der Stammdaten

* `PUT /api/idm/users/{id}`

### Statuspflege

* `PUT /api/idm/users/{id}/activate`
* `PUT /api/idm/users/{id}/deactivate`

### Passwort-Verwaltung

* `PUT /api/idm/users/{id}/password`

### Scope-Verwaltung

* `POST /api/idm/assignments/user-scope`
* `DELETE /api/idm/assignments/user-scope`

### Rollen-Verwaltung

* `POST /api/idm/assignments/user-role`
* `DELETE /api/idm/assignments/user-role`

---

## 12. Klare Schlussfolgerung

Die IDM-API ist im aktuellen Sprint-9-Stand für die geplante **User-UI-Kopplung** fachlich deutlich weiter reif als zu Beginn der Analyse.

Für GWC bedeutet das:

* Liste, Detail und Formular sind auf Basis realer IDM-Verträge fachlich sauber modellierbar
* User wird korrekt als technischer UserAccount und nicht als Personenmodell behandelt
* bestehende Endpunkte wurden bevorzugt erweitert statt unnötig ersetzt
* verbleibende Punkte sind überwiegend **technische Schulden bzw. spätere optionale Erweiterungen**, nicht mehr grundlegende fachliche API-Blocker

Die wichtigste Leitlinie bleibt:

> Die User-UI im GWC muss sich am **technischen IDM-Accountmodell** orientieren und nicht an einem personenorientierten Verwaltungsmodell.
