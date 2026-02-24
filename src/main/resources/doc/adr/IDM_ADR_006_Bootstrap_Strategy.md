# IDM – ADR 006: Bootstrap / Initial Data Strategy

Stand: 2026-02-24
Status: Accepted (Sprint 4 – verbindliche Architektur)

---

## 1. Kontext

Das Identity Management (IDM) benötigt beim Start definierte Basisdaten, um funktionsfähig zu sein:

* ApplicationScopes (z. B. IDM / DEV, TEST, PROD)
* Ein initialer Admin-User
* Scope-Zuordnungen für den Admin

In späteren Ausbaustufen zusätzlich:

* PermissionGroups
* Permissions
* Rollen
* Role–Permission-Zuordnungen

Da das IDM selbst Rollen und Berechtigungen für andere Fachanwendungen verwaltet, muss die Initialisierung:

* stage-spezifisch funktionieren
* idempotent sein
* deterministisch sein
* architekturkonform (DDD) implementiert werden
* ohne Umgehung der Domain-Invarianten erfolgen

Die Initialisierung darf keine bestehenden fachlichen Daten unbeabsichtigt überschreiben oder löschen.

---

## 2. Ziel

Sicherstellen, dass beim Start der Anwendung:

1. Der für diese Instanz konfigurierte Self-Scope existiert.
2. Ein initialer Admin-Account vorhanden ist.
3. Der Admin dem Self-Scope zugeordnet ist.
4. Die Initialisierung deterministisch und wiederholbar ist.
5. Safe- und Force-Modus klar definiert sind.

---

## 3. Architekturentscheidung

### 3.1 Trennung von Definition und Selektion

Bootstrap folgt einem klaren Trennungsprinzip:

**Definition (Source of Truth):**

* XML-Dateien im Classpath
* Versionierbar
* Enthalten alle deklarativen Scope-Definitionen

**Selektion (Deployment-spezifisch):**

* application.yml / Profil-Dateien
* Definieren, welcher Scope für diese Instanz gilt
* Keine implizite Ableitung aus Spring-Profilen

Damit ist die Umgebung explizit konfiguriert und nicht implizit aus Profilnamen abgeleitet.

---

### 3.2 XML-Struktur

Bootstrap verwendet getrennte XML-Dateien:

Pfad (konfigurierbar):

```
src/main/resources/idm/bootstrap/
```

Dateien:

* `admin-user.xml`
* `scopes.xml`

#### scopes.xml

Definiert alle ApplicationScopes deklarativ:

* applicationKey
* stageKey
* description

Beispiel:

* (IDM, DEV)
* (IDM, TEST)
* (IDM, PROD)

#### admin-user.xml

Definiert:

* username
* password (Klartext; wird beim Persistieren gehasht)

---

### 3.3 Self-Scope-Konfiguration

Die laufende Instanz erhält über Konfiguration die Information, welcher Scope ihr eigener ist:

```yaml
idm:
  self:
    application-key: IDM
    stage-key: DEV
```

Der Bootstrap:

1. Lädt alle Scopes aus `scopes.xml`
2. Sucht exakt den konfigurierten Self-Scope
3. Wirft eine Exception, wenn dieser nicht existiert
4. Persistiert/aktualisiert ausschließlich diesen Scope

Es erfolgt keine automatische Ableitung aus `spring.profiles.active`.

---

### 3.4 Ausführung

Die Initialisierung erfolgt über einen `ApplicationReadyEvent`-Listener.

Eigenschaften:

* Orchestrierungskomponente im Domain-Startup-Package
* Keine direkte Repository-Nutzung
* Nutzung von Domain-Services und Entity-Services
* Keine HTTP- oder Security-Abhängigkeit

Bootstrap ist ein interner System-Use-Case und benötigt keinen SYSTEM-Account.

---

## 4. Idempotenz und Modi

Bootstrap kennt zwei Modi:

### 4.1 safe (Default)

* Fehlende Scopes werden angelegt.
* Fehlender Admin wird angelegt.
* Fehlende Scope-Zuordnung wird angelegt.
* Bestehende Daten werden nicht verändert.
* Keine Passwort-Resets.
* Keine State-Änderungen.

Safe-Modus ist vollständig idempotent.

---

### 4.2 force

* Scope-Description wird aus XML aktualisiert.
* Admin-Passwort wird neu gesetzt (gehasht).
* Admin wird aktiviert.
* Scope-Zuordnung wird sichergestellt.

Force überschreibt nur explizit definierte Felder.
Es erfolgen keine Löschoperationen.

---

## 5. Konfiguration

Bootstrap wird über folgende Properties gesteuert:

```yaml
idm:
  bootstrap:
    enabled: false
    mode: safe
    base-path: idm/bootstrap
    admin-xml: admin-user.xml
    scopes-xml: scopes.xml
    admin:
      username: admin
      password: admin
```

### Verhalten

* `enabled=false` → Bootstrap läuft nicht.
* `enabled=true` → Bootstrap wird beim Start ausgeführt.
* `mode` steuert Safe/Force.
* XML-Dateien sind konfigurierbar.

---

## 6. Admin-Strategie

Beim Bootstrap wird:

1. Der Admin erzeugt, falls nicht vorhanden.
2. Das Passwort mittels PasswordEncoder gehasht.
3. Der Admin aktiviert.
4. Die Zuordnung zum Self-Scope sichergestellt.

Im Safe-Modus erfolgt kein Passwort-Reset.
Im Force-Modus wird das Passwort neu gesetzt.

---

## 7. Stage-Semantik

Alle Bootstrap-Daten sind ApplicationScope-gebunden.

Scopes sind eindeutig über:

* applicationKey
* stageKey

identifiziert.

Damit können DEV, TEST und PROD unterschiedliche Konfigurationen besitzen.

Das Modell bleibt vollständig stage-isoliert.

---

## 8. Teststrategie

Die Bootstrap-Architektur ist durch Integrationstests abgesichert:

* Disabled-Modus → keine Daten
* Safe-Modus → Erstellung fehlender Daten
* Idempotenz
* Force-Modus → gezielte Aktualisierung
* Fehlerszenarien (fehlende XML, fehlender Self-Scope)

Tests verwenden separate Test-XML-Dateien und überschreiben Properties per `@TestPropertySource`.

---

## 9. Nicht Bestandteil dieses ADR

* Rollen- und Permission-Bootstrap (separat dokumentiert)
* Liquibase-Migrationen
* Mandantenfähigkeit
* REST-basierter Bootstrap-Trigger

---

## 10. Zusammenfassung

Das IDM verwendet eine deklarative, idempotente Bootstrap-Strategie auf Basis separater XML-Dateien.

Definition (XML) und Deployment-Selektion (application.yml) sind strikt getrennt.

Bootstrap arbeitet deterministisch, stage-spezifisch und überschreibt bestehende Daten ausschließlich im definierten Force-Modus.

Damit ist das IDM nach jedem Start konsistent initialisiert, ohne produktive Daten unbeabsichtigt zu verändern.
