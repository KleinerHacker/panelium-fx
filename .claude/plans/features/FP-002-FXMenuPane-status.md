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
| IP-07 | GroupLayout | COMPLETED |
| IP-08 | GroupLauncher | COMPLETED |
| IP-09 | GroupOverflow | COMPLETED |
| IP-10 | DisabledState | COMPLETED |
| IP-11 | ChromeDocking | COMPLETED |
| IP-12 | ChromeOverlayHook | COMPLETED |
| IP-13 | CollapseAndExpand | COMPLETED |
| IP-14 | RibbonContextMenu | COMPLETED |
| IP-15 | StylingAndCssApi | NOT_STARTED |
| IP-16 | TestHarnessAndCoverage | NOT_STARTED |

## Overall Progress

88%

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

IP-07 (GroupLayout) completed: no enum / attached-property as the original stub read. Layout follows
the JavaFX pane model - two container classes under `org.pcsoft.framework.panelium.menupane` that go
into `FXMenuGroup.content`: `FXMenuGroupLargeBox` (a `StackPane` holding one prominent control,
`maxHeight = MAX_VALUE` so it spans the full group height, style class `menu-group-large-box`) and
`FXMenuGroupSmallBox` (a `VBox` stacking up to `MAX_CONTROLS = 3` controls, style class
`menu-group-small-box`). Columns are emergent: `FXMenuGroup` already arranges `content` horizontally,
so several small boxes side by side form the columns. The three-controls cap is hard in the vararg
constructor (`require` -> `IllegalArgumentException`); a later fourth child is reported via an
`IllegalStateException` routed to the FX thread's uncaught-exception handler, because JavaFX's
`ListListenerHelper` swallows list-listener exceptions rather than propagating them. `FXMenuGroupView`
/ its FXML were left untouched. Showcase, docs (EN + DE) and CHANGELOG updated.

Alongside IP-07, two pre-existing behavioural defects were fixed (see CHANGELOG "Fixed"): the
`MenuChromePane`-docked backstage now closes on Escape / outside click again (the scene-level filter
was skipped whenever an overlay host was set), and the group strip is restored after the backstage
closes (the `fileTabActive` listener only re-rendered on open).

IP-10 (DisabledState) completed: tabs keep the pre-existing `FXMenuTab.disabled` / `isDisabled`
property; IP-10 added the activation guards in `FXMenuPane.activate`, the `activeTab` setter and
`FXMenuPaneView.onKeyPressed` (arrow-key navigation now steps over disabled tabs, leaving the
active tab unchanged when every candidate is disabled). `FXMenuGroup` got NO new API: as a JavaFX
`Node` it uses the inherited `setDisable` / `disableProperty()`, which propagates the disabled
state to every node in `content`. No custom `disabled` pseudo-class was introduced - JavaFX's
built-in `:disabled` (set whenever `disable == true`) is the hook IP-15 will style. Showcase gained
a disabled "Disabled" tab and a disabled "Protected" group; `menu-pane/implementation.md` +
`.de.md` got a "Disabled state" section; CHANGELOG updated.

IP-09 (GroupOverflow) completed: overflow works per group and only on whole layout boxes
(`FXMenuGroupLargeBox` / `FXMenuGroupSmallBox`) - loose content nodes always stay visible. New
internal `MenuGroupOverflowController` (package `org.pcsoft.framework.panelium.menupane`, not
`chrome/menupane` as the stub read); `FXMenuGroupView` gained a `menu-group-overflow-button`
chevron and a `ContextMenu` holding the collapsed boxes in original order. The controller pins
`menu-group-content` `prefWidth` to the full desired width and `FXMenuGroup` is now
`HBox.hgrow=ALWAYS` + `maxWidth=USE_PREF_SIZE`, so the width the group strip grants is a faithful
overflow signal and a widening window pulls boxes back out. Public API: `FXMenuGroup.isOverflowActive`
/ `overflowActiveProperty()`. Showcase "Home" tab widened (Styles/Editing/Insert groups added);
docs (EN + DE) and CHANGELOG updated; headless `FXMenuGroupOverflowTest` added.

IP-09b (PriorityOverflow, COMPLETED, follow-up to IP-09): the group strip no longer shrinks all
groups evenly. New strip-wide `MenuGroupStripOverflowCoordinator` (owned by `FXMenuPaneView`);
each `FXMenuGroup` is pinned to `minWidth = maxWidth = USE_PREF_SIZE`, so an untouched group keeps
its exact width and control sizes. New `FXMenuGroupBoxPriority` (`LOW`/`MEDIUM`/`HIGH`/`ALWAYS`) +
`priority` on `FXMenuGroupLargeBox` / `FXMenuGroupSmallBox`; boxes collapse ascending priority ->
rightmost group -> rightmost box, `ALWAYS` never, widening restores in reverse. `groupStrip` is
wrapped in a `menu-pane-group-strip-scroll-pane` `ScrollPane`; when nothing more can be collapsed
the mouse wheel scrolls the overflowing strip horizontally. `MenuGroupOverflowController` reduced
to a per-group renderer/measurer. New `MenuGroupStripOverflowTest`; `FXMenuGroupOverflowTest`
reworked. Overall progress unchanged (no new IP row).

IP-09c (GroupAnchor + FXML showcase, COMPLETED, follow-up to IP-09b): a group holding layout
boxes now MUST name exactly one as its `anchor` (a normal member of `content`, positioned by its
index there) - mandatory `FXMenuGroup(vararg content, anchor: FXMenuGroupBox)` constructor, `var
anchor` / `anchorProperty()`, and `<anchor><fx:reference/></anchor>` from FXML; removing the anchor
from `content` throws. The anchor is never an overflow candidate, which replaces the previous
"never collapse the last box" cap. New `sealed interface FXMenuGroupBox` on both box types;
`FXMenuGroupBoxPriority` reduced to `LOW`/`MEDIUM`/`HIGH` (`ALWAYS` removed). `FXMenuTab` is now
FXML-instantiable: no-arg constructor, `id`/`title` mutable with defaults, `disabled` attribute
(the `BooleanProperty` accessor became `disabledProperty()`), groups via a `<groups>` element. The
MenuPane showcase moved wholesale into `MenuPaneShowcaseWindow.fxml`; the controller keeps only the
contextual-tab checkbox and the status-label binding. New `FXMenuGroupFxmlTest` +
`menu-pane-fxml-test.fxml`. Overall progress unchanged.

IP-13 (CollapseAndExpand) completed: built under `org.pcsoft.framework.panelium.menupane` (not
`chrome/menupane` as the stub read). Public API `FXMenuPane.isCollapsed` / `collapsedProperty()`;
state (`collapsed`, `peekActive`) in `FXMenuPaneViewModel`. The originally planned `Collapse
controller` was NOT kept as a class - it stayed a thin wrapper and was merged into `FXMenuPaneView`
(private `toggleCollapsed` / `startPeek` / `endPeek`, a `savedCollapsedForBackstage` field and the
`fileTabActive` listener), which already owns all the collapse scene wiring. The existing
`onBackstageClosed` host hook is untouched. Triggers in `FXMenuPaneView`: a `MOUSE_CLICKED`
`clickCount == 2` handler on the active tab button (calling `toggleCollapsed`) plus a new
`menu-pane-collapse-toggle` `ToggleButton` appended to `tabStripRow` in the FXML (chevron `⌃`/`⌄`)
kept in sync both ways with the inverse of `collapsed` (selected/"pinned" while the ribbon is shown,
released while collapsed).
Collapsing hides `groupStripScrollPane` (`visible` + `managed` = false) so the band shrinks; the
`collapsed` pseudo-class is toggled on `FXMenuPane`. While collapsed a single click on a tab starts a
transient peek (`selectStripTab`), re-clicking the peeking tab ends it, and a scene-level
`MOUSE_PRESSED` filter ends it on an outside click. Showcase gained a bound `collapsedLabel`; docs
(EN + DE) "Ribbon collapse/expand" section; CHANGELOG "Added" entry; new headless
`FXMenuPaneCollapseTest`.

IP-14 (RibbonContextMenu) completed: built under `org.pcsoft.framework.panelium.menupane` (not
`chrome/menupane` as the stub read). No `CollapseController` exists (IP-13 dissolved it into
`FXMenuPaneView`), so the new `RibbonContextMenu` binds to `FXMenuPaneViewModel.collapsed` and
`FXMenuPaneView.toggleCollapsed`. `RibbonContextMenu` is a `ContextMenu` with exactly one
`MenuItem` whose text mirrors `collapsed` (`Collapse Ribbon` / `Expand Ribbon`); activating it runs
the toggle and `hide()`. Style class `menu-pane-context-menu` on the menu for IP-15.
`FXMenuPaneView` registers one `ContextMenuEvent.CONTEXT_MENU_REQUESTED` handler on `tabStripRow`
and `groupStrip` that hides any open menu, then shows it at the cursor
(`show(root, screenX, screenY)`) and consumes the event. Showcase FXML gained a one-line hint
label (the menu needs no wiring). Docs (EN + DE) "Ribbon context menu" section; CHANGELOG "Added"
entry; new headless `FXMenuPaneContextMenuTest` (locates the shown menu via `Window.getWindows()`).

IP-08 (GroupLauncher) completed: `FXMenuGroup.onLauncherAction` / `onLauncherActionProperty()`
(`ObjectProperty<EventHandler<ActionEvent>?>`, following the JavaFX `onXxx` event convention and
FXML-settable as `onLauncherAction="#..."`) instead of a `Runnable` or a dedicated callback
interface - `null` means no launcher. `FXMenuGroupView.fxml` puts the `menu-group-title` label into
a new `titleRow` `HBox` with a trailing `launcherButton` (style class `menu-group-launcher`, glyph
`↘`); its `visible` / `managed` are bound to `onLauncherAction != null` and its `onAction` is bound
straight to the view model's handler. The showcase wires the "Font" group's launcher from
`MenuPaneShowcaseWindowController` to a `launcherLabel` counter. Styling stays for IP-15.
`MenuGroupStripOverflowTest` button-width filter now also excludes `menu-group-launcher`; new
headless `FXMenuGroupLauncherTest`. Docs (EN + DE) and CHANGELOG updated.
