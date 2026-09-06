# Implementierungsplan: FP-002-IP-05-BackstageOverlay

## Voraussetzung

* IP-04 (FileMenuTab) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/menutab/FXMenuTab.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/menutab/FXMenuTabViewModel.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/menutab/FXMenuTabView.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/menutab/BackstageOverlayHost.kt` (neu, Contract)
* `docs/docs/menu-pane/implementation.md`, `implementation.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `BackstageOverlayHost`-Interface: `showOverlay(node: Node)`, `hideOverlay()` - Vertrag für IP-12.
* `FXMenuTab` erhält optionale Referenz auf einen `BackstageOverlayHost` (gesetzt durch IP-12/IP-11).
* Datei-Tab-Aktivierung ist eigener Zustand: `fileTabActive`-Boolean-Property auf `FXMenuTab`,
  gesetzt durch Klick auf den Datei-Tab-Button, NICHT über `activeTab`/`visibleTabs`.
* `fileTabActive = true` ruft `showOverlay(backstageContent)`, `false` ruft `hideOverlay()` auf.
* Vor dem Aktivieren wird der zuletzt aktive `visibleTabs`-Tab (`activeTab`) gemerkt und beim
  Schließen wiederhergestellt; `activeTab` bleibt während der Backstage unverändert bestehen.
* Escape-Taste und Klick außerhalb des Backstage-Inhalts setzen `fileTabActive = false`.

## Aufgabe 1: Overlay-Vertrag

* `BackstageOverlayHost`-Interface anlegen (`showOverlay`/`hideOverlay`).
* `overlayHost`-Property (nullable, gesetzt von außen) auf `FXMenuTab` ergänzen.

## Aufgabe 2: Aktivierung/Deaktivierung

* `fileTabActive`-Boolean-Property auf `FXMenuTab`; Datei-Tab-Button togglet sie.
* Beim Wechsel auf `true`: aktuellen `activeTab` merken, `overlayHost?.showOverlay(backstageContent)`.
* Beim Wechsel auf `false`: `overlayHost?.hideOverlay()`, gemerkten Tab wiederherstellen.
* Auswahl eines regulären Tabs im Strip setzt `fileTabActive = false`.
* Collapse-State-Restore-Hook als offene Erweiterung für IP-13 vorsehen (Callback-Property).

## Aufgabe 3: Dismissal

* `KeyEvent`-Filter für `ESCAPE` auf Backstage-Inhalt ergänzen.
* Klick-außerhalb-Erkennung über `Scene`-Mouse-Filter, solange `fileTabActive` ist.
* Beide Trigger setzen `fileTabActive = false` wie ein Tab-Wechsel.

## Aufgabe 4: Dokumentation und Build

* Doku-Abschnitt "Backstage-Overlay" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Öffnen/Schließen per Datei-Tab-Button, Escape, Außenklick; Wiederherstellung des vorherigen
  `visibleTabs`-Tabs testen.
