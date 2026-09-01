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

The shadow inset around the frame is fixed at `12` pixels; leave that margin free of
opaque backgrounds on the root so the shadow stays visible.

!!! note
    Dedicated style classes for the individual frame parts (shadow, border, caption
    placeholder, content area) are still being finalized. Until then, style the
    `ChromePane` and its content nodes directly.
