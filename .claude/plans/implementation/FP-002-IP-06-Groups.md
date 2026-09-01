# Implementierungsplan: FP-002-IP-06-Groups

## Voraussetzung

* IP-01 (MenuTabCore) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuGroup.kt` (neu, Component)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuGroupView.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuGroupViewModel.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuTab.kt` (ändern)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/menutab/MenuGroupView.fxml` (neu)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `MenuGroup` als eigene MVVM-fx-Komponente (`StackPane`, `<fx:root>`), analog zu `FXMenuTab`.
* `MenuTab` erhält `ObservableList<MenuGroup>` für seine Gruppen.
* `MenuGroup` besitzt `title`-Property und `ObservableList<Node>` als Content-Host.
* Group-Strip in `FXMenuTabView` zeigt die Gruppen des aktiven regulären Tabs.

## Aufgabe 1: MenuGroup-Komponente

* `MenuGroup`, `MenuGroupView`, `MenuGroupViewModel` nach MVVM-fx-Muster anlegen.
* `title`-Property und Titel-Label in der View verdrahten.
* `ObservableList<Node>` als Content-Host, gerendert in einer `HBox`.

## Aufgabe 2: Gruppen in Tabs

* `ObservableList<MenuGroup>` auf `MenuTab` ergänzen.
* Reihenfolge-API (Hinzufügen/Entfernen/Umsortieren) auf `MenuTab`.
* Group-Strip-Container in `FXMenuTabView`, gebunden an Gruppen des aktiven Tabs.
* Wechsel des aktiven Tabs aktualisiert die angezeigten Gruppen.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Groups" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Gruppen-Registrierung, Reihenfolge, Anzeige beim Tab-Wechsel headless testen.
