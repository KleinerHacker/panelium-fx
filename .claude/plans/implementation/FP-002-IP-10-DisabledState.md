# Implementierungsplan: FP-002-IP-10-DisabledState

## Voraussetzung

* IP-01 (MenuPaneCore) und IP-06 (Groups) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menupane/FXMenuTab.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menupane/MenuGroup.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menupane/FXMenuPaneView.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menupane/MenuGroupView.kt` (ändern)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `disabledProperty` (`BooleanProperty`) auf `FXMenuTab` und `MenuGroup`.
* Deaktivierter Tab kann nicht aktiviert werden (Aktivierungsversuch wird ignoriert).
* Deaktivierte Gruppe deaktiviert ihren Content-Host (`disableProperty`-Bindung).
* Pseudo-Klasse `disabled` auf Tab-Button und Gruppen-Container für Styling (IP-15).

## Aufgabe 1: Disabled-Flag auf Tab

* `disabledProperty` auf `FXMenuTab` ergänzen.
* Aktivierungs-Guard in `FXMenuPane`: deaktivierte Tabs werden bei Aktivierungsversuch übersprungen.
* Pseudo-Klasse `disabled` am Tab-Button-Node binden.

## Aufgabe 2: Disabled-Flag auf Gruppe

* `disabledProperty` auf `MenuGroup` ergänzen.
* Bindung an `disableProperty` des Content-Hosts in `MenuGroupView`.
* Pseudo-Klasse `disabled` am Gruppen-Container-Node binden.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Disabled-Zustand" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Aktivierungsversuch eines deaktivierten Tabs, deaktivierte Gruppe headless testen.
