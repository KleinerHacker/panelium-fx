# Feature Status: Custom Window Chrome

Status: IN_PROGRESS

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | ChromeCoreAndStageIntegration | COMPLETED |
| IP-02 | WindowOperationsAndResize | COMPLETED |
| IP-03 | CaptionAreaAndContentSlots | COMPLETED |
| IP-04 | DragAndHitTestModel | COMPLETED |
| IP-05 | OsSpecificCaptionButtons | COMPLETED |
| IP-06 | CssStylingApiAndDefaultStylesheet | COMPLETED |
| IP-07 | TestHarnessAndCoverage | NOT_STARTED |

## Overall Progress

~86% (6 of 7 implementation plans completed)

## Notes

IP-01 (ChromeCoreAndStageIntegration) completed: `ChromePane`, `PaneliumChrome.install`,
`PaneliumStage` and the internal `ChromeConfig` routine are in place; `./gradlew build`
is green.

IP-02 (WindowOperationsAndResize) completed: internal `WindowOps` (move, edge/corner
resize with size constraints, minimize, manual maximize/restore) and `ResizeOverlay`
(eight resize zones, disabled when not resizable / maximized / full screen); `ChromePane`
binds to the stage via `attachStage`, drops shadow/insets/corners while maximized or full
screen and hides the caption placeholder in full screen; public
`ChromePane.isShadowEnabled`. Docs moved to the Platinum Chrome MkDocs pages instead of
the removed `usage.md`. `./gradlew build` is green.

IP-04 (DragAndHitTestModel) completed: internal `CaptionHitTest` (picked-node-upwards
resolution, explicit `dragRegion` flag over interactivity heuristic over default drag),
`CaptionDragHandler` (mouse event filters on `ChromeCaptionBar` for window move, primary
double-click maximize on resizable stages, secondary-click window menu) and `WindowMenu`
(rebuilt-on-show `ContextMenu` delegating to `WindowOps`). Public attached property
`ChromeCaptionBar.setDragRegion` / `getDragRegion`. Provisional IP-02 caption move binding
removed. `Move` / `Resize` menu entries stay disabled (no one-shot equivalent). Docs in
`platinum-chrome/implementation.md`. `./gradlew build` and `buildDocs` are green.

IP-03 (CaptionAreaAndContentSlots) completed: `ChromeCaptionBar` plus the MVVM-fx triples
(`de.saxsys:mvvmfx:1.8.0`, `implementation`) for the bar (`<fx:root>`) and `ChromePane`.
Three `ObservableList<Node>` caption slots, reserved caption-button slot on top, default
title / icon bound to `Stage.title` / `Stage.icons` and toggleable, `@DefaultProperty("content")`
on `ChromePane`, `javafx.fxml` added to the JavaFX modules, `CAPTION_MIN_HEIGHT` on
`ChromeConfig`. Docs in `platinum-chrome/implementation.md`. Tests stay in IP-07.
`./gradlew build` and `licensee` are green.

IP-05 (OsSpecificCaptionButtons) completed: public `ChromeOs` enum + `ChromePane.captionOs`
/ `captionOsProperty()` (default from `os.name` detection, overridable). Internal
`ChromeCaptionButtons : HBox` with minimize / max-restore / close buttons, per-OS order,
per-OS vector glyphs (`CaptionButtonSymbols`) and a bundled component stylesheet
`chrome-caption-buttons.css` giving a native look per OS (Windows Fluent flat buttons with
red close hover, macOS traffic lights with hover-only glyphs, GNOME/Adwaita round buttons).
`ChromeCaptionBar.installCaptionButtons(ops, stage)` (from `ChromePane.attachStage`) wires
the buttons to `WindowOps`, drives the `maximized` pseudo-class / glyph swap from
`stage.maximizedProperty` and disables max-restore while the stage is not resizable.
`ChromeCaptionBarView` places the button slot and the default icon/title on OS-dependent
sides (macOS mirrored). `WindowMenu` now uses `ChromeOs.detect()`. Docs in
`platinum-chrome/implementation.md` ("Window buttons") and `.../customize-styles.md`
("Caption buttons"). The scene-wide user-agent stylesheet and `CssMetaData` remain IP-06.
`./gradlew build` is green.

IP-06 (CssStylingApiAndDefaultStylesheet) completed: `ChromePane` gets the `chrome-pane`
style class, the `maximized` / `fullscreen` / `active` / `inactive` pseudo-classes
(focus-driven), `getCssMetaData()` / `getClassCssMetaData()` and five styleable
properties (`-panelium-shadow-radius`, `-panelium-shadow-color`, `-panelium-corner-radius`,
`-panelium-resize-border`, `-panelium-caption-min-height`) that replace the former
`ChromeConfig` constants. `getUserAgentStylesheet()` ships the bundled `chrome/chrome.css`
(which folds in the former `chrome-caption-buttons.css`, removed). Caption bar / slot style
classes `chrome-caption-bar` / `chrome-caption-left` / `-center` / `-right`. Override
verified by a metadata assertion in `ChromeCompileSmokeTest` and a demo toggle; docs in
`platinum-chrome/customize-styles.md`. `./gradlew build` and `buildDocs` are green.
