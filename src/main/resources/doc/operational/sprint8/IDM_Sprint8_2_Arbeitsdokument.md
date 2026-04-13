# IDM Sprint 8 – Arbeitsdokument

**Stand:** 2026-04-13
**Quelle:** Ausschließlich Analyse des jüngsten im Chat verwendeten Textexports (`idm_code-export_2026-04-02_12-42-50`) sowie der darauf im Chat verifizierten Änderungen / Testläufe.
**Zweck:** Operatives Arbeitsdokument für Sprint 8 (interne Referenz, kein PM-Freigabedokument).

---

## 1. Einleitung

Dieses Dokument dient als operatives Arbeitsdokument für den aktuellen Stand von Sprint 8.

Es dokumentiert:

* den bereits umgesetzten und verifizierten Arbeitsstand,
* die aktuell belastbar abgeschlossenen Teilbereiche,
* verbleibende offene Sprint-8-Punkte,
* sowie technische Schulden bzw. Nachbeobachtungspunkte.

Das Dokument ist bewusst auf den fachlich und technisch relevanten Arbeitsstand fokussiert und dient als Referenz für die weitere Sprint-8-Finalisierung.

---

## 2. Sprint-8-Stand – fachliche Einordnung

Sprint 8 ist aktuell **teilweise abgeschlossen**.

### Bereits fachlich abgeschlossen bzw. belastbar umgesetzt

1. **Management-/Admin-API-Block** als eigener Sprint-8-Teilblock
2. **Self-Scope vs. Foreign-Scope** fachlich verbindlich geschärft
3. **Listen-API-Erweiterung für relationale Reads im Assignment-Bereich** umgesetzt und testseitig abgesichert

### Weiterhin offen

1. **Auth-Lifecycle-Block** (Refresh / Logout / Logout-all / Session-Kontext)
2. **Restliche Fehlersemantik-Härtung** (insbesondere konsistente NotFound-Behandlung), soweit noch nicht vollständig umgesetzt

---

## 3. Bereits abgeschlossene Arbeiten in Sprint 8

### 3.1 Fachliche Klärung: Self-Scope vs. Foreign-Scope

Die im Sprint relevante Scope-Semantik wurde im Chat verbindlich geschärft:

* **Self-Scope (IDM-intern)**

    * Permissions sind hier fachlich relevant
    * IDM verwaltet hier eigene Permission-Logik

* **Foreign-Scopes (z. B. Personnel, perspektivisch weitere Apps)**

    * erhalten Rollen-Zuordnungen / Rollen-Claims
    * benötigen im IDM **keine globale Permission-Hauptverwaltung als MVP-Pflicht**
    * Permissions werden in den jeweiligen Fachanwendungen den Rollen zugeordnet

**Folge für Sprint 8:**

* Eine generische, globale Foreign-Permission-Hauptliste ist **kein Sprint-8-MVP-Zwang**.

---

### 3.2 Management-/Admin-API-Block

Der Management-/Admin-API-Teil wurde als eigener, fachlich separater Sprint-8-Block behandelt.

Bearbeiteter Fokus:

* generische Listenfähigkeit zentraler Management-Bereiche
* Relation-Reads für Admin-UI-nahe Anwendungsfälle
* Bewertung von UI-relevanten Fehlerbildern
* Abgrenzung der Scope-Semantik

Dieser Block gilt im aktuellen Stand als **abgeschlossener Zwischenblock**.

---

### 3.3 Listen-API für relationale Reads (Assignment-Bereich)

Der relationale Listen-Read-Bereich wurde produktiv erweitert.

#### Umgesetztes Ziel

Zusätzlich zu bestehenden Listen-Endpunkten wurden **additive paginierte `/list`-Varianten** eingeführt, um UI-taugliche, standardisierte Listen-Responses für relationale Reads bereitzustellen.

#### Umgesetzte Relationsrichtungen

1. **User → Scopes**
2. **Scope → Users**
3. **User → Roles (im Scope)**
4. **Role → Users (im Scope)**
5. **Permission → Roles**
6. **Role → Permissions**

#### Technische Umsetzung (gegen Baseline verifiziert)

* additive `/list`-Endpunkte in den relevanten Assignment-Controllern
* spezifische Paged-Handler pro Relationsrichtung
* paginierte Antwortstruktur über `PagedResponseDTO<...>`
* bestehende Legacy-Read-Endpunkte bleiben bestehen (keine Entfernung / kein Breaking Change)

#### Implementierungsprinzip

* minimal-invasiv
* additive API-Erweiterung
* bestehendes Verhalten unverändert belassen
* neue Listen-API explizit für UI-/DataGrid-nahe Nutzung

---

## 4. Teststand für den Listen-API-Block

### 4.1 Status

Der Listen-API-Block ist aktuell **testseitig grün**.

Nach Korrektur eines einzelnen Tests sind laut Chat-Validierung **alle Tests grün**.

### 4.2 Reale Teststrategie

Die Listen-API wurde **nicht** über neue, künstlich getrennte Testarchitektur abgesichert, sondern über die bereits vorhandenen fachlich passenden Query-Integrationstests.

Die relevanten Query-Integrationstests wurden um zusätzliche `/list`-Szenarien erweitert.

### 4.3 Fachlich abgesicherte `/list`-Szenarien

Es wurden die sechs neuen paginierten Relationsreads testseitig ergänzt:

1. `list_scopes_of_user_paged_as_admin`
2. `list_users_of_scope_paged_as_admin`
3. `list_roles_of_user_in_scope_paged_as_admin`
4. `list_users_of_role_in_scope_paged_as_admin`
5. `list_roles_of_permission_paged_as_admin`
6. `list_permissions_of_role_paged_as_admin`

### 4.4 Testlogik – fachliche Bewertung

Die Tests sind im aktuellen Stand **fachlich sinnvoll und korrekt definiert** für den Sprint-8-Listenblock:

* prüfen erfolgreiche Erreichbarkeit der neuen `/list`-Routen
* prüfen, dass die erwartete Relation im Ergebnis enthalten ist
* prüfen die reale `PagedResponseDTO`-Struktur (`items`, `page`, `size`, `totalElements`, `totalPages`)
* vermeiden unzulässige Annahmen über exakte Treffermengen in Shared-Scope-Szenarien

### 4.5 Wichtige Korrektur im Testbestand

Ein Test musste fachlich korrigiert werden:

* Beim Szenario **Scope → Users (paged)** war eine Annahme „genau 1 Treffer“ nicht stabil/fachlich falsch.
* Der Endpunkt liefert korrekt **alle** Benutzer, die diesem Scope zugeordnet sind.
* Der Test wurde daher auf **Enthaltensein / Mindestmenge / Paging-Plausibilität** umgestellt.

**Ergebnis:** Die Korrektur entspricht der realen Semantik des Endpunkts.

---

## 5. Fachliche Abschlussbewertung des Listen-API-Blocks

### 5.1 Bewertung

Der Sprint-8-Listen-API-Block ist aktuell:

* **fachlich sinnvoll umgesetzt**
* **technisch konsistent umgesetzt**
* **testseitig belastbar abgesichert**
* **additiv und regressionsarm integriert**

### 5.2 Reichweite der Testabdeckung

Die Testabdeckung ist für den Sprint-8-Listenblock **ausreichend**, aber bewusst fokussiert auf den **Happy Path**.

Aktuell **nicht** explizit abgesichert sind insbesondere:

* ungültige Paging-Parameter
* ungültige Sortierparameter
* explizite Berechtigungs-Negativtests für die neuen `/list`-Routen
* Mehrseiten-/Paging-Grenzfälle
* explizite Sortierreihenfolge über mehrere Datensätze

Für Sprint 8 ist das **vertretbar**, sollte aber als technische Schuld notiert werden.

---

## 6. Technische Schulden / Notierpunkte

### 6.1 Listen-API-Testabdeckung ist positivpfad-lastig

Die aktuellen `/list`-Tests prüfen primär den fachlichen Erfolgsfall.

Empfohlene spätere Ergänzungen:

* Negativtests für `page`, `size`, `sortBy`, `sortDir`
* Mehrseiten-Szenarien
* Sortierverifikation über mehrere Datensätze
* Berechtigungs-Negativtests

### 6.2 Test-Setup-Duplizierung in Query-Tests

In den Query-Integrationstests ist Setup-Logik mehrfach ähnlich / redundant.

Potenzielle spätere Verbesserung:

* gemeinsame Helper / Factory-Methoden für Create-/Assign-Abläufe

### 6.3 Shared-Scope-Kardinalität ist testdatenabhängig

Bei Sammelrelationen (z. B. Scope → Users) ist die Treffermenge vom gesamten Testdatenbestand abhängig.

Konsequenz:

* keine Tests auf starre exakte Anzahl, wenn fachlich mehrere Zuordnungen möglich sind
* bevorzugt: Enthaltensein + Paging-Plausibilität

### 6.4 Paged-Handler sind bewusst spezifisch und eher handwerklich

Die aktuelle Lösung mit spezifischen Paged-Handlern ist für Sprint 8 robust und deterministisch, aber nicht maximal generisch.

Kein akuter Änderungsbedarf im Sprint.
Möglicher späterer Refactoring-Kandidat, falls ein generischer Listen-/Query-Standard im IDM stabilisiert wird.

### 6.5 Fehlersemantik-Härtung separat weiter beobachten

Soweit NotFound-/Fehlersemantik im Sprint-8-Scope noch nicht vollständig harmonisiert ist, bleibt dies ein eigener Restpunkt.

---

## 7. Offene Sprint-8-Ziele (Restscope)

### 7.1 Haupt-Restblock: Auth-Lifecycle

Der im Zwischenstand bereits abgegrenzte **Auth-Lifecycle-Block** ist weiterhin offen.

Offene Zielthemen:

1. **Refresh-Endpunkt** (`POST /auth/refresh`)
2. **Logout-Endpunkt** (`POST /auth/logout`)
3. **Logout-all-Endpunkt** (`POST /auth/logout-all`)
4. **Minimaler serverseitiger Session-/Refresh-Kontext**
5. **Invalidierungsregeln**

#### Fachlich notwendige Kernlogik

* gültiger Refresh-Kontext erzeugt neuen Access-Token
* ungültiger / widerrufener / abgelaufener Refresh-Kontext wird abgewiesen
* Logout invalidiert die aktuelle Session
* Logout-all invalidiert alle Sessions des Users
* deaktivierter User darf im Refresh-Pfad nicht erfolgreich weiterarbeiten

---

### 7.2 Auth-Tests fehlen für den offenen Restblock

Für den Auth-Lifecycle-Block fehlen im offenen Restscope die belastbaren Integrationstests.

Mindestens erforderlich:

1. gültiger Refresh liefert neuen Access-Token
2. ungültiger Refresh wird abgewiesen
3. Logout invalidiert aktuelle Session
4. Refresh nach Logout schlägt fehl
5. Logout-all invalidiert alle Sessions
6. Refresh nach Logout-all schlägt fehl

Optional sinnvoll:

* deaktivierter User im Refresh-Pfad
* abgelaufene Session / abgelaufener Refresh-Kontext

---

### 7.3 Fehlersemantik-Härtung (Restprüfung)

Soweit im Sprint-8-Scope noch nicht vollständig abgeschlossen, bleibt als Restpunkt:

* konsistente Behandlung von NotFound-Fällen
* insbesondere dort, wo fachlich `404 Not Found` erwartet wird
* keine gemischte / zufällige `400`-Semantik für echte Nicht-Existenz-Fälle

Dieser Punkt sollte nach dem Auth-Lifecycle-Block separat und minimal-invasiv abgeschlossen werden.

---

## 8. Empfohlene nächste Arbeitsreihenfolge

### Nächster sinnvoller Schritt

**Sprint 8 – Phase A1: Auth-Lifecycle Gap-Analyse gegen die reale Baseline**

Ziel:

* exakt feststellen, was im Auth-Bereich bereits vorhanden ist
* harte Gap-Liste für `refresh`, `logout`, `logout-all`
* minimale Klassen-/Dateimenge definieren
* prüfen, ob eine ADR / Feinkonzept-Entscheidung erforderlich ist

### Danach

1. Auth-Lifecycle minimal und testbar umsetzen
2. Auth-Integrationstests ergänzen
3. Fehlersemantik-Härtung final prüfen und ggf. minimal korrigieren
4. Sprint 8 formal abschließen

---
