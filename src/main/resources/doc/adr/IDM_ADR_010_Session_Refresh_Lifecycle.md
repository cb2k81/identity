# IDM – ADR 010: Session & Refresh Lifecycle nach MVP

Stand: 2026-03-27
Status: Accepted (Sprint 8 – verbindliche Architekturentscheidung)
Version: 1.0

---

## 1. Kontext

Der IDM-Service besitzt nach Sprint 7 einen stabilen JWT-basierten Authentifizierungs- und Autorisierungskern.

Die bestehende Architektur ist durch die vorhandenen ADRs verbindlich geprägt:

* JWT-basierte Authentication
* serverseitige Rollen-/Permission-Auflösung
* keine fachliche Autorisierung über Token-Claims
* klare Trennung zwischen Authentication und Authorization

Die aktuelle Baseline enthält bereits:

* `POST /auth/login`
* `GET /auth/me`
* signierte Access Tokens (JWT)
* serverseitige Authorization-Auflösung

Nicht Bestandteil des aktuellen Stands sind jedoch:

* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`
* serverseitig kontrollierbare Session-Invalidierung
* persistierter Refresh-/Session-Kontext

Für Sprint 8 ist verbindlich:

* Nach dem MVP wird die nächste produktionsrelevante Auth-Stufe eingeführt
* Dies soll **ohne** Ausbau zum vollständigen OAuth2-/OIDC-Authorization-Server erfolgen
* Gleichzeitig muss das Modell für reale UI-Sitzungen (z. B. GWC, Personnel) belastbar sein

---

## 2. Problembeschreibung

### 2.1 Aktuelle Grenze des JWT-MVP

Ein reines JWT-Access-Token-Modell ohne serverseitigen Session-/Refresh-Kontext hat für produktive UI-Nutzung klare Grenzen:

1. Access Tokens sind nicht gezielt widerrufbar
2. Logout kann ohne zusätzlichen Serverzustand nur eingeschränkt wirksam sein
3. Logout-all ist nicht sauber realisierbar
4. Längere UI-Sitzungen benötigen entweder lange Access-Token-Laufzeiten oder Re-Login
5. Passwortänderungen / Benutzer-Deaktivierung können nicht gezielt auf aktive Sessions wirken

### 2.2 Nicht gewünschte Überreaktion

Ein vollständiger OAuth2-/OIDC-Server wäre für den IDM aktuell zu schwergewichtig.

Nicht Ziel von Sprint 8:

* OIDC Discovery
* JWKS
* OAuth2 Authorization Code Flow
* Token Introspection als externer Standardserver
* globale Access-Token-Blacklist
* Device-Management-Vollausbau

Es wird eine **leichtgewichtige, kontrollierte interne Produktionsstufe** benötigt.

---

## 3. Entscheidung

### 3.1 Grundsatzentscheidung

Der bestehende JWT-Ansatz bleibt erhalten, wird aber um einen **serverseitig kontrollierbaren Session-/Refresh-Layer** ergänzt.

Verbindliches Modell:

* **Access Token** bleibt kurzlebig und stateless
* **Refresh-/Session-Kontext** wird serverseitig persistiert und kontrolliert
* Widerrufbarkeit erfolgt über den Session-/Refresh-Layer
* Access Tokens werden nicht serverseitig persistiert
* Es gibt keine globale Access-Token-Blacklist

---

### 3.2 Trennung zwischen Access Token und Refresh-/Session-Kontext

Sprint 8 führt eine klare fachlich-technische Trennung ein:

#### Access Token

* JWT
* kurzlebig
* für API-Aufrufe
* stateless
* nicht serverseitig gespeichert

#### Refresh-/Session-Kontext

* serverseitig persistiert
* aktiv / widerrufen / abgelaufen
* kontrollierbar
* Grundlage für `refresh`, `logout`, `logout-all`

Verbindliche Regel:

> Access Token und Refresh-/Session-Kontext sind unterschiedliche Artefakte mit unterschiedlicher Lebensdauer und unterschiedlicher Sicherheitsrolle.

---

### 3.3 Keine Persistenz von Access Tokens

Access Tokens werden nicht serverseitig gespeichert.

Verbindliche Regel:

> Sprint 8 führt keine Access-Token-Persistenz und keine Access-Token-Blacklist ein.

Begründung:

* unnötige Komplexität
* höherer Betriebsaufwand
* nicht erforderlich für das angestrebte leichtgewichtige Modell
* kurzlebige Access Tokens reichen als akzeptabler Trade-off

---

### 3.4 Persistierter Session-/Refresh-Kontext

Sprint 8 führt eine minimale persistente Session-Repräsentation ein.

Die konkrete Java-Klassenbenennung wird in der Code-Umsetzung anhand der realen Baseline finalisiert und ist **nicht Bestandteil dieses ADR als Klassenvertrag**.

Mindestinhalt der Session-Repräsentation:

* Referenz auf `UserAccount`
* Referenz auf `ApplicationScope`, sofern der Login-/Refresh-Kontext scope-bezogen ist
* serverseitiger Session-Identifier und/oder tokenbezogener Selector/Fingerprint
* Erzeugungszeitpunkt
* Ablaufzeitpunkt
* Status (z. B. aktiv / widerrufen / abgelaufen)
* optional: `revokedAt`
* optional: `revokedReason`

Verbindliche Regel:

> Sprint 8 führt genau den minimalen persistierten Session-/Refresh-Zustand ein, der für Refresh, Logout und Logout-all erforderlich ist – nicht mehr.

---

### 3.5 Refresh-Token-Werte nicht im Klartext persistieren

Falls ein tatsächlicher Refresh-Token-Wert an Clients ausgegeben wird, darf dieser serverseitig nicht im Klartext persistiert werden.

Verbindliche Regel:

> Refresh-Token-Werte werden serverseitig nur als Hash, Fingerprint, Selector-Konzept oder funktional äquivalente sichere Repräsentation persistiert – niemals im Klartext.

---

### 3.6 Login wird erweitert, nicht ersetzt

Der bestehende Login-Endpunkt bleibt fachlich erhalten und wird erweitert.

Bestehender Kern:

* Credentials prüfen
* User-State prüfen
* JWT ausstellen

Sprint-8-Erweiterung:

* Session-/Refresh-Kontext erzeugen
* Access Token + Refresh-/Session-Kontext zurückgeben

Verbindliche Regel:

> `POST /auth/login` bleibt der zentrale Einstiegspunkt und wird erweitert, nicht durch einen neuen parallelen Login-Flow ersetzt.

---

### 3.7 Neue Endpunkte

Sprint 8 ergänzt mindestens:

* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`

Verbindliche Zielsemantik:

#### `POST /auth/refresh`

* validiert Session-/Refresh-Kontext
* prüft Aktivität, Ablauf, Widerruf, User-State, Scope-Kontext
* stellt neuen Access Token aus
* Rotation ist in Sprint 8 optional, aber architektonisch später möglich

#### `POST /auth/logout`

* invalidiert den aktuellen Session-/Refresh-Kontext

#### `POST /auth/logout-all`

* invalidiert alle aktiven Session-/Refresh-Kontexte des aktuellen Benutzers

---

### 3.8 Logout-Strategie ohne Access-Token-Blacklist

Nach Logout / Logout-all gilt:

* bestehende Access Tokens werden nicht aktiv „eingesammelt“
* sie laufen regulär mit ihrer kurzen TTL aus
* neue Refresh-Versuche schlagen deterministisch fehl

Verbindliche Regel:

> Logout und Logout-all wirken primär auf den serverseitigen Session-/Refresh-Layer; bestehende Access Tokens leben maximal bis zu ihrer kurzen Restlaufzeit weiter.

---

### 3.9 Sicherheitsrelevante Invalidierungsregeln

Sprint 8 definiert mindestens folgende Invalidierungsregeln:

* explizites Logout → aktuelle Session invalidieren
* Logout-all → alle Sessions des Users invalidieren
* Benutzer-Deaktivierung → aktive Sessions dürfen nicht weiter erfolgreich refreshen
* Passwortänderung → mindestens konzeptionell berücksichtigt; direkte Umsetzung in Sprint 8 ist empfohlen, aber nicht zwingend, wenn dies den Sprint unverhältnismäßig verbreitert

Verbindliche Regel:

> Benutzer-Deaktivierung muss spätestens im Refresh-Pfad sicher wirksam werden; Passwortänderung muss mindestens architektonisch sauber vorbereitet sein.

---

## 4. Konsequenzen

### 4.1 Positive Konsequenzen

* Produktiv nutzbarer Refresh-Mechanismus für UI-Sitzungen
* Sauberes Logout / Logout-all
* Kontrollierbare Session-Invalidierung
* Kein unnötiger Ausbau zum OAuth2-/OIDC-Server
* Konsistent mit bestehender JWT-Architektur
* Gute Passung für GWC und Personnel als Consumer

### 4.2 Trade-offs

* Zusätzliche persistente Domäne / Datenmodell erforderlich
* Zusätzliche Integrationstests notwendig
* Logout wirkt nicht „sofort hart“ auf bereits ausgestellte Access Tokens, sondern über deren kurze Restlaufzeit

---

## 5. Umsetzungsvorgaben für Sprint 8

Sprint 8 setzt diesen ADR in folgender Reihenfolge um:

1. Session-/Refresh-Domäne minimal einführen
2. `POST /auth/login` erweitern
3. `POST /auth/refresh` implementieren
4. `POST /auth/logout` implementieren
5. `POST /auth/logout-all` implementieren
6. Negativtests / Invalidierungsregeln absichern

Verbindliche Regel:

> Sprint 8 implementiert zuerst den minimalen Session-/Refresh-Kern und erst danach die Endpunkte darauf aufbauend.

---

## 6. Abgrenzung

Nicht Bestandteil dieses ADR:

* vollständiger OAuth2 Authorization Server
* OIDC Discovery / JWKS
* OAuth2 Authorization Code Flow
* externe Token Introspection
* Key Rotation / asymmetrische Signaturen (bleibt ADR-004 / spätere Stufe)
* Passwort-Reset per E-Mail
* E-Mail-Verification
* Device-Management-UI
* SIEM-/Audit-Vollausbau
* globale Access-Token-Blacklist

---

## 7. Beziehung zu bestehenden ADRs

Dieser ADR ergänzt und konkretisiert insbesondere:

* **ADR-003** – Authentication & Authorization Architecture
* **ADR-004** – JWT Hardening
* **ADR-006** – Bootstrap / Initial Data Strategy
* **ADR-007** – Domain Permissions / Rollen-zu-Rechte-Auflösung

Wichtig:

* ADR-010 ersetzt die bestehende JWT-Architektur nicht.
* ADR-010 ergänzt die JWT-Architektur um eine kontrollierbare Session-/Refresh-Stufe.
* Die in ADR-004 festgelegte Trennung „Autorisierung nicht aus Token-Claims“ bleibt unverändert.

---

## 8. Geltung für Consumer (GWC / Personnel)

Sprint 8 schafft damit einen klaren Backend-Vertrag für Consumer-Anwendungen.

### GWC

* Login → Access Token + Refresh-/Session-Kontext
* API-Aufrufe → nur Access Token
* bei Ablauf → `POST /auth/refresh`
* Logout → `POST /auth/logout`

### Personnel

* gleicher technischer Vertrag
* langlebigere UI-Sitzungen werden ohne lange Access-Token-TTL möglich
* Sicherheitsfälle wie Logout-all werden IDM-seitig kontrollierbar

Wichtig:

> Sprint 8 definiert nur den IDM-seitigen Backend-Vertrag, nicht die UI-/Storage-Strategie der Consumer.

---

## 9. Status / Verbindlichkeit

* Accepted (Sprint 8)
* Verbindliche Grundlage für die Post-MVP-Produktionsstufe des IDM
* Muss vor der Code-Umsetzung von Refresh / Logout / Logout-all berücksichtigt werden

---

## 10. Zusammenfassung

Sprint 8 erweitert das JWT-MVP des IDM gezielt um eine produktionsrelevante Session-/Refresh-Stufe.

Die verbindliche Linie lautet:

* JWT-Access-Token bleibt kurzlebig und stateless
* Session-/Refresh-Kontext wird serverseitig kontrolliert
* keine Access-Token-Persistenz
* keine Access-Token-Blacklist
* `login`, `refresh`, `logout`, `logout-all` bauen auf diesem Modell auf

Damit bleibt das IDM leichtgewichtig, architekturkonform und deutlich näher an echter Produktionsreife, ohne unnötig zu einem vollwertigen OAuth2-/OIDC-Server ausgebaut zu werden.