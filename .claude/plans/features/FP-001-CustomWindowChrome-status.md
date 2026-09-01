# Feature Status: Custom Window Chrome

Status: IN_PROGRESS

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | ChromeCoreAndStageIntegration | COMPLETED |
| IP-02 | WindowOperationsAndResize | COMPLETED |
| IP-03 | CaptionAreaAndContentSlots | COMPLETED |
| IP-04 | DragAndHitTestModel | COMPLETED |
| IP-05 | OsSpecificCaptionButtons | NOT_STARTED |
| IP-06 | CssStylingApiAndDefaultStylesheet | NOT_STARTED |
| IP-07 | TestHarnessAndCoverage | NOT_STARTED |

## Overall Progress

~57% (4 of 7 implementation plans completed)

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
