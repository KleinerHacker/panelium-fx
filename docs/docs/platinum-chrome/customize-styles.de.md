# Platinum Chrome - Styles anpassen

Der `ChromePane`-Rahmen liefert ein vollständiges Standard-Erscheinungsbild als
JavaFX-*User-Agent-Stylesheet* (`ChromePane.getUserAgentStylesheet()`), sodass ein
gerahmtes Fenster ohne eigenes Anwendungs-Stylesheet vollständig gestaltet ist. Jeder
Teil trägt stabile Style-Klassen, Pseudo-Klassen und styleable Properties; ein Stylesheet,
das der Host-`Scene` hinzugefügt wird, überschreibt die Standardwerte über die normale
CSS-Priorität.

## Ein Stylesheet einbinden

```kotlin
val stage = PaneliumStage()
stage.content = Label("Hallo, PaneliumFX!")
stage.scene.stylesheets.add(
    javaClass.getResource("/my-app/chrome.css").toExternalForm(),
)
stage.show()
```

Für `PaneliumChrome.install(stage)` und den manuellen `ChromePane`-Aufbau wird das
Stylesheet auf dieselbe Weise der selbst erstellten `Scene` hinzugefügt.

## Style-Klassen

| Style-Klasse | Knoten |
| --- | --- |
| `chrome-pane` | die Rahmen-Wurzel (`ChromePane`) |
| `chrome-caption-backdrop` | der Milchglas-Streifen hinter der Caption (siehe *Glas-Caption*) |
| `chrome-caption-bar` | die Titelleiste |
| `chrome-caption-left` | vorderer Titelleisten-Slot |
| `chrome-caption-center` | wachsender mittlerer Titelleisten-Slot |
| `chrome-caption-right` | hinterer Titelleisten-Slot |
| `chrome-caption-buttons` | der Container der Fensterschaltflächen (zusätzlich die OS-Klasse `windows`, `mac`, `linux` oder `other`) |
| `chrome-button` | eine einzelne Fensterschaltfläche (zusätzlich ihre Rollenklasse `minimize`, `max-restore` oder `close`) |
| `chrome-button-glyph-stroke` / `chrome-button-glyph-fill` | die Vektorformen in einer Fensterschaltfläche |

```css
.chrome-caption-buttons.windows .chrome-button.close:hover {
    -fx-background-color: #b71c1c;
}
```

## Pseudo-Klassen

`chrome-pane` spiegelt den Fensterzustand wider:

| Pseudo-Klasse | Aktiv während |
| --- | --- |
| `:maximized` | das Fenster maximiert ist |
| `:fullscreen` | das Fenster im Vollbild ist |
| `:active` | das Fenster den Fokus besitzt |
| `:inactive` | das Fenster nicht fokussiert ist |

Die `max-restore`-Schaltfläche trägt ebenfalls `:maximized`, solange das Fenster maximiert ist.

```css
.chrome-pane:inactive .chrome-caption-bar {
    -fx-opacity: 0.6;
}
```

## Styleable Properties

Gesetzt auf dem Selektor `chrome-pane`. **Jede Farbe ist ein Paint** - ein
`linear-gradient` funktioniert überall dort, wo auch eine einfache Farbe erlaubt ist.

| Property | Typ | Standard | Wirkung |
| --- | --- | --- | --- |
| `-panelium-surface-color` | Paint | `white` | Füllung der Fensterfläche |
| `-panelium-corner-radius` | Größe | `8` | Eckenradius von Fläche und Rahmen |
| `-panelium-border-mode` | `flat` \| `raised` \| `sunken` | `flat` | flacher Strich oder aufgesetzte / eingelassene Fase |
| `-panelium-border-color` | Paint | `rgba(0,0,0,0.25)` | Strichfarbe im Modus `flat` |
| `-panelium-border-light-color` | Paint | `rgba(255,255,255,0.9)` | helle Kante der Fase (oben / links bei `raised`) |
| `-panelium-border-dark-color` | Paint | `rgba(0,0,0,0.35)` | dunkle Kante der Fase (unten / rechts bei `raised`) |
| `-panelium-border-width` | Größe | `1` | Randdicke |
| `-panelium-border-style` | `solid` \| `dashed` \| `dotted` | `solid` | Strichmuster des Rahmens |
| `-panelium-border-line-cap` | `butt` \| `round` \| `square` | `butt` | Abschluss der Striche / Segmente |
| `-panelium-border-line-join` | `miter` \| `bevel` \| `round` | `miter` | Eckenverbindung des Strichs |
| `-panelium-border-miter-limit` | Größe | `10` | Gehrungsgrenze für `miter`-Verbindungen |
| `-panelium-border-dash-offset` | Größe | `0` | Phasenversatz des Strichmusters |
| `-panelium-shadow-radius` | Größe | `18` | Unschärferadius des eingebauten Schlagschattens |
| `-panelium-shadow-color` | Farbe | `rgba(0,0,0,0.45)` | Farbe des eingebauten Schlagschattens |
| `-panelium-effect` | Effekt | *(nicht gesetzt)* | beliebiges `dropshadow()` / `innershadow()` - ersetzt den eingebauten Schlagschatten, wenn gesetzt |
| `-panelium-shadow-inset` | Größe | `12` | transparenter Rand um den Rahmen, reserviert für den Effekt |
| `-panelium-caption-backdrop-blur` | Größe | `0` | Unschärferadius des Milchglas-Streifens hinter der Caption; `0` deaktiviert ihn |
| `-panelium-resize-border` | Größe | `6` | Breite der Greifzonen an Kanten/Ecken |
| `-panelium-caption-min-height` | Größe | `32` | Mindesthöhe der Titelleiste |

```css
.chrome-pane {
    -panelium-corner-radius: 14;
    -panelium-surface-color: linear-gradient(to bottom, #ffffff, #f2f6ff);
}
```

## Fläche, Rahmen-Stroke und Effekt

Der Rahmen-Stroke ist vollständig beschreibbar: `-panelium-border-mode` wählt flachen
Strich oder Fase, `-panelium-border-style` das Strichmuster und
`-panelium-border-line-cap` / `-panelium-border-line-join` /
`-panelium-border-miter-limit` / `-panelium-border-dash-offset` die Geometrie. Eine
Fase wirkt am besten mit kleinem oder null `-panelium-corner-radius`.

```css
.chrome-pane {
    -panelium-corner-radius: 3;
    -panelium-border-mode: raised;
    -panelium-border-width: 5;
    -panelium-border-light-color: linear-gradient(from 0% 0% to 100% 100%,
        #f2f6ff 0%, #bcd4ff 70%, #7cc4ff 100%);
    -panelium-border-dark-color: linear-gradient(from 0% 0% to 100% 100%,
        #1a63f0 0%, #0b3fb0 75%, #0b1220 100%);
}
```

`-panelium-shadow-radius` / `-panelium-shadow-color` speisen den eingebauten
Schlagschatten. Für alles andere `-panelium-effect` auf einen vollständigen
CSS-Effekt setzen - er ersetzt den eingebauten. Für große Unschärfe Platz über
`-panelium-shadow-inset` reservieren. Der Effekt entfällt automatisch, solange das
Fenster maximiert oder im Vollbild ist, oder wenn `ChromePane.isShadowEnabled`
`false` ist.

```css
.chrome-pane {
    -panelium-effect: dropshadow(gaussian, rgba(11, 18, 32, 0.62), 18, 0.0, 0, 5);
    -panelium-shadow-inset: 18;
}
```

Der Rand-Modus ist auch im Code über `ChromePane.borderMode` /
`borderModeProperty()` verfügbar (`ChromeBorderMode.FLAT`, `RAISED`, `SUNKEN`); der
Rest ist CSS-only.

## Glas-Caption (Backdrop-Blur)

`-panelium-caption-backdrop-blur` aktiviert einen Milchglas-Streifen: eine
verwischte Spiegelung des Inhalts direkt unter dem Caption-Band, gezeichnet hinter
der Titelleiste. Kombiniert mit einer **halbtransparenten** Caption-Füllung (ein
`rgba(...)`-Verlauf) wirkt die Caption wie eine Aero-Glasfläche. `0` (Standard)
deaktiviert alles - kein Snapshot, keine Ebene. Die Spiegelung wird bei
Layout-Änderungen aktualisiert, nicht pro Frame.

```css
.chrome-pane {
    -panelium-caption-backdrop-blur: 28;
}

.chrome-caption-bar {
    -fx-background-color: linear-gradient(to bottom,
        rgba(11, 18, 32, 0.55), rgba(16, 32, 63, 0.62));
    -fx-effect: innershadow(gaussian, rgba(11, 18, 32, 0.45), 10, 0, 0, 2);
}
```

## Die Caption ist reines JavaFX-CSS

Die Titelleiste, ihre drei Slots und die Fensterschaltflächen sind gewöhnliche
JavaFX-Knoten. Alles wird über ihre eigenen `-fx-*`-Properties gestaltet -
`-fx-background-color` (Paints und Verläufe), `-fx-border-color` /
`-fx-border-style`, `-fx-effect`, `-fx-background-radius`, und an den
Button-Glyphs (`chrome-button-glyph-stroke` / `-fill`) `-fx-stroke`,
`-fx-stroke-width`, `-fx-stroke-line-cap`, `-fx-stroke-dash-array` und `-fx-fill`.
Es wird keine Farbe im Code gesetzt, ein Anwendungs-Stylesheet besitzt ihr
Aussehen also vollständig.

## Vollständiges eigenes Theme

Die oben genannten Style-Klassen, Pseudo-Klassen und styleable Properties reichen aus, um
das gesamte Erscheinungsbild zu ersetzen. Ein einzelnes Anwendungs-Stylesheet kann jede
Regel der User-Agent-Standardwerte neu definieren - die Rahmenmaße, die Titelleiste und
alle vier OS-Varianten der Fensterschaltflächen (`windows`, `mac`, `linux`, `other`) -
sodass das Fenster auf jeder Plattform dieselbe Identität behält.

Die Demo liefert ein solches Stylesheet, `chrome-signature.css`, umschaltbar über die
Seite "Chrome options". Es leitet seine Palette vom Projektlogo ab und ergänzt eine
Verlaufsfläche, eine Verlaufs-Fase, einen dunkelblauen Effekt und eine Glas-Caption; es
dient als Vorlage für ein vollständiges Inhouse-Theme.

## OS-gesteuerte Rahmengeometrie

`ChromePane.captionOs` (siehe die Implementierungsseite) setzt außerdem die Defaults für
Eckenradius, Fläche, Rahmen, Effekt und Schatten-Inset, sodass die Fensterform der
gewählten Plattform folgt. Ein expliziter CSS-Wert für die passende
`-panelium-*`-Property auf dem Selektor `chrome-pane` hat Vorrang vor dem OS-Standardwert.

## Transparente Scene-Füllung

Der Rahmen setzt eine transparente Stage und Scene voraus, damit der Effekt außerhalb
des Rahmens gezeichnet werden kann. Wird die `Scene` selbst erstellt, muss die
transparente Füllung erhalten bleiben:

```kotlin
scene.fill = Color.TRANSPARENT
```

Der `-panelium-shadow-inset`-Rand um den Rahmen sollte frei von blickdichten
Hintergründen an der Wurzel bleiben, damit der Effekt sichtbar bleibt.

## Effekt deaktivieren

`ChromePane.isShadowEnabled = false` (bzw. Binden von `shadowEnabledProperty()`) setzen
für einen flachen Rahmen ohne Effekt und ohne äußere Einzüge. Der Effekt wird
außerdem automatisch unterdrückt, solange das Fenster maximiert oder im Vollbild ist.

## Komplexes Beispiel

Ein einzelnes Anwendungs-Stylesheet, das das gesamte Erscheinungsbild ersetzt:
Verlaufsfläche, aufgesetzte Verlaufs-Fase, eigener dunkelblauer Schlagschatten, eine
Glas-Caption im Aero-Stil, zustandsabhängige Caption-Deckkraft, umgestaltete
Fensterschaltflächen für die OS-Variante `windows` und angepasste Rahmenmaße. Es wird
der Host-`Scene` hinzugefügt wie unter *Ein Stylesheet einbinden* gezeigt.

![Vorschau des komplexen Beispiels](../assets/images/complex_example.png)

```css
/* ---- Rahmen: Fläche, Rand, Schatten, Maße ---- */
.chrome-pane {
    -panelium-corner-radius: 12;
    -panelium-caption-min-height: 40;
    -panelium-resize-border: 8;

    -panelium-surface-color: linear-gradient(to bottom, #ffffff 0%, #eef3ff 100%);

    -panelium-border-mode: raised;
    -panelium-border-width: 4;
    -panelium-border-light-color: linear-gradient(from 0% 0% to 100% 100%,
        #f2f6ff 0%, #bcd4ff 70%, #7cc4ff 100%);
    -panelium-border-dark-color: linear-gradient(from 0% 0% to 100% 100%,
        #1a63f0 0%, #0b3fb0 75%, #0b1220 100%);

    -panelium-effect: dropshadow(gaussian, rgba(11, 18, 32, 0.62), 22, 0.0, 0, 6);
    -panelium-shadow-inset: 22;

    /* Glas-Streifen im Aero-Stil hinter der Caption. */
    -panelium-caption-backdrop-blur: 28;
}

/* ---- Zustandsabhängig ---- */
.chrome-pane:inactive .chrome-caption-bar {
    -fx-opacity: 0.6;
}

.chrome-pane:maximized,
.chrome-pane:fullscreen {
    -panelium-corner-radius: 0;
}

/* ---- Titelleiste: halbtransparente Füllung, damit der Backdrop-Blur durchscheint ---- */
.chrome-caption-bar {
    -fx-background-color: linear-gradient(to bottom,
        rgba(11, 18, 32, 0.55), rgba(16, 32, 63, 0.62));
    -fx-effect: innershadow(gaussian, rgba(11, 18, 32, 0.45), 10, 0, 0, 2);
    -fx-padding: 0 8 0 10;
}

.chrome-caption-center .breadcrumb .label {
    -fx-text-fill: rgba(255, 255, 255, 0.85);
}

/* ---- Fensterschaltflächen (Windows-Variante) ---- */
.chrome-caption-buttons.windows .chrome-button {
    -fx-background-color: transparent;
    -fx-background-radius: 6;
}

.chrome-caption-buttons.windows .chrome-button:hover {
    -fx-background-color: rgba(255, 255, 255, 0.14);
}

.chrome-caption-buttons.windows .chrome-button.close:hover {
    -fx-background-color: #b71c1c;
}

.chrome-caption-buttons.windows .chrome-button .chrome-button-glyph-stroke {
    -fx-stroke: rgba(255, 255, 255, 0.9);
    -fx-stroke-width: 1.1;
    -fx-stroke-line-cap: round;
}

.chrome-caption-buttons.windows .chrome-button.max-restore:maximized
        .chrome-button-glyph-stroke {
    -fx-stroke: #7cc4ff;
}
```
