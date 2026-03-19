# IDM Sprint 8 - Planung

Stand: 2026-03-19
Sprint: 8
Ziel: Produktive Session-/Token-Lifecycle-Stufe nach MVP
Status: Geplant

---

# 1. Zielsetzung des Sprints

Sprint 8 verfolgt das Ziel, den nach Sprint 7 erreichten **MVP-fähigen IDM-Stand** um die **nächste zwingend produktionsrelevante Authentifizierungsstufe** zu ergänzen.

Der aktuelle Stand ist für einen sauberen MVP-Release fachlich und technisch belastbar, bildet jedoch noch nicht den vollständigen produktiven Lebenszyklus von Tokens und Sitzungen ab.

Sprint 8 schließt genau diese Lücke – **ohne** den IDM-Service in Richtung vollwertiger OAuth2-/OIDC-Server zu überfrachten.

Kernziel von Sprint 8 ist daher die Einführung eines **klaren, kontrollierbaren und widerrufbaren Session-/Refresh-Konzepts** als Ergänzung zum bestehenden JWT-Access-Token-Modell.

Sprint 8 ist erfolgreich abgeschlossen, wenn:

1. der Unterschied zwischen **Access Token** und **Refresh-/Session-Kontext** fachlich und technisch eindeutig umgesetzt ist,
2. ein produktiv nutzbarer **Refresh-Flow** existiert,
3. aktive Sessions gezielt invalidiert werden können,
4. Logout und Logout-all deterministisch funktionieren,
5. die Lösung mit dem aktuellen IDM-Baseline-Modell, den V2-Stammdokumenten und der geplanten Nutzung durch **Personnel** und **GWC** konsistent bleibt.

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

Nicht als bereits vollständig umgesetzt zu behandeln sind aktuell:

* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`
* kontrollierbare Session-Invalidierung
* persistierter Refresh-/Session-Kontext
* Passwort-Reset- und E-Mail-Verification-Flows

Diese Punkte sind in den V2-Stammdokumenten bereits als **nächste produktive Härtungsstufe** definiert und werden in Sprint 8 nun gezielt operationalisiert.

---

# 3. Scope von Sprint 8

## 3.1 Im Scope (verbindlich)

Sprint 8 umfasst ausschließlich die **nächste produktive Auth-Lifecycle-Stufe**:

### A. Token-Modell präzisieren und umsetzen

* klare Trennung zwischen:

    * **Access Token** (kurzlebiges JWT für API-Zugriffe)
    * **Refresh-/Session-Kontext** (serverseitig kontrollierbar, widerrufbar)

### B. Session-/Refresh-Domäne minimal einführen

* minimale persistente Repräsentation eines aktiven Login-Kontexts
* keine Persistenz von Access Tokens
* keine Blacklist für Access Tokens
* nur kontrollierbare Refresh-/Session-Metadaten

### C. Produktive Auth-Endpunkte ergänzen

* `POST /auth/refresh`
* `POST /auth/logout`
* `POST /auth/logout-all`

### D. Session-Invalidierung

* gezielte Invalidierung einzelner aktiver Session-Kontexte
* globale Invalidierung aller aktiven Session-Kontexte eines Users
* definierte Reaktion bei:

    * Logout
    * Logout-all
    * Benutzer-Deaktivierung
    * Passwortänderung (mindestens konzeptionell vorbereiten; idealerweise direkt umsetzen)

### E. Test- und Fehlerbild absichern

* positive und negative Integrationstests für Refresh / Logout / Logout-all
* deterministische Fehlerszenarien
* keine Regression des bestehenden Login-/JWT-Verhaltens

---

## 3.2 Explizit nicht im Scope

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

Verbindliche Leitentscheidung:

* **Access Token** bleibt kurzlebig und stateless.
* **Refresh-/Session-Kontext** wird serverseitig kontrolliert.
* **Keine Persistenz von Access Tokens.**
* **Keine globale Access-Token-Blacklist.**
* Widerrufbarkeit wird über den Session-/Refresh-Layer gelöst.

Damit bleibt die Lösung:

* produktionsfähig,
* horizontal skalierbar,
* architekturkonform,
* deutlich leichter als ein vollständiger OAuth2-Server.

---

# 5. Fachlich-technisches Zielbild Sprint 8

## 5.1 Login-Flow nach Sprint 8

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

## 5.2 Refresh-Flow

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

## 5.3 Logout-Flow

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

## 7.1 Neue Endpunkte

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

## 7.2 Bestehende Endpunkte (weiterhin verbindlich)

* `POST /auth/login`
* `GET /auth/me`

Wichtig:

* Bestehende Pfade bleiben stabil.
* Keine unnötige Pfad-Neustrukturierung in Sprint 8.

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

Wichtig:

* Sprint 8 liefert die **Backend-Verträge**.
* UI-/Storage-Details des GWC sind **nicht Bestandteil** dieses Sprints.

## 8.2 Personnel

Für Personnel ist Sprint 8 relevant, weil:

* Personnel als Fachanwendung IDM-Tokens konsumiert
* spätere langlebigere UI-Sitzungen produktiv einen Refresh-Mechanismus benötigen
* Logout-all und Session-Invalidierung für Admin-/Sicherheitsfälle fachlich relevant sind

Sprint 8 implementiert jedoch **nur IDM-seitige Infrastruktur**, keine Personnel-spezifische Fachlogik.

---

# 9. Phasenmodell Sprint 8

## Phase 1 – Architektur- und Modellpräzisierung

Ziel:

* finaler technischer Schnitt für Session-/Refresh-Modell festlegen
* Entity-/DTO-/Service-Schnitt minimal und deterministisch definieren

Ergebnisse:

* Session-Konzept finalisiert
* Persistenzstrategie finalisiert
* Request-/Response-DTOs festgelegt
* Endpunkt-Verträge präzisiert

---

## Phase 2 – Login-Erweiterung

Ziel:

* bestehendes Login um Session-Erzeugung erweitern

Ergebnisse:

* Login erzeugt Access Token + Refresh-/Session-Kontext
* bestehende Login-Tests bleiben grün
* keine Regression des aktuellen `/auth/login`

---

## Phase 3 – Refresh-Endpunkt

Ziel:

* `POST /auth/refresh` produktiv und testbar bereitstellen

Ergebnisse:

* gültige Session → neuer Access Token
* widerrufene/abgelaufene Session → deterministischer Fehlerstatus

---

## Phase 4 – Logout / Logout-all

Ziel:

* kontrollierbare Session-Invalidierung

Ergebnisse:

* `POST /auth/logout`
* `POST /auth/logout-all`
* definierte Invalidierungsregeln

---

## Phase 5 – Testhärtung und Regression

Ziel:

* Vollintegration ohne Seiteneffekte

Pflichttests:

* Login erzeugt Session-Kontext
* Refresh mit gültigem Refresh-/Session-Kontext erfolgreich
* Refresh mit ungültigem / widerrufenem / abgelaufenem Kontext schlägt fehl
* Logout invalidiert aktuelle Session
* Logout-all invalidiert alle Sessions
* bestehende `/auth/me`-Funktionalität unverändert
* bestehende Management-APIs regressionsfrei

---

# 10. Risiken und Gegenmaßnahmen

## 10.1 Risiko: Sprint wird zu breit

Wenn Passwort-Reset, E-Mail-Verification, Key-Rotation oder asymmetrische Signatur zusätzlich aufgenommen werden, verliert Sprint 8 seinen klaren Fokus.

**Gegenmaßnahme:** Sprint 8 strikt auf Session-/Refresh-Lifecycle begrenzen.

## 10.2 Risiko: Zu komplexe Token-Architektur

Ein halb implementierter OAuth2-Ansatz würde Komplexität erzeugen, ohne den Projektbedarf zu treffen.

**Gegenmaßnahme:** nur leichtgewichtiger, interner Refresh-/Session-Mechanismus.

## 10.3 Risiko: Falsche Invalidierungsstrategie

Wenn versucht wird, bereits ausgestellte Access Tokens aktiv zu „widerrufen“, entsteht unnötiger technischer Overhead.

**Gegenmaßnahme:** Access Tokens kurzlebig halten; Widerruf nur über Session-/Refresh-Layer.

## 10.4 Risiko: GWC-Integration bleibt unklar

Wenn die Backend-Verträge nicht eindeutig sind, verschiebt sich die Unklarheit in den GWC.

**Gegenmaßnahme:** Request-/Response-Verträge in Sprint 8 explizit und stabil definieren.

---

# 11. Definition of Done Sprint 8

Sprint 8 ist abgeschlossen, wenn:

1. die Trennung zwischen **Access Token** und **Refresh-/Session-Kontext** technisch umgesetzt ist,
2. `POST /auth/login` Access Token plus Refresh-/Session-Kontext liefert,
3. `POST /auth/refresh` implementiert und testabgedeckt ist,
4. `POST /auth/logout` implementiert und testabgedeckt ist,
5. `POST /auth/logout-all` implementiert und testabgedeckt ist,
6. aktive Sessions serverseitig widerrufbar sind,
7. keine Access Tokens serverseitig persistiert oder blacklisted werden,
8. bestehende Login-/JWT-/Management-Tests regressionsfrei grün bleiben,
9. die Lösung mit den V2-Stammdokumenten und den bestehenden ADR-Linien konsistent ist.

---

# 12. Ergebnis / Leitentscheidung

Sprint 8 ist der **erste echte Post-MVP-Produktionssprint** des IDM.

Er erweitert den erreichten MVP nicht breitflächig, sondern gezielt an genau der Stelle, die für reale produktive Nutzung am wichtigsten ist:

* **kontrollierbarer Session-Lifecycle**
* **sauberer Refresh-Mechanismus**
* **Logout / Logout-all**
* **deterministische Invalidierung ohne Access-Token-Blacklist**

Damit bleibt das IDM:

* leichtgewichtig,
* architekturkonform,
* konsistent zu Personnel und GWC,
* und zugleich deutlich näher an echter Produktionsreife.

---

# 13. Nächster Schritt

Nach Freigabe dieses Planungsdokuments erfolgt die Umsetzung **phasenweise und deterministisch** auf Basis des jeweils aktuellen Textexports.

Vor Beginn der Code-Umsetzung ist zunächst nur **Phase 1 (Architektur- und Modellpräzisierung)** detailliert gegen die reale Baseline herunterzubrechen.

Es werden dabei ausdrücklich **keine Klassen, DTOs, Repositories oder Pfade vorab erfunden**, sondern ausschließlich auf dem real vorhandenen Projektstand aufgebaut.
