# Identity Management (IDM) Service – Fachkonzept

**Version:** 2
**Datum:** 19.03.2026

---

## 1. Projekt-Kontext

Der Auftraggeber entwickelt eine neue Anwendung namens **PersonnelApp**, die besonders sensible personenbezogene Daten verarbeitet. Für diese Anwendung – und perspektivisch weitere Fachanwendungen – wird ein **eigener, leichtgewichtiger, aber sicherer Authentifizierungs- und Berechtigungsdienst** benötigt.

Ziele des Auftraggebers:

* **PK-1**: Der IDM-Service soll vollständig **entkoppelt** von bestehenden zentralen IT-Mechanismen sein (z. B. Active Directory, Keycloak). Die zentralen IT-Administratoren sollen **keinen Einfluss auf die Berechtigungen oder den Datenzugriff** in PersonnelApp haben.
* **PK-2**: Der Dienst soll **autark** und **eigenständig administrierbar** sein, um dedizierte Verantwortlichkeiten sicherzustellen.
* **PK-3**: Die Lösung muss sich **präzise an fachliche Anforderungen** anpassen lassen, ohne die Komplexität eines vollwertigen Identity Providers (z. B. Keycloak).
* **PK-4**: Gleichzeitig sollen **moderne Sicherheitsstandards** gewährleistet sein (Passwort-Hashing, JWT, Berechtigungsmodell, sichere Konfiguration, k8s-Tauglichkeit).
* **PK-5**: Der Dienst soll eine **langfristige Basis** sein, die später extern angebunden werden kann (z. B. AD/LDAP/Keycloak) – jedoch erst in späteren Ausbaustufen.
* **PK-6**: Die Architektur muss den Betrieb in einer **Kubernetes-Umgebung** ermöglichen.
* **PK-7**: Der IDM-Service soll als **zentraler Authentifizierungs- und Rollenstammdienst** für mehrere Fachanwendungen dienen, ohne selbst zu einem überfrachteten, generischen Enterprise-IAM zu werden.
* **PK-8**: Die Lösung muss das **Zusammenspiel mit einem entkoppelten Web-Client (GWC)** sowie mit separaten Fachanwendungen (z. B. PersonnelApp) explizit unterstützen.

Damit bildet der IDM-Service eine **spezialisierte, sicherheitssensible Kernkomponente**, die sowohl unabhängig von der IT-Landschaft als auch zukunftsfähig gestaltet ist.

---

## 2. Fachliches Zielbild / Systemrolle

Dieses Kapitel präzisiert die fachliche Rolle des IDM-Services in der ersten produktiven Ausbaustufe.

### 2.1 Rolle des IDM-Services

Der IDM-Service ist in der ersten produktiven Ausbaustufe:

* zentrale Stelle für **Authentifizierung** von Benutzern und Service-Accounts,
* zentrale Stelle für die **Verwaltung von Benutzerkonten**,
* zentrale Stelle für die **Verwaltung von Application Scopes**,
* zentrale Stelle für die **Verwaltung von Rollen, Permission Groups, Permissions und Zuweisungen**,
* zentrale Stelle für die **Ausstellung kurzlebiger Access Tokens**,
* perspektivisch zentrale Stelle für die **kontrollierte Verwaltung von Session-/Refresh-Kontexten als produktionsreife Zielerweiterung**,
* perspektivisch zentrale Stelle für **Passwort-Reset- und E-Mail-Verifikations-Prozesse**,
* zentrale Stelle für die **Selbst-Administration des IDM im Self-Scope**,
* zentrale **Rollen- und Scope-Registry** für Fachanwendungen wie Personnel.

### 2.2 Abgrenzung des IDM-Services

Der IDM-Service ist in der ersten produktiven Ausbaustufe **nicht**:

* kein vollständiger OAuth2 / OIDC Authorization Server,
* kein vollwertiger Ersatz für Keycloak / Entra / AD / LDAP,
* kein generischer, zentraler Personenstamm mit umfassender HR- oder Organisationslogik,
* keine zentrale fachliche Autoritätsquelle für sämtliche feingranularen Berechtigungen fremder Fachanwendungen,
* kein Self-Service-Portal mit voller Benutzeroberfläche.

### 2.3 Zusammenspiel mit Fachanwendungen und GWC

* Der **IDM-Service** authentifiziert Benutzer, verwaltet Rollen und Scope-Zuordnungen und stellt Tokens aus.
* **Fachanwendungen** (z. B. PersonnelApp) vertrauen auf die vom IDM ausgestellten Tokens und dürfen **eigene fachliche Rechte** aus den vom IDM gelieferten Rollen ableiten.
* Der **GWC (Generic Web Client)** ist ein eigenständiges, entkoppeltes Frontend-Projekt und nutzt den IDM-Service als zentrale Authentifizierungs- und Verwaltungsquelle.
* Die erste produktive Benutzerverwaltung im GWC fokussiert auf die Nutzung für **IDM / Personnel** und muss daher einen stabilen „Current Authentication Context“ aus dem IDM beziehen können.

---

## 3. Anforderungen (erste produktive Ausbaustufe)

In diesem Kapitel werden alle bekannten Anforderungen aus dem bisherigen Austausch strukturiert, hierarchisch und nummeriert erfasst. Fokus ist die **erste produktionsreife Ausbaustufe** (MVP+, nicht Spielwiese).

Wichtiger Grundsatz für diese Version:

* Das Dokument übernimmt **alle wesentlichen Inhalte der Version 1**.
* Gleichzeitig werden fachliche Präzisierungen und Ergänzungen aufgenommen, die sich aus dem inzwischen erreichten MVP-Stand, dem realen Projektzuschnitt sowie den Anforderungen an eine erste produktive Nutzung ergeben.
* Ziel ist **kein Scope-Creep**, sondern eine **klare, tragfähige Produktivdefinition** für einen bewusst leichtgewichtigen IDM-Service.

---

## 3.1 Fachliche Anforderungen (Functional Requirements, FR)

### FR-1 Benutzerkonten verwalten

Der IDM-Service muss Benutzerkonten (User Accounts) verwalten können.

* **FR-1.1**: Anlegen, Lesen, Aktualisieren, Deaktivieren von Benutzerkonten.
* **FR-1.2**: Benutzerkonten besitzen Attribute wie: ID, Username, Passwort-Hash, Account-Status, optionale Verfallsdaten, Timestamps.
* **FR-1.3**: Benutzerkonten können ohne zugehörige Person existieren (z. B. technische Accounts / Service Accounts).
* **FR-1.4**: Benutzerkonten unterstützen fachliche Zustände wie aktiv, temporär gesperrt, dauerhaft gesperrt, deaktiviert, abgelaufen oder vergleichbare Statusmodelle.
* **FR-1.5**: Benutzerkonten müssen sicherheitsrelevante Änderungen unterstützen (Passwortwechsel, Sperren, Entsperren, Reset von Fehlversuchen).
* **FR-1.6**: Benutzerkonten können ein Merkmal zur **E-Mail-Verifikation** sowie optionale Flags wie „Passwortwechsel erforderlich“ tragen.

### FR-2 Personen verwalten (optionale spätere Domäne / nicht Bestandteil des aktuellen MVP-Kerns)

Der IDM-Service **kann perspektivisch** Personen als eigenständige Entitäten verwalten.

**Wichtige Einordnung:** Die Person-Domäne ist **fachlich weiterhin zulässig**, gehört aber **nicht zum belastbaren Kern des aktuellen MVP-/Baseline-Stands**. Die erste produktive Fokussierung liegt auf der **Identity-, Authentifizierungs- und Berechtigungsdomäne**. Eine Person-Domäne ist daher **optional und nachgelagert** zu behandeln.

* **FR-2.1**: Eine spätere Einführung einer Person-Entität ist fachlich zulässig.
* **FR-2.2**: Attribute einer Person wären u. a.: ID, Vorname, Nachname, Anzeige-Name, E-Mail, optionale Telefonnummer, optionale organisatorische Zuordnung.
* **FR-2.3**: Beziehungen Person ↔ Benutzerkonto können später wie folgt modelliert werden: Eine Person kann mit **0..n** Benutzerkonten verknüpft sein; ein Benutzerkonto kann mit **0..1** Person verknüpft sein.
* **FR-2.4**: Die Person-Domäne ist **nicht verbindlicher Kernbestandteil** der ersten produktiven Ausbaustufe und darf den MVP-Kern nicht blockieren.

### FR-3 Application Scopes (App + Stage)

Der IDM-Service verwaltet fachliche **Application Scopes** als zentralen Kontext für Rollen, Berechtigungen und Zuordnungen.

* **FR-3.1**: Ein `ApplicationScope` wird eindeutig durch die Kombination aus **`applicationKey`** und **`stageKey`** definiert.
* **FR-3.2**: Beispiele: `IDM/DEV`, `IDM/TEST`, `IDM/PROD`, `PERSONNEL/DEV`, `PERSONNEL/PROD`.
* **FR-3.3**: Rollen, Permission Groups und Permissions sind fachlich an einen Application Scope gebunden.
* **FR-3.4**: Ein Benutzerkonto kann mehreren Application Scopes zugeordnet sein.
* **FR-3.5**: Verwaltung von Application Scopes über eigene Endpoints (CRUD, begrenzt in der ersten Ausbaustufe auf das Notwendige).
* **FR-3.6**: Die Eindeutigkeit eines Application Scopes ist fachlich verbindlich (`UNIQUE(applicationKey, stageKey)`).

### FR-4 Self-Scope und Foreign-Scope

Der IDM-Service muss fachlich zwischen **Self-Scope** und **Foreign-Scope** unterscheiden.

* **FR-4.1**: Der **Self-Scope** ist der Application Scope, unter dem der IDM-Service selbst betrieben wird (z. B. `IDM/TEST`, `IDM/PROD`).
* **FR-4.2**: Im Self-Scope werden die Berechtigungen, Rollen und Zuordnungen gepflegt, die zur **Verwaltung des IDM selbst** dienen.
* **FR-4.3**: **Foreign-Scopes** sind Application Scopes anderer Fachanwendungen (z. B. `PERSONNEL/DEV`, `PERSONNEL/PROD`).
* **FR-4.4**: Rollen in Foreign-Scopes repräsentieren fachanwendungsspezifische Rollen, die vom IDM verwaltet, aber nicht zwingend final fachlich interpretiert werden.
* **FR-4.5**: Die Unterscheidung zwischen Self-Scope und Foreign-Scope ist ein zentrales fachliches Sicherheits- und Governance-Prinzip.

### FR-5 Rollen, Permission Groups und Permissions

Der IDM-Service verwaltet Rollen und Berechtigungsstrukturen in unterschiedlichen Scopes.

* **FR-5.1**: Rollen repräsentieren fachliche Berechtigungsbündel, z. B. `IDM_ADMIN`, `IDM_READONLY`, `PERSONNEL_ADMIN`, `PERSONNEL_READ_ONLY`.
* **FR-5.2**: Rollen sind immer genau einem `ApplicationScope` zugeordnet (Role → ApplicationScope: N:1).
* **FR-5.3**: Permissions repräsentieren feingranulare Rechte (z. B. `IDM_USER_READ`, `IDM_USER_CREATE`, `IDM_USER_UPDATE`).
* **FR-5.4**: Permissions können in **Permission Groups** strukturiert werden.
* **FR-5.5**: Permission Groups und Permissions sind ebenfalls an einen `ApplicationScope` gebunden.
* **FR-5.6**: Rollen können mehreren Permissions zugeordnet werden.
* **FR-5.7**: Verwaltung von Rollen, Permission Groups und Permissions erfolgt über eigene Endpoints (CRUD / Basic CRUD, in der ersten Ausbaustufe auf das Notwendige begrenzt).
* **FR-5.8**: Systemkritische Rollen und Permissions können als **systemgeschützt** markiert werden.

### FR-6 Rollen-Zuweisungen und Scope-Zuweisungen

Der IDM-Service verwaltet Zuweisungen von Scopes und Rollen zu Benutzerkonten.

* **FR-6.1**: Ein Benutzerkonto kann mehreren Application Scopes zugewiesen werden.
* **FR-6.2**: Die Zuordnung Benutzerkonto ↔ Application Scope wird als eigene Entität `UserApplicationScopeAssignment` modelliert.
* **FR-6.3**: Ein Benutzerkonto kann mehrere Rollen besitzen – auch in unterschiedlichen Scopes.
* **FR-6.4**: Rollen-Zuweisungen werden als eigene Entität `UserRoleAssignment` modelliert.
* **FR-6.5**: Optional können Gültigkeitszeiträume (`validFrom`, `validUntil`) und Metadaten (`grantedBy`, Kommentar) gepflegt werden – in der ersten Ausbaustufe mindestens das Grundmodell ohne komplexe Zeitlogik.
* **FR-6.6**: Für die erste produktive Ausbaustufe gilt fachlich mindestens das Grundmodell mit eindeutigen Zuordnungen.

### FR-7 Interne IDM-Rollen & Berechtigungen

Der IDM-Service benötigt ein eigenes internes Berechtigungsmodell für seine Verwaltungs-APIs.

* **FR-7.1**: Es existieren interne IDM-Rollen (z. B. `IDM_ADMIN`, `IDM_USER_MANAGER`, `IDM_READONLY`).
* **FR-7.2**: Diese Rollen werden in Permissions (feingranulare Berechtigungen, z. B. `IDM_USER_READ`, `IDM_USER_CREATE`, `IDM_USER_UPDATE`, `IDM_ROLE_MANAGE`) aufgelöst.
* **FR-7.3**: Interne Admin-Endpoints sind über diese Permissions gesichert.
* **FR-7.4**: Die internen IDM-Rollen und -Permissions sind fachlich dem **Self-Scope** zugeordnet.
* **FR-7.5**: Es soll in der ersten Ausbaustufe mindestens eine starke Admin-Rolle geben, ohne dass daraus fachlich eine unstrukturierte „God Role“ abgeleitet werden muss.

### FR-8 Authentifizierung (Login) mit Access Token + optionalem produktivem Session-/Refresh-Ausbau

Der IDM-Service stellt Endpoints zur Benutzer-Authentifizierung bereit.

* **FR-8.1**: Authentifizierung erfolgt typischerweise über Username/Passwort gegen interne User Accounts.
* **FR-8.2**: Bei erfolgreicher Authentifizierung wird ein **JWT Access Token** zurückgegeben.
* **FR-8.3**: Der **belastbar nachweisbare Baseline-Kern** ist ein kurzlebiges Access-Token-basiertes Login.
* **FR-8.4**: Für die erste produktive Härtung ist zusätzlich ein **Refresh Token / Session Token** oder ein funktional äquivalenter Session-Kontext fachlich vorgesehen.
* **FR-8.5**: Das Access Token dient ausschließlich dem API-Zugriff und ist kurzlebig.
* **FR-8.6**: Das Refresh-/Session-Token dient ausschließlich der kontrollierten Erneuerung und dem Session-Lifecycle.
* **FR-8.7**: Das JWT enthält Claims zu User, Scope-Kontext und zugewiesenen Rollen; im aktuellen Baseline-Stand sind zunächst reduzierte Identitätsclaims nachweisbar.
* **FR-8.8**: Der Login-Prozess berücksichtigt Benutzerstatus, Passwort-Hash, Sperren, Fehlversuche und weitere Sicherheitsprüfungen.

### FR-9 Token-Validierung, Current Auth Context und Token-Erneuerung

Der IDM-Service stellt Endpoints bereit, um Tokens zu prüfen, den aktuellen Auth-Kontext zu liefern und Tokens zu erneuern.

* **FR-9.1**: Ein Endpoint zur Erneuerung eines Access Tokens über ein Refresh-/Session-Token ist für die produktive Härtung vorgesehen; er ist **nicht** als belastbarer Bestandteil des aktuellen Baseline-Kerns vorauszusetzen.
* **FR-9.2**: Ein Endpoint zur Validierung eines Tokens (Introspection) für interne Services ist zulässig und für bestimmte Betriebsmodelle vorgesehen.
* **FR-9.3**: Fehlerhafte oder abgelaufene Tokens werden klar erkennbar zurückgewiesen.
* **FR-9.4**: Es existiert ein Endpoint für den **aktuellen Authentifizierungs-Kontext** (baseline-nah aktuell `GET /auth/me`; fachlich äquivalente Varianten sind zulässig).
* **FR-9.5**: Der Current Auth Context liefert mindestens: User-ID, Username, Scope-Kontext(e), Rollen, relevante Zustandsinformationen und optional abgeleitete Rechte. Der aktuelle MVP kann bewusst schlanker starten.
* **FR-9.6**: Der Current Auth Context muss für die Integration mit dem GWC stabil und ausreichend ausdrucksstark sein.

### FR-10 Session- und Token-Lifecycle

Der IDM-Service muss den fachlichen Lebenszyklus von Sessions und Tokens kontrollieren können.

* **FR-10.1**: Anmeldung soll perspektivisch eine kontrollierbare Session bzw. einen kontrollierbaren Refresh-Kontext erzeugen.
* **FR-10.2**: Ein fachlicher Logout für die aktuelle Session ist für die produktive Härtung verbindlich vorzusehen, auch wenn er im aktuellen Baseline-Stand noch nicht vollständig umgesetzt sein muss.
* **FR-10.3**: Ein fachlicher Logout für alle aktiven Sessions eines Benutzers ist für die produktive Härtung verbindlich vorzusehen, auch wenn er im aktuellen Baseline-Stand noch nicht vollständig umgesetzt sein muss.
* **FR-10.4**: Access Tokens selbst müssen **nicht** dauerhaft persistiert werden.
* **FR-10.5**: Refresh-/Session-Tokens oder die zugehörigen Session-Metadaten müssen kontrollierbar, prüfbar und widerrufbar sein.
* **FR-10.6**: Bei Passwortänderung, Benutzer-Deaktivierung, dauerhafter Sperrung oder vergleichbaren sicherheitsrelevanten Änderungen müssen Sessions fachlich invalidierbar sein.
* **FR-10.7**: Das Session-Modell darf bewusst leichtgewichtig sein, muss aber produktiv belastbar sein.

### FR-11 Bereitstellung für andere Fachanwendungen

Der IDM-Service ist als zentraler Dienst für andere Anwendungen konzipiert.

* **FR-11.1**: Andere Fachanwendungen nutzen den IDM-Service zur Authentifizierung von Nutzern.
* **FR-11.2**: Andere Anwendungen lesen keine Passwörter, sondern vertrauen auf das vom IDM ausgestellte Access Token.
* **FR-11.3**: Der IDM-Service fungiert als zentrale Stelle für Rollenverwaltung, Rollen-Zuweisungen und User-/Scope-Informationen.
* **FR-11.4**: Fachanwendungen dürfen Rollen aus **Foreign-Scopes** in **eigene fachliche Rechte** auflösen.
* **FR-11.5**: IDM verwaltet die Rollen- und Scope-Zuordnung zentral, ohne alle fachanwendungsspezifischen Einzelrechte zentral erzwingen zu müssen.
* **FR-11.6**: Das Zusammenspiel IDM ↔ Fachanwendung muss bewusst leichtgewichtig und entkoppelt bleiben.

### FR-12 Service-Accounts / technische Nutzer

Neben menschlichen Nutzern muss der Service technische Accounts verwalten können.

* **FR-12.1**: Service-Accounts sind Benutzerkonten ohne verknüpfte Person.
* **FR-12.2**: Service-Accounts können eigene Rollen/Scopes erhalten (z. B. `APP_XYZ_SERVICE`).
* **FR-12.3**: Für Service-Accounts können fachlich abweichende Sicherheitsregeln gelten (z. B. kein E-Mail-Reset-Flow).

### FR-13 Passwort-Reset

Der IDM-Service muss einen produktiv nutzbaren, aber bewusst schlanken Passwort-Reset-Prozess unterstützen.

* **FR-13.1**: Ein Administrator kann ein Passwort administrativ zurücksetzen.
* **FR-13.2**: Optional bzw. vorgesehen ist ein nutzerinitiierter Passwort-Reset über E-Mail-Token.
* **FR-13.3**: Reset-Tokens sind einmalig, kurzlebig und serverseitig kontrollierbar.
* **FR-13.4**: Nach erfolgreichem Passwort-Reset müssen aktive Sessions fachlich invalidierbar sein.
* **FR-13.5**: Der Passwort-Reset ist Bestandteil der produktiven Zielarchitektur, auch wenn die erste technische Ausprägung bewusst schlank gehalten wird.

### FR-14 E-Mail-Verification

Der IDM-Service muss einen produktiv nutzbaren E-Mail-Verifikationsprozess unterstützen.

* **FR-14.1**: Benutzerkonten bzw. zugeordnete Identitäten mit E-Mail-Adresse können den Status „verifiziert / nicht verifiziert“ besitzen.
* **FR-14.2**: Es muss ein Verifikations-Token bzw. ein funktional äquivalenter Verifikationsprozess existieren.
* **FR-14.3**: Verifikations-Tokens sind kurzlebig, serverseitig kontrollierbar und einmalig verwendbar.
* **FR-14.4**: Bestimmte Self-Service-Funktionen (z. B. nutzerinitiierter Passwort-Reset) können eine verifizierte E-Mail voraussetzen.
* **FR-14.5**: Die E-Mail-Verification ist Bestandteil der produktiven Zielarchitektur, auch wenn die erste technische Ausprägung bewusst schlank gehalten wird.

### FR-15 API-Dokumentation

Die bereitgestellten Endpoints sollen dokumentiert sein.

* **FR-15.1**: Verwendung von OpenAPI/Swagger zur Generierung einer maschinen- und menschenlesbaren API-Dokumentation.
* **FR-15.2**: Auth-, Admin- und Self-Service-nahe Endpoints müssen nachvollziehbar dokumentiert sein.

### FR-16 Deterministisches Bootstrapping / Initialisierung

Der IDM-Service benötigt eine kontrollierte Initialisierung seiner sicherheitsrelevanten Stammdaten.

* **FR-16.1**: Es muss möglich sein, initiale Scopes, Admin-User, Permission Groups, Permissions, Rollen und Basis-Zuordnungen deterministisch bereitzustellen.
* **FR-16.2**: Das Bootstrapping darf datei- bzw. definitionsbasiert erfolgen (z. B. XML-basierte Ressourcen).
* **FR-16.3**: Der **Self-Scope** muss fachlich gesondert und bewusst initialisiert werden.
* **FR-16.4**: Zusätzliche Benutzer, Scope-Zuordnungen, Foreign-Scope-Rollen und Foreign-Scope-Rollen-Zuweisungen müssen optional kontrolliert initialisierbar sein.
* **FR-16.5**: Das Bootstrapping ist nicht nur Seed-Data, sondern ein bewusstes Mittel zur reproduzierbaren Sicherheits- und Betriebsinitialisierung.

---

## 3.2 Nicht-funktionale Anforderungen (NFR)

### NFR-1 Stateless Service (bezogen auf HTTP-Session)

Der IDM-Service ist bezogen auf klassische HTTP-Sessions vollständig stateless.

* **NFR-1.1**: Keine Verwendung von serverseitigen HTTP-Sessions; Authentisierung im API-Betrieb erfolgt ausschließlich tokenbasiert.
* **NFR-1.2**: Zustand (z. B. Userdaten, Rollen, Session-/Refresh-Metadaten) wird ausschließlich in persistenten Systemen (DB, optional Cache) gehalten.
* **NFR-1.3**: Die Einführung kontrollierbarer Session-/Refresh-Kontexte widerspricht nicht dem Stateless-Ansatz, solange keine klassischen Web-Server-Sessions verwendet werden.

### NFR-2 Deployment auf Kubernetes

Der Service soll später (oder direkt) auf Kubernetes laufen.

* **NFR-2.1**: Container-fähige Spring-Boot-Anwendung.
* **NFR-2.2**: Bereitstellung von Health Endpoints (Liveness/Readiness) für k8s Probes.
* **NFR-2.3**: Konfiguration über Environment-Variablen/ConfigMaps/Secrets.

### NFR-3 Code-Qualität & Standards

Hohe Code-Qualität hat Priorität.

* **NFR-3.1**: Klare Schichtung (Controller, Service, Domain, Persistence, Security).
* **NFR-3.2**: Unit- und Integrationstests für wesentliche Komponenten (insb. Auth, Security, Persistence, Bootstrap, später Session-/Token-Lifecycle).
* **NFR-3.3**: Code, Klassennamen, Methoden, Log-Meldungen und Fehlermeldungen in Englisch.
* **NFR-3.4**: Kommentare zur Erläuterung in Deutsch erlaubt.
* **NFR-3.5**: Die Testarchitektur soll deterministisch, reproduzierbar und profilgestützt sein.

### NFR-4 Performance & Skalierbarkeit

Der Service muss mit wachsenden Nutzerzahlen skalieren.

* **NFR-4.1**: Horizontale Skalierung über mehrere Instanzen im Cluster.
* **NFR-4.2**: Caching für häufig gelesene Metadaten (z. B. Rollen/Scopes) ist optional einsetzbar.
* **NFR-4.3**: Ein späteres Session-/Refresh-Modell muss horizontal skalierbar sein.

### NFR-5 Erweiterbarkeit

Das Design muss spätere Integrationen unterstützen (Keycloak, LDAP, AD, MS Entra).

* **NFR-5.1**: Trennung zwischen Authentifizierungs-Logik und externen Identity Providern.
* **NFR-5.2**: Möglichkeit, später einen externen Provider anstelle des internen User-Stores zu nutzen (Interface-basierter Ansatz).
* **NFR-5.3**: Das fachliche Rollen-/Scope-Modell soll auch bei späterer externer Authentifizierung erhalten bleiben können.

### NFR-6 Dokumentation

* **NFR-6.1**: Basis-Architektur und APIs werden dokumentiert (z. B. in einem README/Architecture Overview).
* **NFR-6.2**: Fachkonzept und Implementierungskonzept sind verbindliche Stamm-Dokumente.
* **NFR-6.3**: Wesentliche Architekturentscheidungen werden zusätzlich über ADRs dokumentiert.

### NFR-7 Leichtgewichts-Prinzip

Der IDM-Service soll produktiv belastbar, aber bewusst leichtgewichtig bleiben.

* **NFR-7.1**: Es sollen nur die Funktionen implementiert werden, die für eine erste produktive Nutzung fachlich erforderlich sind.
* **NFR-7.2**: Enterprise-IAM-Features mit hohem Overhead (z. B. vollständige OIDC-Provider-Rolle, MFA, komplexe Föderation) sind nicht automatisch Bestandteil der ersten Ausbaustufe.
* **NFR-7.3**: Neue Features müssen sich am Nutzen für reale Betriebsanforderungen messen lassen.

---

## 3.3 Sicherheitsanforderungen (SEC)

### SEC-1 Passwort-Handling

* **SEC-1.1**: Passwörter werden niemals im Klartext gespeichert.
* **SEC-1.2**: Verwendung eines sicheren Passwort-Hashing-Algorithmus (z. B. Argon2id, BCrypt über Spring Security DelegatingPasswordEncoder).
* **SEC-1.3**: Möglichkeit, Hash-Algorithmen in Zukunft zu migrieren (präfix-basiertes Schema `{bcrypt}`, `{argon2}`, ...).
* **SEC-1.4**: Passwörter müssen bei Anlage, Änderung und Reset validiert werden (z. B. Mindestlänge; spätere Policy-Erweiterung möglich).

### SEC-2 Token-Sicherheit

* **SEC-2.1**: Signierte JWT Access Tokens sind verpflichtend. Der aktuelle belastbare Baseline-Stand kann bewusst mit **symmetrischer Signatur (z. B. HS256)** arbeiten; für die produktive Härtung ist **asymmetrische Signatur (z. B. RS256/ES256)** die bevorzugte Zielrichtung.
* **SEC-2.2**: Access Tokens mit kurzer Laufzeit (z. B. 5–15 Minuten).
* **SEC-2.3**: Refresh-/Session-Tokens mit längerer Laufzeit und Möglichkeit zur Revokation.
* **SEC-2.4**: Tokens werden über HTTPS übertragen; keine Speicherung sensibler Daten im Token.
* **SEC-2.5**: Access Tokens sollen nicht dauerhaft serverseitig gespeichert werden müssen.
* **SEC-2.6**: Refresh-/Session-Tokens oder deren kontrollierbare Repräsentation müssen widerrufbar sein.
* **SEC-2.7**: Logout, Logout-all und sicherheitsrelevante Zustandsänderungen müssen den Session-Lifecycle sicher beeinflussen können.

### SEC-3 Rechtemanagement & Autorisierung

* **SEC-3.1**: Autorisierung auf Basis von Rollen und Permissions.
* **SEC-3.2**: Nutzung von Methoden- oder Endpoint-basierten Sicherheitsannotationen (z. B. `@PreAuthorize`).
* **SEC-3.3**: Interne Admin-Endpunkte sind nur mit speziellen IDM-Rollen/Permissions erreichbar.
* **SEC-3.4**: Die Trennung zwischen Self-Scope und Foreign-Scope ist sicherheitlich bindend.
* **SEC-3.5**: Fachanwendungen dürfen Foreign-Scope-Rollen in eigene Rechte auflösen; diese Domänenhoheit bleibt fachlich gewollt.

### SEC-4 Logging & Audit

* **SEC-4.1**: Keine Passwörter, Tokens oder andere hochsensible Daten in Logs.
* **SEC-4.2**: Logging sicherheitsrelevanter Ereignisse (Logins, Fehl-Logins, später Refresh, Logout, Rollenänderungen, Konto-Sperrungen, Passwort-Reset, E-Mail-Verifikation) in angemessener Tiefe.
* **SEC-4.3**: Basis-Audit-Informationen in Entities (Created/Modified By/Date).
* **SEC-4.4**: Die Auditierung soll bewusst leichtgewichtig starten, aber sicherheitsrelevante Kernereignisse zwingend abdecken.

### SEC-5 Rate Limiting / Bruteforce-Schutz (Basis)

* **SEC-5.1**: Basis-Schutz vor Bruteforce-Angriffen auf Login-Endpunkt (z. B. Rate Limiting pro IP/User in der ersten Ausbaustufe als einfacher Mechanismus).
* **SEC-5.2**: Fehlversuche müssen zu temporären Sperren oder funktional äquivalenten Schutzmechanismen führen können.

### SEC-6 Reset- und Verifikations-Tokens

* **SEC-6.1**: Passwort-Reset- und E-Mail-Verifikations-Tokens sind kurzlebig, einmalig und serverseitig kontrollierbar.
* **SEC-6.2**: Tokens für Reset und Verifikation dürfen nicht im Klartext dauerhaft persistiert werden, wenn dies sicherheitlich vermeidbar ist.
* **SEC-6.3**: Missbrauchsrelevante Vorgänge (z. B. wiederholte Reset-Anforderung) müssen begrenzbar sein.

---

## 3.4 Technische Anforderungen & Stack (TECH)

### TECH-1 Technologie-Stack

* **TECH-1.1**: Java 17+.
* **TECH-1.2**: Spring Boot 3.x.
* **TECH-1.3**: Spring Web / Spring MVC.
* **TECH-1.4**: Spring Security (JWT-basiert, stateless im HTTP-Sinne).
* **TECH-1.5**: Spring Data JPA mit Hibernate.
* **TECH-1.6**: Datenbank: z. B. PostgreSQL oder MariaDB (konfigurierbar).
* **TECH-1.7**: Maven als Build-System.
* **TECH-1.8**: Lombok zur Reduktion von Boilerplate.
* **TECH-1.9**: Liquibase für DB-Migrationen.

### TECH-2 DTO-Mapping

* **TECH-2.1**: Einführung bzw. Nutzung einer dedizierten Mapping-Lösung (z. B. MapStruct) für DTO↔Entity.
* **TECH-2.2**: Trennung von Request/Response-DTOs und Entities.

### TECH-3 Observability & Actuator

* **TECH-3.1**: Einsatz von Spring Boot Actuator (Health, Info, ggf. Metrics).
* **TECH-3.2**: Health Endpoints für k8s (Liveness/Readiness).

### TECH-4 Test- und Bootstrap-Architektur

* **TECH-4.1**: Die Testarchitektur muss reproduzierbare Testprofile und definierte Test-Bootstrap-Daten unterstützen.
* **TECH-4.2**: Security-, Auth- und Bootstrap-nahe Tests sind integraler Bestandteil der ersten produktiven Ausbaustufe.
* **TECH-4.3**: Die Bootstrap-Architektur ist als bewusster technischer Träger fachlicher Initialisierungsanforderungen zu behandeln.

---

## 3.5 Betriebsanforderungen (OPS)

### OPS-1 Containerisierung & k8s

* **OPS-1.1**: Erzeugung eines lauffähigen Container-Images.
* **OPS-1.2**: Bereitstellung von Konfiguration über Environment-Variablen.
* **OPS-1.3**: Unterstützung typischer k8s Patterns (Probes, ConfigMaps, Secrets).

### OPS-2 SBOM & Security-Scanning

* **OPS-2.1**: Erzeugung einer Software Bill of Materials (SBOM), z. B. mit CycloneDX Maven Plugin.
* **OPS-2.2**: Optionaler Einsatz von Dependency-Check/OWASP oder ähnlichen Tools im Build-Prozess.

### OPS-3 Betriebsrelevante Sicherheitsinitialisierung

* **OPS-3.1**: Der IDM-Service muss eine deterministische Initialisierung sicherheitskritischer Stammdaten ermöglichen.
* **OPS-3.2**: Der initiale Admin-Zugang, Self-Scope-Berechtigungen und definierte Basis-Rollen müssen reproduzierbar bereitstellbar sein.
* **OPS-3.3**: Unterschiede zwischen DEV/TEST/PROD dürfen bewusst über Konfiguration und Bootstrap-Definitionen abgebildet werden.

---

## 3.6 Zukunftsanforderungen / Integrationsanforderungen (nur referenziert, nicht Teil der ersten Ausbaustufe)

Diese Anforderungen dienen als Orientierung, fließen aber in der ersten Ausbaustufe nur in die Architektur ein, nicht in die Umsetzung.

### FUT-1 Integration externer Identity Provider

* **FUT-1.1**: Anbindung an Keycloak, Active Directory, LDAP, MS Entra.

### FUT-2 Erweiterte IDM-Funktionen

* **FUT-2.1**: Self-Service (Passwort ändern, Profil bearbeiten).
* **FUT-2.2**: Mandantenfähigkeit (Tenants).
* **FUT-2.3**: Objektbezogene Berechtigungen (z. B. auf Organisationseinheitsebene).

### FUT-3 Erweiterte Security-Funktionen

* **FUT-3.1**: MFA / 2FA.
* **FUT-3.2**: Passwortlose Authentifizierung (Magic Links, Einmal-Codes, WebAuthn/FIDO2).
* **FUT-3.3**: Erweiterte Policies (z. B. zeit- oder ortsabhängig).

### FUT-4 Erweiterte Compliance- und Audit-Funktionen

* **FUT-4.1**: Vollständige Audit Trails aller ändernden Aktionen mit Historisierung.

### FUT-5 Technische Erweiterungen

* **FUT-5.1**: Refactoring in Module / Hexagonal Architecture.
* **FUT-5.2**: Mehr Observability (Prometheus/Micrometer, Tracing, OpenTelemetry).
* **FUT-5.3**: Automatisierte Security-Checks (Snyk, Dependabot, etc.).

---

## 4. Umsetzungsplan (Abbildung der Anforderungen auf die Lösung)

In diesem Kapitel wird beschrieben, wie die Anforderungen der ersten produktionsreifen Ausbaustufe umgesetzt werden sollen. Wo sinnvoll, werden Optionen aufgezeigt; Entscheidungen können später getroffen werden.

**Wichtiger Hinweis für Version 2:**

* Dieses Kapitel übernimmt die Grundstruktur der Version 1.
* Gleichzeitig werden die inzwischen klar gewordenen fachlichen Leitplanken (ApplicationScope, Self-/Foreign-Scope, Session-/Refresh-Lifecycle, Bootstrap, GWC-/Fachanwendungs-Integration) explizit ergänzt.
* Die endgültige technische Ausprägung wird im separaten Dokument `IDM_Implementierungskonzept.md` verbindlich festgelegt.

---

## 4.1 Architektur & Schichtung

**Abdeckung:** NFR-1, NFR-3, NFR-5, TECH-1

Vorgeschlagene Schichten:

* **API Layer (Web/REST)**: Spring Web Controller, Request-/Response-DTOs.
* **Application/Service Layer**: Fachlogik, Transaktionen, Security-bezogene Checks.
* **Domain/Persistence Layer**: JPA-Entities & Repositories.
* **Security Layer**: Konfiguration von Authentifizierung und Autorisierung (JWT, Filter, Method Security).

**Option A (klassisch):** Monolithischer Spring-Boot-Service mit klarer Package-Struktur (z. B. `auth`, `user`, `person`, `scope`, `role`, `permission`, `security`, `config`, `common`).
**Option B:** Frühzeitige Aufteilung in Module (Maven Multi-Module, z. B. `idm-core`, `idm-web`, `idm-adapters`).

→ Für die erste Ausbaustufe bietet sich **Option A** an (einfacher Build, weniger Overhead), aber mit einem Architektur-Design, das Option B später ermöglicht.

Zusätzliche Leitplanken für Version 2:

* Das Scope-Modell ist fachlich auf **ApplicationScope (applicationKey + stageKey)** auszurichten.
* Die Trennung zwischen **Self-Scope** und **Foreign-Scope** ist bereits im Domänen- und Service-Design zu berücksichtigen.
* Das Zusammenspiel mit dem **GWC** und mit **Fachanwendungen** ist als eigenständiger Integrationsfall mitzudenken.

---

## 4.2 Datenmodell & Persistenz

**Abdeckung:** FR-1 bis FR-7, FR-10, FR-13, FR-14, TECH-1, TECH-2, NFR-4

### Kern-Entities (erste produktive Ausbaustufe / baseline-nah)

* `UserAccount`
* `ApplicationScope`
* `PermissionGroup`
* `Permission`
* `Role`
* `UserApplicationScopeAssignment`
* `UserRoleAssignment`
* `RolePermissionAssignment`

### Zusätzliche fachlich vorgesehene Kern-Entities für Produktivreife (nächste Ausbaustufe)

* `UserSession` / `RefreshSession` / funktional äquivalente Session-Entität
* `PasswordResetToken` / funktional äquivalente Reset-Entität
* `EmailVerificationToken` / funktional äquivalente Verifikations-Entität

### Optionale / nachgelagerte Domäne

* `Person`

### Umsetzung

* Verwendung von UUID als Primärschlüssel (entweder Applikations-generiert oder DB-generiert).
* Beziehungen:

    * `Person` 1:n `UserAccount` (optional verknüpft).
    * `Role` n:1 `ApplicationScope`.
    * `PermissionGroup` n:1 `ApplicationScope`.
    * `Permission` n:1 `ApplicationScope`.
    * `Permission` n:1 `PermissionGroup` (optional je nach Modellierung).
    * `UserApplicationScopeAssignment` n:1 `UserAccount`, n:1 `ApplicationScope`.
    * `UserRoleAssignment` n:1 `UserAccount`, n:1 `Role`.
    * `RolePermissionAssignment` n:1 `Role`, n:1 `Permission`.

### Liquibase

* Erstellung von Changelogs pro Entität/Änderung.
* Bootstrap-Daten für Basis-Scopes, Basis-Rollen, Permissions, Permission Groups und initialen Admin-User.
* Erweiterung um Session-/Reset-/Verifikations-bezogene Tabellen in der produktiven Härtung.

### Optionen

* **Option A:** Permissions sofort als eigene Entität einführen (saubere RBAC-Architektur).
* **Option B:** Zunächst nur Rollen, Permissions später ergänzen.

Empfehlung: **Option A**, da die interne IDM-Absicherung ein Kernfeature ist.

---

## 4.3 Authentifizierung, Tokens und Sessions

**Abdeckung:** FR-8, FR-9, FR-10, SEC-1, SEC-2, SEC-3, NFR-1

### Login-Flow (baseline-nah / erste produktive Ausbaustufe)

1. Client sendet baseline-nah `POST /auth/login` (oder fachlich äquivalent) mit Username/Passwort.
2. Service validiert Benutzer gegen `UserAccount` (inkl. Status, Passwort-Hash, Sperren, Fehlversuche).
3. Bei Erfolg:

    * Erstellung eines **kurzlebigen Access Tokens**.
    * In der produktiven Härtung zusätzlich: Erstellung eines **Refresh-/Session-Tokens** bzw. einer kontrollierbaren Session.

### Token-Inhalt (Access Token)

* Standard-Claims: `sub`, `iat`, `exp`, `iss`, `typ`.
* Custom Claims (fachlich):

    * `userId`, `username`.
    * `applicationScope` bzw. relevante Scope-Kontexte.
    * `roles`: Liste der Rollen inkl. Scope-Kontext.
    * optional: reduzierte Person-Informationen.
    * optional: abgeleitete Permissions für den Self-Scope.

**Wichtiger Baseline-Hinweis:** Der aktuelle MVP startet bewusst schlanker; im nachweisbaren Baseline-Kern sind zunächst reduzierte Identitätsclaims belastbar dokumentiert.

### Wichtige fachliche Leitlinie

* In **Foreign-Scopes** kommen fachlich primär **Rollen**, nicht zwingend alle finalen Einzelrechte.
* Die auswertende Fachanwendung darf diese Rollen in **eigene Rechte** auflösen.
* Im **Self-Scope** kann IDM zusätzlich effektive Rechte für die eigene API-Sicherung bereitstellen.

### Optionen für Signaturverfahren

* **Option A – HS256 (symmetrisch):**

    * Einfacher Start (Shared Secret in k8s Secret).
    * Nachteile: Alle validierenden Services benötigen dasselbe Secret.

* **Option B – RS256/ES256 (asymmetrisch):**

    * Private Key im IDM, Public Key in anderen Services.
    * Bessere Trennung und Sicherheit, besonders in größeren Landschaften.

Empfehlung: **Option B** für langfristige Sicherheit und k8s-/Multi-Service-Landschaften; **Option A** ist im aktuellen Baseline-Stand als bewusst begrenzte Vereinfachung akzeptabel.

### Token-Refresh / Session-Lifecycle (produktive Härtung)

* Einführung eines Refresh Tokens mit einer einfachen, aber sicheren Strategie:

    * Speicherung von Refresh-/Session-IDs oder einer funktional äquivalenten serverseitigen Repräsentation.
    * Ein Endpoint wie `/auth/refresh` prüft Token, ID und Gültigkeit und stellt neuen Access Token aus.
    * Token-Rotation kann als optionaler nächster Schritt vorbereitet werden.

### Logout / Invalidierung (produktive Härtung)

* Ein Endpoint wie `/auth/logout` beendet die aktuelle Session.
* Ein Endpoint wie `/auth/logout-all` beendet alle aktiven Sessions des Benutzers.
* Passwortänderung, Passwort-Reset, Benutzer-Deaktivierung oder sicherheitsrelevante Sperren müssen aktive Sessions invalidierbar machen.

---

## 4.4 Autorisierung & internes Rechtemanagement

**Abdeckung:** FR-5, FR-7, FR-11, SEC-3, SEC-4, NFR-3, NFR-5

### Ansatz

* Rollen und Permissions werden im IDM als Entities geführt.
* Für den **Self-Scope** werden effektive IDM-Permissions aus Rollen abgeleitet.
* In Spring Security werden Permissions zu `GrantedAuthority`s gemappt (z. B. Präfix `PERM_` oder projektspezifische Authority-Konstanten).

### Beispiele

* Admin-Endpunkt zum Anlegen eines Users: `@PreAuthorize("hasAuthority('IDM_USER_CREATE')")` bzw. projektspezifische Authority-Konstante.
* Lese-Endpunkte: `@PreAuthorize("hasAuthority('IDM_USER_READ')")`.

### Wichtige fachliche Präzisierung (Version 2)

* Der IDM-Service verwaltet zentral **Rollen und Zuordnungen**.
* Die **finale Rollen→Rechte-Auflösung für Foreign-Scopes** darf bewusst in der jeweiligen Fachanwendung erfolgen.
* Dadurch bleibt die Fachdomäne Herr über ihre konkreten Berechtigungsmodelle, während der IDM die zentrale Rollen- und Zuordnungsquelle bleibt.

### Optionen für Granularität

* **Option A:** Grobe Permissions (User Read/Manage, Scope Read/Manage, Permission Manage).
* **Option B:** Feinere Permissions (z. B. getrennt für Create, Update, Delete, Assign, Unassign).

Für die erste Ausbaustufe ist **Option B fachlich zulässig und mittelfristig vorzuziehen**, wenn sie ohne unnötige Komplexität umsetzbar ist.

---

## 4.5 API-Design & DTOs

**Abdeckung:** FR-1 bis FR-16, TECH-2

### Grundprinzipien

* Klare Trennung von Entities und DTOs.
* Versionierte REST-API ist fachlich wünschenswert (z. B. `/api/v1/...`), kann aber in einer bewusst leichtgewichtigen ersten Ausprägung pragmatisch gestaltet werden, sofern die API-Struktur konsistent und dokumentiert bleibt.
* MapStruct oder funktional äquivalente Mapping-Lösung für DTO ↔ Entity.

### Zentrale Endpoints (erste produktive Ausbaustufe)

* **Baseline-nachweisbar / MVP-Kern:**

    * `/auth/login`
    * `/auth/me` (oder fachlich äquivalenter Current-Context-Endpoint)

* **Produktive Härtung / nächste Ausbaustufe:**

    * `/auth/refresh`
    * `/auth/logout`
    * `/auth/logout-all`
    * `/auth/introspect` (optional / intern)

* **IDM-Administration:**

    * `/api/.../users`
    * `/api/.../application-scopes`
    * `/api/.../roles`
    * `/api/.../permissions`
    * `/api/.../permission-groups`
    * Endpoints für Scope-/Rollen-Zuweisungen

* **Produktive Zusatzflüsse / nächste Ausbaustufe:**

    * Passwort-Reset-Endpoints
    * E-Mail-Verifikations-Endpoints

### DTO-Strategie

* Eigene DTOs für Create/Update.
* Response-DTOs, die nur relevante Felder enthalten und keine sensitiven Daten (z. B. kein Passwort-Hash).
* Für `/auth/me` / Current Context ist ein eigener, stabiler Response-Typ vorzusehen.

---

## 4.6 Security-Implementierung & Password Handling

**Abdeckung:** SEC-1, SEC-2, SEC-5, SEC-6

### Passwort-Handling

* Einsatz des `DelegatingPasswordEncoder` in Spring Security.
* Standard-Algorithmus: z. B. BCrypt oder Argon2 (konfigurierbar).
* Beim Anlegen/Ändern/Resetten von Passwörtern wird das Passwort validiert (Länge etc.) und gehasht.

### Bruteforce-Schutz

* Einfache Implementierung in erster Ausbaustufe:

    * Zählung von Fehlversuchen pro User/IP in-memory oder in DB.
    * Temporäre Sperrung nach X Fehlversuchen.

* Optional: Integration einer Rate-Limiting-Library (z. B. Bucket4j).

### Reset- und Verifikations-Sicherheit

* Reset- und Verifikations-Tokens sind kurzlebig, einmalig und serverseitig kontrollierbar.
* Versandkanäle (z. B. E-Mail) werden sicherheitsbewusst behandelt.
* Self-Service-nahe Flows dürfen bewusst schlank starten, müssen aber fachlich korrekt abgesichert sein.

---

## 4.7 Observability, Logging, Audit

**Abdeckung:** SEC-4, TECH-3, OPS-1

### Actuator

* Aktivierung von `/actuator/health`, `/actuator/info`.
* Nutzung von dedizierten Health-Indikatoren (DB-Check).

### Logging

* Standard-SLF4J/Logback-Konfiguration.
* Keine sensiblen Daten in Logs.
* Logging besonders für:

    * Login-Vorgänge
    * Fehl-Logins
    * später: Refresh
    * später: Logout / Logout-all
    * Passwort-Reset
    * E-Mail-Verifikation
    * Rollenänderungen / Scope-Zuordnungen / Sperrungen

### Audit-Felder

* Verwendung von Spring Data JPA Auditing für `createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate`.

---

## 4.8 Build Setup, SBOM, CI/CD

**Abdeckung:** OPS-2, TECH-1, NFR-3

### Build

* Maven-Projekt mit klaren Dependencies.
* Verwendung von Maven-Plugins:

    * CycloneDX Maven Plugin für SBOM-Erzeugung.
    * (Optional) OWASP Dependency-Check oder ähnliche Tools für Sicherheitsanalyse.

### Optionen

* **Option A:** SBOM nur im `verify`-Lifecycle generieren.
* **Option B:** SBOM zusätzlich als Artefakt in CI/CD speichern und automatisiert auswerten.

### Container

* Erstellung eines Docker/OCI-Images (z. B. per Jib oder Buildpacks).
* Image wird in Container Registry bereitgestellt und kann auf k8s ausgeliefert werden.

---

## 4.9 Kubernetes-Integration

**Abdeckung:** NFR-2, OPS-1

### Deployment-Grundlagen

* k8s Deployment mit Replikas (z. B. 2–3) für Hochverfügbarkeit.
* Service (ClusterIP/Ingress) zur Erreichbarkeit.

### Health Probes

* Liveness: `/actuator/health/liveness` (oder `.../health` mit Konfiguration).
* Readiness: `/actuator/health/readiness`.

### Konfiguration

* DB-Zugangsdaten, JWT-Keys und sonstige Secrets in k8s Secrets.
* Nicht-sensible Konfiguration (z. B. Token-Laufzeiten) über ConfigMaps.
* Bootstrap-bezogene Ressourcen und Sicherheitsparameter müssen umgebungsbewusst steuerbar sein.

---

## 4.10 Projektorganisation & erste Milestones

Vorschlag für eine erste Umsetzung in Iterationen:

1. **Iteration 1:**

    * Projekt-Setup, Basiskonfiguration, DB-Anbindung, Liquibase-Basis.
    * Entities `UserAccount`, `ApplicationScope`, `PermissionGroup`, `Permission`, `Role`, Zuweisungs-Entities.
    * Einfache CRUD-Endpunkte für User und Scope-nahe Kernstrukturen.

2. **Iteration 2:**

    * Implementierung von Auth (`/auth/login`) mit JWT-Erzeugung.
    * Integration von Spring Security, Stateless-Konfiguration.
    * Basic internes Rechtemanagement.

3. **Iteration 3:**

    * Ausbau des Rollen-/Permission-Modells.
    * Absicherung der Admin-APIs.
    * Deterministisches Bootstrap für Self-Scope und Basis-Sicherheitsdaten.
    * SBOM-Integration, erste k8s Deployment-Definition, Health-Endpoints.

4. **Iteration 4:**

    * Token-Refresh-Flow.
    * Session-/Logout-Konzept.
    * Logging/Auditing ausbauen.
    * Hardenings (Rate Limiting, zusätzliche Tests).

5. **Iteration 5:**

    * Passwort-Reset.
    * E-Mail-Verification.
    * Current Auth Context für GWC.
    * Weitere Produktiv-Härtung.

---

## 5. Ausblick: Weitere Ausbaustufen & Features

Dieses Kapitel beschreibt mögliche Erweiterungen über die erste produktive Ausbaustufe hinaus. Sie sind wichtig für die langfristige Roadmap, aber **nicht** Teil des initialen Scopes.

### 5.1 Integration externer Identity Provider

* **Keycloak-Integration:**

    * IDM-Service als „Fach-User- & Rollen-Registry“; Authentifizierung über Keycloak.
    * Synchronisation von Rollen/Benutzern zwischen IDM und Keycloak.

* **Active Directory / LDAP:**

    * Anbindung an bestehende Verzeichnisdienste für Benutzer-Authentifizierung.
    * Mapping von AD-Gruppen auf IDM-Rollen.

* **MS Entra (Azure AD):**

    * Nutzung von OIDC/OAuth2-Flows.
    * Externe Auth, interne Rollen- und Rechtevergabe im IDM.

### 5.2 Erweiterte IDM-Funktionalitäten

* **Self-Service Portal:**

    * Nutzer können ihre Profil-Daten und Passwörter selbst verwalten.
    * Password-Reset via E-Mail (weiterer Ausbau über die Basisfunktion hinaus).

* **Mandantenfähigkeit (Tenants):**

    * Einführung einer Tenant-Entität.
    * Tenant-spezifische Rollen und Scopes.

* **Feingranulare objektbezogene Berechtigungen:**

    * z. B. Zugriff nur auf bestimmte Organisationseinheiten, Projekte oder Datenbereiche.
    * Custom `PermissionEvaluator` für Domain-Objekte.

* **Erweiterte Audit- und Compliance-Funktionen:**

    * Vollständige Audit Trails aller ändernden Aktionen mit Historisierung.

### 5.3 Security & Convenience Features

* **MFA / 2FA:**

    * Zusätzliche starke Authentifizierung für ausgewählte Rollen oder Kontexte.

* **Passwortlose Authentifizierung:**

    * Magic Links, Einmal-Codes, WebAuthn/FIDO2.

* **Erweiterte Policies:**

    * Dynamische Policies (z. B. zeit- oder ortsabhängig).

### 5.4 Technische Erweiterungen

* **Refactoring in Module/Hexagonal Architecture:**

    * Aufteilung in Core, Adapters, Ports für noch bessere Erweiterbarkeit.

* **Mehr Observability:**

    * Metriken (Prometheus/Micrometer), verteiltes Tracing (OpenTelemetry).

* **Automatisierte Security-Checks:**

    * Integration mit Sicherheitsplattformen (Snyk, GitHub Dependabot, etc.).

---

## 6. Zusammenfassung und Leitentscheidung für Version 2

Dieses Fachkonzept in **Version 2** hält die erste produktive Ausbaustufe des IDM-Services fachlich verbindlich fest.

Wesentliche Präzisierungen gegenüber Version 1:

* Der IDM-Service bleibt **leichtgewichtig**, wird aber klar auf **produktive Nutzbarkeit** ausgerichtet.
* Das Scope-Modell wird verbindlich als **ApplicationScope = (applicationKey, stageKey)** präzisiert.
* Die Trennung zwischen **Self-Scope** und **Foreign-Scope** wird als zentrales Sicherheits- und Governance-Prinzip eingeführt.
* Das Token-Modell wird fachlich auf **Access Token + kontrollierbaren Session-/Refresh-Kontext als Zielarchitektur** erweitert.
* **Logout**, **Logout-all**, **Session-Invalidierung**, **Passwort-Reset** und **E-Mail-Verification** bleiben als produktiv relevante Zielanforderungen erhalten, werden aber sauber vom aktuell nachweisbaren MVP-Kern getrennt.
* Das Zusammenspiel zwischen **IDM**, **GWC** und **Fachanwendungen** wird fachlich explizit gemacht.
* **Bootstrapping** wird als deterministische Sicherheits- und Betriebsinitialisierung ausdrücklich als fachlich relevante Anforderung aufgenommen.
* Inhalte der Version 1 bleiben erhalten, werden jedoch an den inzwischen erreichten Projektstand und die Zielarchitektur angepasst.

---

Dieses Konzept bildet die Grundlage für die erste produktionsreife Ausbaustufe des IDM-Services. Die Anforderungen sind strukturiert und nummeriert, der Umsetzungsplan beschreibt, wie diese Anforderungen fachlich auf die Lösung abgebildet werden, und der Ausblick skizziert sinnvolle Erweiterungen für spätere Phasen.
