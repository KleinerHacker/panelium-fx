# FP-001-IP-06: CssStylingApiAndDefaultStylesheet

## Abhängigkeiten

* IP-03
* IP-05

## Betroffene Bereiche

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromePane.kt` — Pseudo-Klassen, CssMetaData, User-Agent-Stylesheet
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromeCaptionBar.kt` — Style-Klassen
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromeCaptionButtons.kt` — Style-Klassen
* `src/main/resources/org/pcsoft/framework/panelium/chrome/chrome.css` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/internal/ChromeConfig.kt` — Konstanten entfernen
* `docs/docs/usage.md`, `CHANGELOG.md`

## Design-Festlegungen

* Style-Klassen: `chrome-pane`, `chrome-caption-bar`, `chrome-caption-left`, `chrome-caption-center`, `chrome-caption-right`, `chrome-caption-buttons`, `chrome-button` mit `minimize` / `max-restore` / `close`
* Pseudo-Klassen auf `ChromePane`: `maximized`, `fullscreen`, `active`, `inactive` (aus `stage.focusedProperty`)
* Pseudo-Klasse `maximized` zusätzlich auf `max-restore`-Button
* Styleable Properties via `CssMetaData` auf `ChromePane`: `-panelium-shadow-radius`, `-panelium-shadow-color`, `-panelium-corner-radius`, `-panelium-resize-border`, `-panelium-caption-min-height`
* diese ersetzen die Konstanten aus IP-02 und IP-03
* `getUserAgentStylesheet()` liefert `chrome.css` per Ressourcen-URL
* kein OS-spezifisches Stylesheet; Position stammt aus IP-05-Logik
* App-Stylesheet überschreibt die User-Agent-Defaults durch normale CSS-Priorität

## Aufgaben

### Aufgabe 1: Style-Klassen vergeben

* Style-Klassen an Pane, Caption-Bar, drei Slots, Button-Box, drei Buttons setzen
* Namensschema dokumenttauglich festhalten

### Aufgabe 2: Pseudo-Klassen

* `PseudoClass`-Objekte für `maximized`, `fullscreen`, `active`, `inactive`
* Listener auf `stage.maximizedProperty`, `fullScreenProperty`, `focusedProperty`
* `pseudoClassStateChanged` auf `ChromePane` und `max-restore`-Button

### Aufgabe 3: CssMetaData

* fünf styleable Properties mit `StyleableProperty` und `CssMetaData`
* `getCssMetaData()` / `getClassCssMetaData()` überschreiben
* IP-02/IP-03-Konstanten durch diese Properties ersetzen

### Aufgabe 4: User-Agent-Stylesheet

* `getUserAgentStylesheet()` gibt Ressourcen-URL von `chrome.css` zurück
* Ressourcenordner und Datei anlegen

### Aufgabe 5: chrome.css

* Defaultwerte der styleable Properties
* generisches Button-Design mit `-fx-shape`-Symbolen für Minimieren/Maximieren/Restaurieren/Schließen
* `:hover`, `:pressed`, `.close:hover`, `:maximized`-Regeln

### Aufgabe 6: Override prüfen, Doku, Build

* Testfall: App-Stylesheet ändert Schattenfarbe und Button-Hintergrund
* `docs/docs/usage.md` Styling-Referenz (Klassen, Pseudo-Klassen, Properties) — `project-docs`-Regeln
* CHANGELOG-Eintrag; `./gradlew build`

## Fertig-Kriterien

* ohne App-Stylesheet vollständiger Default-Look inklusive Button-Symbolen
* alle dokumentierten Style- und Pseudo-Klassen wirken und sind überschreibbar
* styleable Properties steuern Schatten, Ecken, Resize-Rand und Caption-Höhe
* `build` grün
