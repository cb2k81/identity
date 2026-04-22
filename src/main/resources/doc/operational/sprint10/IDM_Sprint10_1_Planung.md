# IDM Sprint 10 – Planung (Produktionshärtung nach MVP)

## 1. Ziel des Sprints

Sprint 9 dient der **gezielten Produktionshärtung nach dem MVP-Abschluss von Sprint 8**.

Ziel ist ein Stand, bei dem das IDM nicht nur als technisch belastbarer MVP, sondern als **für den produktiven Einsatz fachlich und betrieblich sinnvoller V1-Stand** verwendet werden kann.

Der Schwerpunkt liegt auf den noch fehlenden produktionsrelevanten Funktionen für reale Benutzerkonten:

* Passwort-Reset
* E-Mail-Versand als technische Grundlage
* E-Mail-Verifikation
* ergänzende User-Attribute für produktive Self-Service-/Recovery-Flows
* saubere DB-Migrationsbasis über Liquibase

**Wichtig:**
Sprint 9 ist **kein neuer Architektur-Neubau**, sondern eine **gezielte Erweiterung der bestehenden IDM-Basis** auf Grundlage des jeweils aktuellen Textexports.

---

## 2. Ausgangslage / Baseline

**Fachlicher Stand nach Sprint 8:**

* Auth-Lifecycle mit Login + Refresh + Logout + Logout-all vorhanden
* serverseitig kontrollierbarer Refresh-/Session-Kontext vorhanden
* HTTP-API für den Web-Client im Kern stabil und erweitert
* Sprint 8 wurde als MVP-Abschluss des Auth-Lifecycle umgesetzt

**Kontext aus den Anforderungen / Konzepten:**

* Person-Domäne ist aktuell **nicht MVP-Kern** und kein Sprint-9-Pflichtziel
* Passwort-Reset und E-Mail-Verifikation sind als **nächste produktive Härtung** vorgesehen
* Liquibase soll perspektivisch als echte Migrationsbasis aktiviert werden

**Verbindliche Regel:**
Maßgebliche technische Baseline bleibt immer:

* der **jeweils aktuelle Textexport**
* plus im Chat bestätigte Änderungen

---

## 3. Sprint-9-Zielbild (Definition of Done auf hoher Ebene)

Sprint 9 ist fachlich erreicht, wenn mindestens folgende Punkte erfüllt sind:

1. **Forgot-Password-Flow** vorhanden
2. **Passwort-Reset-Flow** vorhanden
3. **Reset-Tokens** serverseitig kontrollierbar (persistiert, TTL, einmalig verwendbar)
4. **E-Mail-Versand** als technische Grundlage vorhanden
5. **E-Mail-Verifikation** für UserAccount vorhanden
6. **UserAccount** enthält die für Recovery/Verification nötigen Attribute
7. **HTTP-API** bleibt für bestehende Web-Client-Pfade stabil (nur additive Erweiterungen)
8. **Liquibase-Migrationsbasis** ist produktionsgeeignet eingeführt oder verbindlich vorbereitet
9. **Tests** für Kernpfade und zentrale Negativfälle sind grün
10. **Dokumentation / ADRs** sind konsistent

---

## 4. Nicht-Ziele für Sprint 9

Diese Punkte sind **bewusst nicht Bestandteil** von Sprint 9, sofern nicht im Verlauf zwingend erforderlich:

* Einführung einer Person-Domäne als neues fachliches Kernmodell
* umfangreiche Self-Service-Registrierung / Public Signup
* vollständiges Invite-/Onboarding-System
* Umstellung auf asymmetrische JWT-Signatur (RS256/ES256)
* tiefgreifender Umbau bestehender Auth-/User-Architektur ohne unmittelbaren Bedarf
* generische Notification-Plattform für alle zukünftigen Kanäle

**Begründung:**
Sprint 9 soll gezielt produktionskritische Gaps schließen, nicht das IDM unnötig verbreitern.

---

## 5. Priorisierte Produktiv-Gaps (aus Requirements abgeleitet)

## 5.1 P0 – Unmittelbar produktionsrelevant

### P0-01 – Passwort-Reset

Erforderlich für reale Benutzerkonten und Recovery im Fehlerfall.

### P0-02 – E-Mail-Versand

Technische Voraussetzung für Reset- und Verifikationsflüsse.

### P0-03 – E-Mail-Verifikation

Wichtig für belastbare Benutzerkonten und saubere Recovery-Semantik.

### P0-04 – UserAccount-Erweiterung

Ergänzende Attribute / Flags für produktive Self-Service- und Recovery-Fähigkeit.

---

## 5.2 P1 – Sehr sinnvolle Produktiv-Härtung

### P1-01 – Liquibase-Migrationsführung

Reproduzierbare und kontrollierte Schema-Änderungen für reale Deployments.

### P1-02 – Fehlersemantik / API-Härtung nachziehen

Falls in der jeweiligen Baseline noch Restpunkte offen sind.

### P1-03 – Testmatrix für neue Recovery-/Verification-Flows

Kernfälle + zentrale Negativfälle.

---

## 6. Empfohlene Phasenstruktur (iterativ und regressionsarm)

## Phase B1 – Analyse & Architektur-Festlegung

**Ziel:**
Deterministische Analyse des aktuellen Textexports vor Sprint-9-Umsetzung.

**Inhalte:**

* Prüfen, welche UserAccount-Felder bereits existieren
* Prüfen, ob bereits Mail-/Notification-Bausteine vorhanden sind
* Prüfen, ob Liquibase bereits teilweise vorbereitet ist
* Prüfen, welche bestehende Exception-/HTTP-Fehlerstruktur genutzt werden kann
* Prüfen, ob ADR für Reset-/Verification-Token-Modell erforderlich ist

**Ergebnis:**

* belastbare Phase-B2/B3/B4-Grundlage
* ggf. ADR(s) vor Umsetzung

---

## Phase B2 – Passwort-Reset-Domäne (intern, ohne öffentliche API zuerst)

**Ziel:**
Interne, serverseitig kontrollierbare Reset-Token-Basis schaffen.

**Voraussichtliche Inhalte (nur nach Textexport validieren):**

* Reset-Token-Entity / Statusmodell
* Repository + EntityService
* Lifecycle-Service für:

    * Token-Erzeugung
    * Token-Validierung
    * Token-Verbrauch
    * Token-Ablauf / Widerruf
* Hash-basierte Token-Speicherung (kein Klartext in der DB)
* TTL-Konfiguration

**Wichtig:**
Noch keine endgültige HTTP-API, wenn die interne Domäne nicht zuerst stabil ist.

**Testziel:**

* Domänen-/Integrationstest für Lifecycle-Kernpfade

---

## Phase B3 – Mail-Versand als technische Grundlage

**Ziel:**
Minimaler, projektkonformer Mail-Adapter für produktive Flows.

**Inhalte (baseline-abhängig konkretisieren):**

* technische Abstraktion für E-Mail-Versand
* Konfiguration für SMTP / Provider
* sauberer Port/Adapter-Schnitt (keine Fachlogik im Transport-Layer)
* robuste Fehlerbehandlung / Logging

**Wichtig:**

* zunächst minimal und fokussiert
* keine generische Over-Engineering-Notification-Plattform

**Testziel:**

* Integrations-/Komponententest mit testbarer Versand-Abstraktion

---

## Phase B4 – Forgot Password / Reset Password HTTP-API

**Ziel:**
Öffentliche API für Recovery-Flows, aber strikt additive Erweiterung.

**Mögliche Endpunkte (nur nach Baseline konkretisieren):**

* `POST /auth/forgot-password`
* `POST /auth/reset-password`

**Anforderungen:**

* keine User-Existenz nach außen leaken
* stabile Fehlersemantik
* Reset-Token nur einmal verwendbar
* Passwort-Policy muss greifen
* optional: bestehende Sessions nach erfolgreichem Reset widerrufen

**Testziel:**

* Erfolgsfall
* ungültiger Token
* abgelaufener Token
* bereits verbrauchter Token
* unbekannter Benutzer ohne Informationsleck

---

## Phase B5 – UserAccount-Erweiterung für produktive Recovery-/Verification-Flows

**Ziel:**
User-Domäne so ergänzen, dass produktive Flows sauber abbildbar sind.

**Mögliche Inhalte (nur nach Baseline konkretisieren):**

* E-Mail-Adresse
* `emailVerified`
* optional `emailVerifiedAt`
* optional `passwordChangeRequired`
* ggf. ergänzende Security-Metadaten

**Wichtig:**

* bestehende APIs möglichst stabil halten
* Erweiterungen additive und UI-verträglich gestalten

**Testziel:**

* Mapping / DTO / Persistenz / Validierung

---

## Phase B6 – E-Mail-Verifikation

**Ziel:**
Verifikationsfluss für E-Mail-Adressen ergänzen.

**Mögliche Inhalte (baseline-abhängig):**

* Verifikations-Token-Modell oder Wiederverwendung eines generischen Token-Musters
* API zum Bestätigen der E-Mail
* optional API zum erneuten Versand
* konsistente User-Status-Aktualisierung

**Wichtig:**
Vorher prüfen, ob Reset- und Verification-Token technisch gemeinsam oder getrennt modelliert werden sollen.

**Testziel:**

* Erfolgsfall
* ungültiger / abgelaufener Token
* Wiederholungsfall

---

## Phase B7 – Liquibase produktionsfähig aktivieren / nachschärfen

**Ziel:**
Reale Deployments mit kontrollierter Schema-Entwicklung absichern.

**Inhalte:**

* prüfen, was bereits vorhanden ist
* bestehende Struktur übernehmen, nicht neu erfinden
* ChangeSets für neue Sprint-9-Tabellen/Felder sauber abbilden
* Startverhalten / Dev-Test-Kompatibilität prüfen

**Wichtig:**
Nur deterministisch anhand der vorhandenen Projektstruktur.

---

## Phase B8 – Gesamtreview / Release-Readiness nach Sprint 9

**Ziel:**
Formale Prüfung, ob der Stand jetzt als „echte produktive V1“ tragfähig ist.

**Prüfkriterien:**

* Requirements-Abdeckung
* API-Stabilität
* Auth-/Recovery-/Verification-Semantik
* Teststatus
* Security-Basis
* Dokumentationskonsistenz
* bekannte Restrisiken

---

## 7. ADR-Bedarf (vor jeder relevanten Phase neu prüfen)

Voraussichtlich ADR-pflichtig oder ADR-prüfpflichtig:

### ADR-S9-01 – Reset-/Verification-Token-Modell

Zu klären:

* getrennte Modelle vs. generisches Token-Muster
* Statusmodell
* TTL / Einmalverwendung
* Hashing

### ADR-S9-02 – Mail-Versand-Architektur

Zu klären:

* minimaler Port/Adapter-Schnitt
* Fehlerbehandlung
* Sync vs. bewusst später Async

### ADR-S9-03 – Passwort-Reset-Sicherheitssemantik

Zu klären:

* User-Enumeration-Vermeidung
* Session-Revocation nach Reset
* Rate-Limit / Missbrauchsschutz (soweit im Scope)

**Wichtig:**
ADRs nur erstellen, wenn die Baseline die Entscheidung nicht bereits deterministisch vorgibt.

---

## 8. Qualitäts- und Stabilitätsregeln für Sprint 9

Für Sprint 9 gilt ausdrücklich:

1. Bestehende Web-Client-HTTP-API nur **additiv** erweitern
2. Keine regressiven Änderungen an bestehenden Login-/Refresh-/Logout-Pfaden
3. Vor jeder Phase: neuer Textexport prüfen
4. Nur vorhandene Projektstrukturen verwenden
5. Keine erfundenen Klassen / Ordner / Methoden
6. Klassengröße, Namen, Logging, Exceptions, DTO-Schnitt sauber projektkonform halten
7. Tests pro Phase grün halten
8. Technische Schulden / bewusst verschobene Punkte sofort dokumentieren

---

## 9. Erwartetes Ergebnis nach Sprint 9

Wenn Sprint 9 wie geplant umgesetzt wird, sollte das IDM anschließend einen Stand erreichen, der fachlich und technisch deutlich näher an einer **echten produktiven V1** liegt:

* belastbarer Auth-Lifecycle
* Recovery für reale Benutzerkonten
* E-Mail-basierte Verifikation
* produktionsgeeignete User-Basisattribute
* kontrollierbare DB-Migrationen
* stabile, additive HTTP-API

**Interpretation:**
Sprint 8 = produktionsnaher MVP-Abschluss

Sprint 9 = gezielte Schließung der wichtigsten produktiven Feature-Gaps

---

## 10. Nächster deterministischer Schritt

Vor Beginn der Umsetzung ist zwingend:

* **aktuellen Textexport analysieren**
* **bestehende Requirements-/ADR-Dokumente erneut gegenprüfen**
* **Phase B1 konkretisieren**
* **ADR-Bedarf vor B2 verbindlich bewerten**

Erst danach darf die eigentliche Umsetzung starten.
