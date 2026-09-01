# Platinum Chrome - Implementation

`panelium-fx` can turn a JavaFX window into an undecorated, transparent window with its
own frame (`ChromePane`) around the actual content: drop shadow, border, a caption
placeholder and the content area.

## Add the dependency

The artifacts are published to Maven Central under the group `org.pcsoft.framework`.

=== "Gradle (Kotlin DSL)"

    ```kotlin
    dependencies {
        implementation("org.pcsoft.framework:panelium:<version>")
    }
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>org.pcsoft.framework</groupId>
        <artifactId>panelium</artifactId>
        <version>VERSION</version>
    </dependency>
    ```

JavaFX (`javafx.controls`, version 25 or newer, Java 25 or newer) is exposed
transitively, so no additional JavaFX dependency is required for the public API types.

## Entry points

There are three entry points, all producing the same frame structure via a shared
configuration routine:

- `PaneliumChrome.install(stage)` - converts an existing `Stage` before it is shown; the
  current scene root becomes the chrome's content. Returns the created `ChromePane`.
- `PaneliumStage` - a `Stage` subclass that is already preconfigured with a `ChromePane`;
  set its `content` property to place your UI. Supports `initOwner`/`initModality` like
  any other `Stage`.
- `ChromePane` - the frame itself, for cases where you build the `Scene` and `Stage`
  manually.

!!! warning
    `PaneliumChrome.install(stage)` must be called before `Stage.show()`. It switches the
    stage to `StageStyle.TRANSPARENT` and installs a new transparent `Scene`.

## PaneliumStage

```kotlin
val stage = PaneliumStage()
stage.content = Label("Hello, PaneliumFX!")
stage.show()
```

## PaneliumChrome.install

```kotlin
val stage = Stage()
stage.scene = Scene(buildRoot())
val chrome = PaneliumChrome.install(stage)
stage.show()
```

## ChromePane

```kotlin
val chrome = ChromePane(Label("Hello, PaneliumFX!"))
val scene = Scene(chrome)
scene.fill = Color.TRANSPARENT

val stage = Stage()
stage.initStyle(StageStyle.TRANSPARENT)
stage.scene = scene
stage.show()
```

The content can be replaced at runtime through the `content` property (or
`contentProperty()` for binding).
