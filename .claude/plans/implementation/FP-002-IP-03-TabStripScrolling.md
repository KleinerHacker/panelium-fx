# Implementierungsplan: FP-002-IP-03-TabStripScrolling

## Voraussetzung

* IP-01 (MenuTabCore) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabView.kt` (ändern)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabView.fxml` (ändern)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* Tab-Strip-HBox in eine horizontal scrollende `ScrollPane` (nur horizontal) einbetten.
* Mausrad-Scroll horizontal umleiten (vertikales Delta auf horizontales `hvalue` mappen).
* Aktiver Tab wird bei Aktivierung automatisch in die sichtbare Fläche gescrollt.
* Kein Tab-Overflow-Menü, kein Schrumpfen der Tab-Buttons.

## Aufgabe 1: Scroll-Viewport

* Tab-Strip-`HBox` in `ScrollPane` verpacken, vertikale Scrollbar deaktivieren.
* Mausrad-Handler für horizontales Scrollen ergänzen.
* `ScrollPane.fitToHeight` und Größenverhalten an bestehendes Layout anpassen.

## Aufgabe 2: Sichtbarkeit des aktiven Tabs

* Methode zum Scrollen auf einen bestimmten Tab-Button ergänzen.
* Aufruf bei Aktivierung eines Tabs (programmatisch, Klick, Pfeiltasten aus IP-01).

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Tab-Strip-Scrolling" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Scroll-Verhalten bei vielen Tabs, Sichtbarkeit des aktiven Tabs headless testen.
