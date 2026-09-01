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
| `chrome-caption-bar` | die Titelleiste |
| `chrome-caption-left` | vorderer Titelleisten-Slot |
| `chrome-caption-center` | wachsender mittlerer Titelleisten-Slot |
| `chrome-caption-right` | hinterer Titelleisten-Slot |
| `chrome-caption-buttons` | der Container der Fensterschaltflächen (zusätzlich die kleingeschriebene OS-Klasse `windows`, `mac`, `linux` oder `other`) |
| `chrome-button` | eine einzelne Fensterschaltfläche (zusätzlich ihre Rollenklasse `minimize`, `max-restore` oder `close`) |

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

Gesetzt auf dem Selektor `chrome-pane`:

| Property | Typ | Standard | Wirkung |
| --- | --- | --- | --- |
| `-panelium-shadow-radius` | Größe | `18` | Unschärferadius des Schlagschattens |
| `-panelium-shadow-color` | Farbe | `rgba(0,0,0,0.45)` | Farbe des Schlagschattens |
| `-panelium-corner-radius` | Größe | `8` | Eckenradius von Oberfläche und Rahmenlinie |
| `-panelium-resize-border` | Größe | `6` | Breite der Greifzonen an Kanten/Ecken zur Größenänderung |
| `-panelium-caption-min-height` | Größe | `32` | Mindesthöhe der Titelleiste |

```css
.chrome-pane {
    -panelium-corner-radius: 14;
    -panelium-shadow-color: rgba(59, 130, 246, 0.5);
}
```

## Vollständiges eigenes Theme

Die oben genannten Style-Klassen, Pseudo-Klassen und styleable Properties reichen aus, um
das gesamte Erscheinungsbild zu ersetzen, nicht nur es anzupassen. Ein einzelnes
Anwendungs-Stylesheet kann jede Regel der User-Agent-Standardwerte neu definieren - die
Rahmenmaße, die Titelleiste und alle vier OS-Varianten der Fensterschaltflächen
(`windows`, `mac`, `linux`, `other`) - sodass das Fenster auf jeder Plattform dieselbe
Identität behält.

Die Demo liefert ein solches Stylesheet, `chrome-signature.css`, das über die Seite
"Chrome options" umschaltbar ist. Es leitet seine Farbpalette vom Projektlogo ab und
überschreibt jede überschreibbare Regel; es kann als Vorlage für ein vollständiges
Inhouse-Theme dienen.

## OS-gesteuerte Rahmengeometrie

`ChromePane.captionOs` (siehe die Implementierungsseite) setzt außerdem den Eckenradius,
den Unschärferadius und die Farbe des Schlagschattens, die Rahmenfarbe sowie, ob ein
Schatten gezeichnet wird, sodass die Fensterform der gewählten Plattform folgt. Ein
expliziter CSS-Wert für die passende `-panelium-*`-Property auf dem Selektor `chrome-pane`
hat Vorrang vor dem OS-Standardwert.

## Transparente Scene-Füllung

Der Rahmen setzt eine transparente Stage und Scene voraus, damit der Schlagschatten
außerhalb des Rahmens gezeichnet werden kann. Wird die `Scene` selbst erstellt, muss die
transparente Füllung erhalten bleiben:

```kotlin
scene.fill = Color.TRANSPARENT
```

Der Schatteneinzug um den Rahmen beträgt `12` Pixel; dieser Rand an der Wurzel sollte frei
von blickdichten Hintergründen bleiben, damit der Schatten sichtbar bleibt.

## Schlagschatten deaktivieren

`ChromePane.isShadowEnabled = false` (bzw. Binden von `shadowEnabledProperty()`) setzen
für einen flachen Rahmen ohne Schatten und ohne äußere Einzüge. Der Schatten wird
außerdem automatisch unterdrückt, solange das Fenster maximiert oder im Vollbild ist.
