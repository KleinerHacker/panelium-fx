# Feature Status: Custom Window Chrome

Status: IN_PROGRESS

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | ChromeCoreAndStageIntegration | COMPLETED |
| IP-02 | WindowOperationsAndResize | NOT_STARTED |
| IP-03 | CaptionAreaAndContentSlots | NOT_STARTED |
| IP-04 | DragAndHitTestModel | NOT_STARTED |
| IP-05 | OsSpecificCaptionButtons | NOT_STARTED |
| IP-06 | CssStylingApiAndDefaultStylesheet | NOT_STARTED |
| IP-07 | TestHarnessAndCoverage | NOT_STARTED |

## Overall Progress

~14% (1 of 7 implementation plans completed)

## Notes

IP-01 (ChromeCoreAndStageIntegration) completed: `ChromePane`, `PaneliumChrome.install`,
`PaneliumStage` and the internal `ChromeConfig` routine are in place; `./gradlew build`
is green.
