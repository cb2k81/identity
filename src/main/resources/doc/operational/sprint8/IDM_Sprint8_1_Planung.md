# IDM Sprint 8 - Planung

Stand: 2026-03-19
Sprint: 8
Ziel: Nutzbares MVP inkl. Admin-UI-Fähigkeit **und** produktive Session-/Token-Lifecycle-Stufe
Status: Geplant

---

# 1. Zielsetzung des Sprints

Sprint 8 verfolgt das Ziel, den nach Sprint 7 erreichten **technisch belastbaren IDM-Stand** gezielt zu einem **nutzbaren MVP** weiterzuführen und gleichzeitig die **nächste zwingend produktionsrelevante Authentifizierungsstufe** zu ergänzen.

Die ursprüngliche Planung fokussierte sich auf den produktiven Session-/Token-Lifecycle. Auf Basis der aktuellen Baseline-Prüfung wurde jedoch verbindlich festgestellt, dass für ein tatsächlich nutzbares MVP zusätzlich noch **UI-relevante API-Lücken** geschlossen werden müssen.

Ein MVP nach Sprint 8 ist nur dann erreicht, wenn:

1. ein Admin-UI **sofort sauber** auf die IDM-REST-API aufsetzen kann,
2. Listen-APIs **generisch** für UI-Komponenten nutzbar sind,
3. Relationen / Zuordnungen **nicht nur schreibbar, sondern auch lesbar und anzeigbar** sind,
4. Fehlerbilder **UI-tauglich, deterministisch und fachlich stabil** sind,
5. zusätzlich ein produktiv nutzbarer **Refresh-/Session-Lifecycle** existiert.

Sprint 8 ist damit **kein reiner Post-MVP-Auth-Sprint mehr**, sondern der **MVP-Abschlusssprint mit produktiver Auth-Lifecycle-Härtung**.

Kernziel von Sprint 8 ist daher die Kombination aus:

* **MVP-API-Readiness für das Admin-UI**
* **klarer, kontrollierbarer und widerrufbarer Session-/Refresh-Stufe** als Ergänzung zum bestehenden JWT-Access-Token-Modell

Sprint 8 ist erfolgreich abgeschlossen, wenn:

1. die Management-APIs für **Application Scopes, Rollen und Benutzer** UI-fähig sind,
2. Listen generisch **filterbar, sortierbar und paginierbar** sind,
3. Relationen / Zuordnungen vollständig **lesbar und anzeigbar** sind,
4. Duplicate-/Validierungs-/NotFound-Fehlerbilder UI-tauglich und konsistent sind,
5. der Unterschied zwischen **Access Token** und **Refresh-/Session-Kontext** fachlich und technisch eindeutig umgesetzt ist,
6. ein produktiv nutzbarer **Refresh-Flow** existiert,
7. aktive Sessions gezielt invalidiert werden können,
8. Logout und Logout-all deterministisch funktionieren,
9. die Lösung mit dem aktuellen IDM-Baseline-Modell, den V2-Stammdokumenten und der geplanten Nutzung durch **Personnel** und **GWC** konsistent bleibt.

---

# 2. Ausgangslage / Baseline (verbindlich)

Der aktuelle, durch Textexport belegte Stand des IDM umfasst bereits:

* `POST /auth/login`
* `GET /auth/me`
* JWT-basierte Authentifizierung
* persistentes User-/Scope-/Role-/Permission-Modell
* serverseitige Rollen-/Rechte-Auflösung
* deterministisches XML-Bootstrap
* konfigurierbaren Login-Schutz
* stabile Security-/Authorization-Basis nach Sprint 7
* CRUD-REST-Endpunkte für:

    * Application Scopes
    * Rollen
    * Benutzer (mit aktuellem MVP-Umfang)
* Assignment-Endpunkte für:

    * User ↔ Scope (Assign / Unassign)
    * User ↔ Role (Assign / Unassign + Teil-Read)
    * Role ↔ Permission (Assign / Unassign)

Nicht als bereits vollständig umgesetzt zu behandeln sind aktuell:

## 2.1 MVP-Readiness-Gaps (verbindlich offen)

* generische Listenfähigkeit für Admin-UI-Komponenten

    * Filterung
    * Sortierung
    * Pagination
* UI-stabile Listen-Response-Strukturen
* vollständige Read-/List-Fähigkeit aller relevanten Relationen
* UI-taugliche, konsistente Fehlerbilder für Management-/Assignment-APIs
* saubere fachliche Behandlung von Duplicate-Assignments (statt technischer 5xx-Fehler)

## 2.2 Auth-Lifecycle-Gaps (verbindlich offen)

* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`
* kontrollierbare Session-Invalidierung
* persistierter Refresh-/Session-Kontext
* Passwort-Reset- und E-Mail-Verification-Flows

Diese Punkte sind in den V2-Stammdokumenten bereits als **nächste produktive Härtungsstufe** definiert und werden in Sprint 8 nun gezielt operationalisiert. Die **MVP-Readiness-Gaps** werden aufgrund der Baseline-Analyse nun ebenfalls verbindlich in Sprint 8 aufgenommen.

---

# 3. Scope von Sprint 8

## 3.1 Im Scope (verbindlich)

Sprint 8 umfasst verbindlich **zwei zusammengehörige Teilziele**:

1. **MVP-API-Readiness für das Admin-UI**
2. **nächste produktive Auth-Lifecycle-Stufe**

---

## 3.2 Teil A – MVP-API-Readiness (neu verbindlich im Scope)

### A1. Listenstandard für Admin-UI

Für die relevanten Management- und Relations-APIs ist ein konsistenter Listenstandard einzuführen.

Verbindliche Ziele:

* **serverseitige Pagination**
* **serverseitige Sortierung**
* **serverseitige Filterung**
* für generische UI-Komponenten stabil nutzbare Listen-Responses

Mindestens betroffen:

* Benutzer
* Rollen
* Application Scopes
* Relation-Listen / Zuordnungslisten

### A2. Relation-Read-Completeness

Relationen dürfen nicht nur schreibbar sein, sondern müssen für das Admin-UI vollständig lesbar sein.

Verbindliche Ziele:

* bestehende Relation-Read-Endpunkte prüfen und auf den Listenstandard heben
* fehlende Relation-Read-Endpunkte ergänzen
* Zuordnungen im UI eindeutig anzeigbar machen

Mindestens fachlich relevant:

* User ↔ Scope
* User ↔ Role
* Role ↔ Permission (soweit Rollen-/Rechte-Management Teil des MVP ist; fachlich stark empfohlen)

### A3. UI-taugliche Fehlerbilder

Verbindliche Ziele:

* keine technischen 5xx-Fehler als API-Vertrag bei fachlich erwartbaren Situationen
* deterministische Behandlung von:

    * Duplicate-Assignments
    * Not Found
    * Validation-Fehlern
    * fachlich unzulässigen Zuständen
* stabile, UI-verwertbare Fehlerstruktur

---

## 3.3 Teil B – Produktive Auth-Lifecycle-Stufe (ursprünglicher Sprint-8-Kern)

### B1. Token-Modell präzisieren und umsetzen

* klare Trennung zwischen:

    * **Access Token** (kurzlebiges JWT für API-Zugriffe)
    * **Refresh-/Session-Kontext** (serverseitig kontrollierbar, widerrufbar)

### B2. Session-/Refresh-Domäne minimal einführen

* minimale persistente Repräsentation eines aktiven Login-Kontexts
* keine Persistenz von Access Tokens
* keine Blacklist für Access Tokens
* nur kontrollierbare Refresh-/Session-Metadaten

### B3. Produktive Auth-Endpunkte ergänzen

* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`

### B4. Session-Invalidierung

* gezielte Invalidierung einzelner aktiver Session-Kontexte
* globale Invalidierung aller aktiven Session-Kontexte eines Users
* definierte Reaktion bei:

    * Logout
    * Logout-all
    * Benutzer-Deaktivierung
    * Passwortänderung (mindestens konzeptionell vorbereiten; idealerweise direkt umsetzen)

### B5. Test- und Fehlerbild absichern

* positive und negative Integrationstests für Refresh / Logout / Logout-all
* deterministische Fehlerszenarien
* keine Regression des bestehenden Login-/JWT-Verhaltens

---

## 3.4 Explizit nicht im Scope

Sprint 8 umfasst **nicht**:

* vollständigen OAuth2 Authorization Server
* OIDC Discovery / JWKS / Token Introspection als externer Standardserver
* Key Rotation / Key Management Ausbau
* asymmetrische Signaturmigration (RS256/ES256)
* Passwort-Reset-Flow per E-Mail
* E-Mail-Verification
* Multi-Tenancy / Mandantenmodell
* Device-Management-UI
* Audit-Vollausbau / SIEM-Integration
* Token-Blacklisting für Access Tokens

Diese Themen bleiben bewusst außerhalb des Sprint-8-Scope, um den Service leichtgewichtig zu halten.

---

# 4. Leitentscheidung für Sprint 8

## 4.1 Architekturprinzip

Der IDM bleibt ein **leichtgewichtiger, kontrollierter AuthN/AuthZ-Service**.

Das bestehende JWT-Modell wird **nicht ersetzt**, sondern um einen serverseitig kontrollierbaren Session-Layer ergänzt.

Zusätzlich werden die bestehenden Management-APIs auf einen **UI-fähigen, generischen Listen- und Fehlerbild-Standard** gehoben.

Verbindliche Leitentscheidungen:

* **Access Token** bleibt kurzlebig und stateless.
* **Refresh-/Session-Kontext** wird serverseitig kontrolliert.
* **Keine Persistenz von Access Tokens.**
* **Keine globale Access-Token-Blacklist.**
* Widerrufbarkeit wird über den Session-/Refresh-Layer gelöst.
* Management- und Relations-Listen müssen für generische UI-Komponenten konsistent und stabil nutzbar sein.
* Fehlerbilder müssen fachlich deterministisch und UI-verwertbar sein.

Damit bleibt die Lösung:

* MVP-fähig,
* produktionsfähig,
* horizontal skalierbar,
* architekturkonform,
* deutlich leichter als ein vollständiger OAuth2-Server.

---

# 5. Fachlich-technisches Zielbild Sprint 8

## 5.1 Admin-UI-Fähigkeit nach Sprint 8 (neu verbindlich)

Nach Sprint 8 muss ein Admin-UI unmittelbar und ohne projektspezifische Workarounds auf die IDM-API aufsetzen können.

Das bedeutet verbindlich:

1. zentrale Listen liefern serverseitig:

    * Pagination
    * Sortierung
    * Filterung
2. Listen-Responses sind für generische UI-Komponenten stabil verwendbar
3. Relationen / Zuordnungen sind lesbar und anzeigbar
4. erwartbare fachliche Fehler werden als UI-taugliche 4xx-/fachliche Fehlerbilder geliefert
5. technische Persistenz-/Constraint-Fehler dürfen nicht den öffentlichen API-Vertrag bilden

---

## 5.2 Login-Flow nach Sprint 8

### Vor Sprint 8 (Ist)

1. Client sendet Credentials an `POST /auth/login`
2. IDM validiert Credentials
3. IDM liefert Access Token (JWT)

### Nach Sprint 8 (Ziel)

1. Client sendet Credentials an `POST /auth/login`
2. IDM validiert Credentials und Login-Schutz
3. IDM erzeugt:

    * **Access Token (JWT)**
    * **Refresh-/Session-Kontext**
4. IDM liefert:

    * Access Token
    * Refresh Token oder funktional äquivalente Session-Referenz
    * optional Session-Metadaten (z. B. expiresAt)

---

## 5.3 Refresh-Flow

1. Client ruft `POST /auth/refresh` auf
2. IDM validiert den Refresh-/Session-Kontext
3. IDM prüft:

    * Session aktiv?
    * nicht widerrufen?
    * nicht abgelaufen?
    * User noch aktiv?
    * Scope-Kontext noch gültig?
4. IDM stellt neuen Access Token aus
5. optional / empfohlen: Rotation des Refresh-Tokens in Phase 2 oder als sauber gekapselte Erweiterung

### Sprint-8-Entscheidung

* **Rotation ist optional**, aber die Architektur muss sie später sauber erlauben.
* Für Sprint 8 reicht eine **nicht-rotierende, aber serverseitig widerrufbare Refresh-Session**, wenn dadurch die Umsetzung klarer und regressionsärmer bleibt.

---

## 5.4 Logout-Flow

### `POST /auth/logout`

* invalidiert den **aktuellen Session-/Refresh-Kontext**
* bestehende Access Tokens laufen kurzlebig aus
* keine nachträgliche Access-Token-Blacklist

### `POST /auth/logout-all`

* invalidiert **alle aktiven Session-/Refresh-Kontexte des Benutzers**
* bestehende Access Tokens laufen aus
* nachfolgende Refresh-Versuche schlagen deterministisch fehl

---

# 6. Minimales Datenmodell für Sprint 8

## 6.1 Neue fachlich-technische Repräsentation

Sprint 8 führt **eine minimale Session-/Refresh-Entity** ein.

Bezeichner ist noch nicht festzulegen; zulässige Zielnamen wären z. B.:

* `UserSession`
* `RefreshSession`
* `AuthSession`

**Wichtig:** Der finale Name wird erst anhand des realen Codes festgelegt. Im Plan wird bewusst kein Klassenname als bereits existent vorausgesetzt.

## 6.2 Mindestattribute (fachlich)

Die Session-Repräsentation muss mindestens abbilden:

* Referenz auf `UserAccount`
* Referenz auf `ApplicationScope`
* serverseitiger Session-Identifier oder gehashter Refresh-Identifier
* Erzeugungszeitpunkt
* Ablaufzeitpunkt
* Status (aktiv / widerrufen / abgelaufen)
* optional: `revokedAt`
* optional: `revokedReason`

## 6.3 Verbindliche Sicherheitsregel

* **Refresh-Token-Werte dürfen nicht im Klartext persistiert werden.**
* Falls ein tatsächlicher Refresh-Token an den Client ausgegeben wird, ist serverseitig nur ein **Hash / Fingerprint / Token-Selector-Konzept** zulässig.

---

# 7. API-Schnittstellen Sprint 8

## 7.1 Neue / erweiterte Management- und Relations-APIs (neu verbindlich)

### Zielbild

Die bestehenden Management- und Relations-Endpunkte bleiben grundsätzlich stabil, werden aber um die für das Admin-UI notwendige Listen- und Read-Fähigkeit ergänzt bzw. auf einen konsistenten Listenstandard gehoben.

Verbindliche Ziele:

* bestehende Listen-Endpunkte für Benutzer, Rollen und Scopes auf den neuen Listenstandard heben
* bestehende Relation-Read-Endpunkte auf denselben Listenstandard heben
* fehlende Relation-Read-Endpunkte ergänzen
* keine unnötige Pfad-Neustrukturierung ohne Baseline-Zwang

### Mindestfachlichkeit

* Benutzer-Liste: filterbar / sortierbar / paginierbar
* Rollen-Liste: filterbar / sortierbar / paginierbar
* Scopes-Liste: filterbar / sortierbar / paginierbar
* User ↔ Scope: lesbar / anzeigbar
* User ↔ Role: lesbar / anzeigbar
* Role ↔ Permission: lesbar / anzeigbar (für sauberes Rollen-/Rechte-UI fachlich stark empfohlen)

**Wichtig:**

* Konkrete Pfade, Request-Parameter, DTOs und Response-Wrapper werden **erst in Phase 1 gegen die reale Baseline** final festgelegt.
* Im Plan werden bewusst keine neuen Klassen, DTOs oder Endpunkt-Signaturen als bereits existent vorausgesetzt.

---

## 7.2 Neue Auth-Endpunkte

### `POST /auth/refresh`

Ziel:

* Erzeugt einen neuen Access Token auf Basis eines gültigen Refresh-/Session-Kontexts.

Antwort mindestens:

* neuer Access Token
* optional: aktualisierte Session-/Refresh-Metadaten

### `POST /auth/logout`

Ziel:

* Beendet die aktuelle Session.

Antwort:

* `204 No Content` oder schlanke Success-Response

### `POST /auth/logout-all`

Ziel:

* Beendet alle aktiven Sessions des aktuell authentifizierten Benutzers.

Antwort:

* `204 No Content` oder schlanke Success-Response

---

## 7.3 Bestehende Endpunkte (weiterhin verbindlich)

* `POST /auth/login`
* `GET /auth/me`
* bestehende Management-Endpunkte für Scopes / Rollen / Benutzer
* bestehende Assignment-Endpunkte

Wichtig:

* Bestehende Pfade bleiben nach Möglichkeit stabil.
* Keine unnötige Pfad-Neustrukturierung in Sprint 8.
* Erweiterungen erfolgen baseline-konform und regressionsarm.

---

# 8. Zusammenspiel mit GWC und Personnel

Sprint 8 ist nicht nur IDM-intern relevant, sondern bildet die Grundlage für den sauberen Betrieb der parallelen Projekte.

## 8.1 GWC

Der GWC ist als erster relevanter Consumer zu betrachten.

Sprint 8 muss für den GWC ein klares Client-Verhalten ermöglichen:

* Login liefert Access Token + Refresh-/Session-Kontext
* API-Calls verwenden ausschließlich Access Token
* bei Access-Token-Ablauf kann der GWC kontrolliert `POST /auth/refresh` nutzen
* bei Logout wird die Session sauber beendet

Zusätzlich wichtig:

* ein GWC-Admin-UI bzw. administrative Oberflächen können auf generische Listen- und Relations-APIs aufsetzen
* Backend-Fehlerbilder bleiben stabil und UI-tauglich

Wichtig:

* Sprint 8 liefert die **Backend-Verträge**.
* UI-/Storage-Details des GWC sind **nicht Bestandteil** dieses Sprints.

## 8.2 Personnel

Für Personnel ist Sprint 8 relevant, weil:

* Personnel als Fachanwendung IDM-Tokens konsumiert
* spätere langlebigere UI-Sitzungen produktiv einen Refresh-Mechanismus benötigen
* Logout-all und Session-Invalidierung für Admin-/Sicherheitsfälle fachlich relevant sind
* administrative Oberflächen auf stabile Listen- und Relations-Verträge angewiesen sind

Sprint 8 implementiert jedoch **nur IDM-seitige Infrastruktur**, keine Personnel-spezifische Fachlogik.

---

# 9. Phasenmodell Sprint 8

## Phase 1 – Architektur- und Modellpräzisierung (erweitert)

Ziel:

* finalen technischen Schnitt für **MVP-Readiness + Session-/Refresh-Modell** festlegen
* Entity-/DTO-/Service-/Controller-Schnitt minimal und deterministisch definieren
* ADR-Bedarf für Listenstandard / Fehlerbild / Session-Modell prüfen

Ergebnisse:

* Listenstandard fachlich-technisch finalisiert
* Relation-Read-Strategie finalisiert
* Fehlerbild-Strategie finalisiert
* Session-Konzept finalisiert
* Persistenzstrategie finalisiert
* Request-/Response-DTOs festgelegt
* Endpunkt-Verträge präzisiert

---

## Phase 2 – Listenstandard und Management-API-Härtung

Ziel:

* zentrale Management-APIs UI-fähig machen

Ergebnisse:

* Benutzer-Liste generisch nutzbar
* Rollen-Liste generisch nutzbar
* Scopes-Liste generisch nutzbar
* Filter / Sortierung / Pagination deterministisch testbar
* keine unnötigen Regressionen der bestehenden Pfade

---

## Phase 3 – Relation-Read-Completeness und UI-Fehlerbilder

Ziel:

* Relationen vollständig lesbar machen
* Fehlerbilder für Admin-UI stabilisieren

Ergebnisse:

* User ↔ Scope lesbar / anzeigbar
* User ↔ Role auf Listenstandard gehoben
* Role ↔ Permission lesbar / anzeigbar (soweit final als MVP-verbindlich bestätigt)
* Duplicate-Assignments liefern fachlich deterministische 4xx-Fehler
* erwartbare Fehlerfälle sind UI-tauglich

---

## Phase 4 – Login-Erweiterung

Ziel:

* bestehendes Login um Session-Erzeugung erweitern

Ergebnisse:

* Login erzeugt Access Token + Refresh-/Session-Kontext
* bestehende Login-Tests bleiben grün
* keine Regression des aktuellen `/auth/login`

---

## Phase 5 – Refresh-Endpunkt

Ziel:

* `POST /auth/refresh` produktiv und testbar bereitstellen

Ergebnisse:

* gültige Session → neuer Access Token
* widerrufene/abgelaufene Session → deterministischer Fehlerstatus

---

## Phase 6 – Logout / Logout-all

Ziel:

* kontrollierbare Session-Invalidierung

Ergebnisse:

* `POST /auth/logout`
* `POST /auth/logout-all`
* definierte Invalidierungsregeln

---

## Phase 7 – Testhärtung und Regression

Ziel:

* Vollintegration ohne Seiteneffekte

Pflichttests:

### Management / UI-Readiness

* Benutzer-Liste filterbar / sortierbar / paginierbar
* Rollen-Liste filterbar / sortierbar / paginierbar
* Scopes-Liste filterbar / sortierbar / paginierbar
* Relation-Listen lesbar / anzeigbar
* Duplicate-Assignment liefert fachlichen Fehler statt technischem 5xx
* bestehende Management-APIs regressionsfrei

### Auth Lifecycle

* Login erzeugt Session-Kontext
* Refresh mit gültigem Refresh-/Session-Kontext erfolgreich
* Refresh mit ungültigem / widerrufenem / abgelaufenem Kontext schlägt fehl
* Logout invalidiert aktuelle Session
* Logout-all invalidiert alle Sessions
* bestehende `/auth/me`-Funktionalität unverändert

---

# 10. Risiken und Gegenmaßnahmen

## 10.1 Risiko: Sprint wird zu breit

Durch Aufnahme der MVP-Readiness-Themen wird Sprint 8 breiter als ursprünglich geplant.

**Gegenmaßnahme:** strikte Phasentrennung und klare Priorisierung:

1. MVP-Readiness (Listen / Relationen / Fehlerbilder)
2. danach Auth-Lifecycle

Wenn nötig, ist ein sauber definierter Zwischenstand nach Phase 3 herzustellen.

## 10.2 Risiko: Uneinheitlicher Listenstandard

Wenn einzelne Listen unterschiedlich modelliert werden, kann das Admin-UI nicht generisch aufsetzen.

**Gegenmaßnahme:** in Phase 1 einen verbindlichen Listenstandard definieren und für alle relevanten Endpunkte konsistent anwenden.

## 10.3 Risiko: Zu komplexe Token-Architektur

Ein halb implementierter OAuth2-Ansatz würde Komplexität erzeugen, ohne den Projektbedarf zu treffen.

**Gegenmaßnahme:** nur leichtgewichtiger, interner Refresh-/Session-Mechanismus.

## 10.4 Risiko: Falsche Invalidierungsstrategie

Wenn versucht wird, bereits ausgestellte Access Tokens aktiv zu „widerrufen“, entsteht unnötiger technischer Overhead.

**Gegenmaßnahme:** Access Tokens kurzlebig halten; Widerruf nur über Session-/Refresh-Layer.

## 10.5 Risiko: GWC-/Personnel-Integration bleibt unklar

Wenn Backend-Verträge für Listen, Relationen oder Auth nicht eindeutig sind, verschiebt sich die Unklarheit in die Consumer.

**Gegenmaßnahme:** Request-/Response-Verträge in Sprint 8 explizit und stabil definieren.

---

# 11. Definition of Done Sprint 8

Sprint 8 ist abgeschlossen, wenn:

## MVP-Readiness

1. die Management-APIs für **Application Scopes, Rollen und Benutzer** für generische Admin-UI-Komponenten nutzbar sind,
2. relevante Listen serverseitig **filterbar, sortierbar und paginierbar** sind,
3. Relationen / Zuordnungen im MVP fachlich vollständig **lesbar und anzeigbar** sind,
4. UI-relevante Fehlerbilder konsistent, deterministisch und fachlich stabil sind,
5. Duplicate-Assignments keine technischen 5xx-Fehler mehr erzeugen,

## Auth Lifecycle

6. die Trennung zwischen **Access Token** und **Refresh-/Session-Kontext** technisch umgesetzt ist,
7. `POST /auth/login` Access Token plus Refresh-/Session-Kontext liefert,
8. `POST /auth/refresh` implementiert und testabgedeckt ist,
9. `POST /auth/logout` implementiert und testabgedeckt ist,
10. `POST /auth/logout-all` implementiert und testabgedeckt ist,
11. aktive Sessions serverseitig widerrufbar sind,
12. keine Access Tokens serverseitig persistiert oder blacklisted werden,
13. bestehende Login-/JWT-/Management-Tests regressionsfrei grün bleiben,
14. die Lösung mit den V2-Stammdokumenten und den bestehenden ADR-Linien konsistent ist.

---

# 12. Ergebnis / Leitentscheidung

Sprint 8 ist **nicht mehr nur ein Post-MVP-Produktionssprint**, sondern der **verbindliche MVP-Abschlusssprint mit produktiver Auth-Lifecycle-Härtung**.

Er erweitert den erreichten Stand gezielt an den zwei Stellen, die für reale Nutzung am wichtigsten sind:

## A. Nutzbares MVP / Admin-UI-Fähigkeit

* **generische Listenfähigkeit**
* **lesbare Relationen / Zuordnungen**
* **UI-taugliche Fehlerbilder**

## B. Produktive Auth-Härtung

* **kontrollierbarer Session-Lifecycle**
* **sauberer Refresh-Mechanismus**
* **Logout / Logout-all**
* **deterministische Invalidierung ohne Access-Token-Blacklist**

Damit bleibt das IDM:

* leichtgewichtig,
* architekturkonform,
* konsistent zu Personnel und GWC,
* MVP-fähig für ein sofort anschließbares Admin-UI,
* und zugleich deutlich näher an echter Produktionsreife.

---

# 13. Nächster Schritt

Nach Freigabe dieses Planungsdokuments erfolgt die Umsetzung **phasenweise und deterministisch** auf Basis des jeweils aktuellen Textexports.

Vor Beginn der Code-Umsetzung ist zunächst nur **Phase 1 (Architektur- und Modellpräzisierung)** detailliert gegen die reale Baseline herunterzubrechen.

Es werden dabei ausdrücklich **keine Klassen, DTOs, Repositories oder Pfade vorab erfunden**, sondern ausschließlich auf dem real vorhandenen Projektstand aufgebaut.

Wichtig:

* Vor jeder Phase ist der ADR-Bedarf zu prüfen.
* Nach jeder Phase muss ein klarer, testbarer Zwischenstand existieren.
* Wenn sich innerhalb von Phase 1 zeigt, dass der bestehende Plan für Listenstandard oder Fehlerbild-Standard ohne zusätzliche Architekturentscheidung nicht deterministisch umsetzbar ist, wird die Umsetzung vorab gestoppt und per ADR / Klärung abgesichert.
