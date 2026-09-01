# Implementierungsplan: FP-002-IP-14-RibbonContextMenu

## Voraussetzung

* IP-13 (CollapseAndExpand) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabView.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/RibbonContextMenu.kt` (neu)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `RibbonContextMenu`: `ContextMenu` mit genau einem `MenuItem` (Minimieren/Erweitern).
* Menü-Label spiegelt aktuellen `collapsed`-Zustand (`"Ribbon minimieren"` /
  `"Ribbon erweitern"`).
* Rechtsklick auf Tab-Strip oder Group-Strip öffnet das Menü an der Cursor-Position.
* Kein Eintrag für "Ribbon anpassen" oder Schnellzugriffsleiste.

## Aufgabe 1: Kontextmenü-Komponente

* `RibbonContextMenu` mit einem `MenuItem`, gebunden an `CollapseController` (IP-13).
* Label-Aktualisierung bei Änderung von `collapsedProperty`.

## Aufgabe 2: Auslösung

* `ContextMenuEvent`-Handler auf Tab-Strip und Group-Strip in `FXMenuTabView` ergänzen.
* Menü an Mausposition anzeigen, `MenuItem`-Klick ruft Toggle auf `CollapseController` auf.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Ribbon-Kontextmenü" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Rechtsklick öffnet Menü, Klick auf Eintrag togglet Collapse-Zustand headless testen.
