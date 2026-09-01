# Feature Status: Custom Window Chrome

Status: IN_PROGRESS

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | ChromeCoreAndStageIntegration | COMPLETED |
| IP-02 | WindowOperationsAndResize | COMPLETED |
| IP-03 | CaptionAreaAndContentSlots | NOT_STARTED |
| IP-04 | DragAndHitTestModel | NOT_STARTED |
| IP-05 | OsSpecificCaptionButtons | NOT_STARTED |
| IP-06 | CssStylingApiAndDefaultStylesheet | NOT_STARTED |
| IP-07 | TestHarnessAndCoverage | NOT_STARTED |

## Overall Progress

~29% (2 of 7 implementation plans completed)

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
