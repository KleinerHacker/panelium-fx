# Implementierungsplan: FP-002-IP-05-BackstageOverlay

## Voraussetzung

* IP-04 (FileMenuTab) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTab.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabViewModel.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/BackstageOverlayHost.kt` (neu, Contract)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `BackstageOverlayHost`-Interface: `showOverlay(node: Node)`, `hideOverlay()` - Vertrag für IP-12.
* `FXMenuTab` erhält optionale Referenz auf einen `BackstageOverlayHost` (gesetzt durch IP-12/IP-11).
* Aktivierung des Datei-Tabs ruft `showOverlay`, Deaktivierung `hideOverlay` auf dem Host auf.
* Vorheriger aktiver regulärer Tab wird vor Aktivierung des Datei-Tabs gemerkt und restauriert.
* Escape-Taste und Klick außerhalb des Backstage-Inhalts lösen Deaktivierung aus.

## Aufgabe 1: Overlay-Vertrag

* `BackstageOverlayHost`-Interface anlegen (`showOverlay`/`hideOverlay`).
* `overlayHost`-Property (nullable, gesetzt von außen) auf `FXMenuTab` ergänzen.

## Aufgabe 2: Aktivierung/Deaktivierung

* Merken des zuletzt aktiven regulären Tabs vor Datei-Tab-Aktivierung.
* Bei Datei-Tab-Aktivierung: `overlayHost?.showOverlay(backstageContent)` aufrufen.
* Bei Deaktivierung: `overlayHost?.hideOverlay()`, vorherigen Tab reaktivieren.
* Collapse-State-Restore-Hook als offene Erweiterung für IP-13 vorsehen (Callback-Property).

## Aufgabe 3: Dismissal

* `KeyEvent`-Filter für `ESCAPE` auf Backstage-Inhalt ergänzen.
* Klick-außerhalb-Erkennung über `Scene`-Mouse-Filter, solange Backstage aktiv ist.
* Beide Trigger rufen dieselbe Deaktivierungs-Methode wie ein Tab-Wechsel auf.

## Aufgabe 4: Dokumentation und Build

* Doku-Abschnitt "Backstage-Overlay" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Öffnen/Schließen per Tab-Klick, Escape, Außenklick; Wiederherstellung des vorherigen Tabs testen.
