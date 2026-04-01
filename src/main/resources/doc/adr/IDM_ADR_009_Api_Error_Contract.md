# IDM – ADR 009: API Error Response & Conflict Handling für UI-taugliche Management-APIs

Stand: 2026-03-27
Status: Accepted (Sprint 8 – verbindliche Architekturentscheidung)
Version: 1.0

---

## 1. Kontext

Der IDM-Service besitzt bereits eine REST-basierte Management-API sowie ein bestehendes globales Error-Handling auf technischer Ebene.

Im aktuellen Baseline-Stand ist jedoch nicht sichergestellt, dass alle fachlich erwartbaren Fehlersituationen für ein Admin-UI in einer konsistenten, UI-tauglichen Form ausgeliefert werden.

Insbesondere für Sprint 8 ist das Zielbild verbindlich:

* Fehlerbilder müssen für ein Admin-UI verlässlich auswertbar sein
* fachliche Konflikte dürfen nicht als technische 500er erscheinen
* Query-/Listenfehler müssen sauber als Client-Fehler erkennbar sein
* bestehende technische Error-Handling-Strukturen sollen genutzt, nicht unnötig ersetzt werden

Die vorhandene Architektur enthält bereits:

* globales Exception-Handling
* standardisierte Error-Response-Strukturen
* zentrale HTTP-/Fehler-Infrastruktur

Diese vorhandene Basis soll in Sprint 8 **konsequent fachlich gehärtet** werden.

---

## 2. Problembeschreibung

### 2.1 Ist-Problem

In der aktuellen Baseline können fachliche Konflikte – insbesondere doppelte Zuordnungen / Duplicate Assignments – zumindest in Teilen noch als technische Fehler (z. B. 500 aufgrund von DB-Constraint-Verletzungen) sichtbar werden.

Das ist für ein Admin-UI problematisch, weil:

1. fachliche Konflikte nicht zuverlässig von echten Serverfehlern unterscheidbar sind,
2. UI-Komponenten keine stabile Konfliktbehandlung implementieren können,
3. technische Persistenzdetails in das API-Verhalten „durchschlagen“,
4. ein nutzbares MVP dadurch unnötig fragil wird.

### 2.2 Zusätzlicher Sprint-8-Bedarf

Mit Einführung UI-fähiger Listen steigt der Bedarf an konsistenten Client-Fehlern zusätzlich:

* ungültige `page`-Werte
* ungültige `size`-Werte
* ungültige `sortBy`-Felder
* ungültige `sortDir`-Werte
* ungültige oder nicht erlaubte Filterparameter

Diese Fälle müssen sauber und deterministisch behandelt werden.

---

## 3. Entscheidung

### 3.1 Grundsatzentscheidung

Sprint 8 führt einen **verbindlichen UI-tauglichen Fehlerstandard** für die Management-API ein.

Verbindliches Ziel:

* fachliche Konflikte → **4xx**
* technische Systemfehler → **5xx**
* kein fachlicher Standardfall darf primär über einen rohen DB-Constraint-Fehler bis ins API durchschlagen

---

### 3.2 Konflikte sind fachliche Fehler, keine 500er

Duplicate-Assignments und vergleichbare Zustandskonflikte gelten als **fachliche Konflikte**.

Typische Beispiele:

* Rolle bereits dem User zugeordnet
* Scope bereits dem User zugeordnet
* Permission bereits der Rolle zugeordnet

Verbindliche Regel:

> Fachliche Konflikte werden als `409 Conflict` behandelt, nicht als `500 Internal Server Error`.

---

### 3.3 Vorab-Prüfung vor Persistenz bevorzugen

Wo fachlich und technisch sauber möglich, werden Konflikte **vor** der Persistenz erkannt.

Beispiele:

* „Existiert Assignment bereits?“
* „Ist Zuordnung bereits aktiv vorhanden?“

Verbindliche Regel:

> Fachliche Konflikte sollen bevorzugt vor dem Persistenzzugriff bzw. vor dem finalen Speichern erkannt werden, damit API-Antworten deterministisch bleiben.

---

### 3.4 GlobalExceptionHandler bleibt zentrale technische Instanz

Der bestehende globale Error-Handling-Mechanismus bleibt erhalten.

Sprint 8 führt **keinen** kompletten Error-Handling-Neubau ein.

Stattdessen gilt:

* fachliche Exceptions / fachliche Vorab-Prüfungen werden sauber in das bestehende globale Error-Handling eingespeist
* technische Fallbacks bleiben über das bestehende Error-Handling abgesichert

Verbindliche Regel:

> Der vorhandene `GlobalExceptionHandler` bleibt die zentrale technische Fehlergrenze; Sprint 8 ergänzt ihn fachlich, ersetzt ihn aber nicht.

---

### 3.5 Listen- und Query-Fehler sind Client-Fehler

Ungültige Listen-Parameter gelten als Client-Fehler.

Beispiele:

* negative `page`
* `size <= 0`
* nicht erlaubtes `sortBy`
* ungültiges `sortDir`
* nicht unterstützter Filterparameter
* ungültiges Filterformat

Verbindliche Regel:

> Ungültige Listen- und Query-Parameter werden als `400 Bad Request` behandelt, nicht als `500`.

---

### 3.6 Not Found bleibt fachlich sauber

Fehlende referenzierte Objekte (z. B. User, Role, Scope, Permission) bleiben fachlich klar unterscheidbar.

Verbindliche Regel:

> Fehlende fachliche Zielobjekte werden als `404 Not Found` behandelt, sofern der Use Case ein konkretes Zielobjekt adressiert.

---

### 3.7 Validierungsfehler bleiben 4xx

Request-Validierungsfehler (z. B. Bean Validation, leere Pflichtfelder, fehlerhafte Formate) bleiben Client-Fehler.

Verbindliche Regel:

> Validierungsfehler werden konsistent als `400 Bad Request` ausgeliefert.

---

### 3.8 500 bleibt echte technische Fehlergrenze

`500 Internal Server Error` bleibt ausschließlich für echte technische Fehler reserviert.

Beispiele:

* unerwartete Infrastrukturfehler
* nicht abgefangene technische Defekte
* echte Systemzustandsfehler außerhalb des fachlich erwartbaren Pfads

Verbindliche Regel:

> Ein fachlich erwartbarer Standardfehler darf nicht als primäres 500-Verhalten modelliert werden.

---

## 4. Zielstatuscodes (verbindliche Leitlinie)

### 4.1 Konflikte

* Duplicate Assignment
* bereits vorhandene fachlich exklusive Zuordnung
* vergleichbare Zustandskonflikte

**→ `409 Conflict`**

---

### 4.2 Client-Fehler / Request-Fehler

* ungültige Listenparameter
* ungültige Sort-/Filterparameter
* Bean Validation Fehler
* ungültige Request-Struktur

**→ `400 Bad Request`**

---

### 4.3 Fehlende Objekte

* User nicht gefunden
* Role nicht gefunden
* Scope nicht gefunden
* Permission nicht gefunden

**→ `404 Not Found`**

---

### 4.4 Security / Auth

* nicht authentifiziert

**→ `401 Unauthorized`**

* authentifiziert, aber fehlende Berechtigung

**→ `403 Forbidden`**

Diese Regeln bleiben konsistent mit den bestehenden Security-ADRs.

---

## 5. Konsequenzen

### 5.1 Positive Konsequenzen

* UI kann Konflikte und echte Fehler sauber unterscheiden
* Duplicate-Assignments werden stabil behandelbar
* Listen-/Query-Fehler sind deterministisch auswertbar
* Bestehende Error-Infrastruktur wird sinnvoll weiterverwendet
* Kein unnötiger Großumbau des Error-Frameworks

### 5.2 Trade-offs

* Zusätzliche fachliche Vorab-Prüfungen in Use Cases nötig
* Möglicherweise zusätzliche fachliche Exception-Typen oder klare Mappings erforderlich
* Einzelne Altpfade müssen gezielt nachgehärtet werden

---

## 6. Umsetzungsvorgaben für Sprint 8

Sprint 8 setzt diesen ADR in folgender Reihenfolge um:

1. Duplicate-Handling für Assignment-Use-Cases:
  * User ↔ Role
  * User ↔ ApplicationScope
  * Role ↔ Permission

2. Listen-/Query-Parameter-Validierung für neue UI-Listen

3. Regressionstests:
  * Duplicate → 409
  * ungültige Listenparameter → 400
  * fehlende Referenzobjekte → 404
  * bestehende Security-Fehler unverändert

Verbindliche Regel:

> Fehlerbild-Härtung erfolgt nach Stabilisierung der Listen- und Relation-Read-Schnitte, damit keine doppelte Nacharbeit entsteht.

---

## 7. Abgrenzung

Nicht Bestandteil dieses ADR:

* vollständiger globaler Error-Handling-Refactor des gesamten Projekts
* neue umfassende Exception-Hierarchie als Selbstzweck
* Internationalisierung / Fehlertext-Strategie im Vollausbau
* UI-spezifische Frontend-Fehlerdarstellung
* Business-Workflow-spezifische Fehlersemantik außerhalb des IDM-Management-Scope

---

## 8. Beziehung zu bestehenden ADRs

Dieser ADR ergänzt und konkretisiert insbesondere:

* **ADR-001** – Basisstrukturen / Abhängigkeitsregeln
* **ADR-003** – Authentication & Authorization Architecture
* **ADR-004** – JWT Hardening
* **ADR-007** – Domain Permissions / Rollen-zu-Rechte-Auflösung

Wichtig:

* ADR-009 ändert nicht die Security-Architektur.
* ADR-009 definiert ausschließlich die verbindliche Fehlersemantik für die Management-API und die neuen UI-fähigen Listen.

---

## 9. Status / Verbindlichkeit

* Accepted (Sprint 8)
* Verbindliche Grundlage für UI-taugliche Fehlerszenarien im IDM
* Muss vor bzw. parallel zur Sprint-8-Implementierung berücksichtigt werden

---

## 10. Zusammenfassung

Für ein nutzbares MVP nach Sprint 8 müssen fachliche Fehler als fachliche Fehler sichtbar werden.

Die verbindliche Linie lautet:

* Duplicate-/Konfliktfälle → `409 Conflict`
* ungültige Listen-/Query-Parameter → `400 Bad Request`
* fehlende Zielobjekte → `404 Not Found`
* echte technische Fehler → `500 Internal Server Error`

Der bestehende globale Error-Handling-Rahmen bleibt erhalten und wird fachlich gezielt gehärtet, statt neu erfunden zu werden.