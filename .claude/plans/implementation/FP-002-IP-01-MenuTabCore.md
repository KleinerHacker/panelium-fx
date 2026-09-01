# Implementierungsplan: FP-002-IP-01-MenuTabCore

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTab.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabView.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabViewModel.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuTab.kt` (neu)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabView.fxml` (neu)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* Paket `org.pcsoft.framework.panelium.chrome.menutab`, analog zu `chrome/component`.
* `FXMenuTab` als `StackPane`, MVVM-fx-Triple mit `<fx:root>`, wie `ChromeCaptionBar`.
* `MenuTab` als öffentliche Datenklasse: `id`, `title`, `disabled`-Property (Basis für IP-10).
* Tab-Strip als `HBox` in der View, ein Button pro Tab, Style-Klasse `menu-tab-strip-button`.
* Aktiver Tab als `ObjectProperty<MenuTab?>` auf `FXMenuTab`.

## Aufgabe 1: Grundgerüst

* `FXMenuTab`, `FXMenuTabView`, `FXMenuTabViewModel` nach MVVM-fx-Muster anlegen.
* `MenuTab`-Datenklasse mit `id`, `title` anlegen.
* `ObservableList<MenuTab>` für permanente Tabs auf `FXMenuTab` exponieren.
* `FXMenuTabView.fxml` mit `<fx:root>` und Tab-Strip-Container erstellen.

## Aufgabe 2: Tab-Umschaltung

* `activeTabProperty` als öffentliche JavaFX-Property implementieren.
* Methode zum programmatischen Aktivieren eines Tabs ergänzen.
* Tab-Strip-Buttons dynamisch aus der Tab-Liste generieren (Listener auf `ObservableList`).
* Style-Klasse `active` per Pseudo-Klasse auf aktivem Tab-Button setzen.

## Aufgabe 3: Pfeiltasten-Navigation

* `KeyEvent`-Filter auf dem Tab-Strip für `LEFT`/`RIGHT` ergänzen.
* Aktivierung des vorherigen/nächsten Tabs bei Pfeiltaste, wenn Tab-Strip fokussiert ist.
* Fokus-Traversal des Tab-Strips prüfen (Standard-JavaFX-Verhalten, keine Sonderbehandlung).

## Aufgabe 4: Dokumentation und Build

* `docs/docs/menu-pane/implementation.md` und `.de.md` um Grundgerüst-Abschnitt ergänzen.
* CHANGELOG-Eintrag für `FXMenuTab`-Grundgerüst ergänzen.
* `ci-pipeline`-Skill nach Strukturänderung erneut prüfen.
* `./gradlew build` grün.

## Tests

* Siehe `testing`-Skill vor Testerstellung laden.
* Tab-Registrierung, Aktivierung, Pfeiltasten-Navigation headless testen (Grundlage für IP-16).
