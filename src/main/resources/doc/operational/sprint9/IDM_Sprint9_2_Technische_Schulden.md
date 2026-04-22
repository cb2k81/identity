# IDM Sprint 9.2 – Technische Schulden

## 1. Ziel und Einordnung

Dieses Dokument hält die aktuell identifizierten **technischen Schulden** im IDM-Backend fest, die im Zuge der **UI-Reifmachung der API für das GWC-Projekt** sichtbar wurden.

Sprint 9 dient aktuell **nicht** dem ursprünglich geplanten Folgeumfang des bisherigen Sprint-9-Plans, sondern der **gezielten Erhöhung des UI-Reifegrads der IDM-API**.

Der ursprünglich vorgesehene fachliche/operative Ablauf des bisherigen Sprint-9-Plans wird dadurch **auf Sprint 10 verschoben**.

Dieses Dokument dient ausschließlich der **Erfassung technischer Schulden** im aktuellen Stand. Es ist **kein Umsetzungsplan** und **kein Abschlussbericht**.

---

## 2. Ausgangslage

Im aktuellen Stand wurden gezielt minimale Backend-Erweiterungen vorgenommen, damit die geplanten GWC-User-Oberflächen auf Basis stabiler und fachlich korrekter API-Verträge arbeiten können.

Dabei galt ausdrücklich:

* bestehende API-Verträge dürfen erweitert werden, wenn dies rückwärtskompatibel erfolgt
* neue Endpunkte werden nur dort ergänzt, wo keine passende bestehende Operation vorhanden ist
* Änderungen sollen so klein wie möglich bleiben
* der aktuelle Stand ist funktional für den MVP nutzbar
* die Tests sind im aktuellen Stand grün

Die nachfolgend aufgeführten Punkte sind daher **bewusst akzeptierte technische Schulden**, nicht akute Fehler.

---

## 3. Technische Schulden

## 3.1 Verteiltes und inkonsistentes Mapping von `UserAccountDTO`

### Beschreibung

`UserAccountDTO` wurde im Zuge der UI-Vorbereitung erweitert, damit die User-UI fachlich sinnvollere Informationen erhalten kann.

Aktuell wird dieses DTO jedoch **nicht zentral und einheitlich** gemappt.

Stattdessen existieren mehrere manuelle Mapping-Stellen, insbesondere:

* zentrales Mapping im `UserAccountDomainService`
* zusätzliche manuelle DTO-Erzeugung in verschiedenen Assignment-/Query-Handlern

Dadurch entsteht das Risiko, dass derselbe DTO-Typ je nach verwendetem Endpoint **unterschiedlich vollständig** befüllt wird.

### Fachliche/technische Auswirkung

* gleicher DTO-Typ mit potenziell unterschiedlicher Feldsemantik
* höheres Risiko bei zukünftigen DTO-Erweiterungen
* erhöhter Pflegeaufwand
* erschwerte API-Konsistenz für Client-Kopplung

### Bewertung

**Relevanz: hoch**

Dieser Punkt ist die derzeit wichtigste technische Schuld im Bereich der User-API.

### Zielbild

* einheitliches, zentrales Mapping für `UserAccountDTO`
* keine verteilten manuellen DTO-Bauten für denselben Vertrag

---

## 3.2 Mapper-Strategie für User-Mapping nicht vollständig projektkonform (MapStruct)

### Beschreibung

Im Projekt ist MapStruct etabliert und für Mapper fachlich/technisch bevorzugt.

Im aktuellen User-Bereich wurde das neue bzw. erweiterte Mapping jedoch weiterhin manuell umgesetzt, statt einen dedizierten Mapper (bevorzugt MapStruct) einzuführen.

### Fachliche/technische Auswirkung

* Abweichung von der bevorzugten Projektkonvention
* höhere Wahrscheinlichkeit für inkonsistente Feldnachführung
* zusätzlicher Refactoring-Bedarf bei späteren DTO-Erweiterungen

### Bewertung

**Relevanz: hoch (architektonische Konventionsschuld)**

Dies ist aktuell kein funktionaler Mangel, aber eine berechtigte technische Schuld im Sinne der Projektstandards.

### Zielbild

* Einführung eines dedizierten `UserAccountMapper`
* bevorzugt auf Basis von MapStruct
* Folgeanpassung aller relevanten User-bezogenen Read-Pfade auf diesen Mapper

---

## 3.3 N+1-/Performance-Schuld bei `loginCount` und `lastLogin`

### Beschreibung

Die neuen UI-relevanten Felder `loginCount` und `lastLogin` werden aktuell im Mapping über zusätzliche Repository-Abfragen ermittelt.

Das ist für den aktuellen MVP-Stand fachlich korrekt und bewusst akzeptabel.

Für Listen oder größere Ergebnismengen kann dies jedoch zu zusätzlicher Query-Last führen, da pro User weitere Abfragen anfallen.

### Fachliche/technische Auswirkung

* potenziell unnötige Mehrfachabfragen bei Listen
* schlechtere Skalierung bei großen Seiten / großen Datenmengen
* mittelfristig ungeeignet für finale DataGrid-Nutzung als Standardlistenansicht

### Bewertung

**Relevanz: mittel**

Für den aktuellen MVP akzeptabel, für den finalen UI-Ausbau jedoch mittelfristig zu bereinigen.

### Zielbild

* dedizierte Listenprojektion bzw. aggregierte Query
* UI-taugliche Bereitstellung von `loginCount` und `lastLogin` ohne per-Entity-Zusatzabfragen

---

## 3.4 Listenfähigkeit der neuen UI-relevanten Felder noch nicht vollständig ausgebaut

### Beschreibung

Die API liefert inzwischen zusätzliche UI-relevante Felder, insbesondere für die geplante finale User-Listenansicht.

Die serverseitige Listenlogik unterstützt diese Felder jedoch aktuell noch nicht vollständig für:

* Sortierung
* Filterung
* dedizierte, listenoptimierte Projektion

### Fachliche/technische Auswirkung

* sichtbare UI-Spalten sind nicht automatisch vollständig serverseitig nutzbar
* finale DataGrid-Anforderungen sind nur teilweise erfüllt
* spätere Nacharbeit an Listen-API wahrscheinlich

### Bewertung

**Relevanz: mittel**

Kein MVP-Blocker, aber relevant für die finale Standard-Listenansicht im GWC.

### Zielbild

Prüfung und ggf. Erweiterung für mindestens:

* `lastLogin`
* `loginCount`
* `failedLoginAttempts`
* `lockedUntil`
* `lastModifiedAt`

---

## 3.5 Testabdeckung für neue User-Operationen noch nicht vollständig ausgehärtet

### Beschreibung

Die aktuelle Testbasis ist für den MVP-Stand ausreichend und grün.

Für die neu hinzugekommenen bzw. erweiterten User-API-Verträge fehlen jedoch noch einzelne ergänzende Testfälle, insbesondere im Bereich:

* Negativfälle
* Berechtigungs-/Security-Verhalten
* ggf. Konsistenz gleicher DTO-Verträge über verschiedene Read-Endpunkte

### Fachliche/technische Auswirkung

* reduzierte Absicherung gegen spätere Regressionen
* inkonsistente DTO-Befüllung könnte unbemerkt bleiben

### Bewertung

**Relevanz: niedrig bis mittel**

Für den aktuellen Stand akzeptabel, aber mittelfristig sinnvoll zu ergänzen.

### Zielbild

Ergänzende Tests insbesondere für:

* Update-Negativfall (unbekannte ID)
* Berechtigungsfall des allgemeinen User-Updates
* ggf. Vertragskonsistenz von `UserAccountDTO` über relevante Endpunkte

---

## 4. Bewusst nicht als aktuelle technische Schuld aufgenommen

Die folgenden Themen wurden im Kontext der UI-Kopplung diskutiert, sind aber **vorerst nicht MVP-relevant** und werden deshalb aktuell **nicht** als priorisierte technische Schuld für Sprint 9 geführt:

* `tags`
* `keyValuePairs`
* Admin-Session-Read-API für User-Detailseiten

Diese Themen können später wieder aufgenommen werden, sind aber aktuell **nicht Bestandteil des minimalen UI-reifen API-Zuschnitts**.

---

## 5. Priorisierung

### Hohe Priorität

1. Verteiltes / inkonsistentes `UserAccountDTO`-Mapping vereinheitlichen
2. Mapper-Strategie für User-Mapping projektkonform auf dedizierten Mapper (bevorzugt MapStruct) umstellen

### Mittlere Priorität

3. N+1-/Performance-Schuld bei `loginCount` und `lastLogin` reduzieren
4. Listenfähigkeit der neuen UI-relevanten Felder vollständig herstellen

### Niedrigere Priorität

5. Zusätzliche Negativ-/Security-/Vertragskonsistenztests ergänzen

---

## 6. Einordnung für Sprint 9

Sprint 9 ist im aktuellen Projektstand als **Sprint zur UI-Reifmachung der IDM-API** zu verstehen.

Die hier dokumentierten Punkte sind **bewusst akzeptierte technische Schulden**, um die Client-Kopplung fachlich sauber und mit minimalen Eingriffen voranzubringen.

Der Stand ist aktuell:

* funktional tragfähig
* für den MVP nutzbar
* testseitig stabil
* architektonisch grundsätzlich sauber
* jedoch mit klar benennbaren Folge-Bereinigungen für die nächste technische Vertiefung

---

## 7. Zusammenfassung

Das IDM-Backend ist im aktuellen Stand **MVP-tauglich für die geplante User-UI-Kopplung**, aber noch nicht vollständig auf den späteren Zielzustand einer technisch ausgehärteten, konsistenten und listenoptimierten UI-API gebracht.

Die wichtigste aktuelle technische Schuld liegt **nicht primär in fehlender Funktionalität**, sondern in der **Konsistenz und technischen Sauberkeit des User-DTO-Mappings**.

Diese Punkte sind bewusst dokumentiert und können in einer nachgelagerten Bereinigungsphase gezielt adressiert werden.
