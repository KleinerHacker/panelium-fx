# Feature Status: FXMenuPane

Status: IN_PROGRESS

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | MenuPaneCore | COMPLETED |
| IP-02 | ContextualTabs | COMPLETED |
| IP-03 | TabStripScrolling | COMPLETED |
| IP-04 | FileMenuTab | COMPLETED |
| IP-05 | BackstageOverlay | COMPLETED |
| IP-06 | Groups | COMPLETED |
| IP-07 | GroupLayout | NOT_STARTED |
| IP-08 | GroupLauncher | NOT_STARTED |
| IP-09 | GroupOverflow | NOT_STARTED |
| IP-10 | DisabledState | NOT_STARTED |
| IP-11 | ChromeDocking | COMPLETED |
| IP-12 | ChromeOverlayHook | COMPLETED |
| IP-13 | CollapseAndExpand | NOT_STARTED |
| IP-14 | RibbonContextMenu | NOT_STARTED |
| IP-15 | StylingAndCssApi | NOT_STARTED |
| IP-16 | TestHarnessAndCoverage | NOT_STARTED |

## Overall Progress

50%

## Notes

IP-01 (MenuPaneCore) completed: `FXMenuPane` core built under the standalone root package
`org.pcsoft.framework.panelium.menupane` (not `chrome`, per explicit user request).

IP-02 (ContextualTabs) completed: implementation plan placed the new files directly under
`org.pcsoft.framework.panelium.menupane` (not `.../chrome/menupane`), consistent with the IP-01
package decision. `FXMenuContextTabGroup.color` stores a raw color string; actual color styling is
deferred to IP-15. The demo (`MenuPaneShowcaseWindowController`) was also updated to showcase a
toggleable "Table Tools" contextual group.

IP-03 (TabStripScrolling) completed: `FXMenuPaneView.fxml` wraps the tab-strip `HBox` in a
`ScrollPane` (`menu-pane-strip-scroll-pane`, both scrollbars hidden). `FXMenuPaneView` redirects
vertical mouse-wheel delta into `ScrollPane.hvalue` and scrolls the active tab's button into view
on every activation (click, code, arrow keys) and on tab-list changes. No overflow menu, no
button shrinking, as scoped.

IP-04 (FileMenuTab) completed: the file tab is a dedicated `FXMenuPane.fileTab`
(`ObjectProperty<FXMenuTab?>`) slot rather than an `isFileTab` marker inside `tabs`. It is kept out
of `tabs`/`contextualTabs`/`visibleTabs` and rendered as a separate `menu-pane-strip-file-button`
`ToggleButton` pinned before the scrolling strip (new `tabStripRow` HBox in the FXML), so it never
scrolls and arrow-key navigation ignores it. `FXMenuPane.backstageContent`
(`ObjectProperty<Node?>`) is parked invisible/unmanaged in a `#backstageContentSlot` overlay
`StackPane`; no activation or overlay wiring yet (IP-05). IP-05's plan file was updated to trigger
backstage activation from this button / a `fileTabActive` flag instead of an `activeTab` selection.

IP-05 (BackstageOverlay) completed: the file-tab button toggles
`FXMenuPaneViewModel.fileTabActive`; `FXMenuPane` exposes it as `isFileTabActive` /
`fileTabActiveProperty()`. A new `BackstageOverlayHost` interface (`showOverlay` / `hideOverlay`)
is the contract for IP-12; `FXMenuPane.overlayHost` is nullable and, when set, receives the panel
instead of the local slot (tracked via `FXMenuPaneViewModel.hasOverlayHost`). Without a host, the
view fades `#backstageContentSlot` in/out over `Duration.seconds(0.3)` (`FadeTransition`). That
slot stays unmanaged and is hand-laid-out (`positionBackstageSlot`) below `tabStripRow`, so
opening it neither enlarges the ribbon band nor covers the pressed file-tab button.
Dismissal uses scene-level `KEY_PRESSED` (Escape) and `MOUSE_PRESSED` (outside-click) filters
installed only while active, plus strip-tab selection (click or arrow key) via a shared
`selectStripTab` helper. `activeTab` is left untouched while the backstage is open;
`onBackstageClosed` is the collapse-state restore hook for IP-13. The showcase's `backstageContent`
is now a real panel and its status label reflects the open backstage.

IP-11 (ChromeDocking) completed: the plan was reduced to a composition pattern on the user's
request - `ChromePane` gets NO API for the ribbon (no `menuPaneProperty`, no band slot). Docking is
`ChromePane.content = BorderPane(top = FXMenuPane, center = body)`, so no production code changed in
`src/main`. Delivered: "Docking" sections in the Platinum-Chrome and MenuPane implementation docs
(EN + DE); headless `ChromeDockingTest` (still covers the plain composition path). The dedicated
integration the user originally asked about landed in IP-12 as `MenuChromePane`; the showcase moved
there too.

IP-06 (Groups) completed: built under `org.pcsoft.framework.panelium.menupane` (not
`.../chrome/menupane` as the stub read). The group component is `FXMenuGroup` (MVVM-fx triple
`FXMenuGroup` / `FXMenuGroupView` / `FXMenuGroupViewModel` + FXML), named `FX…` to match
`FXMenuPane` per user request - the plan's original `MenuGroup` name was dropped. `FXMenuTab.groups`
(`ObservableList<FXMenuGroup>`) is the ordering API. `FXMenuPaneView.fxml` now wraps `tabStripRow`
plus a new `groupStrip` HBox (`menu-pane-group-strip`) in a `bandColumn` VBox; `backstageContentSlot`
stays a direct `root` child and `positionBackstageSlot()` now anchors to `bandColumn`. The group
strip renders the active regular tab's groups, follows live list edits while that tab is active,
swaps on tab change and clears while the backstage is open. `FXMenuGroupView.fxml` puts the title
label below the content HBox (ribbon convention). Showcase gives "Home"/"View" demo groups.

IP-12 (ChromeOverlayHook) completed: at the user's request the docking API + backstage overlay were
delivered together as a dedicated `ChromePane` subclass `MenuChromePane`
(`org.pcsoft.framework.panelium.chrome`), not by putting `BackstageOverlayHost` on the base
`ChromePane` and not by a parent/scene lookup. `MenuChromePane` (`open`, `@DefaultProperty("body")`)
holds an internal `BorderPane` as its frame `content`; `menuPane` docks into its `top` and gets
`overlayHost = this`, `body` sits in the `center` inside a `StackPane` shared with the
`.chrome-backstage-overlay` layer. `showOverlay` / `hideOverlay` fade that layer over the `body`
only - the caption bar and the docked ribbon (File button included) stay visible, so the backstage
stays dismissible (File button, Escape, outside click). Base `ChromePane` only became `open`. `FXMenuPaneView`'s outside-click filter now also
counts a click inside the reparented `backstageContent` as "inside". Showcase moved to
`MenuChromePane`; new headless `MenuChromePaneTest`. CHANGELOG updated (`MenuChromePane` is
end-user-visible API).
