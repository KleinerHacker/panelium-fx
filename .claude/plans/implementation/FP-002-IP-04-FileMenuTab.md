# Implementierungsplan: FP-002-IP-04-FileMenuTab

## Voraussetzung

* IP-01 (MenuTabCore) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTab.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuTab.kt` (ändern)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `MenuTab` erhält ein `isFileTab`-Flag; genau ein Tab in der permanenten Liste trägt es.
* Datei-Tab wird beim Hinzufügen automatisch an Position 0 der permanenten Liste einsortiert.
* `backstageContentProperty` (`ObjectProperty<Node?>`) auf `FXMenuTab` für den Panel-Inhalt.
* Kein Show/Hide-Verhalten in diesem Plan - reine Modell- und Slot-Erweiterung.

## Aufgabe 1: Datei-Tab-Markierung

* `isFileTab`-Flag auf `MenuTab` ergänzen.
* Einfüge-Logik in `FXMenuTab`: Datei-Tab immer an erster Position, nur ein Datei-Tab erlaubt.
* Validierung: zweiter Datei-Tab wird abgelehnt (`IllegalArgumentException`).

## Aufgabe 2: Backstage-Content-Slot

* `backstageContentProperty` / `var backstageContent: Node?` auf `FXMenuTab` ergänzen.
* Platzhalter-Node im FXML für spätere Anzeige (noch nicht sichtbar geschaltet) vorsehen.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Datei-Tab" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Erzwungene erste Position, Ablehnung eines zweiten Datei-Tabs, Content-Slot-API headless testen.
