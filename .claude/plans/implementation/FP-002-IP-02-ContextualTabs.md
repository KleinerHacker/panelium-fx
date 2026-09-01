# Implementierungsplan: FP-002-IP-02-ContextualTabs

## Voraussetzung

* IP-01 (MenuTabCore) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTab.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuTab.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/ContextTabGroup.kt` (neu)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabView.kt` (ändern)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* Separate `ObservableList<MenuTab>` für kontextuelle Tabs, getrennt von der permanenten Liste.
* Zusammenführung permanent + kontextuell für die sichtbare Reihenfolge in einer berechneten Liste.
* `ContextTabGroup`-Datenklasse: `name`, `color`, referenzierte `MenuTab`-Ids.
* Entfernen eines aktiven kontextuellen Tabs aktiviert automatisch den vorherigen permanenten Tab.

## Aufgabe 1: Kontextuelle Tab-Verwaltung

* `ObservableList<MenuTab>` für kontextuelle Tabs auf `FXMenuTab` ergänzen.
* Merge-Logik: kontextuelle Tabs erscheinen nach den permanenten, in Einfügereihenfolge.
* Entfernen eines kontextuellen Tabs aus der sichtbaren Liste bei Remove-Aufruf.
* Aktivierungs-Fallback beim Entfernen des aktiven kontextuellen Tabs implementieren.

## Aufgabe 2: Kontext-Gruppen

* `ContextTabGroup`-Klasse mit Name und Farbwert anlegen.
* API zum Zuordnen mehrerer kontextueller Tabs zu einer `ContextTabGroup`.
* Gruppen-Header-Node im Tab-Strip über den zugehörigen Tabs rendern (ohne Farb-Styling).
* Style-Hook (Style-Klasse) für spätere Farbgebung in IP-15 vorbereiten.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Kontextuelle Tabs" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Hinzufügen/Entfernen kontextueller Tabs, Reihenfolge, Gruppierung headless testen.
