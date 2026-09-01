# Implementierungsplan: FP-002-IP-08-GroupLauncher

## Voraussetzung

* IP-06 (Groups) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuGroup.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuGroupView.kt` (ändern)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/menutab/MenuGroupView.fxml` (ändern)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `MenuGroup.launcherActionProperty`: `ObjectProperty<Runnable?>` - `null` bedeutet kein Launcher.
* Launcher-Button nur sichtbar/verwaltet, wenn `launcherAction` gesetzt ist.
* Button an der Titelzeile der Gruppe positioniert (unteres rechtes Eck, wie Office).

## Aufgabe 1: Launcher-API

* `launcherActionProperty` (`ObjectProperty<Runnable?>`) auf `MenuGroup` ergänzen.
* Sichtbarkeits-Bindung: Button sichtbar nur wenn Property nicht `null`.

## Aufgabe 2: Launcher-UI

* Launcher-Button (Icon-Button) in `MenuGroupView.fxml` neben dem Titel ergänzen.
* Klick auf Button ruft `launcherAction?.run()` auf.
* Style-Klasse `menu-group-launcher` für spätere Styling-Anpassung (IP-15).

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Group-Launcher" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Sichtbarkeit mit/ohne Launcher, Callback-Aufruf headless testen.
