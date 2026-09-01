# Feature Plan: Custom Window Chrome

## 1. Objective

Replace the operating system window frame with an own, fully CSS-stylable frame. An undecorated
(transparent) window is used and a custom caption area plus OS-appropriate window buttons are
provided. The caption area is a composable container in the scene graph so later components (the
ribbon) can inject their own controls into the frame, as modern desktop applications do. Everything
is implemented with plain JavaFX means only. The API must be hard to misuse and intuitive.

## 2. Current State

Single Gradle module `panelium` (Kotlin, JavaFX 25, `javafx.controls`, `explicitApi()`), group
`org.pcsoft.framework`, no `src/` yet. No existing window, control or styling infrastructure. The
feature establishes the first production code and its architecture.

## 3. Target State

* A `ChromePane` acts as the `Scene` root and draws the frame: caption area, borders, optional
  drop shadow, rounded corners.
* Three entry points that all produce the identical configuration: `ChromePane` directly,
  `install(stage)` for an existing stage (including the primary stage), and a `PaneliumStage`
  subclass for secondary windows.
* Window operations (move, edge/corner resize, minimize, maximize/restore) work on the undecorated
  window; shadow and outer insets are dropped while maximized.
* One generic, OS-independent frame appearance with no OS-version differences; only broad caption
  button placement differs per OS: Windows right / square, macOS traffic lights left, Linux
  right-aligned (GNOME/Adwaita-oriented fallback). Default look is overridable via CSS.
* Caption area exposes left / center / right content slots and a public API to insert nodes;
  window title and icon are shown by default (bound to `Stage.title` / `Stage.icons`) and can be
  removed by the application.
* `Stage.resizable`, size constraints and `Stage.fullScreen` are honoured; the custom frame also
  applies to modal and owned windows.
* Drag vs. interactive hit-testing: caption background drags the window, interactive controls pass
  through, explicit drag-handle marker for filled strips.
* Style classes and pseudo-classes following JavaFX conventions; a default stylesheet is applied
  automatically via `getUserAgentStylesheet()`.
* FXML usable: `<ChromePane>` as root element and the `<fx:root>` pattern.

## 4. Requirements

### Functional Requirements

* Undecorated, transparent window with custom caption area and frame.
* Move window by dragging the caption background; double-click on caption toggles maximize.
* Resize on all four edges and corners.
* Minimize, maximize/restore; maximize respects the screen work area on multi-monitor setups.
* Honour `Stage.resizable` and the `minWidth`/`minHeight`/`maxWidth`/`maxHeight` constraints.
* `Stage.fullScreen`: caption area hidden while full screen, restored on exit.
* Custom frame also applies to modal and owned windows via `PaneliumStage`.
* OS-oriented caption button set and placement; buttons trigger the matching window operation.
* Caption area with left / center / right slots and a public API for inserting arbitrary nodes.
* Window title text and icon shown by default, bound to the `Stage` properties, removable by the
  application.
* Interactive controls in the caption area do not drag the window; explicit drag-handle opt-in.
* Native-style context / system menu on the caption drag zone.
* Default appearance provided automatically; every part restylable via CSS.
* Works on Windows, macOS and Linux.

### Technical Requirements

* Plain JavaFX only for the runtime - no native code, no JNI/JNA, no additional runtime
  dependencies.
* Test dependencies approved: TestFX and OpenJFX Monocle (test scope only) for headless UI tests.
* Kotlin, Gradle, `explicitApi()`; public API of the ribbon-facing hook kept stable.
* Code in module `panelium`, package `org.pcsoft.framework.panelium.chrome`.
* One generic OS-independent appearance; no differentiation between OS versions (e.g. no
  Windows 10 vs 11 distinction). Only broad per-OS caption button placement.
* Follow JavaFX control conventions: `Control` + `Skin` where a skin is warranted, styleable
  properties via `CssMetaData`, style classes without vendor collisions, pseudo-classes for state.
* Optional drop shadow and rounded corners via `StageStyle.TRANSPARENT` + effect on an inset inner
  container; hit-testing accounts for the transparent shadow insets. A shadowless flat frame is a
  valid outcome where a shadow cannot be rendered cleanly.
* No behavioural regressions for the primary stage from `Application.start()`.
* Documentation (README / MkDocs / CHANGELOG per the `project-docs` rules) and any CI-pipeline
  adjustment for the touched area are part of each implementation plan; the headless-test CI setup
  belongs to IP-07.

## 5. Architecture

* **`ChromePane`** (module `panelium`, package `...panelium.chrome`) - root container / control. Owns
  an inner content container (with shadow insets), the caption area and the resize layer. Provides
  `getUserAgentStylesheet()`.
* **Caption area** - a sub-container with `left` / `center` / `right` slots, the OS caption button
  box and the default title/icon nodes. Public node-insertion API is the hook the future ribbon
  consumes; the ribbon itself is out of scope here.
* **Window operation service** - move, resize, resizable/size-constraint handling, minimize,
  maximize/restore, work-area handling, full-screen handling, shadow/inset toggling per window
  state. Consumed by caption buttons and the drag layer.
* **Hit-test / drag model** - decides drag vs. passthrough; attached/styleable marker for explicit
  drag handles; owns the system menu trigger.
* **OS caption buttons** - platform-selected button set, order and placement; default look is one
  generic style with per-OS placement only; wired to the window operation service.
* **Styling layer** - style classes for pane / caption / slots / buttons, pseudo-classes
  (`:maximized`, `:active` / `:inactive`, `:fullscreen`), styleable properties, default stylesheet.
* **Entry points** - `install(stage)` and `PaneliumStage` are thin facades that create the exact
  same `ChromePane` configuration; no extra logic. `PaneliumStage` also covers modal and owned
  windows.
* **Tests** - TestFX headless with OpenJFX Monocle.

## 6. Implementation Plan Overview

| ID    | Implementation Plan                | Objective                                                                 | Dependencies   |
| ----- | --------------------------------- | ------------------------------------------------------------------------- | -------------- |
| IP-01 | ChromeCoreAndStageIntegration (COMPLETED) | `ChromePane` root, transparent-stage wiring, `install()`, `PaneliumStage` (incl. modal/owned) | -      |
| IP-02 | WindowOperationsAndResize (COMPLETED) | Move, resize, size constraints, min/max/restore, full screen, optional shadow | IP-01       |
| IP-03 | CaptionAreaAndContentSlots (COMPLETED) | Caption container, left/center/right slots, node API, default title/icon, FXML | IP-01      |
| IP-04 | DragAndHitTestModel (COMPLETED)   | Drag vs. passthrough, drag-handle marker, system menu, double-click max   | IP-02, IP-03   |
| IP-05 | OsSpecificCaptionButtons          | Per-OS button placement, generic default look, wired to operations        | IP-02, IP-03   |
| IP-06 | CssStylingApiAndDefaultStylesheet | Style classes, pseudo-classes, styleable props, auto user-agent sheet     | IP-03, IP-05   |
| IP-07 | TestHarnessAndCoverage            | TestFX + Monocle headless tests, headless CI setup, entry-point equivalence | IP-01, IP-02, IP-03, IP-04, IP-05, IP-06 |

Each plan also carries the documentation and CI-pipeline changes for the area it touches.

## 7. Implementation Plans

### IP-01: ChromeCoreAndStageIntegration (COMPLETED)

**Objective**

Establish `ChromePane` as the scene root and the three equivalent entry points into one correct
configuration.

**Scope**

* In: `ChromePane` structural skeleton (inner content container with shadow insets, caption slot,
  resize layer placeholder), transparent stage setup, `install(stage)`, `PaneliumStage` including
  modal and owned windows.
* Out: actual resize/move logic, caption content API, buttons, styling.

**Dependencies**

`-` (independent, foundation).

**Interfaces to Other Plans**

Provides the container structure, the content region and the entry-point contract every other plan
builds on.

**Delivered**

`ChromePane` (Region), `PaneliumChrome.install(stage)` and `PaneliumStage` share the internal
`ChromeConfig.apply(stage, chromePane)` routine. Deviations from the original design: the public
content hook is a JavaFX bean property (`contentProperty()`) plus a Kotlin `var content` instead of
a bare `ObjectProperty`; the caption placeholder height is a private constant on `ChromePane`
rather than a constant on `ChromeConfig` (only the shadow inset lives there, as planned).

### IP-02: WindowOperationsAndResize (COMPLETED)

**Objective**

Make the undecorated window movable, resizable and min/max/restore-capable, honour size and
full-screen state, with an optional drop shadow and rounded corners drawn in JavaFX.

**Scope**

* In: edge/corner resize honouring shadow insets, `Stage.resizable` and min/max size constraints,
  minimize, maximize/restore, screen work-area and multi-monitor handling, `Stage.fullScreen`
  (hide/restore caption), optional shadow + rounded-corner rendering, dropping shadow/insets while
  maximized, shadowless fallback.
* Out: which UI triggers these (buttons, double-click) - only the operation service and resize
  layer.

**Dependencies**

IP-01.

**Interfaces to Other Plans**

Exposes the window operation service and window-state signals consumed by IP-04, IP-05, IP-06.

**Delivered**

`WindowOps` (internal) provides move, edge/corner resize (honouring `Stage.resizable` and the
min/max size constraints), minimize and manual maximize/restore via
`Screen.getScreensForRectangle(...).visualBounds`; `ResizeOverlay` (internal) hosts the eight
resize zones just inside the shadow insets and disables itself when the stage is not resizable,
maximized or full screen. `ChromePane` binds to the stage through `attachStage(stage)`, tracks
`maximizedProperty` / `fullScreenProperty`, drops the shadow, outer insets and rounded corners
while collapsed, and removes the caption placeholder from the layout while full screen.

Deviations from the plan:

* Docs went into `docs/docs/platinum-chrome/implementation.md` and `.../customize-styles.md`
  (section "Window operations" / "Disable the drop shadow"); the planned `docs/docs/usage.md`
  no longer exists after the MkDocs restructure.
* `ResizeEdge` lives in `WindowOps.kt`, not a separate file.
* `shadowEnabled` is exposed publicly as `ChromePane.isShadowEnabled` plus
  `shadowEnabledProperty()`; `WindowOps` stays internal, reachable via internal
  `ChromePane.windowOps`.
* Maximize also sets `Stage.isMaximized = true` for the state pseudo-signal, but
  `toggleMaximize` keys off `WindowOps`' own tracked restore bounds rather than
  `Stage.isMaximized`.
* Default rounded corners / border are a white surface background plus a 1&nbsp;px border on
  the inner frame box; full CSS control follows in IP-06.

### IP-03: CaptionAreaAndContentSlots (COMPLETED)

**Objective**

Turn the caption area into a composable container with public content slots - the hook the future
ribbon uses.

**Scope**

* In: left / center / right slots, public node-insertion API, default title and icon nodes shown by
  default and bound to `Stage.title` / `Stage.icons` (removable by the application), variable
  caption height, z-order vs. the button box, FXML support (`<ChromePane>` root and `<fx:root>`).
* Out: drag behaviour, caption buttons, styling specifics.

**Dependencies**

IP-01.

**Interfaces to Other Plans**

Provides the caption container and slot API consumed by IP-04, IP-05, IP-06 and later the ribbon.

**Delivered**

`ChromeCaptionBar` (public, `StackPane`) plus an MVVM-fx triple `ChromeCaptionBar` /
`ChromeCaptionBarView` (`FxmlView`, `<fx:root>`) / `ChromeCaptionBarViewModel` and a mirrored
`ChromePane` / `ChromePaneView` / `ChromePaneViewModel` triple. The bar has three
`ObservableList<Node>` slots (leading / growing center / trailing) with the reserved caption-button
slot (`ObjectProperty<Node?>`, filled by IP-05) stacked on top. The default title node binds to
`Stage.title`, the default icon node to the first image of `Stage.icons`; both toggle via
`isDefaultTitleVisible` / `isDefaultIconVisible`. `ChromePane` carries `@DefaultProperty("content")`
and exposes `captionBar` plus `captionLeftItems` / `captionCenterItems` / `captionRightItems`.
Caption drags route to `WindowOps` through the bar. `CAPTION_MIN_HEIGHT` moved onto `ChromeConfig`
as planned.

Deviations from the plan:

* MVVM-fx (`de.saxsys:mvvmfx:1.8.0`, `implementation` scope) is the required component pattern for
  this and later chrome components; `javafx.fxml` added to the JavaFX modules; `licensee` allows
  `eu.lestard:doc-annotations` (quoted MIT URL in its POM).
* `ChromeCaptionBar` uses `<fx:root>`; `ChromePane` keeps `: Region` with its own
  `layoutChildren()` inset math and loads `ChromePaneView` as an internal subtree (no `<fx:root>`),
  matching ai-ghost's `MainWindow` pattern - `Region` is not an FXML-populatable root.
* `ResizeOverlay` stays a direct code-built child of `ChromePane`, outside the FXML tree.
* The reserved caption-button slot is fixed to the top-right and the default title/icon to the
  leading slot; making both sides OS-dependent (macOS mirrored) is deferred to IP-05.
* Docs went into `docs/docs/platinum-chrome/implementation.md` (section "Content and caption
  slots"), not the removed `docs/docs/usage.md`.
* Tests remain deferred to IP-07; only the existing `ChromeCompileSmokeTest` is kept green.
* `processDemoResources` got `duplicatesStrategy = INCLUDE` because the demo source set names its
  convention resource dir a second time.

### IP-04: DragAndHitTestModel (COMPLETED)

**Objective**

Decide drag vs. interactive passthrough on a caption strip that may be fully populated.

**Scope**

* In: caption background as drag region, automatic passthrough for interactive controls, explicit
  drag-handle marker (attached/styleable property), double-click-to-maximize routing, system /
  context menu on the drag zone.
* Out: the maximize implementation itself (IP-02), button rendering (IP-05).

**Dependencies**

IP-02, IP-03.

**Interfaces to Other Plans**

Consumes the operation service (IP-02) and caption container (IP-03); defines the drag-handle
marker other components (ribbon) will set.

**Delivered**

Internal `CaptionHitTest` (picked-node-upwards resolution: first explicit flag wins, else
interactivity heuristic - `Control`, `isFocusTraversable` or a set mouse handler - else drag),
`CaptionDragHandler` (mouse-`PRESSED`/`DRAGGED` event filters on `ChromeCaptionBar`: drag zone
-> `WindowOps.startMove`/`moveTo`, primary double-click -> `toggleMaximize` only when
`stage.isResizable`, secondary click -> `WindowMenu`) and `WindowMenu` (a rebuilt-on-show
`ContextMenu` whose entries delegate to `WindowOps` and disable per window state). Public
attached property `ChromeCaptionBar.setDragRegion` / `getDragRegion` over `Node.getProperties()`
with `true` / `false` / `null`. The provisional IP-02 caption move binding
(`ChromeCaptionBar.onMoveStart` / `onMove` and the view's `MOUSE_PRESSED`/`DRAGGED` handlers)
was removed.

The default title / icon nodes are `isMouseTransparent`, so dragging on the title still
moves the window. `CaptionDragHandler` hides the `WindowMenu` on every caption press, so a
click on a slot closes it. `WindowMenu` is split with a `SeparatorMenuItem` before `Close`
and carries the host OS window shortcuts (`Alt+F4` on Windows/Linux, `Meta+W` / `Meta+M` on
macOS) via a private `os.name` check.

Deviations from the plan:

* Docs went into `docs/docs/platinum-chrome/implementation.md` (section "Drag regions and
  passthrough"); the planned `docs/docs/usage.md` no longer exists.
* `WindowMenu` lists `Move` and `Size` but keeps them permanently disabled - a one-shot
  `ContextMenu` cannot host their interactive drag loop and `WindowOps` has no one-shot
  equivalent.
* OS detection is a private `os.name` check inside `WindowMenu`; the shared `ChromeOs` enum
  from IP-05 can replace it later.

### IP-05: OsSpecificCaptionButtons

**Objective**

Provide the window button set with generic default styling and per-OS placement.

**Scope**

* In: min/max/close button set; placement per OS (Windows right, macOS left, Linux right /
  GNOME-Adwaita fallback); ordering rules; coexistence with injected slot content; wiring to the
  window operation service; one generic default look (no OS-version variants). IP-03 reserves the
  button slot on the right; this plan makes the button-slot side and the default title/icon side
  OS-dependent (macOS mirrors them: buttons left, title/icon right).
* Out: full CSS metadata and stylesheet (IP-06), drag logic (IP-04).

**Dependencies**

IP-02, IP-03.

**Interfaces to Other Plans**

Adds the button box into the caption container (IP-03) and drives the operation service (IP-02);
its nodes and states are styled by IP-06.

### IP-06: CssStylingApiAndDefaultStylesheet

**Objective**

Make every part of the frame stylable following JavaFX conventions and ship a default look that
applies automatically.

**Scope**

* In: style classes for pane / caption / slots / button box / buttons, pseudo-classes
  (`:maximized`, `:active` / `:inactive`, `:fullscreen`), styleable properties via `CssMetaData`,
  `getUserAgentStylesheet()` returning a bundled default stylesheet.
* Out: new behaviour - styling only.

**Dependencies**

IP-03, IP-05.

**Interfaces to Other Plans**

Depends on the node structure from IP-03 and IP-05; consumes window-state signals from IP-02 for
state pseudo-classes.

### IP-07: TestHarnessAndCoverage

**Objective**

Verify all behaviours headlessly, set up headless-UI CI and lock the entry-point equivalence.

**Scope**

* In: TestFX + OpenJFX Monocle headless setup and the corresponding CI change, tests for
  move/resize, size constraints, min/max/restore, work-area and full-screen handling, drag vs.
  passthrough, caption slot API, OS button actions, state pseudo-classes, and that
  `ChromePane` / `install()` / `PaneliumStage` yield the same configuration.
* Out: production behaviour changes.

**Dependencies**

IP-01, IP-02, IP-03, IP-04, IP-05, IP-06.

**Interfaces to Other Plans**

Consumes the public API and observable state of all other plans.

## 8. Dependency Graph

```text
IP-01 (COMPLETED)
├── IP-02 (COMPLETED)
│   ├── IP-04 (COMPLETED)
│   ├── IP-05
│   └── IP-06
└── IP-03 (COMPLETED)
    ├── IP-04 (COMPLETED)
    ├── IP-05
    └── IP-06
IP-06
└── IP-07   (also requires IP-01, IP-02, IP-03, IP-04, IP-05)
```

Parallelizable: IP-02 and IP-03 after IP-01; IP-04 and IP-05 after IP-02 + IP-03.

## 9. Risks and Open Questions

* OS snapping (Aero-Snap, Win11 snap layouts) is not reproduced. Resolved: accepted and intended -
  the frame is one generic, OS-independent surface with no OS-version differences. FX `maximize` is
  the only snapping-like behaviour.
* Transparent-window drop shadow behaves differently per OS. Resolved: the shadow is optional and
  may be omitted entirely (candidate: macOS); a shadowless flat frame is a valid v1 outcome.
* Linux caption button conventions vary by desktop environment. Resolved: fall back to the most
  widespread convention (right-aligned buttons, GNOME/Adwaita-oriented).
* Test approach resolved: TestFX + OpenJFX Monocle (test scope), approved by the maintainer.
* Per-pixel transparency and resize hit-testing over the shadow inset zone need careful tuning to
  avoid dead zones near the window edge.
* macOS traffic-light "zoom" semantics differ from maximize; the mapping is decided in IP-05.
* `Control` + `Skin` vs. plain `Region`: decided per component inside the implementation plans, not
  fixed here.

## 10. Feature Completion Criteria

* A JavaFX application can obtain a fully custom-framed window through any of the three entry points
  with identical results, including modal and owned windows.
* The window can be moved, resized on all edges/corners within its size constraints, minimized,
  maximized, restored and taken full screen; maximize respects the work area on every monitor;
  shadow and insets vanish while maximized; the caption hides in full screen.
* Caption buttons appear in the OS-appropriate place on Windows, macOS and Linux and perform their
  operation.
* An application can insert its own nodes into the caption slots and mark drag handles; interactive
  controls there do not drag the window; the default title and icon can be removed.
* Without any application stylesheet the frame has a complete default look; every documented style
  class and pseudo-class allows overriding it via CSS.
* Documentation and CI reflect the delivered state.
* The build passes with `build` and the headless test suite is green.
