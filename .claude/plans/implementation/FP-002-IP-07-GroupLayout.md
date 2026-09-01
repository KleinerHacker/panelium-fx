# Implementierungsplan: FP-002-IP-07-GroupLayout

## Voraussetzung

* IP-06 (Groups) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/GroupLayoutVariant.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuGroup.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuGroupView.kt` (ändern)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `GroupLayoutVariant`-Enum: `LARGE`, `SMALL_STACK`, `COLUMN`.
* Anwendung weist jedem Content-Node über eine begleitende Property/Map eine Variante zu.
* `LARGE`: Node füllt die volle Gruppenhöhe. `SMALL_STACK`: mehrere Nodes vertikal gestapelt.
* `COLUMN`: mehrere `SMALL_STACK`-Spalten nebeneinander innerhalb der Gruppe.

## Aufgabe 1: Layout-Varianten-Modell

* `GroupLayoutVariant`-Enum anlegen.
* Attached-Property `MenuGroup.setLayoutVariant(node, variant)` / `getLayoutVariant(node)`.
* Default-Variante `LARGE`, wenn nichts gesetzt ist.

## Aufgabe 2: Layout-Berechnung

* `MenuGroupView`: Content-Host durch layoutbewusste Anordnung ersetzen (kein reines `HBox`-Flow).
* `LARGE`-Nodes einzeln nebeneinander, volle Höhe.
* `SMALL_STACK`-Nodes gruppieren und vertikal stapeln (max. 3 pro Stapel, wie Office).
* `COLUMN`: mehrere Stapel nebeneinander anordnen.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Layout-Varianten" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Jede Layout-Variante einzeln und gemischt in einer Gruppe headless testen.
