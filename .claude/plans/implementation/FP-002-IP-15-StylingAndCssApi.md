# Implementierungsplan: FP-002-IP-15-StylingAndCssApi

## Voraussetzung

* IP-01 bis IP-14 abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTab.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuGroup.kt` (ändern)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/menutab/menu-tab.css` (neu)
* `docs/docs/menu-pane/customize-styles.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* Style-Klassen: `menu-tab`, `menu-tab-strip`, `menu-tab-button`, `menu-tab-file`,
  `menu-tab-contextual`, `menu-tab-context-group`, `menu-group-strip`, `menu-group`,
  `menu-group-launcher`, `menu-group-overflow-chevron`, `menu-tab-collapsed`.
* Pseudo-Klassen: `active`, `contextual`, `disabled`, `collapsed`.
* Styleable Properties via `CssMetaData` für Kontext-Gruppen-Farbe (`-panelium-context-color`).
* `FXMenuTab.getUserAgentStylesheet()` liefert gebündeltes `menu-tab.css`, analog `chrome.css`.

## Aufgabe 1: Style-Klassen und Pseudo-Klassen

* Style-Klassen aus den Design-Entscheidungen an den jeweiligen Nodes ergänzen.
* Pseudo-Klassen `active`/`contextual`/`disabled`/`collapsed` an Zustands-Properties binden.

## Aufgabe 2: Styleable Properties

* `CssMetaData` für `-panelium-context-color` auf `ContextTabGroup`/`FXMenuTab` ergänzen.
* `getCssMetaData()`/`getClassCssMetaData()` überschreiben, analog `ChromePane`.

## Aufgabe 3: Default-Stylesheet

* `menu-tab.css` mit Default-Look für alle Style-Klassen aus Aufgabe 1 erstellen.
* `FXMenuTab.getUserAgentStylesheet()` implementieren.

## Aufgabe 4: Dokumentation und Build

* `docs/docs/menu-pane/customize-styles.md` und `.de.md` mit allen Klassen befüllen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Pseudo-Klassen-Zustände, CSS-Metadaten-Override, Default-Stylesheet-Anwendung headless testen.
