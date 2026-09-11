# Implementierungsplan: FP-001-IP-03-Schnellaktionsleiste

## Voraussetzung

* IP-01 (GrundgeruestUndDatenmodell) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageMenuPaneView.kt` (aendern)
* `src/main/resources/org/pcsoft/framework/panelium/menupane/backstage-menu-pane.css` (aendern)
* `src/test/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageMenuPaneQuickActionsTest.kt` (neu)

## Design-Entscheidungen

* Fusszeile als `HBox` reiner Icon-Buttons am Fuss des Menuebereichs.
* Buttons werden aus `quickActions` erzeugt und bei Listenaenderung live nachgefuehrt.
* Klick auf einen Button ruft direkt den `onAction`-Callback der zugehoerigen Aktion auf.
* Schnellaktionen beeinflussen weder `selectedItem` noch den Inhaltsbereich.

## Aufgabe 1: Fusszeile-Layout

* `HBox` am unteren Rand des linken Menuebereichs anlegen.
* `HBox`-Kinder aus `quickActions` erzeugen.

## Aufgabe 2: Icon-Button

* Reinen Icon-Button ohne Text je `FXBackstageQuickAction` erzeugen.
* Klick-Handler ruft `onAction`-Callback der jeweiligen Aktion direkt auf.

## Aufgabe 3: Listensynchronisation

* Hinzufuegen und Entfernen in `quickActions` aktualisiert die Buttons live.
* `ListChangeListener` analog bestehenden Mustern im Projekt verwenden.

## Aufgabe 4: Styling

* CSS-Klassen fuer Fusszeile und Icon-Buttons in `backstage-menu-pane.css` ergaenzen.

## Aufgabe 5: Tests und Build

* `testing`-Skill vor Testerstellung laden.
* Klick auf Schnellaktion und Listensynchronisation testen.
* `./gradlew build` erfolgreich ausfuehren.

## Tests

* `testing`-Skill vor Testerstellung laden.
* TestFX-Test: Klick auf Icon-Button loest den zugehoerigen `onAction`-Callback aus.
* TestFX-Test: Hinzufuegen/Entfernen einer Aktion aktualisiert die Buttons.
