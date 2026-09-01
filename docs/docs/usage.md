# Usage

!!! note
    The public API is still under active development. This page will grow as
    controls are added.

## Overview

The ribbon pane arranges its content into tabs; each tab contains groups; each
group contains the actual action controls.

Detailed control reference and customization options will be documented here
once the corresponding API is stable.

## Custom window frame

`panelium-fx` can turn a JavaFX window into an undecorated, transparent window with its
own frame (`ChromePane`) around the actual content. There are three entry points:

- `PaneliumChrome.install(stage)` - converts an existing `Stage` before it is shown; the
  current scene root becomes the chrome's content.
- `PaneliumStage` - a `Stage` subclass that is already preconfigured with a `ChromePane`;
  set its `content` property to place your UI.
- `ChromePane` - the frame itself, for cases where you build the `Scene` and `Stage`
  manually.

All three entry points produce the same frame structure (shadow, border, caption
placeholder and content area) via the shared configuration routine. `PaneliumStage`
supports `initOwner`/`initModality` like any other `Stage`.

```kotlin
val stage = PaneliumStage()
stage.content = Label("Hello, PaneliumFX!")
stage.show()
```
