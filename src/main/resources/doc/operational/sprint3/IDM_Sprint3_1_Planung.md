# IDM – Sprint 3 Planungsdokument (IDM_Sprint3_1_Planung)

Stand: 2026-02-20  (Baseline: `idm_code-export_2026-02-20_11-50-29` + Referenz: `personnel_code-export_2026-02-20_11-42-23`)

## 1. Kontext / Klarstellung (Scope-Fix)

Für das IDM-MVP wird **keine Personen-Domäne** benötigt. Sprint 3 fokussiert **ausschließlich**:

* **Anmeldung (Login)** gegen IDM
* **JWT-Erzeugung** (Token-Ausgabe), damit externe Apps authentifizieren können

Konsequenz:

* Alle bisherigen **Person-Strukturen im IDM** dürfen (für Übersichtlichkeit) zunächst entfernt werden.

---

## 2. Ziel des Sprints (präzise definiert)

Sprint 3 implementiert eine **technisch vollständige, minimal lauffähige Authentifizierungsarchitektur (MVP)** für den IDM-Service.

Das Ziel ist erreicht, wenn folgende Bedingungen gleichzeitig erfüllt sind:

1. Es existiert eine persistente **User-Konto-Domäne** (keine Person-Domäne).
2. Ein externer Client kann über `POST /api/auth/login` gültige Credentials senden.
3. IDM prüft die Credentials deterministisch gegen persistierte User-Daten.
4. IDM erzeugt ein **signiertes JWT** mit definierter Laufzeit.
5. Geschützte Endpunkte akzeptieren ausschließlich Requests mit gültigem `Authorization: Bearer <token>` Header.
6. Die Security-Architektur entspricht strukturell den in `personnel` etablierten Architekturprinzipien (ADR-basiert).

Nicht Ziel des Sprints:

* OAuth2 Authorization Server
* Refresh Tokens
* Rollen- oder Berechtigungsmodell auf Fachlogik-Ebene
* Personenverwaltung

---

## 3. Baseline-Analyse (IST-Stand) Baseline-Analyse (IST-Stand)

### 3.1 IDM – aktueller Zustand (relevante Funde)

* Spring Boot 3.3.10, Java 17.
* `spring-boot-starter-security` ist enthalten, aber es existiert **keine** erkennbare HTTP-Security-Konfiguration im Export.
* Swagger ist vorhanden und deklariert aktuell ein **API-Key Header Schema** (`X-API-Token`), jedoch ohne nachweisbare serverseitige Durchsetzung (nur OpenAPI-Deklaration).
* IDM enthält aktuell eine **Person-Domäne** inkl. Repository/Mapper/Domain-Service-Skizze und DTOs; IDs werden im Domain-Service per `UUID.randomUUID()` gesetzt (was perspektivisch nicht zu den im personnel etablierten ADR-Prinzipien passt).

### 3.2 Personnel – relevanter Referenzstand

Der `personnel`-Export enthält einen deutlich weiter entwickelten technischen Kern (`system`):

* `system/security` inkl.

  * `MethodSecurityConfig` (EnableMethodSecurity)
  * `HttpSecurityConfig` (DEV-Profil, alle Endpunkte offen)
  * `DevAuthenticationFilter` (DEV-Profil, injiziert Default-User + Authorities)
* ADRs als belastbare Referenz für Architektur/Governance, u. a.:

  * **ADR 004** Layerarchitektur (Controller → Domain Service → Entity Service → Repository)
  * **ADR 007** Error- und Paging-Standards
  * **ADR 009** Command Permissions
  * **ADR 010** Record-Level Security
  * weitere ADRs zur Basiskern-Governance (u. a. ID/Audit, Update/Versionierung)

---

## 4. Zielbild-Architektur für IDM (Sprint 3)

### 4.1 Minimaler Security-Stack (MVP)

* **Login-Endpoint** (öffentlich): nimmt Credentials entgegen, validiert User-Konto, gibt JWT zurück.
* **JWT-Validierung** (für geschützte Endpunkte): Filter/Resource-Server-Konfiguration, die JWT prüft und SecurityContext setzt.
* **DEV-Profile** Unterstützung: so wie im `personnel`-Projekt (offene Endpunkte + optionaler DevAuthenticationFilter), damit Entwicklung/Tests deterministisch möglich bleiben.

### 4.2 Architekturprinzipien gemäß ADR 001 (personnel)

ADR 001 (personnel) definiert die grundlegende technische Basissystematik:

* Klare Layertrennung (Controller → Domain Service → Entity Service → Repository)
* Keine Vermischung von technischen und fachlichen Verantwortlichkeiten
* Security als systemnaher technischer Kern
* Konfigurationsgetriebene Profile (DEV/Produktiv)
* Erweiterbarkeit ohne strukturelle Brüche

Für IDM Sprint 3 bedeutet das verbindlich:

1. Authentifizierung ist ein technischer Use Case im Domain Service.
2. Passwort-Hashing ist Infrastruktur, nicht Fachlogik.
3. JWT-Erzeugung ist ein technischer Service im `system`-Kontext.
4. Security-Konfiguration ist strikt vom Domain-Code getrennt.
5. Controller enthalten keine Authentifizierungslogik.

Diese Prinzipien sind nicht optional, sondern strukturelle Vorgabe.

---

### 4.3 Strategie: Übernahme des `system` Packages aus personnel

Option A – Selektive Adaption

* Einzelne Klassen übernehmen
* Strukturen manuell nachbauen

Risiko:

* Architektur-Drift zwischen personnel und IDM
* Inkonsistenzen im Security-Kern
* Spätere Integrationsprobleme

Option B – 1:1 Übernahme des `system` Packages (empfohlen)

* Komplettes `de.cocondo.app.system` Package aus personnel übernehmen
* Bestehendes IDM-`system` Package überschreiben
* Danach nur minimal notwendige Anpassungen für JWT ergänzen

Vorteile:

* Architektur-Konsistenz zwischen Projekten
* ADR-Konformität ohne Interpretation
* Keine partielle oder inkonsistente Implementierung
* Reduzierte Wartungskosten langfristig

Technische Voraussetzung:

* Abhängigkeiten aus personnel (z. B. jjwt) werden deterministisch in IDM ergänzt
* Keine stillschweigende funktionale Erweiterung

Entscheidung für Sprint 3:

→ Das `system` Package aus personnel wird **vollständig übernommen** und dient als technische Baseline für IDM.
→ Darauf aufbauend wird die JWT-Authentifizierungslogik ergänzt.

---

### 4.4 Entfernen der Person-Domäne im IDM

* Person-Aggregat/DTOs/Mapper/Services/Repository werden aus dem IDM entfernt oder isoliert (MVP-Scope).
* Danach bleibt im Domain-Layer ausschließlich das, was für **User-Konten + Auth** benötigt wird.

---

## 5. Sprint-3 Scope

### 5.1 In Scope (verbindlich)

1. **User-Konto-Domäne (minimal)**

* Persistente Entity für User-Konto (Username/Login + Passwort-Hash + Status)
* Repository + Entity Service + Domain Service nach Layerprinzip

2. **Auth API**

* `POST /api/auth/login`

  * Request: Credentials
  * Response: JWT + Metadaten (z. B. expiresAt)

3. **JWT-Erzeugung**

* Signierter JWT (Symmetric key im MVP)
* Claims mindestens:

  * subject (user id / username)
  * issuedAt, expiresAt
  * optional: roles/permissions (als Vorbereitung)

4. **JWT-Validierung**

* HTTP-Security-Konfiguration:

  * `/api/auth/login` ist `permitAll`
  * Rest: JWT erforderlich (mindestens für `/api/**`)

5. **Technischer Basiskern (Teilübernahme aus personnel)**

* `EnableMethodSecurity` in IDM (wie personnel)
* DEV-Security-Konfiguration analog personnel (DEV offen + optional DevAuthenticationFilter)

6. **Dokumentation**

* Sprint 3 Abschluss mit Plan + Review + ggf. minimaler Testplan (analog der Vorgehensweise in personnel)

### 5.2 Out of Scope (explizit)

* Personenverwaltung / Person-Aggregat in IDM
* RLS (fachliche Regeln) – höchstens technische Vorbereitungen, falls ohnehin durch System-Package mitgebracht
* vollständiges Permission-Modell + Admin UI
* Refresh Tokens / OAuth2 Authorization Server (MVP nicht)
* Multi-Tenant / Mandantenfähigkeit

---

## 6. ToDos / Backlog-Struktur (Sprint 3)

### Epic E1 – Code-Bereinigung (Scope: „keine Personen“)

**Ziel:** IDM wird übersichtlich, Person-Domäne verschwindet aus MVP.

* [ ] E1.1 Analyse: Welche IDM-Pakete/Controller referenzieren `domain/idm/person`?
* [ ] E1.2 Entfernen: Person Entity/Repository/Mapper/DTOs/Services + ggf. Controller
* [ ] E1.3 Build fix: Projekt muss nach Entfernen weiterhin bauen.

**Abnahme:** `mvn clean test` läuft (oder mindestens `mvn clean package`, je nach Testbestand im IDM).

---

### Epic E2 – System-Kern konsolidieren (Übernahme aus personnel)

**Ziel:** IDM erhält eine robuste technische Basis wie personnel.

* [ ] E2.1 Übernahme `de.cocondo.app.system.security.MethodSecurityConfig`
* [ ] E2.2 DEV HTTP-Security analog `personnel` (`@Profile("dev")`, `permitAll`, CSRF off, form/basic off)
* [ ] E2.3 Optional (DEV): `DevAuthenticationFilter` übernehmen, aber Authorities an IDM-Context anpassen (für Sprint 3 vorerst minimal/leer oder Auth-spezifisch)
* [ ] E2.4 Swagger-Security: OpenAPI-Schema von `API-Token` auf `Bearer JWT` umstellen (oder ergänzen), konsistent zur Implementierung.

**Abnahme:** DEV-Profil startet deterministisch ohne echte Auth; Security-Mechanik ist strukturell vorhanden.

---

### Epic E3 – User-Konto-Domäne (minimal)

**Ziel:** Persistente User-Konten als Auth-Grundlage.

* [ ] E3.1 Datenmodell definieren (minimal):

  * username/login
  * passwordHash
  * enabled/locked (oder minimal enabled)
  * technische ID
* [ ] E3.2 Liquibase-Changelog ergänzen/anlegen (deterministisch nach vorhandenem IDM-Liquibase-Setup)
* [ ] E3.3 Repository + EntityService
* [ ] E3.4 DomainService: Use Cases

  * create user (initial, ggf. nur per Dev Seed)
  * load user by username

**Abnahme:** User kann in DB persistiert und wieder geladen werden.

---

### Epic E4 – Auth API + JWT

**Ziel:** Login + Token-Ausgabe.

* [ ] E4.1 Request/Response DTOs:

  * `LoginRequestDTO` (username, password)
  * `LoginResponseDTO` (token, tokenType=bearer, expiresAt)
* [ ] E4.2 Passwort-Hashing:

  * `PasswordEncoder` (BCrypt) als Bean
  * DomainService prüft Password gegen Hash
* [ ] E4.3 JWT Service:

  * secret/key aus Config
  * token TTL
  * sign + verify
* [ ] E4.4 Controller `/api/auth/login`

  * validiert Request
  * ruft DomainService
  * liefert Response

**Abnahme:** `POST /api/auth/login` liefert für gültige Credentials ein JWT.

---

### Epic E5 – JWT-Validierung in HTTP Security

**Ziel:** Geschützte Endpunkte funktionieren unter JWT.

* [ ] E5.1 SecurityFilterChain (non-dev):

  * permitAll: `/api/auth/login`, swagger endpoints
  * authenticated: `/api/**`
* [ ] E5.2 JWT Authentication Filter:

  * liest `Authorization: Bearer <token>`
  * validiert
  * setzt Authentication im SecurityContext

**Abnahme:** Zugriff auf geschützten Dummy-Endpoint ohne JWT → 401; mit JWT → 200.

---

### Epic E6 – Tests (minimal, sicherheitsrelevant)

**Ziel:** deterministische Absicherung des MVP-Flows.

* [ ] E6.1 Positive Login: valid user → token returned
* [ ] E6.2 Negative Login: wrong password → 401
* [ ] E6.3 Protected endpoint: no token → 401
* [ ] E6.4 Protected endpoint: valid token → 200

---

## 7. Abnahmekriterien (Definition of Done)

### Funktional

* [ ] Keine Person-Strukturen mehr im IDM-MVP-Codepfad.
* [ ] `POST /api/auth/login` existiert und liefert ein signiertes JWT.
* [ ] JWT kann serverseitig validiert werden und schützt `/api/**`.
* [ ] DEV-Profil ermöglicht deterministisches Arbeiten ohne echte Auth-Flows (analog personnel).

### Architektur/Qualität

* [ ] `system`-Security-Basis ist konsistent (EnableMethodSecurity, DEV-Konfig).
* [ ] Swagger Security ist konsistent zur Implementierung (Bearer JWT).
* [ ] Keine Annahmen/Erfindungen außerhalb der Baseline.
* [ ] Projekt ist baubar.

---

## 8. Offene Punkte / Risiken (deterministisch aus Baseline)

1. IDM enthält aktuell nur Swagger-API-Token-Schema, aber keine serverseitige Enforcement-Schicht – das muss ersetzt/ergänzt werden.
2. IDM verwendet derzeit Person-Code (inkl. ID-Generierung im Domain-Service). Der Sprint setzt explizit auf Entfernung und ersetzt das durch Auth-User-Domäne.
3. Abhängigkeiten: `personnel` nutzt u. a. `jjwt` Version 0.11.5. IDM hat diese Dependency aktuell nicht.

---

## 9. Referenzen (aus personnel, zur Übernahme/Orientierung)

* `de.cocondo.app.system.security.MethodSecurityConfig`
* `de.cocondo.app.system.security.HttpSecurityConfig` (DEV Profil)
* `de.cocondo.app.system.security.DevAuthenticationFilter` (DEV Profil)

Relevante ADRs (personnel) als Governance-Input:

* PERS_ADR_004_Layerarchitektur
* PERS_ADR_007_Error_und_Paging_Standards
* PERS_ADR_009_Command_Permissions
* PERS_ADR_010_Record_Level_Security

> Hinweis: Sprint 3 nutzt ADRs primär für Struktur/Governance. Inhaltliche RLS/Permissions werden nicht „erfunden“, sondern nur vorbereitet, sofern beim Übernehmen des system packages ohnehin vorhanden.
