# FP-001-IP-01: ChromeCoreAndStageIntegration

## Abhängigkeiten

* keine (Fundament)

## Betroffene Bereiche

* `build.gradle.kts` — Kotlin-Quellordner, ggf. `javafx.graphics`-Modul
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromePane.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/PaneliumChrome.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/PaneliumStage.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/internal/ChromeConfig.kt` — neu
* `README.md`, `docs/docs/usage.md`, `CHANGELOG.md`

## Design-Festlegungen

* `ChromePane` erbt von `javafx.scene.layout.Region`, Anordnung in `layoutChildren()`
* Fensterstil `StageStyle.TRANSPARENT`, Scene-Fill `Color.TRANSPARENT`
* Aufbau: `shadowRoot` (StackPane, Schatten-Insets) → `frameBox` (BorderPane) → `top` Caption-Platzhalter, `center` App-Inhalt; darüber `resizeOverlay`-Platzhalter
* Öffentliche API: `contentProperty: ObjectProperty<Node>`, Konstruktoren `ChromePane()` und `ChromePane(content)`
* Einstiegspunkte: `object PaneliumChrome { fun install(stage): ChromePane }`, `open class PaneliumStage : Stage`
* `install` und `PaneliumStage` rufen dieselbe Routine `ChromeConfig.apply(stage, chromePane)`
* Schatten-Insets als Konstante in `ChromeConfig` (CSS folgt in IP-06)
* Modale/owned Fenster: `PaneliumStage` erlaubt `initOwner`/`initModality` unverändert

## Aufgaben

### Aufgabe 1: Projekt- und Paketstruktur

* `src/main/kotlin` und `src/test/kotlin` anlegen, Paket `org.pcsoft.framework.panelium.chrome`
* `build.gradle.kts` prüfen: benötigte JavaFX-Module (`javafx.controls`, ggf. `javafx.graphics`)
* leeren Kompilier-Smoke-Test vorsehen

### Aufgabe 2: ChromePane-Grundgerüst

* `ChromePane : Region` mit internen Containern `shadowRoot`, `frameBox`, `resizeOverlay`-Platzhalter
* `contentProperty` an `frameBox.center` binden
* `layoutChildren()`: Schatten-Insets, Caption-Höhe (Platzhalter), Center füllt Rest
* Konstruktoren `ChromePane()` und `ChromePane(content)`

### Aufgabe 3: Gemeinsame Konfigurationsroutine

* `ChromeConfig.apply(stage, chromePane)`: neue `Scene(chromePane)` mit transparentem Fill setzen
* `stage.initStyle(StageStyle.TRANSPARENT)` vor `show()`
* Guard: Exception, wenn `stage.isShowing`

### Aufgabe 4: Einstiegspunkt install(stage)

* bisherigen `stage.scene?.root` als `content` in neuen `ChromePane` übernehmen
* `ChromeConfig.apply` aufrufen, `ChromePane` zurückgeben
* dokumentierten Hinweis: vor `stage.show()` aufrufen

### Aufgabe 5: Einstiegspunkt PaneliumStage

* im Init `ChromePane` erzeugen, `ChromeConfig.apply(this, pane)` aufrufen
* `contentProperty` der Stage an `pane.contentProperty` delegieren
* modale/owned Nutzung testen (initOwner, initModality)

### Aufgabe 6: Dokumentation und Build

* README-Abschnitt und `docs/docs/usage.md` „Custom window frame" mit den drei Einstiegspunkten
* CHANGELOG-Eintrag „Added"
* `./gradlew build` ausführen

## Fertig-Kriterien

* Undekoriertes, transparentes Fenster über alle drei Einstiegspunkte mit identischer Struktur
* App-Inhalt erscheint im Center-Bereich; Caption-Bereich als leerer Platzhalter vorhanden
* `install` auf bereits sichtbarer Stage wirft eine klare Exception
* `build` grün
