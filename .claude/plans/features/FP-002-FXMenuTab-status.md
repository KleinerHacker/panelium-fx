# Feature Status: FXMenuTab

Status: IN_PROGRESS

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | MenuTabCore | COMPLETED |
| IP-02 | ContextualTabs | COMPLETED |
| IP-03 | TabStripScrolling | COMPLETED |
| IP-04 | FileMenuTab | COMPLETED |
| IP-05 | BackstageOverlay | COMPLETED |
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

31%

## Notes

IP-01 (MenuTabCore) completed: `FXMenuTab` core built under the standalone root package
`org.pcsoft.framework.panelium.menutab` (not `chrome`, per explicit user request).

IP-02 (ContextualTabs) completed: implementation plan placed the new files directly under
`org.pcsoft.framework.panelium.menutab` (not `.../chrome/menutab`), consistent with the IP-01
package decision. `ContextTabGroup.color` stores a raw color string; actual color styling is
deferred to IP-15. The demo (`MenuTabShowcaseWindowController`) was also updated to showcase a
toggleable "Table Tools" contextual group.

IP-03 (TabStripScrolling) completed: `FXMenuTabView.fxml` wraps the tab-strip `HBox` in a
`ScrollPane` (`menu-tab-strip-scroll-pane`, both scrollbars hidden). `FXMenuTabView` redirects
vertical mouse-wheel delta into `ScrollPane.hvalue` and scrolls the active tab's button into view
on every activation (click, code, arrow keys) and on tab-list changes. No overflow menu, no
button shrinking, as scoped.

IP-04 (FileMenuTab) completed: the file tab is a dedicated `FXMenuTab.fileTab`
(`ObjectProperty<MenuTab?>`) slot rather than an `isFileTab` marker inside `tabs`. It is kept out
of `tabs`/`contextualTabs`/`visibleTabs` and rendered as a separate `menu-tab-strip-file-button`
`ToggleButton` pinned before the scrolling strip (new `tabStripRow` HBox in the FXML), so it never
scrolls and arrow-key navigation ignores it. `FXMenuTab.backstageContent`
(`ObjectProperty<Node?>`) is parked invisible/unmanaged in a `#backstageContentSlot` overlay
`StackPane`; no activation or overlay wiring yet (IP-05). IP-05's plan file was updated to trigger
backstage activation from this button / a `fileTabActive` flag instead of an `activeTab` selection.

IP-05 (BackstageOverlay) completed: the file-tab button toggles
`FXMenuTabViewModel.fileTabActive`; `FXMenuTab` exposes it as `isFileTabActive` /
`fileTabActiveProperty()`. A new `BackstageOverlayHost` interface (`showOverlay` / `hideOverlay`)
is the contract for IP-12; `FXMenuTab.overlayHost` is nullable and, when set, receives the panel
instead of the local slot (tracked via `FXMenuTabViewModel.hasOverlayHost`). Without a host, the
view fades `#backstageContentSlot` in/out over `Duration.seconds(0.3)` (`FadeTransition`). That
slot stays unmanaged and is hand-laid-out (`positionBackstageSlot`) below `tabStripRow`, so
opening it neither enlarges the ribbon band nor covers the pressed file-tab button.
Dismissal uses scene-level `KEY_PRESSED` (Escape) and `MOUSE_PRESSED` (outside-click) filters
installed only while active, plus strip-tab selection (click or arrow key) via a shared
`selectStripTab` helper. `activeTab` is left untouched while the backstage is open;
`onBackstageClosed` is the collapse-state restore hook for IP-13. The showcase's `backstageContent`
is now a real panel and its status label reflects the open backstage.
