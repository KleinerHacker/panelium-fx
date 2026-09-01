# Implementierungsplan: FP-002-IP-12-ChromeOverlayHook

## Voraussetzung

* IP-05 (BackstageOverlay) und IP-11 (ChromeDocking) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromePane.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromePaneView.kt` (ändern)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/ChromePaneView.fxml` (ändern)
* `docs/docs/platinum-chrome/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `ChromePane` implementiert `BackstageOverlayHost` (Vertrag aus IP-05) intern.
* Overlay-`StackPane`-Layer über dem gesamten Inhalt (Band + Content), unterhalb der Resize-Layer.
* `ChromePane` verdrahtet sich automatisch als `overlayHost` an `FXMenuTab`, sobald über
  `menuTabProperty` (IP-11) gesetzt.
* Overlay standardmäßig unsichtbar/`managed=false`, bis `showOverlay` aufgerufen wird.

## Aufgabe 1: Overlay-Layer

* Overlay-`StackPane` in `ChromePaneView.fxml` über Band und Content ergänzen.
* Sichtbarkeits-/Managed-Steuerung beim Ein-/Ausblenden implementieren.

## Aufgabe 2: Verdrahtung mit FXMenuTab

* `ChromePane` implementiert `BackstageOverlayHost.showOverlay`/`hideOverlay`.
* Automatisches Setzen von `FXMenuTab.overlayHost` bei Zuweisung über `menuTabProperty`.
* Zurücksetzen des Hooks beim Entfernen des Ribbons.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Backstage-Overlay-Anbindung" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Anzeige/Verbergen des Overlays über `ChromePane`, korrekte Verdrahtung headless testen.
