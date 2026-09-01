# Übersicht: FP-002-FXMenuTab

Feature Plan: `.claude/plans/features/FP-002-FXMenuTab.md`

## Implementierungspläne

| ID | Name | Datei |
|----|------|-------|
| IP-01 | MenuTabCore | FP-002-IP-01-MenuTabCore.md |
| IP-02 | ContextualTabs | FP-002-IP-02-ContextualTabs.md |
| IP-03 | TabStripScrolling | FP-002-IP-03-TabStripScrolling.md |
| IP-04 | FileMenuTab | FP-002-IP-04-FileMenuTab.md |
| IP-05 | BackstageOverlay | FP-002-IP-05-BackstageOverlay.md |
| IP-06 | Groups | FP-002-IP-06-Groups.md |
| IP-07 | GroupLayout | FP-002-IP-07-GroupLayout.md |
| IP-08 | GroupLauncher | FP-002-IP-08-GroupLauncher.md |
| IP-09 | GroupOverflow | FP-002-IP-09-GroupOverflow.md |
| IP-10 | DisabledState | FP-002-IP-10-DisabledState.md |
| IP-11 | ChromeDocking | FP-002-IP-11-ChromeDocking.md |
| IP-12 | ChromeOverlayHook | FP-002-IP-12-ChromeOverlayHook.md |
| IP-13 | CollapseAndExpand | FP-002-IP-13-CollapseAndExpand.md |
| IP-14 | RibbonContextMenu | FP-002-IP-14-RibbonContextMenu.md |
| IP-15 | StylingAndCssApi | FP-002-IP-15-StylingAndCssApi.md |
| IP-16 | TestHarnessAndCoverage | FP-002-IP-16-TestHarnessAndCoverage.md |

## Reihenfolge

1. IP-01 (Fundament)
2. Parallel nach IP-01: IP-02, IP-03, IP-04, IP-06, IP-11
3. IP-05 nach IP-04
4. IP-07 nach IP-06; IP-08 nach IP-06 (parallel zu IP-07)
5. IP-09 nach IP-06 und IP-07
6. IP-10 nach IP-01 und IP-06
7. IP-12 nach IP-05 und IP-11
8. IP-13 nach IP-01 und IP-11
9. IP-14 nach IP-13
10. IP-15 nach IP-01 bis IP-14
11. IP-16 nach IP-15
