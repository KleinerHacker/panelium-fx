# Feature Status: FXMenuTab

Status: IN_PROGRESS

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | MenuTabCore | COMPLETED |
| IP-02 | ContextualTabs | COMPLETED |
| IP-03 | TabStripScrolling | NOT_STARTED |
| IP-04 | FileMenuTab | NOT_STARTED |
| IP-05 | BackstageOverlay | NOT_STARTED |
| IP-06 | Groups | NOT_STARTED |
| IP-07 | GroupLayout | NOT_STARTED |
| IP-08 | GroupLauncher | NOT_STARTED |
| IP-09 | GroupOverflow | NOT_STARTED |
| IP-10 | DisabledState | NOT_STARTED |
| IP-11 | ChromeDocking | NOT_STARTED |
| IP-12 | ChromeOverlayHook | NOT_STARTED |
| IP-13 | CollapseAndExpand | NOT_STARTED |
| IP-14 | RibbonContextMenu | NOT_STARTED |
| IP-15 | StylingAndCssApi | NOT_STARTED |
| IP-16 | TestHarnessAndCoverage | NOT_STARTED |

## Overall Progress

13%

## Notes

IP-01 (MenuTabCore) completed: `FXMenuTab` core built under the standalone root package
`org.pcsoft.framework.panelium.menutab` (not `chrome`, per explicit user request).

IP-02 (ContextualTabs) completed: implementation plan placed the new files directly under
`org.pcsoft.framework.panelium.menutab` (not `.../chrome/menutab`), consistent with the IP-01
package decision. `ContextTabGroup.color` stores a raw color string; actual color styling is
deferred to IP-15. The demo (`MenuTabShowcaseWindowController`) was also updated to showcase a
toggleable "Table Tools" contextual group.
