# Implementierungsplan: FP-002-IP-16-TestHarnessAndCoverage

## Voraussetzung

* IP-01 bis IP-15 abgeschlossen.

## Betroffene Dateien

* `src/test/kotlin/org/pcsoft/framework/panelium/chrome/menutab/*` (neu, mehrere Testklassen)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (Status aktualisieren, falls nötig)

## Design-Entscheidungen

* Wiederverwendung von `AbstractChromeUiTest` aus `CustomWindowChrome`, falls anwendbar.
* Testklassen mirroring der Implementierungspläne: je ein Schwerpunkt pro Klasse.
* JUnit-Jupiter-API mit TestFX, analog IP-07 aus `FP-001-CustomWindowChrome`.

## Aufgabe 1: Testklassen Tab-Modell

* `TabCoreTest`: Registrierung, Aktivierung, Pfeiltasten-Navigation (IP-01).
* `ContextualTabsTest`: Hinzufügen/Entfernen, Kontext-Gruppen (IP-02).
* `TabStripScrollingTest`: Scroll-Verhalten, Sichtbarkeit aktiver Tab (IP-03).

## Aufgabe 2: Testklassen Datei-Tab und Backstage

* `FileMenuTabTest`: Position, Einzigartigkeit des Datei-Tabs (IP-04).
* `BackstageOverlayTest`: Öffnen/Schließen, Escape, Außenklick, Restore (IP-05).

## Aufgabe 3: Testklassen Groups

* `GroupsTest`: Registrierung, Reihenfolge, Anzeige (IP-06).
* `GroupLayoutTest`: Layout-Varianten (IP-07).
* `GroupLauncherTest`: Sichtbarkeit, Callback (IP-08).
* `GroupOverflowTest`: Overflow-Auslösung, Chevron-Menü (IP-09).

## Aufgabe 4: Testklassen Zustand, Chrome-Integration, Styling

* `DisabledStateTest`: Tab- und Gruppen-Disabling (IP-10).
* `ChromeDockingTest`: Band-Platzierung, Layoutverhalten (IP-11).
* `ChromeOverlayHookTest`: Overlay-Anzeige über `ChromePane` (IP-12).
* `CollapseAndExpandTest`: Doppelklick, Toggle, Peek, Restore (IP-13).
* `RibbonContextMenuTest`: Rechtsklick-Menü, Toggle-Wirkung (IP-14).
* `StylingTest`: Pseudo-Klassen, CSS-Metadaten, Default-Stylesheet (IP-15).

## Aufgabe 5: Abschluss

* Kover-Coverage-Report für das neue Paket prüfen.
* README-Implementierungsstatus-Zeile für `FXMenuTab` auf "Done" setzen.
* `./gradlew build` (inkl. `test`, `koverVerify`, `licensee`) grün.
