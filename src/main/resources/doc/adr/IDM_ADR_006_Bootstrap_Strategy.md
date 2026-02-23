# IDM – ADR 006: Bootstrap / Initial Data Strategy

Stand: 2026-02-23
Status: Accepted (Sprint 4 – Grundlage für Initialisierung)

---

## 1. Kontext

Das Identity Management (IDM) benötigt beim Start definierte Basisdaten, um funktionsfähig zu sein:

* ApplicationScopes (z. B. IDM / DEV, TEST, PROD)
* PermissionGroups
* Permissions
* Rollen
* Role–Permission-Zuordnungen
* Ein initialer Admin-User

Da das IDM selbst Rollen und Berechtigungen für andere Fachanwendungen verwaltet, muss die Initialisierung:

* stage-spezifisch funktionieren
* idempotent sein
* architekturkonform (DDD) implementiert werden
* sicherheitstechnisch kontrolliert ablaufen

Die Initialisierung darf keine bestehenden fachlichen Daten überschreiben oder löschen.

---

## 2. Ziel

Sicherstellen, dass beim Start der Anwendung:

1. Alle IDM-eigenen Basisdaten existieren.
2. Ein initialer Admin-Account vorhanden ist.
3. Die Initialisierung deterministisch und wiederholbar ist.
4. Keine Daten überschrieben oder gelöscht werden.

---

## 3. Architekturentscheidung

### 3.1 Datenquelle

Die Bootstrap-Daten werden deklarativ in einer XML-Datei definiert.

Pfad:

```
src/main/resources/idm/bootstrap/idm-bootstrap.xml
```

Die Datei enthält:

* ApplicationScopes
* PermissionGroups
* Permissions
* Rollen
* Role–Permission-Zuordnungen

Die Struktur ist vollständig deklarativ und versionierbar.

---

### 3.2 Ausführung

Die Initialisierung erfolgt über einen Startup-Listener:

* `ApplicationReadyEvent`
* zentrale Orchestrierungskomponente (z. B. `IdmBootstrapStartupComponent`)

Der Startup-Handler:

* liest die XML-Datei
* ruft ausschließlich Domain-Services auf
* greift niemals direkt auf Repositories zu

Die Persistenz erfolgt ausschließlich über:

* Management-Domain-Services
* Handler
* EntityServices

Damit bleibt die DDD-Architektur gewahrt.

---

### 3.3 Idempotenz-Modus

Bootstrap arbeitet standardmäßig im Modus:

```
safe
```

Bedeutung:

* Fehlende Daten werden ergänzt.
* Bestehende Daten werden nicht überschrieben.
* Keine Löschoperationen.
* Kein Zurücksetzen von Passwörtern.

Ein zukünftiger Modus `force` ist optional vorgesehen, jedoch nicht Bestandteil des MVP.

---

## 4. Konfiguration

Bootstrap wird über Konfiguration gesteuert:

```yaml
idm:
  bootstrap:
    enabled: false
    mode: safe
    admin:
      username: admin
      password: admin
```

### Profil-Strategie

| Profil | enabled |
| ------ | ------- |
| test   | true    |
| dev    | true    |
| prod   | false   |

In `prod` ist Bootstrap standardmäßig deaktiviert.

---

## 5. Admin-Strategie

Beim Bootstrap wird ein Admin-User erzeugt, wenn er nicht existiert.

Regeln:

* Kein Überschreiben bestehender User.
* Passwort wird einmalig gesetzt.
* Kein Passwort-Reset bei erneutem Start.
* Admin erhält IDM_ADMIN Rolle.
* Admin erhält Scope-Zuordnung.

Sicherheitsprinzip:

* In Produktionsumgebungen muss das Default-Passwort geändert werden.
* Bootstrap überschreibt kein bestehendes Passwort.

---

## 6. Stage-Semantik

Alle Bootstrap-Daten sind ApplicationScope-gebunden.

Das XML definiert Daten pro:

* applicationKey
* stageKey

Damit können:

* DEV
* TEST
* PROD

unterschiedliche Permission-Definitionen besitzen.

Das Modell bleibt vollständig stage-isoliert.

---

## 7. systemProtected-Policy

Alle IDM-eigenen:

* Permissions
* Rollen

werden mit `systemProtected = true` angelegt.

Konsequenzen:

* Nicht löschbar.
* Nicht manipulierbar über Standard-Delete-Handler.
* Schutz vor versehentlicher Entfernung.

---

## 8. Nicht Bestandteil dieses ADR

* Liquibase-Migrationen
* Mandantenfähigkeit
* Force-Reapply-Modus
* REST-basierter Bootstrap-Trigger

---

## 9. Konsequenzen

### Vorteile

* Vollständig deklarativ
* Versionierbar
* Stage-fähig
* Idempotent
* Architekturkonform
* Sicherheitstechnisch kontrolliert

### Trade-offs

* XML-Mapping erforderlich
* Zusätzlicher Startup-Code
* Pflege der Bootstrap-Datei notwendig

---

## 10. Zusammenfassung

Das IDM verwendet eine deklarative, idempotente Bootstrap-Strategie auf Basis einer XML-Datei.

Die Initialisierung erfolgt ausschließlich über Domain-Services und wahrt strikt die DDD-Architektur.

Bootstrap ergänzt fehlende Basisdaten, überschreibt jedoch niemals bestehende fachliche Daten.

Damit ist das IDM nach jedem Start deterministisch funktionsfähig, ohne produktive Daten zu gefährden.
