# Platinum Chrome - Customize Styles

The `ChromePane` frame is a regular JavaFX `Region`, so its appearance is customized
with standard JavaFX CSS applied to the scene that hosts the chrome.

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

## Caption buttons

The caption button set already carries stable style classes. The container has
`chrome-caption-buttons` plus the lower-case OS class (`windows`, `mac`, `linux` or
`other`); each button has `chrome-button` plus its role class (`minimize`, `max-restore`
or `close`), and `max-restore` gets the `maximized` pseudo-class while the window is
maximized. A stylesheet added to the scene overrides the bundled native look through
normal CSS precedence:

```css
.chrome-caption-buttons.windows .chrome-button.close:hover {
    -fx-background-color: #b71c1c;
}
```

!!! note
    Dedicated style classes for the remaining frame parts (shadow, border, caption
    placeholder, content area) and the styleable properties are still being finalized.
    Until then, style the `ChromePane` and its content nodes directly.
