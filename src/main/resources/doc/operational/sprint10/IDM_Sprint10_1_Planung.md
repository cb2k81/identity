# IDM Sprint 10 – Planung (Produktionshärtung nach Sprint 9)

## 1. Ziel des Sprints

Sprint 10 dient der **gezielten Produktionshärtung nach dem Abschluss von Sprint 9**.

Sprint 9 fokussiert bewusst die **UI-Reifmachung der IDM-API für GWC**. Sprint 10 übernimmt im Anschluss den zuvor zurückgestellten Themenblock zur **produktiven Härtung über den aktuellen Admin-/UI-fähigen Stand hinaus**.

Ziel ist ein Stand, bei dem das IDM nicht nur als technisch belastbarer MVP bzw. UI-fähige Verwaltungsbasis, sondern als **für den produktiven Einsatz fachlich und betrieblich sinnvoller V1-Stand** verwendet werden kann.

Der Schwerpunkt liegt auf den noch fehlenden produktionsrelevanten Funktionen für reale Benutzerkonten und produktive Betriebsfähigkeit:

* Passwort-Reset / Forgot-Password
* E-Mail-Versand als technische Grundlage
* E-Mail-Verifikation
* ergänzende User-Attribute für produktive Self-Service-/Recovery-Flows
* saubere DB-Migrationsbasis über Liquibase
* ggf. ergänzende produktionsrelevante Sicherheits- und Stabilitätsmaßnahmen

**Wichtig:**

Sprint 10 ist **kein Architektur-Neubau**, sondern eine **gezielte Erweiterung und Härtung der bestehenden IDM-Basis** auf Grundlage des jeweils aktuellen Textexports.

---

## 2. Ausgangslage / Baseline

### 2.1 Fachlicher Stand nach Sprint 8

Sprint 8 hat das IDM auf einen **produktiven MVP-/Admin-UI-nahen Kernstand** gebracht.

Dieser Stand umfasst insbesondere:

* Auth-Lifecycle mit `login`, `refresh`, `logout`, `logout-all`
* serverseitig kontrollierbaren Session-/Refresh-Kontext
* JWT-basierte Access-Token-Architektur
* belastbare Management-/Admin-API-Grundlagen
* UI-taugliche Listen-/Relationen-Basis für den Admin-Kontext

Damit ist das IDM nach Sprint 8 **MVP-fähig**, aber noch nicht vollständig auf produktive Benutzerkonten-Recovery und produktive Betriebsprozesse gehärtet.

---

### 2.2 Zusätzlicher Stand nach Sprint 9

Sprint 9 dient im aktuellen Projektstand der **UI-Reifmachung der IDM-API** für die GWC-User-Oberflächen.

Das bedeutet für Sprint 10 als Ausgangslage:

* User-bezogene API-Verträge sind fachlich klarer und UI-tauglicher
* die GWC-Kopplung für Liste / Detail / Formular ist konzeptionell sauberer
* User wird verbindlich als **technischer UserAccount** behandelt, nicht als Personenmodell
* bestehende API-Verträge wurden bevorzugt **additiv erweitert** statt unnötig ersetzt
* bekannte Architektur-/Konsistenz-Themen sind separat als **Technische Schulden** dokumentiert

Sprint 10 baut damit **nicht mehr auf einem reinen MVP**, sondern auf einem **UI-reiferen Admin-/Management-Stand** auf.

---

### 2.3 Verbindliche Baseline-Regel

Maßgebliche technische Baseline bleibt immer:

* der **jeweils aktuelle Textexport**
* plus im Chat bestätigte Änderungen

Es gilt weiterhin strikt:

* keine Annahmen über nicht sichtbaren Code
* keine erfundenen Klassen, Methoden oder Pfade
* keine stillen Architektur- oder Persistenzumbauten

Diese Regeln sind konsistent mit dem Arbeitsvertrag und den AGENTS-Regeln. fileciteturn27file2turn27file4

---

## 3. Sprint-10-Zielbild (Definition of Done auf hoher Ebene)

Sprint 10 ist fachlich erreicht, wenn mindestens folgende Punkte erfüllt sind:

1. Ein Benutzer kann einen **sicheren Passwort-Reset-Prozess** durchlaufen, ohne dass das IDM dabei unnötig Enumeration-anfällig wird.
2. Es existiert eine **saubere E-Mail-Versand-Basis**, die projektkonform in die bestehende Architektur integriert ist.
3. Benutzerkonten können **E-Mail-verifiziert** werden, sofern dieser Prozess fachlich vorgesehen ist.
4. Die für produktive Recovery-/Verification-Flows benötigten **zusätzlichen User-Attribute** sind fachlich sauber im Modell verankert.
5. Die DB-Migrationsbasis ist nicht mehr nur „vorbereitet“, sondern fachlich und technisch **belastbar über Liquibase steuerbar**.
6. Bestehende Login-/Refresh-/Logout-Pfade bleiben regressionsfrei.
7. Neue produktionsrelevante API-Verträge sind additiv und konsistent.
8. Sicherheitsrelevante Flows (Reset / Verification) sind testseitig sinnvoll abgesichert.
9. Bewusst zurückgestellte Themen oder Rest-Risiken sind sauber dokumentiert.
10. Der resultierende Stand ist fachlich näher an einer **echten produktiven V1** als der Sprint-8-/Sprint-9-Zustand.

---

## 4. Fachlicher Scope von Sprint 10

Sprint 10 umfasst ausschließlich die **produktive Härtung nach dem UI-Reifegrad-Sprint 9**.

### 4.1 Im Scope

* Passwort-Reset / Forgot-Password-Use-Case
* Token-/Schlüssel-/Ablauf-Mechanik für Reset-Prozesse
* E-Mail-Versand als technische Infrastruktur für IDM-relevante Flows
* E-Mail-Verifikation (sofern aus Requirements / Baseline bestätigt)
* zusätzliche User-Attribute für Recovery / Verification, sofern erforderlich
* saubere Aktivierung bzw. belastbare Nutzung von Liquibase als Migrationsbasis
* API-/DTO-/Persistenzergänzungen, die dafür fachlich zwingend notwendig sind
* Tests für neue produktionsrelevante Flows
* Dokumentation verbleibender Risiken / bewusster Grenzen

---

### 4.2 Explizit nicht im Scope

Nicht Bestandteil von Sprint 10 sind aktuell:

* Einführung einer Personen-Domäne
* Ausbau zu einem vollwertigen OAuth2 Authorization Server
* OIDC / Federation / SSO
* generischer Mail-Workflow-Baukasten über den IDM-Bedarf hinaus
* allgemeines Notification-System
* Frontend-Implementierung in GWC
* nicht zwingend benötigte Session-Admin-Features, sofern nicht direkt aus Requirements ableitbar
* größere Refactorings nur aus „Code-Schönheit“

---

## 5. Fachliche Leitlinien für Sprint 10

### 5.1 Bestehende IDM-Architektur bleibt Leitplanke

Sprint 10 baut auf dem vorhandenen monolithischen IDM-Backend auf.

Es gilt weiterhin:

* bestehende Schichten strikt beibehalten
* Controller / Command / Query / Service / Mapper / Repository / DTO / Entity nicht vermischen
* keine stillen Vertragsänderungen
* keine impliziten Persistenzänderungen

Diese Regeln sind in den AGENTS-Dateien verbindlich hinterlegt. fileciteturn27file2turn27file4

---

### 5.2 Minimalstrategie für produktive Erweiterungen

Auch in Sprint 10 gilt bewusst:

* bestehende Endpunkte bevorzugt **additiv erweitern**, wenn fachlich passend
* neue Endpunkte nur bei echter funktionaler Lücke
* bestehende User-/Auth-Modelle nur so weit erweitern, wie für Recovery / Verification nötig
* keine „vorsorglichen“ Erweiterungen ohne klare fachliche Nutzung
* keine stillen Nebenbei-Refactorings

---

### 5.3 Sicherheit vor Komfort

Bei Passwort-Reset und E-Mail-Verifikation gilt:

* Sicherheitssemantik hat Vorrang vor UX-Komfort
* Enumeration-Vermeidung ist zentral
* Ablaufzeiten / Token-Semantik müssen sauber definiert sein
* Session- und Auth-Lifecycle-Folgen nach sensitiven Aktionen müssen bewusst entschieden werden
* Rate-Limit / Missbrauchsschutz ist mindestens fachlich zu bewerten, auch wenn nicht alles im Sprint vollumfänglich umgesetzt wird

---

## 6. Bereits erkennbare Themenblöcke für Sprint 10

Aus der bisherigen Planung und den früheren Sprint-9-Vorlagen ergeben sich für Sprint 10 die folgenden Hauptblöcke.

### Block A – User-Modell für produktive Recovery / Verification

Mögliche Themen (nur sofern Baseline/Requirements dies tragen):

* E-Mail-Verifikationsstatus
* Verifikationszeitpunkt
* Reset-Token-Kontext
* Reset-Token-Ablauf
* ggf. weitere minimal nötige Security-/Recovery-Attribute

Wichtig:

Die konkrete Modellierung darf **nicht vorab erfunden** werden, sondern muss aus Baseline + Requirements + ADR-Bedarf abgeleitet werden.

---

### Block B – Passwort-Reset / Forgot-Password

Ziel:

* sicherer Reset-Prozess für reale Benutzerkonten

Typische fachliche Teilfragen:

* Anfrage eines Reset-Prozesses
* Ausgabe einer neutralen / enumeration-armen Antwort
* Generierung eines sicheren Reset-Kontexts
* Validierung des Reset-Kontexts
* Setzen eines neuen Passworts
* Entscheidung zu Session-Revocation / Logout-all nach Reset

---

### Block C – E-Mail-Versand-Basis

Ziel:

* IDM kann E-Mails für Reset / Verifikation kontrolliert versenden

Typische fachliche Teilfragen:

* minimaler Port/Adapter-Schnitt
* Konfiguration / Profile
* Fehlerbehandlung
* sync vs. bewusst später async
* testbare Abstraktion ohne harte Kopplung an konkrete Infrastruktur

---

### Block D – E-Mail-Verifikation

Ziel:

* produktive Verifikation der Benutzer-E-Mail, sofern fachlich vorgesehen

Typische fachliche Teilfragen:

* Verifikations-Token / Ablauf
* einmalige Nutzung
* Re-Verification bei E-Mail-Änderung
* Verhalten bei nicht verifizierten Konten
* Wechselwirkung mit Login-/Recovery-Flow

---

### Block E – Liquibase-Härtung

Ziel:

* DB-Migrationsbasis fachlich und technisch belastbar machen

Typische fachliche Teilfragen:

* aktueller Realzustand der Changelogs
* Aktivierungsstrategie ohne Regression
* bestehende Baseline vs. neue Migrationen
* deterministische Einordnung vorhandener Dateien
* Umgang mit bereits laufender DB / DEV-Profilen

Die Baseline zeigt bereits eine vorbereitete, aber noch nicht belastbar genutzte Migrationsbasis; dies ist ausdrücklich zu prüfen und nicht blind „einzuschalten“. fileciteturn27file15

---

## 7. Erwartete ADR-Bedarfe in Sprint 10

Vor jeder Umsetzungsphase ist ADR-Bedarf zu prüfen.

Aktuell sind mindestens diese potenziellen ADR-Themen erkennbar:

### ADR-S10-01 – Passwort-Reset-Sicherheitssemantik

Zu klären:

* User-Enumeration-Vermeidung
* Reset-Token-Lebenszyklus
* Session-Revocation nach Reset
* Rate-Limit / Missbrauchsschutz (mindestens fachlich bewertet)

---

### ADR-S10-02 – Mail-Versand-Architektur

Zu klären:

* minimaler Port/Adapter-Schnitt
* Fehlerbehandlung
* sync vs. bewusst später async
* Testbarkeit / Mockbarkeit

---

### ADR-S10-03 – E-Mail-Verifikationssemantik

Zu klären:

* Verifikationsstatus im User-Lebenszyklus
* Re-Verification bei E-Mail-Änderung
* Einfluss auf Login oder sensible Aktionen

---

### ADR-S10-04 – Liquibase-Aktivierungsstrategie

Zu klären:

* wie die bestehende Changelog-Basis deterministisch übernommen wird
* welche Migration als Baseline gilt
* wie Regressionen in DEV/Test vermieden werden

**Wichtig:**

ADRs nur erstellen, wenn die Baseline die Entscheidung nicht bereits deterministisch vorgibt.

---

## 8. Geplanter Sprint-10-Arbeitszuschnitt (phasenweise)

Sprint 10 soll deterministisch in klaren, testbaren Teilzuständen umgesetzt werden.

### Phase A – Baseline-/Requirements-/ADR-Prüfung

Ziel:

* aktuellen Textexport prüfen
* Requirements / bestehende operative Dokumente / ADRs erneut gegenprüfen
* echte Muss-Themen von Wunschthemen trennen
* ADR-Bedarf verbindlich festlegen

Abschlusskriterium:

* keine unklaren Architekturentscheidungen mehr für Phase B

---

### Phase B – Modell- und Vertragspräzisierung für Recovery / Verification

Ziel:

* minimale fachlich nötige Modell- und DTO-Erweiterungen sauber festlegen
* API-Verträge für Reset / Verification deterministisch definieren

Abschlusskriterium:

* klares Umsetzungsziel für die produktiven Flows ohne Spekulation

---

### Phase C – Passwort-Reset-Flow

Ziel:

* Reset anfordern
* Reset validieren / durchführen
* sichere Response-Semantik
* sinnvolle Tests

Abschlusskriterium:

* vollständiger, fachlich belastbarer Reset-Flow

---

### Phase D – E-Mail-Versand-Basis + Verifikation

Ziel:

* Mail-Infrastruktur minimal und testbar integrieren
* E-Mail-Verifikations-Flow umsetzen, sofern fachlich bestätigt

Abschlusskriterium:

* E-Mail-gestützte produktive Kernprozesse technisch nutzbar

---

### Phase E – Liquibase-Härtung

Ziel:

* Migrationsbasis deterministisch prüfen und belastbar machen

Abschlusskriterium:

* dokumentierte, reproduzierbare DB-Migrationsstrategie

---

### Phase F – Abschlussreview / Rest-Risiken / Dokumentation

Ziel:

* Gesamtprüfung gegen Requirements / ADRs / Teststand
* klare Trennung zwischen erreichtem V1-Stand und verbleibenden Folgepunkten

Abschlusskriterium:

* sauberer Abschlussbericht mit belastbarer Einordnung

---

## 9. Qualitäts- und Stabilitätsregeln für Sprint 10

Für Sprint 10 gilt ausdrücklich:

1. Bestehende Web-Client-HTTP-API nur **additiv** erweitern
2. Keine regressiven Änderungen an bestehenden Login-/Refresh-/Logout-Pfaden
3. Vor jeder Phase: neuen Textexport prüfen
4. Nur vorhandene Projektstrukturen verwenden
5. Keine erfundenen Klassen / Ordner / Methoden
6. DTO-/Mapper-/Service-Regeln aus AGENTS strikt beachten
7. Persistenz nur ändern, wenn fachlich eindeutig nötig
8. Sicherheitsrelevante Flows besonders streng testen
9. Technische Schulden / bewusst verschobene Punkte sofort dokumentieren
10. Keine stillen Nebenbei-Refactorings

Diese Regeln entsprechen den aktuell verbindlichen AGENTS-Vorgaben. fileciteturn27file2turn27file4

---

## 10. Erwartetes Ergebnis nach Sprint 10

Wenn Sprint 10 wie geplant umgesetzt wird, sollte das IDM anschließend einen Stand erreichen, der fachlich und technisch deutlich näher an einer **echten produktiven V1** liegt:

* belastbarer Auth-Lifecycle (bereits aus Sprint 8)
* UI-reife User-API-Kopplung (aus Sprint 9)
* Recovery für reale Benutzerkonten
* E-Mail-basierte Verifikation
* produktionsgeeignete User-Basisattribute für Recovery / Verification
* kontrollierbare DB-Migrationen
* stabile, additive HTTP-API

**Interpretation:**

* Sprint 8 = MVP-Abschluss inkl. produktivem Auth-Lifecycle
* Sprint 9 = UI-Reifegrad der API für GWC
* Sprint 10 = produktive Härtung der Benutzerkonto-Lifecycle-Fähigkeiten

---

## 11. Nächster deterministischer Schritt

Vor Beginn der Umsetzung ist zwingend:

1. aktuellen Textexport analysieren
2. bestehende Requirements-/ADR-Dokumente erneut gegenprüfen
3. prüfen, welche Punkte aus Sprint 10 echte Muss-Anforderungen sind
4. ADR-Bedarf für Phase B verbindlich bewerten
5. erst danach Phase B konkret herunterbrechen

Erst danach darf die eigentliche Umsetzung starten.

---

## 12. Zusammenfassung

Sprint 10 übernimmt den **bewusst aus Sprint 9 herausgelösten Produktionshärtungsblock**.

Der Sprint fokussiert:

* sichere Recovery-Prozesse
* E-Mail-gestützte produktive Benutzerkonto-Flows
* minimale, fachlich saubere Erweiterung des User-Modells
* belastbare Migrationsfähigkeit
* strikte Wahrung der bestehenden Architektur und API-Stabilität

Damit ist Sprint 10 der logische Folge-Sprint, um den Stand nach **Sprint 8 (MVP + Auth-Lifecycle)** und **Sprint 9 (UI-Reifegrad)** gezielt in Richtung **echter produktiver V1** weiterzuführen.
