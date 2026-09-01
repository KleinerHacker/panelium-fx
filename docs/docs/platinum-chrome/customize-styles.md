# Platinum Chrome - Customize Styles

The `ChromePane` frame ships a complete default look as a JavaFX *user-agent
stylesheet* (`ChromePane.getUserAgentStylesheet()`), so a framed window is fully
styled without any application stylesheet. Every part carries stable style classes,
pseudo-classes and styleable properties; a stylesheet you add to the hosting `Scene`
overrides the defaults through normal CSS precedence.

## Attach a stylesheet

```kotlin
val stage = PaneliumStage()
stage.content = Label("Hello, PaneliumFX!")
stage.scene.stylesheets.add(
    javaClass.getResource("/my-app/chrome.css").toExternalForm(),
)
stage.show()
```

For `PaneliumChrome.install(stage)` and the manual `ChromePane` setup, add the
stylesheet to the `Scene` you created in the same way.

## Style classes

| Style class | Node |
| --- | --- |
| `chrome-pane` | the frame root (`ChromePane`) |
| `chrome-caption-bar` | the caption bar |
| `chrome-caption-left` | leading caption slot |
| `chrome-caption-center` | growing center caption slot |
| `chrome-caption-right` | trailing caption slot |
| `chrome-caption-buttons` | the window-button container (plus the lower-case OS class `windows`, `mac`, `linux` or `other`) |
| `chrome-button` | a single window button (plus its role class `minimize`, `max-restore` or `close`) |

```css
.chrome-caption-buttons.windows .chrome-button.close:hover {
    -fx-background-color: #b71c1c;
}
```

## Pseudo-classes

`chrome-pane` reflects the window state:

| Pseudo-class | Active while |
| --- | --- |
| `:maximized` | the window is maximized |
| `:fullscreen` | the window is in full screen |
| `:active` | the window is the focused window |
| `:inactive` | the window is not focused |

The `max-restore` button also carries `:maximized` while the window is maximized.

```css
.chrome-pane:inactive .chrome-caption-bar {
    -fx-opacity: 0.6;
}
```

## Styleable properties

Set on the `chrome-pane` selector:

| Property | Type | Default | Effect |
| --- | --- | --- | --- |
| `-panelium-shadow-radius` | size | `18` | drop-shadow blur radius |
| `-panelium-shadow-color` | color | `rgba(0,0,0,0.45)` | drop-shadow colour |
| `-panelium-corner-radius` | size | `8` | rounded-corner radius of the surface and border |
| `-panelium-resize-border` | size | `6` | width of the edge/corner resize grab zones |
| `-panelium-caption-min-height` | size | `32` | minimum caption-bar height |

```css
.chrome-pane {
    -panelium-corner-radius: 14;
    -panelium-shadow-color: rgba(59, 130, 246, 0.5);
}
```

## Full custom theme

The style classes, pseudo-classes and styleable properties above are enough to
replace the whole look, not just tweak it. A single application stylesheet can
redefine every rule of the user-agent defaults - the frame metrics, the caption
bar and all four caption-button OS variants (`windows`, `mac`, `linux`, `other`) -
so the window keeps one identity on every platform.

The demo ships such a stylesheet, `chrome-signature.css`, toggled from the
"Chrome options" page. It derives its palette from the project logo and overrides
every overridable rule; use it as a template for a complete in-house theme.

## OS-driven frame geometry

`ChromePane.captionOs` (see the implementation page) also sets the corner radius,
drop-shadow radius and colour, border colour and whether a shadow is drawn, so the
window form follows the selected platform. Any explicit CSS value for the matching
`-panelium-*` property on the `chrome-pane` selector wins over the OS default.

## Transparent scene fill

The chrome relies on a transparent stage and scene so that the drop shadow can be
drawn outside the frame. When you build the `Scene` yourself, keep the transparent
fill:

```kotlin
scene.fill = Color.TRANSPARENT
```

The shadow inset around the frame is `12` pixels; leave that margin free of opaque
backgrounds on the root so the shadow stays visible.

## Disable the drop shadow

Set `ChromePane.isShadowEnabled = false` (or bind `shadowEnabledProperty()`) for a flat
frame without a shadow and without the outer insets. The shadow is also suppressed
automatically while the window is maximized or full screen.
