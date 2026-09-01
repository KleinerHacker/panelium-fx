# FP-001 Übersicht: Custom Window Chrome

Feature Plan: `.claude/plans/features/FP-001-CustomWindowChrome.md`

## Implementierungspläne

| ID | Name | Datei |
|----|------|-------|
| IP-01 | ChromeCoreAndStageIntegration (COMPLETED) | `FP-001-IP-01-ChromeCoreAndStageIntegration.md` (entfernt) |
| IP-02 | WindowOperationsAndResize (COMPLETED) | `FP-001-IP-02-WindowOperationsAndResize.md` (entfernt) |
| IP-03 | CaptionAreaAndContentSlots (COMPLETED) | `FP-001-IP-03-CaptionAreaAndContentSlots.md` (entfernt) |
| IP-04 | DragAndHitTestModel (COMPLETED) | `FP-001-IP-04-DragAndHitTestModel.md` (entfernt) |
| IP-05 | OsSpecificCaptionButtons (COMPLETED) | `FP-001-IP-05-OsSpecificCaptionButtons.md` (entfernt) |
| IP-06 | CssStylingApiAndDefaultStylesheet (COMPLETED) | `FP-001-IP-06-CssStylingApiAndDefaultStylesheet.md` (entfernt) |
| IP-07 | TestHarnessAndCoverage | `FP-001-IP-07-TestHarnessAndCoverage.md` |

## Reihenfolge und Voraussetzungen

1. IP-01 — Voraussetzungen: keine (COMPLETED)
2. IP-02 — Voraussetzungen: IP-01 (COMPLETED)
3. IP-03 — Voraussetzungen: IP-01 (COMPLETED)
4. IP-04 — Voraussetzungen: IP-02, IP-03 (COMPLETED)
5. IP-05 — Voraussetzungen: IP-02, IP-03 (COMPLETED)
6. IP-06 — Voraussetzungen: IP-03, IP-05 (COMPLETED)
7. IP-07 — Voraussetzungen: IP-01, IP-02, IP-03, IP-04, IP-05, IP-06
