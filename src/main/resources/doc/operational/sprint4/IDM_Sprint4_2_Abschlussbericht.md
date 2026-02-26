# IDM – Sprint 4 Abschlussbericht

Stand: 2026-02-26
Sprint: 4
Status: Abgeschlossen

---

# 1. Executive Summary

Sprint 4 hatte das Ziel, die Autorisierungsarchitektur des IDM vollständig umzusetzen und architektonisch zu stabilisieren.

Der Fokus lag auf:

* vollständigem Rollen- und Permission-Modell
* serverseitiger Authorization-Resolution
* Bootstrap-Erweiterung auf Rollen- und Berechtigungsdaten
* Konsolidierung der ADRs
* Sicherstellung vollständiger Integrationstest-Abdeckung

Ergebnis:

Sprint 4 wurde funktional erfolgreich abgeschlossen.
Alle Tests sind grün.
Die Architektur ist konsistent, deterministisch und DDD-konform.

---

# 2. Geplante Ziele (Sprint 4 Planung)

## 2.1 Fachliche Ziele

1. Vollständiges Role → Permission Modell
2. Persistente Speicherung von:

    * PermissionGroups
    * Permissions
    * Roles
    * RolePermissionAssignments
    * UserRoleAssignments
3. Method-Level-Security mittels `@PreAuthorize`
4. Serverseitige Permission-Resolution
5. Integration des Modells in den Bootstrap-Prozess
6. Testbare, deterministische Authorization

---

# 3. Umgesetzte Ergebnisse

## 3.1 Persistentes Rollen- und Permission-Modell

Folgende Aggregate wurden stabil implementiert:

* ApplicationScope
* PermissionGroup
* Permission
* Role
* RolePermissionAssignment
* UserRoleAssignment
* UserApplicationScopeAssignment
* UserAccount

Eigenschaften:

* Stage-isoliert
* Eindeutige Constraints (Unique-Keys)
* Deterministische Zuordnungen
* Keine Hardcoded-Role-Mappings im Code

---

## 3.2 Serverseitige Authorization-Resolution

Wesentliche Architekturentscheidung:

JWT enthält keine Rollen oder Permissions.

Stattdessen:

* JWT liefert Identität
* Rollen werden datenbankbasiert geladen
* Permissions werden aus Rollen aggregiert
* GrantedAuthorities entstehen serverseitig

Vorteile:

* Keine Stale-Authorization
* Änderungen an Rollen wirken sofort
* Kein Rollen-Drift durch Token-Caching

---

## 3.3 Bootstrap-Erweiterung (ADR-006)

Bootstrap unterstützt jetzt vollständig:

* Scopes
* Admin-User
* PermissionGroups
* Permissions
* Roles
* RolePermissionAssignments
* UserRoleAssignments

Eigenschaften:

* Safe-Modus vollständig idempotent
* Force-Modus kontrolliert überschreibend
* Self-Scope strikt isoliert
* Keine Repository-Nutzung im Listener
* Nutzung von Entity-Services

Die Bootstrap-Integration ist durch Integrationstests abgesichert.

---

## 3.4 Teststabilität

Alle Tests sind grün:

* Authentication-Tests
* Path-Security-Tests
* Authorization-Tests
* Bootstrap-Tests
* Idempotenz-Tests

Besonderer Fokus in Sprint 4:

* Architekturkonforme Teststruktur
* Kein Mocking von Kern-Domain
* Echte H2-Integration
* Deterministische Assertions

---

## 3.5 ADR-Konsolidierung

Folgende ADRs wurden konsolidiert:

* ADR-003 – Authentication & Authorization
* ADR-004 – JWT Hardening
* ADR-006 – Bootstrap / Initial Data Strategy

Ergebnis:

* Keine Widersprüche zwischen JWT-Strategie und Authorization-Modell
* Keine Inkonsistenzen zwischen Bootstrap und Persistenzmodell
* Dokumentation entspricht dem Code-Stand

---

# 4. Architektureller Reifegrad

Sprint 4 bringt das IDM auf folgenden Reifegrad:

## 4.1 Authentication

* Stabil
* Stateless JWT
* Konfigurierbares Secret & TTL
* Saubere Exception-Übersetzung

Reifegrad: Hoch

---

## 4.2 Authorization

* Persistentes Rollenmodell
* Stage-isoliert
* Serverseitige Permission-Resolution
* Method-Level-Security aktiv

Reifegrad: Hoch

---

## 4.3 Bootstrap

* Deterministisch
* Idempotent
* Architekturrein
* Testabgedeckt

Reifegrad: Hoch

---

# 5. Technische Schulden

## 5.1 Performance / Caching

Aktuell:

* Rollen und Permissions werden pro Request aus der Datenbank geladen.

Risiko:

* Performance-Impact bei hoher Last

Option:

* Einführung eines Permission-Resolution-Caches (z. B. Caffeine)

Priorität: Mittel

---

## 5.2 Key Rotation (JWT)

Aktuell:

* Ein symmetrisches Secret

Fehlt:

* Rotation-Strategie
* Mehrere parallele Keys
* Optional asymmetrische Signaturen

Priorität: Mittel

---

## 5.3 Refresh Tokens

Aktuell:

* Kein Refresh-Token-Modell

Konsequenz:

* Login erforderlich nach Token-Ablauf

Priorität: Niedrig–Mittel

---

## 5.4 Fehlende Management-Endpunkte

Derzeit fehlen REST-Endpunkte für:

* User-Management (CRUD vollständig)
* Role-Management
* Permission-Management
* Scope-Management

Domänenlogik ist vorhanden, aber nicht vollständig exponiert.

Priorität: Hoch (Sprint 5 Thema)

---

## 5.5 Fehlende Audit-Strategie für Security-Events

* Keine dedizierte Audit-Tabelle für Login/Access-Denied
* Keine strukturierte Security-Event-Historie

Priorität: Mittel

---

# 6. Offene Aufgaben (Sprint 5 Kandidaten)

1. Vollständige REST-API für IDM-Administration
2. Performance-Optimierung der Permission-Resolution
3. JWT-Key-Rotation-Konzept
4. Optional: Refresh-Token-Flow
5. Optional: Record-Level-Permissions
6. Security-Audit-Trail

---

# 7. Risiken

* Wachsende Komplexität bei fehlender UI
* Performance bei hoher Benutzerzahl
* Fehlkonfiguration von Self-Scope in Multi-Stage-Umgebungen

---

# 8. Fazit

Sprint 4 schließt die grundlegende Sicherheitsarchitektur des IDM fachlich und technisch ab.

Das System besitzt nun:

* saubere Trennung von Authentication und Authorization
* persistentes Rollen- und Permission-Modell
* deterministischen Bootstrap
* testabgedeckte Sicherheitsarchitektur
* konsistente ADR-Dokumentation

Das IDM ist architektonisch stabil und bereit für die funktionale Erweiterung in Sprint 5.
