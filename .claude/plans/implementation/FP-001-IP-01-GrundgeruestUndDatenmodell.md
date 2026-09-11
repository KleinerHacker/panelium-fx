# Implementierungsplan: FP-001-IP-01-GrundgeruestUndDatenmodell

## Voraussetzung

* Keine - unabhaengig umsetzbar.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageMenuPane.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageMenuPaneView.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageMenuPaneViewModel.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageMenuItem.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/menupane/FXBackstageQuickAction.kt` (neu)
* `src/main/resources/org/pcsoft/framework/panelium/menupane/backstage-menu-pane.css` (neu)
* `CHANGELOG.md` (aendern)

## Design-Entscheidungen

* MVVM-Aufbau analog `FXMenuPane` - View und ViewModel getrennt, Verdrahtung ueber `FluentViewLoader`.
* `FXBackstageMenuPane` ist `fx:root`-faehig und per FXML instanziierbar, wie andere Komponenten.
* Layout als `BorderPane`: links ein Menuebereich (Platz fuer Liste und Fusszeile), Zentrum ein `StackPane` als Inhaltsbereich.
* `menuWidth` als styleable Property mit Default `300.0`, von aussen ueberschreibbar.
* `FXBackstageMenuItem` traegt `icon` (optional), `text` und `content` (Node).
* `FXBackstageQuickAction` traegt `icon` und einen `onAction`-Callback.

## Aufgabe 1: Datenmodelle

* `FXBackstageMenuItem` mit `icon: Node?`, `text: String`, `content: Node` anlegen.
* `FXBackstageQuickAction` mit `icon: Node` und `onAction: () -> Unit` anlegen.
* Beide Klassen im Package `menupane` platzieren, analog `FXMenuTab`.

## Aufgabe 2: ViewModel

* `FXBackstageMenuPaneViewModel` mit `items: ObservableList<FXBackstageMenuItem>` anlegen.
* `quickActions: ObservableList<FXBackstageQuickAction>` ergaenzen.
* `selectedItem: ObjectProperty<FXBackstageMenuItem?>` (Default `null`) ergaenzen.
* `menuWidth`-Property mit Default `300.0` anlegen.

## Aufgabe 3: View-Skelett

* `FXBackstageMenuPaneView` mit `BorderPane`-Grundlayout anlegen.
* Linken Bereich als Container fuer Menueliste (IP-02) und Fusszeile (IP-03) reservieren.
* Zentralen `StackPane` als Inhaltsbereich anlegen, zunaechst ohne Inhalt.

## Aufgabe 4: Komponentenklasse

* `FXBackstageMenuPane` als `fx:root`-faehige `Region` analog `FXMenuPane` anlegen.
* Oeffentliche API `items`, `quickActions`, `menuWidth` (Property plus Kotlin-Property-Zugriff) ergaenzen.
* View und ViewModel per `FluentViewLoader` verdrahten.

## Aufgabe 5: Build und Dokumentation

* CHANGELOG-Eintrag fuer die neue Komponente ergaenzen.
* `./gradlew build` erfolgreich ausfuehren.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Default-Wert von `menuWidth` sowie leere `items`- und `quickActions`-Listen pruefen.
