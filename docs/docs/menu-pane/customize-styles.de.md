# MenuPane - Styles anpassen

`FXMenuPane` liefert über ein JavaFX-*User-Agent-Stylesheet*
(`FXMenuPane.getUserAgentStylesheet()`) einen vollständigen Standard-Look, sodass ein Ribbon ohne
Anwendungs-Stylesheet komplett gestylt ist. Jeder Teil trägt stabile Style-Klassen und
Pseudoklassen; ein Stylesheet, das an die Host-`Scene` angehängt wird, überschreibt die Vorgaben
über die normale CSS-Priorität.

## Ein Stylesheet anhängen

```kotlin
val menuPane = FXMenuPane()
val scene = Scene(menuPane)
scene.stylesheets.add(
    javaClass.getResource("/my-app/menu-pane.css").toExternalForm(),
)
```

In einem `MenuChromePane` angedockt, wird das Stylesheet auf dieselbe Weise an die `Scene` des
Chrome-Fensters angehängt.

## Style-Klassen

| Style-Klasse | Knoten |
| --- | --- |
| `menu-pane` | die Komponenten-Wurzel (`FXMenuPane`) |
| `menu-pane-strip` | die Tab-Streifenzeile mit den Tab-Buttons |
| `menu-pane-strip-scroll-pane` | der scrollende Viewport um die Tableiste |
| `menu-pane-strip-button` | ein einzelner Tab-Button |
| `menu-pane-strip-file-button` | der Datei-Tab-Button |
| `menu-pane-collapse-toggle` | der Chevron-Button zum Ein-/Ausklappen |
| `menu-pane-collapse-toggle-icon` | die Chevron-Icon-Region in diesem Button (`-fx-shape`; wechselt mit dem `:collapsed`-Zustand) |
| `menu-pane-context-group-header` | das Header-Label über einer kontextuellen Tab-Gruppe |
| `menu-pane-group-strip` | der Gruppenstreifen unter der Tab-Zeile |
| `menu-pane-group-strip-scroll-pane` | der scrollende Viewport um den Gruppenstreifen |
| `menu-pane-context-menu` | das Ribbon-Kontextmenü bei Rechtsklick |
| `menu-group` | eine einzelne Gruppe (`FXMenuGroup`) |
| `menu-group-content` | die Steuerelement-Zeile innerhalb einer Gruppe |
| `menu-group-title` | das Beschriftungs-Label unter einer Gruppe |
| `menu-group-launcher` | der Dialog-Launcher-Button in der Titelzeile einer Gruppe |
| `menu-group-launcher-icon` | die Pfeil-Icon-Region in diesem Button (`-fx-shape`) |
| `menu-group-overflow-button` | der Chevron-Button mit den eingeklappten Boxen einer Gruppe |
| `menu-group-large-box` / `menu-group-small-box` | die Ribbon-Layout-Boxen |

```css
.menu-pane-strip-button:hover {
    -fx-background-color: rgba(0, 0, 0, 0.08);
}
```

## Pseudoklassen

| Pseudoklasse | Knoten | Aktiv, solange |
| --- | --- | --- |
| `:active` | `menu-pane-strip-button` | der Tab der aktive Tab ist |
| `:contextual` | `menu-pane-strip-button` | der Tab in `contextualTabs` liegt |
| `:disabled` | `menu-pane-strip-button`, `menu-group` | der Tab / die Gruppe deaktiviert ist (Standard-JavaFX) |
| `:collapsed` | `menu-pane` | das Ribbon eingeklappt ist |

```css
.menu-pane:collapsed .menu-pane-group-strip {
    -fx-padding: 0;
}

.menu-pane-strip-button:active {
    -fx-font-weight: bold;
}
```

## Styleable Properties

Auf dem `menu-pane`-Selektor gesetzt. Jede Farbe ist ein Paint, ein `linear-gradient` funktioniert
also ebenfalls.

| Property | Typ | Default | Wirkung |
| --- | --- | --- | --- |
| `-panelium-menu-pane-accent-color` | paint | `#2b579a` | Akzent für kontextuelle Tab-Buttons, die nicht in einer gefärbten Gruppe liegen |

Die Property ist auch im Code als `FXMenuPane.accentColor` / `accentColorProperty()` verfügbar.

```css
.menu-pane {
    -panelium-menu-pane-accent-color: #b7472a;
}
```

## Farbe einer Kontextgruppe

`FXMenuContextTabGroup(name, color)` trägt eine gruppenspezifische Farbe. `color` ist ein beliebiger
von JavaFX als Farbe interpretierbarer Wert (`#rrggbb`, `rgb(...)`, ein benannter Farbwert); er wird
auf das Gruppen-Header-Label und auf den Akzent der Tab-Buttons dieser Gruppe angewendet und hat
Vorrang vor `-panelium-menu-pane-accent-color`. Ein von JavaFX nicht interpretierbarer Wert wird
ignoriert.

```kotlin
val tools = FXMenuContextTabGroup("Table Tools", "#b7472a")
menuPane.assignToGroup(designTab, tools)
```

## Theming (hell und dunkel)

Das Standard-Stylesheet ist ein helles Theme. Für ein dunkles Theme werden die `menu-pane*`- und
`menu-group*`-Regeln in einem Anwendungs-Stylesheet neu definiert - die obigen Style-Klassen und
Pseudoklassen reichen aus, um den gesamten Look zu ersetzen, nicht nur anzupassen.
