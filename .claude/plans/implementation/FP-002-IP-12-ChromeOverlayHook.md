# Implementierungsplan: FP-002-IP-12-ChromeOverlayHook

## Voraussetzung

* IP-05 (BackstageOverlay) und IP-11 (ChromeDocking) abgeschlossen.

## Design-Entscheidungen

* `ChromePane` kennt `FXMenuTab` NICHT direkt (Entscheidung aus IP-11: Docking per Komposition
  `ChromePane.content = BorderPane(top = FXMenuTab)`, keine `menuTabProperty`).
* `ChromePane` implementiert `BackstageOverlayHost` (`showOverlay(Node)` / `hideOverlay()` aus IP-05).
* Overlay-`StackPane`-Layer über dem gesamten Inhalt (Caption + Content), unterhalb der Resize-Layer.
* Overlay standardmäßig unsichtbar/`managed=false`, bis `showOverlay` aufgerufen wird.
* Verdrahtung Host <-> `FXMenuTab` NICHT über eine `ChromePane`-Property, sondern über
  Parent-/Scene-Lookup: `FXMenuTab` sucht beim Anhängen an die Scene aufwärts den nächsten
  `BackstageOverlayHost` und setzt sich selbst als dessen Nutzer; beim Abhängen wird der Hook gelöst.
* Ist kein Host im Baum, bleibt das bestehende lokale Fade-Verhalten aus IP-05 aktiv.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/window/ChromePane.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/window/ChromePaneView.kt` (ändern)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/ChromePaneView.fxml` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/menutab/FXMenuTab.kt` (ändern: Host-Lookup)
* `docs/docs/platinum-chrome/implementation.md`, `.de.md` (aktualisieren)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Aufgabe 1: Overlay-Layer auf `ChromePane`

* Overlay-`StackPane` in `ChromePaneView.fxml`/`ChromePane` über Caption und Content ergänzen,
  unterhalb `resizeOverlay` in der `children`-Reihenfolge.
* Sichtbarkeits-/`managed`-Steuerung beim Ein-/Ausblenden.
* `ChromePane` implementiert `BackstageOverlayHost.showOverlay` / `hideOverlay` gegen diesen Layer.

## Aufgabe 2: Host-Lookup in `FXMenuTab`

* Beim `sceneProperty`-/`parentProperty`-Wechsel aufwärts den nächsten `BackstageOverlayHost` suchen.
* Gefundenen Host als `overlayHost` setzen; bei Entfernen aus dem Baum `overlayHost = null`.
* Kein Host gefunden: lokales Fade-Verhalten aus IP-05 unverändert.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Backstage-Overlay-Anbindung" ergänzen (Lookup-Mechanismus, kein API-Zwang).
* `project-docs`-Skill vor dem Editieren laden.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Aufgabe 4: Plan-Abhakung

* IP-12 in `FP-002-FXMenuTab.md`, `FP-002-FXMenuTab-status.md`, `FP-002-Overview.md` als `COMPLETED`.
* "Delivered vs. planned"-Notiz: Anbindung über Parent-/Scene-Lookup statt `menuTabProperty`.
* Diese Plandatei im selben Change-Set wie die letzte Aufgabe per `git rm` entfernen.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Headless: `FXMenuTab` in `BorderPane(top)` unter `ChromePane` findet den Host, Backstage
  erscheint im `ChromePane`-Overlay über dem Content.
* Headless: `FXMenuTab` ohne umgebenden Host nutzt weiter das lokale Fade.
