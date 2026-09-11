# Implementierungsplan: FP-001-IP-02-MenuelisteUndAuswahl

## Voraussetzung

* IP-01 (GrundgeruestUndDatenmodell) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageMenuPaneView.kt` (aendern)
* `src/main/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageMenuPaneViewModel.kt` (aendern)
* `src/main/resources/org/pcsoft/framework/panelium/menupane/backstage-menu-pane.css` (aendern)
* `src/test/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageMenuPaneSelectionTest.kt` (neu)

## Design-Entscheidungen

* `ListView<FXBackstageMenuItem>` fuer die Menueliste, gebunden an `items`.
* Custom `ListCell` zeigt optionales Icon und Text nebeneinander in einer `HBox`.
* Auswahl in der `ListView` aktualisiert `selectedItem` im ViewModel.
* Aenderung von `selectedItem` zeigt dessen `content`-Node im zentralen `StackPane`.
* Ohne Auswahl bleibt der Inhaltsbereich leer.

## Aufgabe 1: ListView-Integration

* `ListView<FXBackstageMenuItem>` im linken Bereich der View einbauen.
* `ListView`-Items an `items` binden (`FXCollections`/direkte Zuweisung).

## Aufgabe 2: Custom Cell

* `ListCell`-Factory mit `HBox` aus optionalem Icon und Text implementieren.
* Fehlendes Icon blendet den Icon-Platz aus, der Text bleibt sichtbar.

## Aufgabe 3: Auswahlsteuerung

* Klick auf einen Eintrag setzt `selectedItem` im ViewModel.
* Aenderung von `selectedItem` aktualisiert die Kinder des Inhalts-`StackPane`.
* Kein `selectedItem` gesetzt leert die Kinder des `StackPane`.

## Aufgabe 4: Styling

* CSS-Klassen fuer Menueliste und Zellen in `backstage-menu-pane.css` ergaenzen.
* Ausgewaehlten Eintrag visuell hervorheben.

## Aufgabe 5: Tests und Build

* `testing`-Skill vor Testerstellung laden.
* Auswahl-Klick, Inhaltsanzeige und leeren Ausgangszustand testen.
* `./gradlew build` erfolgreich ausfuehren.

## Tests

* `testing`-Skill vor Testerstellung laden.
* TestFX-Test: Klick auf Eintrag zeigt zugehoerige `content`-Node im Inhaltsbereich.
* TestFX-Test: Ohne Auswahl bleibt der Inhaltsbereich leer.
