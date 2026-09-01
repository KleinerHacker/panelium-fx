# Implementierungsplan: FP-002-IP-11-ChromeDocking

## Voraussetzung

* IP-01 (MenuTabCore) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromePane.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromePaneView.kt` (ändern)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/ChromePaneView.fxml` (ändern)
* `docs/docs/platinum-chrome/implementation.md`, `.de.md` (aktualisieren)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `ChromePane.menuTabProperty`: `ObjectProperty<FXMenuTab?>` - eigenständiges Band unter Caption Bar.
* Band liegt strukturell zwischen Caption Bar und Content-Container in `ChromePaneView.fxml`.
* Setzen der Property fügt/entfernt den Node im Band; `null` bedeutet kein Ribbon.
* Größenanpassung: Band nimmt bevorzugte Höhe von `FXMenuTab` ein, Content-Bereich schrumpft.

## Aufgabe 1: Band-Struktur

* Neuen Container-Bereich zwischen Caption Bar und Content in `ChromePaneView.fxml` ergänzen.
* `menuTabProperty` auf `ChromePane` mit Change-Listener zum Ein-/Aushängen des Nodes.

## Aufgabe 2: Layout-Integration

* `ChromePane`-Inset-/Layout-Berechnung um die Band-Höhe erweitern (analog Caption-Höhe).
* Verhalten bei `null` (kein Ribbon gesetzt) verifizieren: keine Layoutänderung.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Ribbon-Docking" in Platinum-Chrome- und MenuPane-Dokumentation ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `ci-pipeline`-Skill nach Strukturänderung erneut prüfen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Setzen/Entfernen von `menuTab`, Layout-Verschiebung des Content-Bereichs headless testen.
