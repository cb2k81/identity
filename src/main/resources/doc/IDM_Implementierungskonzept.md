# Technisches Implementierungskonzept – IDM Service

## 1. Einordnung und Zielsetzung

Dieses technische Implementierungskonzept definiert die verbindlichen Architektur‑, Security‑ und Implementierungsregeln für den **Identity Management (IDM) Service**. Es ergänzt das vorhandene Fachkonzept und bildet gemeinsam mit diesem die maßgebliche Grundlage für den Projektstart sowie für eine konsistente, deterministische Umsetzung.

Der IDM Service wird als **Spring‑Boot‑basiertes Backend‑System** umgesetzt und stellt die im Fachkonzept definierten **REST‑Schnittstellen** bereit. Ein Web‑Client ist **nicht Bestandteil** dieses Projekts und wird als eigenständiges, entkoppeltes Produkt betrachtet.

Der Projekt‑Scope umfasst:

* Implementierung der fachlichen Aggregate und Services
* Bereitstellung versionierter REST‑APIs
* Security (Authentifizierung, Autorisierung, JWT)
* Persistenz, Auditierung und Fehlermanagement

Nicht Bestandteil:

* Frontend / Web‑Client
* Externe IAM‑Integrationen (z. B. LDAP, OAuth Provider), sofern nicht explizit beauftragt

---

## 2. Laufzeit- und Betriebsmodell

Das Laufzeit- und Betriebsmodell des IDM Service ist auf einen stabilen, skalierbaren und möglichst einfachen Betrieb ausgerichtet. Die Anwendung wird als **eigenständig lauffähiges Spring-Boot-JAR** ausgeliefert und nutzt einen embedded Webserver. Dadurch ist kein separater Applikationsserver erforderlich, was den Betrieb vereinfacht und die Portabilität zwischen unterschiedlichen Umgebungen erhöht.

Der IDM Service folgt konsequent einem **stateless Architekturansatz**. Es werden keine serverseitigen HTTP-Sessions verwendet. Alle für die Verarbeitung eines Requests notwendigen Informationen werden entweder im Request selbst oder über valide Security-Tokens bereitgestellt. Dieser Ansatz ist eine wesentliche Voraussetzung für horizontale Skalierung und einen robusten Betrieb in containerisierten Umgebungen.

Die Anwendung ist von Beginn an **containerfähig** konzipiert und kann in modernen Plattformen wie Kubernetes betrieben werden. Aspekte wie schnelle Startzeiten, klare Health-Endpunkte und externe Konfiguration sind integraler Bestandteil des Betriebsmodells und keine nachträglichen Ergänzungen.

### 2.1 Profile und Konfiguration

Für unterschiedliche Einsatzszenarien werden klar abgegrenzte Spring-Profile verwendet. Diese ermöglichen eine saubere Trennung von Entwicklungs‑, Test‑ und Produktionskonfigurationen, ohne dass Code angepasst werden muss.

Es existieren die Profile:

* `dev` für lokale Entwicklung
* `test` für automatisierte Tests und Integrationsumgebungen
* `prod` für den Produktivbetrieb

Die Konfigurationskonventionen sind verbindlich festgelegt:

* Die Datei `application.yml` repräsentiert das produktive Default-Profil und wird auch im Pod- bzw. Containerbetrieb verwendet.
* Environmentspezifische Abweichungen werden über `application-dev.yml` und `application-test.yml` definiert.
* Sensible Konfigurationswerte wie Zugangsdaten, Schlüssel oder Secrets werden **ausschließlich** über Environment-Variablen oder dedizierte Secret-Mechanismen bereitgestellt und niemals im Repository abgelegt.

---

## 3. Technische Basis

Die technische Basis des IDM Service ist bewusst konservativ, stabil und langfristig wartbar gewählt. Ziel ist es, auf etablierte Technologien zu setzen, die gut unterstützt sind und eine verlässliche Grundlage für den Betrieb eines sicherheitskritischen Kernsystems bieten.

### 3.1 Plattform und Frameworks

Der IDM Service wird auf **Java 22** betrieben und nutzt **Spring Boot 3.3.10** als zentrales Anwendungsframework. Spring Boot stellt die notwendigen Bausteine für Web‑APIs, Security, Persistenz, Konfiguration und Observability in integrierter Form bereit und ermöglicht eine konsistente Projektstruktur.

Der Build erfolgt mit **Maven 3**, wodurch ein reproduzierbarer und standardisierter Build‑Prozess gewährleistet ist. Für Persistenz und ORM wird **Spring Data JPA mit Hibernate** eingesetzt, ergänzt durch **MariaDB** als relationale Datenbank. Schema‑Versionierung und Migrationen werden über **Liquibase** realisiert, um kontrollierte und nachvollziehbare Datenbankänderungen sicherzustellen.

Querschnittliche technische Anforderungen werden durch etablierte Bibliotheken abgedeckt, darunter **SLF4J/Logback** für Logging, **Lombok** zur Reduktion von Boilerplate‑Code, **JUnit und Spring Test** für automatisierte Tests sowie **Swagger/OpenAPI** zur Dokumentation der REST‑Schnittstellen.

### 3.2 Ergänzende Libraries und Versionsstrategie

Ergänzend zur Kernplattform können weitere Libraries und Tools eingesetzt werden, sofern sie die funktionalen Anforderungen des IDM Service sinnvoll unterstützen, mit Spring Boot 3.x kompatibel sind und keine unnötigen Abhängigkeiten oder Vendor-Lock-ins einführen.

Für die **Datenvalidierung** wird Bean Validation (Jakarta Validation) verwendet, das bereits integraler Bestandteil des Spring-Ökosystems ist und eine einheitliche Validierung von Eingabedaten ermöglicht. Dadurch werden formale Fehler frühzeitig erkannt und konsistent behandelt.

Für das **Mapping zwischen DTOs und Domain-Entities** kann MapStruct eingesetzt werden. MapStruct ermöglicht typsicheres, performantes Mapping zur Compile-Zeit und vermeidet reflektionsbasierte Lösungen, was insbesondere in sicherheitskritischen Kernsystemen von Vorteil ist.

Zur **Erzeugung und Validierung von JSON Web Tokens** kann eine etablierte JWT-Bibliothek wie JJWT verwendet werden. Die Auswahl erfolgt unter dem Aspekt langfristiger Wartbarkeit, aktiver Pflege und Kompatibilität mit aktuellen Java- und Spring-Versionen.

Für **JSON-Verarbeitung und Serialisierung** wird Jackson eingesetzt. Jackson ist Bestandteil des Spring-Boot-Stacks und erlaubt eine flexible Anpassung der Serialisierung, beispielsweise für Versionierung, Custom Deserializer oder Security-relevante Maskierungen.

Zur **Resilienz und Fehlerrobustheit** kann perspektivisch der Einsatz von Resilience4j vorgesehen werden, etwa für Timeouts, Circuit Breaker oder Bulkheads bei internen oder externen Abhängigkeiten. Der Einsatz ist optional und erfolgt nur bei konkretem Bedarf.

Für **Observability und Metriken** wird primär auf die durch Spring Boot Actuator bereitgestellten Mechanismen gesetzt. Eine spätere Erweiterung um Micrometer als Metrik-Fassade ist vorgesehen, da Micrometer integraler Bestandteil des Spring-Ökosystems ist und eine nahtlose Anbindung an Monitoring-Backends ermöglicht.

Alle Abhängigkeiten werden zentral in der `pom.xml` verwaltet. Das Versionsmanagement orientiert sich primär am **Spring-Boot-BOM**, um kompatible Abhängigkeitsversionen sicherzustellen. Darüber hinaus werden möglichst aktuelle, stabile Versionen verwendet.

Spring Boot 3.x wird bewusst beibehalten, da ein erheblicher Teil des eingesetzten Ökosystems zum aktuellen Zeitpunkt noch nicht vollständig mit Spring Boot 4 kompatibel ist. Diese Entscheidung stellt Stabilität und Planbarkeit gegenüber einem frühzeitigen Technologiewechsel in den Vordergrund.

---

## 4. Architekturprinzipien

Die Architektur des IDM Service ist bewusst so gestaltet, dass sie Fachlichkeit, technische Stabilität und Erweiterbarkeit miteinander verbindet. Ziel ist eine Struktur, die fachliche Änderungen isoliert erlaubt, technische Querschnittsthemen klar verortet und gleichzeitig eine deterministische Implementierung ohne implizite Abhängigkeiten ermöglicht.

Zentrales Leitprinzip ist die **Trennung von Verantwortlichkeiten entlang fachlicher und technischer Grenzen**. Jede Schicht besitzt eine klar definierte Aufgabe und darf ausschließlich mit den direkt benachbarten Schichten interagieren. Dadurch wird verhindert, dass sich fachliche Logik, technische Infrastruktur und Präsentationsaspekte unkontrolliert vermischen.

### 4.1 Schichtenarchitektur und Zusammenspiel

Der **Web- bzw. Controller-Layer** bildet den äußeren Einstiegspunkt in die Anwendung. Er stellt ausschließlich die technischen REST-Endpunkte bereit und ist verantwortlich für Request- und Response-Verarbeitung, Parameterbindung sowie formale Validierung der Eingaben. Fachliche Entscheidungen werden hier bewusst nicht getroffen. Diese strikte Beschränkung stellt sicher, dass Controller einfach testbar bleiben und sich Änderungen an der Fachlogik nicht auf die API-Schicht auswirken.

Der **API-Layer (Application Facades)** bildet die zentrale Integrations- und Orchestrierungsschicht der Anwendung. In dieser Schicht werden fachliche Use-Cases umgesetzt, indem mehrere Domain-Services koordiniert werden. Gleichzeitig erfolgt hier das Mapping zwischen externen DTOs und internen Domain-Entities. Ein wesentlicher Aspekt dieser Schicht ist die Durchsetzung der Endbenutzer-Autorisierung sowie die Definition der Transaktionsgrenzen. Dadurch wird sichergestellt, dass fachliche Operationen atomar, konsistent und sicher ausgeführt werden. Der API-Layer fungiert damit als stabiler Schutzschirm zwischen externer API und internem Fachmodell.

Der **Domain-Layer** stellt den fachlichen Kern der Anwendung dar. Er ist konsequent nach den Prinzipien des Domain Driven Design aufgebaut. Aggregate Roots kapseln den konsistenten Zustand eines fachlichen Kontexts und stellen die einzigen erlaubten Zugriffspunkte für Änderungen dar. Domain Services enthalten fachliche Logik, die nicht sinnvoll einem einzelnen Aggregate zugeordnet werden kann. Diese Schicht arbeitet ausschließlich mit Entities und kennt weder DTOs noch technische Aspekte wie HTTP, Security oder Persistenzdetails. Durch diese Isolation bleibt das Fachmodell langlebig und unabhängig von technologischen Änderungen.

Der **Persistence-Layer** ist für die Speicherung und das Laden von Aggregaten verantwortlich. Er basiert auf JPA/Hibernate und stellt Repositories ausschließlich für Aggregate Roots bereit. Die Persistenzschicht enthält keinerlei Fachlogik, sondern dient ausschließlich der technischen Umsetzung der Datenhaltung. Diese klare Trennung verhindert, dass fachliche Regeln unbemerkt in Datenzugriffsschichten abwandern.

Der **Security-Layer** ist als querschnittliche technische Schicht ausgeprägt. Er stellt Mechanismen für Authentifizierung, Autorisierung und Method Security bereit und wird sowohl vom Web- als auch vom API-Layer genutzt. Fachliche Entscheidungen über Berechtigungen werden nicht im Domain-Layer getroffen, sondern explizit an den Schnittstellen zur Außenwelt durchgesetzt. Dadurch bleibt das Fachmodell frei von sicherheitstechnischen Abhängigkeiten.

### 4.2 Architektonische Leitentscheidungen

Die Kombination aus Schichtenarchitektur und DDD-orientiertem Domain-Modell verfolgt mehrere Ziele gleichzeitig:

* **Determinismus:** Jeder fachliche Use-Case folgt einem klaren Ausführungspfad vom Controller über den API-Layer in den Domain-Layer und zurück.
* **Testbarkeit:** Fachliche Logik kann isoliert getestet werden, ohne Spring-Kontext oder Infrastrukturabhängigkeiten.
* **Wartbarkeit:** Änderungen an API, Security oder Persistenz haben keine unmittelbaren Auswirkungen auf das Fachmodell.
* **Erweiterbarkeit:** Neue Use-Cases oder technische Querschnittsthemen (z. B. Observability, zusätzliche Security-Mechanismen) können ergänzt werden, ohne bestehende Strukturen aufzubrechen.

Diese Architektur stellt sicher, dass der IDM Service langfristig stabil bleibt und gleichzeitig flexibel genug ist, um zukünftige Anforderungen oder Integrationen aufzunehmen.

---

## 5. Domain Driven Design (DDD)

Der IDM Service folgt bewusst den Prinzipien des **Domain Driven Design**, um die fachliche Komplexität des Identitäts‑ und Berechtigungsmanagements beherrschbar und langfristig wartbar zu halten. Ziel ist es, das Fachmodell klar von technischen Aspekten zu trennen und fachliche Regeln explizit und nachvollziehbar im Code abzubilden.

Im Mittelpunkt steht dabei nicht die technische Struktur der Anwendung, sondern die fachliche Konsistenz der Domäne. Das Domain‑Modell bildet die Begriffe, Regeln und Invarianten des Fachkonzepts direkt ab und dient als verbindliche Grundlage für alle fachlichen Operationen.

### 5.1 Aggregate und fachliche Konsistenz

Jeder fachliche Teilbereich wird durch ein **Aggregate** repräsentiert, das einen klar abgegrenzten Konsistenzrahmen bildet. Das **Aggregate Root** fungiert dabei als einziger erlaubter Zugriffspunkt für Änderungen am internen Zustand. Diese Entscheidung stellt sicher, dass fachliche Invarianten jederzeit eingehalten werden und Änderungen nicht unkontrolliert über mehrere Objekte hinweg erfolgen.

Repositories existieren ausschließlich für Aggregate Roots. Zugehörige Entitäten (Parts) sind dem jeweiligen Aggregate eindeutig untergeordnet und dürfen weder direkt geladen noch außerhalb des Roots verändert werden. Dadurch wird verhindert, dass fachliche Regeln umgangen oder inkonsistente Zustände erzeugt werden.

Typische Aggregate Roots im IDM‑Kontext sind beispielsweise UserAccount, Person, Role oder Scope. Sie spiegeln jeweils einen eigenständigen fachlichen Verantwortungsbereich wider und sind so zugeschnitten, dass sie unabhängig voneinander weiterentwickelt werden können.

### 5.2 Domain Services und fachliche Logik

Nicht jede fachliche Operation lässt sich sinnvoll einem einzelnen Aggregate zuordnen. Für solche Fälle werden **Domain Services** eingesetzt. Diese kapseln fachliche Logik, die mehrere Aggregate betrifft oder keinen natürlichen Besitzer im Datenmodell hat.

Domain Services arbeiten ausschließlich mit Entities und Value Objects und sind vollständig frei von technischen Abhängigkeiten. Sie kennen weder DTOs noch Persistenz‑ oder Security‑Mechanismen. Dadurch bleiben sie leicht testbar und fachlich eindeutig.

Die Aufrufe der Domain Services erfolgen stets über den API Layer. Dieser stellt sicher, dass fachliche Operationen in einem klar definierten Kontext ausgeführt werden und nicht unkontrolliert aus technischen Schichten heraus angestoßen werden.

### 5.3 Transaktionsmodell im DDD‑Kontext

Das Transaktionsmanagement ist bewusst **nicht** Bestandteil des Domain‑Layers. Domain Services sind transaktionslos und formulieren ausschließlich fachliche Regeln und Zustandsänderungen.

Die Verantwortung für Transaktionen liegt im **API Layer (Application Facades)**. Dort wird pro fachlichem Use‑Case genau eine Transaktion gestartet, innerhalb derer alle beteiligten Domain Services und Aggregate konsistent ausgeführt werden. Dieses Modell stellt sicher, dass fachliche Operationen atomar sind und entweder vollständig oder gar nicht wirksam werden.

Rollback‑Regeln orientieren sich an Runtime Exceptions. Fachliche Fehler werden explizit über Domain‑spezifische Exceptions signalisiert und vom API Layer in konsistente HTTP‑Antworten übersetzt.

---

## 6. Datenmodell und Persistenz

Das Datenmodell des IDM Service ist eng an das fachliche Domain‑Modell gekoppelt, bleibt jedoch bewusst auf seine technische Aufgabe beschränkt: die zuverlässige und konsistente Persistenz des fachlichen Zustands. Fachliche Logik oder Validierungsregeln sind nicht Bestandteil der Persistenzschicht.

### 6.1 Identitäten und Lebenszyklus von Entities

Alle fachlichen Entities erhalten **frühzeitig im Erstellungsprozess eine UUID** als technische Identität. Die UUID wird applikationsseitig erzeugt und bleibt über den gesamten Lebenszyklus der Entity stabil. Diese Entscheidung stellt sicher, dass Objekte eindeutig identifizierbar sind, auch bevor sie persistiert wurden, und erleichtert die Korrelation von Audit‑Logs, Events und API‑Antworten.

Die Persistenz der UUID erfolgt einheitlich entweder als nativer UUID‑Typ oder als String. Die konkrete technische Ausprägung wird einmalig festgelegt und konsequent im gesamten Datenmodell angewendet.

### 6.2 Modellierungsregeln und Vererbung

Bei der Modellierung der JPA Entities wird bewusst auf Vererbung verzichtet. Erfahrungsgemäß führt Entity‑Vererbung häufig zu komplexen Mapping‑Strukturen, eingeschränkter Erweiterbarkeit und schwer nachvollziehbaren Performance‑Effekten.

Stattdessen werden fachliche Beziehungen explizit über Relationen modelliert. Dies erhöht die Transparenz des Datenmodells, erleichtert spätere Anpassungen und sorgt für eine klare Abbildung der fachlichen Zusammenhänge.

### 6.3 Persistenzzugriff und Repositories

Repositories stellen den technischen Zugriff auf die Datenbank dar und sind ausschließlich für Aggregate Roots definiert. Sie dienen dem Laden, Speichern und Löschen vollständiger Aggregate. Feingranulare fachliche Abfragen oder Zustandsänderungen einzelner Parts außerhalb des Aggregate‑Kontexts sind nicht vorgesehen.

Diese Beschränkung stellt sicher, dass alle Änderungen am Datenmodell über den fachlich definierten Weg erfolgen und das Domain‑Modell die alleinige Quelle der Wahrheit für fachliche Regeln bleibt.

### 6.4 Schema‑Migrationen mit Liquibase

Datenbankschemata werden ausschließlich über **Liquibase** versioniert und migriert. Jede fachliche Änderung am Datenmodell wird durch einen eigenen, nachvollziehbaren Changelog repräsentiert. Migrationen laufen konsistent in allen Profilen (dev, test, prod), sodass Abweichungen zwischen Umgebungen vermieden werden.

Initiale Seed‑Daten, wie Scopes, Basis‑Rollen und Permissions, werden ebenfalls über Liquibase eingespielt. Dadurch ist sichergestellt, dass jede Umgebung einen definierten, reproduzierbaren Ausgangszustand besitzt.

---

## 7. Security-Konzept

Das Security-Konzept des IDM Service ist darauf ausgelegt, fachliche Sicherheit, technische Robustheit und Erweiterbarkeit miteinander zu verbinden. Security wird dabei als **zentrales Querschnittsthema** verstanden, das konsequent an den Schnittstellen zur Außenwelt durchgesetzt wird, ohne das Fachmodell selbst mit sicherheitstechnischen Details zu belasten.

Ziel ist es, eine klare Trennung zwischen **Authentifizierung**, **Autorisierung** und fachlicher Logik zu erreichen. Dadurch bleibt das Domain-Modell unabhängig, testbar und langfristig stabil, während sicherheitsrelevante Entscheidungen explizit und nachvollziehbar an definierten Stellen getroffen werden.

### 7.1 Authentifizierung

Die Authentifizierung erfolgt vollständig **JWT-basiert** und folgt einem strikt stateless Ansatz. Nach erfolgreichem Login wird ein signiertes JSON Web Token ausgestellt, das den Benutzer eindeutig identifiziert und für nachfolgende Requests verwendet wird. Serverseitige HTTP-Sessions werden nicht eingesetzt, was die horizontale Skalierbarkeit des Systems erleichtert und den Betrieb in containerisierten Umgebungen vereinfacht.

Passwörter werden niemals im Klartext gespeichert oder verarbeitet. Stattdessen kommt ein `DelegatingPasswordEncoder` zum Einsatz, der es erlaubt, den verwendeten Hash-Algorithmus (z. B. BCrypt oder Argon2) zukünftig zu wechseln, ohne bestehende Passwort-Hashes ungültig zu machen. Diese Entscheidung stellt sicher, dass das System auch langfristig auf neue sicherheitstechnische Anforderungen reagieren kann.

### 7.2 Token-Struktur und JWT-Governance

Die ausgestellten JWTs enthalten sowohl **stabile Claims** als auch **abgeleitete Claims**. Stabile Claims dienen der eindeutigen Identifikation des Benutzers und ändern sich über die Lebensdauer eines Tokens nicht. Dazu gehören insbesondere `sub`, `userId`, `username`, `iat`, `exp` und `iss`.

Abgeleitete Claims, wie `roles` oder `permissions`, werden aus dem aktuellen Berechtigungsmodell berechnet und dienen ausschließlich der effizienten Autorisierungsentscheidung während der Request-Verarbeitung. Sie gelten ausdrücklich **nicht** als fachliche Quelle der Wahrheit. Änderungen an Rollen oder Berechtigungen werden serverseitig überprüft und nicht allein auf Basis des Tokens akzeptiert.

Für die Signierung der Tokens wird bevorzugt ein asymmetrisches Verfahren (RS256) eingesetzt. Dadurch kann der öffentliche Schlüssel für die Token-Validierung verteilt werden, ohne den privaten Signierschlüssel preiszugeben.

### 7.3 Autorisierung und Berechtigungsmodell

Die Autorisierung im IDM Service basiert auf einem klar getrennten Rollen- und Berechtigungsmodell. Rollen dienen ausschließlich der fachlichen Bündelung von Berechtigungen und besitzen selbst keine technische Bedeutung für Autorisierungsentscheidungen.

Die eigentliche Autorisierung erfolgt auf Basis von **Permissions**, die als Authorities in Spring Security eingebunden werden. Um Konsistenz und Lesbarkeit sicherzustellen, wird eine verbindliche Namenskonvention verwendet:

```
PERM_<DOMAIN>_<ACTION>
```

Beispiele sind `PERM_USER_READ`, `PERM_USER_MANAGE` oder `PERM_ROLE_ASSIGN`. Diese Konvention ermöglicht eine eindeutige Zuordnung zwischen fachlicher Operation und technischer Zugriffskontrolle.

Autorisierungsentscheidungen werden im **API Layer** oder über Method Security (`@PreAuthorize`) durchgesetzt. Dadurch bleibt der Domain-Layer vollständig frei von sicherheitsrelevanten Abhängigkeiten, während gleichzeitig sichergestellt ist, dass fachliche Use-Cases nur von berechtigten Benutzern ausgeführt werden können.

### 7.4 Deaktivierung, Sperrung und Token-Revocation

Der Status eines Benutzers (z. B. aktiv, deaktiviert, gesperrt) wird bei jedem Request serverseitig geprüft. Ein gültiges JWT allein berechtigt nicht zur Ausführung fachlicher Operationen, wenn der zugehörige Benutzer inzwischen deaktiviert oder gesperrt wurde.

Optional kann das Sicherheitsmodell um Mechanismen wie eine Token-Version oder einen sogenannten Security-Stamp erweitert werden. Dadurch lassen sich Tokens gezielt invalidieren, etwa bei sicherheitsrelevanten Änderungen wie Passwortwechseln oder Rollenentzug.

---

## 8. API-Design

Das API-Design des IDM Service verfolgt das Ziel, eine **konsistente, verständliche und langfristig stabile REST-Schnittstelle** bereitzustellen. Die API stellt die fachlichen Fähigkeiten des Systems nach außen bereit, ohne interne Implementierungsdetails preiszugeben. Gleichzeitig dient sie als klar definierter Vertrag zwischen Backend und konsumierenden Systemen.

### 8.1 Versionierung und Stabilität

Die API wird explizit versioniert und über die URL bereitgestellt, beispielsweise unter `/api/v1/...`. Diese Form der Versionierung macht Breaking Changes transparent und erlaubt eine kontrollierte Weiterentwicklung der Schnittstellen.

Innerhalb einer Hauptversion wird **Backward Compatibility garantiert**. Änderungen an bestehenden Endpunkten erfolgen ausschließlich in kompatibler Weise. Inkompatible Änderungen führen zu einer neuen Hauptversion der API.

### 8.2 Ressourcenorientierung und HTTP-Semantik

Die Endpunkte sind ressourcenorientiert aufgebaut und folgen den etablierten HTTP-Semantiken. Jede Ressource besitzt eine eindeutige URL, und die Bedeutung der HTTP-Methoden ist klar definiert.

* `GET` wird für lesenden Zugriff verwendet und ist idempotent.
* `POST` dient der Erstellung neuer Ressourcen.
* `PUT` ersetzt eine Ressource vollständig und setzt alle relevanten Felder.
* `PATCH` wird für partielle Änderungen eingesetzt, bei denen nur einzelne Attribute aktualisiert werden.
* `DELETE` entfernt eine Ressource oder markiert sie fachlich als gelöscht.

Diese klare Trennung erleichtert sowohl die Nutzung der API als auch ihre Wartung und Erweiterung.

### 8.3 Listen-Endpunkte, Filterung und Pagination

Für Endpunkte, die Listen von Ressourcen zurückgeben, wird ein einheitliches Schema für Pagination, Sortierung und Filterung verwendet. Ziel ist es, eine konsistente Nutzungserfahrung über alle Ressourcen hinweg sicherzustellen.

Standardisierte Query-Parameter sind:

* `page` und `size` zur Steuerung der Pagination
* `sort` im Format `field,asc|desc`
* `q` für einfache Volltext- oder Fuzzy-Suchen

Die Responses enthalten neben der eigentlichen Ergebnisliste stets Metadaten wie `totalElements` und `totalPages`. Dadurch können Clients Listen effizient darstellen und paginieren, ohne zusätzliche Requests durchführen zu müssen.

---

## 9. DTO-Strategie

Die DTO-Strategie des IDM Service dient der klaren Trennung zwischen externen API-Verträgen und dem internen Domain-Modell. DTOs sind ausschließlich ein Mittel zur Kommunikation über die API-Grenzen hinweg und dürfen keine fachliche Logik enthalten.

Für jede fachliche Operation werden spezialisierte DTO-Typen eingesetzt. Create-DTOs repräsentieren die zum Anlegen neuer Ressourcen erforderlichen Informationen, Update-DTOs beschreiben gezielte Änderungen an bestehenden Ressourcen, und Response-DTOs bilden den nach außen sichtbaren Zustand einer Ressource ab. Diese Trennung verhindert Überladung einzelner DTOs und erhöht die Verständlichkeit sowie Stabilität der API.

DTOs enthalten grundsätzlich **keine sensiblen Daten**. Insbesondere sicherheitsrelevante Informationen wie Passwort-Hashes, Tokens oder interne Statusinformationen werden niemals über die API exponiert. Dadurch bleibt die API auch bei internen Modelländerungen sicher und konsistent.

Das Mapping zwischen DTOs und Domain-Entities erfolgt ausschließlich im **API Layer**. Weder Domain- noch Persistenzschicht kennen DTOs oder Mapping-Logik. Diese klare Zuordnung stellt sicher, dass Änderungen an API-Verträgen keine Auswirkungen auf das Fachmodell haben und umgekehrt.

---

## 10. Fehlermanagement

Ein einheitliches und nachvollziehbares Fehlermanagement ist ein zentraler Bestandteil des technischen Konzepts. Ziel ist es, Fehler sowohl für API-Konsumenten als auch für Entwickler eindeutig interpretierbar zu machen, ohne interne Implementierungsdetails offenzulegen.

Fachliche und technische Fehler werden klar voneinander getrennt. Fachliche Fehler resultieren aus Verletzungen von Geschäftsregeln oder ungültigen Zustandsübergängen, während technische Fehler auf Infrastruktur- oder Laufzeitprobleme zurückzuführen sind. Beide Kategorien werden konsistent behandelt und in definierte HTTP-Antworten übersetzt.

### 10.1 Exception-Modell

Der IDM Service verwendet eine eigene Exception-Hierarchie, um fachliche Fehler explizit auszudrücken und sie von technischen Framework-Exceptions abzugrenzen. Zentrale Exception-Typen sind:

* `EntityNotFoundException` für den Zugriff auf nicht existierende Ressourcen
* `BusinessRuleViolationException` für fachliche Regelverletzungen
* `ConflictException` für konkurrierende oder widersprüchliche Zustände
* `AuthorizationViolationException` für unzulässige Zugriffsversuche

Diese Exceptions werden gezielt im Domain- oder API-Layer ausgelöst und dienen als klare Signale für fehlerhafte Use-Cases.

### 10.2 HTTP-Abbildung und Fehlerantworten

Die Übersetzung von Exceptions in HTTP-Antworten erfolgt zentral über einen `@ControllerAdvice`. Dadurch wird sichergestellt, dass Fehler unabhängig vom Auslöseort einheitlich behandelt werden.

Die Abbildung auf HTTP-Statuscodes folgt festen Regeln:

* Nicht gefundene Ressourcen führen zu einem **404 Not Found**.
* Ungültige Eingaben oder Validierungsfehler werden mit **400 Bad Request** beantwortet.
* Fehlende Berechtigungen resultieren in **403 Forbidden**.
* Fachliche Konflikte werden als **409 Conflict** zurückgegeben.
* Verletzungen fachlicher Regeln werden mit **422 Unprocessable Entity** signalisiert.

Fehlerantworten enthalten strukturierte Informationen, die eine eindeutige Diagnose ermöglichen, ohne sensible oder interne Details preiszugeben. Dazu zählen insbesondere ein verständlicher Fehlercode, eine aussagekräftige Beschreibung sowie eine Korrelation über eine TraceId.

Umsetzung über `@ControllerAdvice`.

---

## 11. Audit Logging

Audit Logging ist ein fachlich motivierter Bestandteil des IDM Service und dient der Nachvollziehbarkeit sowie Revisionssicherheit sicherheits‑ und identitätsrelevanter Änderungen. Im Gegensatz zum technischen Application Logging bildet das Audit Logging fachliche Ereignisse ab und ist daher strikt von Debug‑ oder Betriebslogs getrennt zu betrachten.

Für jede fachlich relevante Zustandsänderung wird ein eigenständiger Audit‑Eintrag erzeugt. Dieser dokumentiert nachvollziehbar, **wer** zu **welchem Zeitpunkt** eine **welche Änderung** an einer fachlichen Entität vorgenommen hat. Typische Audit‑Informationen umfassen dabei den ausführenden Benutzer (actor), den Zeitpunkt der Aktion, den Typ und die Identität der betroffenen Entität sowie die Art der Änderung (z. B. CREATE, UPDATE, DELETE). Optional können geänderte Felder oder strukturierte Änderungsinformationen ergänzt werden, sofern dies fachlich erforderlich ist.

Audit‑Logs sind revisionsrelevant und werden unveränderlich gespeichert. Sie sind nicht für Debug‑Zwecke gedacht, sondern dienen der fachlichen Nachvollziehbarkeit, internen Kontrolle und ggf. externen Prüfungen.

---

## 12. Logging & Observability

Logging und Observability sind zentrale technische Querschnittsthemen, die den stabilen Betrieb, die Fehleranalyse und die Weiterentwicklung des IDM Service unterstützen. Ziel ist es, betriebliche Transparenz zu schaffen, ohne die Fachlichkeit oder Sicherheit des Systems zu kompromittieren.

### 12.1 Logging-Grundlagen

Der IDM Service verwendet SLF4J als Logging‑API mit Logback als Standard‑Implementierung. Ein einheitliches Log‑Level‑Konzept (ERROR, WARN, INFO, DEBUG, TRACE) sorgt dafür, dass Logausgaben konsistent interpretierbar bleiben und in unterschiedlichen Betriebsumgebungen angemessen gefiltert werden können.

Ein zentrales Sicherheitsprinzip ist, dass **keine Secrets oder Credentials in Logs erscheinen dürfen**. Dazu zählen insbesondere Passwörter, Passwort‑Hashes, JWTs, Refresh‑Tokens, API‑Keys, private Schlüssel sowie sonstige sicherheitsrelevante Zugangsdaten. Sensible Informationen sind vor dem Logging konsequent zu maskieren oder vollständig zu unterdrücken.

### 12.2 Structured Logging

Die Anwendung setzt verbindlich auf **Structured Logging**, um Logs maschinenlesbar, auswertbar und korrelierbar zu machen. Log‑Einträge bestehen aus strukturierten Key‑Value‑Daten und werden bevorzugt im JSON‑Format ausgegeben, insbesondere im Container‑ und Produktivbetrieb.

Diese Form des Loggings ermöglicht eine spätere Integration von Observability‑Plattformen wie OpenTelemetry, Elastic, Loki oder Datadog, ohne bestehende Log‑Statements refaktorieren zu müssen.

### 12.3 Korrelation und Context Propagation

Zur Nachvollziehbarkeit von Requests über mehrere Komponenten hinweg wird pro eingehendem Request eine TraceId erzeugt oder aus vorhandenen Headern übernommen. Diese TraceId wird über den MDC propagiert und in allen relevanten Log‑Einträgen geführt. Zusätzlich wird sie in Fehlerantworten zurückgegeben, um eine eindeutige Korrelation zwischen Client‑Fehlern und Server‑Logs zu ermöglichen.

Optional kann ergänzend eine SpanId verwendet werden, um zukünftig verteiltes Tracing zu unterstützen.

### 12.4 OpenTelemetry-Readiness

Der IDM Service ist konzeptionell auf eine spätere Nutzung von OpenTelemetry vorbereitet, ohne initiale Abhängigkeiten einzuführen. Logging, Tracing und Metriken sind klar voneinander getrennt, und proprietäre APIs werden vermieden. Eine spätere Erweiterung kann durch den Einsatz eines OpenTelemetry Java Agents oder SDKs erfolgen, ohne bestehende Logging‑ oder Anwendungscode‑Strukturen anzupassen.

### 12.5 Actuator & Health

Zur Überwachung des Systemzustands wird Spring Boot Actuator eingesetzt. Health‑, Readiness‑ und Liveness‑Endpoints ermöglichen eine zuverlässige Integration in containerisierte Betriebsumgebungen und unterstützen automatisierte Deployments sowie Monitoring.

---

## 13. Build, SBOM und Qualitätssicherung

Der Build‑ und Qualitätsprozess des IDM Service ist darauf ausgelegt, reproduzierbare Artefakte, transparente Abhängigkeiten und eine hohe Code‑Qualität sicherzustellen.

Der Build erfolgt über Maven und umfasst die vollständige Kompilierung, das Ausführen automatisierter Tests sowie die Erstellung eines lauffähigen JAR‑Artefakts. Abhängigkeiten werden zentral verwaltet, sodass der Build deterministisch und nachvollziehbar bleibt.

Zur Erfüllung von Compliance‑ und Sicherheitsanforderungen wird eine **Software Bill of Materials (SBOM)** erzeugt, beispielsweise im CycloneDX‑ oder SPDX‑Format. Dadurch sind alle eingesetzten Abhängigkeiten und deren Versionen transparent dokumentiert.

Optional kann der Build‑Prozess um eine Containerisierung erweitert werden, etwa über Jib oder Buildpacks, um ein OCI‑konformes Image für den Betrieb in Kubernetes‑Umgebungen zu erzeugen.

---

## 14. Teststrategie

Die Teststrategie des IDM Service verfolgt das Ziel, fachliche Korrektheit, technische Stabilität und sicherheitsrelevantes Verhalten frühzeitig und automatisiert abzusichern. Tests sind integraler Bestandteil des Entwicklungsprozesses und nicht als nachgelagerte Qualitätssicherung zu verstehen.

Die Tests werden bewusst in unterschiedliche Ebenen unterteilt. **Domain Tests** prüfen fachliche Logik isoliert und ohne Spring‑Kontext, um schnelle Rückmeldungen und eine hohe Teststabilität zu gewährleisten. **Integration Tests** werden mit Spring Boot ausgeführt und verifizieren das Zusammenspiel von Komponenten, Persistenz und Konfiguration.

Zusätzlich werden **Security‑Tests** eingesetzt, um Authentifizierungs‑ und Autorisierungsmechanismen zu überprüfen, insbesondere Login‑Flows und Permission‑basierte Zugriffskontrollen.

Liquibase‑Migrationen werden auch im Testprofil ausgeführt, sodass Tests stets gegen ein konsistentes und realitätsnahes Datenbankschema laufen.

---
