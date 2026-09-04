# Feature Plan: FXMenuTab

## 1. Objective

Provide `FXMenuTab`, a ribbon-style menu component for the application chrome: a strip of tabs,
each holding groups of action controls, with a dedicated first "file" tab that opens a full
backstage view instead of a group strip, and support for temporary contextual tabs that appear and
disappear based on application state.

## 2. Current State

`ChromePane` / `ChromeCaptionBar` (feature `CustomWindowChrome`, all IPs COMPLETED) provide the
undecorated window frame and a caption area with left/center/right content slots plus a reserved
button slot. These slots already give applications a place for a Quick Access Toolbar-style
control cluster, so `FXMenuTab` does not need its own QAT concept. No ribbon, tab or group
component exists yet. `docs/docs/menu-pane/*` are placeholder pages describing a future "MenuPane"
control (tabs -> groups -> action controls) that has not been implemented. Components follow the
MVVM-fx triple pattern established by `chrome/component` (`de.saxsys:mvvmfx`, `<fx:root>` FXML,
Component/View/ViewModel), documented in the `component` skill.

## 3. Target State

* `FXMenuTab` is the ribbon root control, docked as its own band directly below `ChromePane`'s
  caption bar and above the window content - a separate row, not sharing the caption bar's slots.
* The leftmost tab is a distinct "file" tab; activating it opens a full-window backstage overlay
  (its own content, covering ribbon and window content alike) instead of switching to a group row.
  The overlay closes via Escape, via a click outside its content, or via the application/tab
  switching back to a regular tab, always restoring the previously active regular tab and the
  ribbon's prior collapse state.
* Regular tabs hold an ordered set of groups; each group has a title and hosts arbitrary action
  controls, laid out through a set of standard group layout variants (large single control, small
  stacked controls, columns) so groups can present a mix of prominent and compact controls like a
  typical ribbon.
* Groups whose controls exceed the available width push the overflowing controls into a
  chevron-triggered overflow menu at the end of the group, rather than clipping or wrapping.
* A group can optionally show a launcher control that opens a dialog for the group's extended
  options.
* Tabs can be registered as temporary/contextual: shown only while relevant (e.g. an object is
  selected) and removed again afterwards, without disturbing the permanent tab order; contextual
  tabs can additionally be bundled into a named, colour-highlighted context group (e.g. "Table
  Tools"), matching the Office convention.
* When more tabs exist than fit the available width, the tab strip scrolls horizontally instead of
  shrinking or hiding tabs.
* Tabs and groups can be disabled, with a distinct visual (pseudo-class) state; controls inside a
  group are disabled individually through standard JavaFX means.
* Tab switching supports keyboard navigation: arrow keys move between tabs when the tab strip has
  focus, and standard JavaFX focus traversal (Tab key) reaches groups and their controls.
* The ribbon can be collapsed to just the tab strip and expanded again, triggered either by a
  double-click on the active tab or by a dedicated collapse/expand button; collapsing hides the
  group strip (and reserves a temporary "peek" show of the active tab's groups on tab click while
  collapsed, matching the Office pattern) without affecting the file-tab backstage.
* Right-clicking the ribbon (tab strip or group strip) opens a context menu offering the same
  minimize/expand toggle as the collapse/expand button, mirroring Office's ribbon context menu -
  without a "customize" or "add to Quick Access Toolbar" entry, since neither concept is in scope.
* The active tab, group layout, collapse state and file-tab backstage state persist correctly
  across resizing and content changes.
* Follows the existing chrome conventions: MVVM-fx component triple, `<fx:root>` FXML, style
  classes/pseudo-classes, `explicitApi()`, Kotlin/Gradle.

## 4. Requirements

### Functional Requirements

* Tab strip with an always-first, visually distinct file tab.
* File tab activation opens a full-window backstage overlay; it closes on Escape, on a click
  outside its content, or when the application/user switches back to a regular tab, restoring the
  previously active regular tab's group strip and the ribbon's prior collapse state.
* Regular tabs contain groups; groups contain arbitrary content controls.
* A group arranges its controls through standard layout variants (e.g. one large control spanning
  the group's height, several small controls stacked vertically, controls arranged in columns),
  selectable per control/group so applications can mix prominent and compact actions.
* A group can optionally show a launcher control that opens a dialog window/overlay for that
  group's extended settings.
* A group whose controls do not fit the available width collapses the overflow into a
  chevron/overflow menu at the group's trailing edge.
* Tabs (and their groups) can be added and removed at runtime; contextual tabs appear/disappear
  without reordering the permanent tabs, and can optionally be bundled into a named,
  colour-highlighted context group spanning several contextual tabs.
* When the tab strip is wider than the available space, it scrolls horizontally (mouse wheel and/or
  scroll buttons) instead of shrinking or overflowing tabs into a menu.
* Tabs and groups expose a disabled state with a distinct visual (pseudo-class); individual controls
  inside a group are disabled the standard JavaFX way, unaffected by this component-level state.
* Arrow-key navigation switches between tabs when the tab strip has focus; standard JavaFX focus
  traversal (Tab key) reaches into the active tab's groups and controls.
* Ribbon collapse/expand via double-click on the active tab and via an explicit toggle button; a
  single click on a tab while collapsed temporarily reveals its groups.
* Public API to select a tab programmatically, to observe the active tab, and to observe/toggle
  the collapse state.
* Right-click on the ribbon (tab strip or group strip) opens a context menu with a
  minimize/expand-ribbon toggle entry, wired to the same collapse/expand state as the explicit
  toggle button.
* Tooltips on group controls use standard JavaFX `Tooltip` - no dedicated ScreenTip API.
* No dedicated Quick Access Toolbar - `ChromeCaptionBar`'s existing slots cover that need.
* FXML usable via `<fx:root>`, consistent with the rest of the chrome components.

### Technical Requirements

* Kotlin, Gradle, `explicitApi()`, MVVM-fx component triple pattern (see `component` skill).
* Package `org.pcsoft.framework.panelium.chrome` (or a ribbon-specific sub-package, decided in
  IP-01) mirroring the existing chrome module layout.
* Docks as a dedicated band between `ChromePane`'s caption bar and its content area - not routed
  through the caption bar's left/center/right slots.
* The backstage overlay must be paintable above the entire `ChromePane` (ribbon and content), which
  requires a dedicated overlay layer/hook on `ChromePane` - designed in its own plan.
* No new runtime dependencies beyond what `CustomWindowChrome` already introduced, unless a plan
  identifies a concrete need - any new dependency requires asking the user first
  (`dependencies` rule).
* Reuses `ChromePane` / `ChromeCaptionBar` integration points rather than duplicating window
  chrome logic.
* Documentation (README / MkDocs / CHANGELOG per `project-docs`) and CI-pipeline adjustments
  (`ci-pipeline` skill) are part of each implementation plan, mirroring the `CustomWindowChrome`
  feature.

## 5. Architecture

* **`FXMenuTab`** - ribbon root control/container; owns the tab strip, the active tab's content
  area (group strip or backstage panel), the temporary-tab bookkeeping and the collapse state.
* **Tab model** - ordered permanent tabs plus a separate set of temporary/contextual tabs merged
  into the visible strip, optionally bundled into named/coloured context groups; tracks the active
  tab and file-tab state; supports a disabled flag per tab.
* **Tab strip viewport** - hosts the tab buttons and scrolls them horizontally once they exceed the
  available width; owns the arrow-key tab-switching behaviour.
* **File tab / backstage** - the distinguished first tab; its content is a full-window overlay
  panel supplied by the application, painted above both ribbon and content while active, dismissed
  via Escape, outside click, or tab switch.
* **Group strip** - per-tab ordered groups; each group is a titled container of arbitrary nodes,
  arranged through a small set of standard layout variants (large / stacked small / columns), a
  disabled flag, plus an optional launcher button wired to open an application-supplied dialog, and
  an overflow chevron for controls that do not fit the available width.
* **Collapse controller** - tracks expanded/collapsed state, wires the double-click and toggle-
  button triggers, and the transient "peek" reveal while collapsed.
* **Ribbon context menu** - right-click menu on the ribbon surface offering the minimize/expand
  toggle, delegating to the collapse controller.
* **Chrome integration** - the dedicated band between `ChromePane`'s caption bar and its content
  area where `FXMenuTab` docks, plus the overlay hook the backstage panel paints into - each its
  own plan. The Quick Access Toolbar need is intentionally left to `ChromeCaptionBar`'s existing
  slots rather than a new mechanism.
* **Styling layer** - style classes for the root, tab strip, tabs (incl. file tab, contextual,
  context-group and disabled state), group strip, groups (incl. layout variants and disabled
  state), launcher buttons, overflow chevron/menu and the collapsed state; pseudo-classes for
  active/contextual/disabled/collapsed state; default stylesheet, following the `chrome.css`
  convention.
* **Tests** - TestFX headless, mirroring `CustomWindowChrome`'s `AbstractChromeUiTest` harness.

## 6. Implementation Plan Overview

| ID    | Implementation Plan   | Objective                                                                        | Dependencies   |
| ----- | ---------------------- | ----------------------------------------------------------------------------------- | -------------- |
| IP-01 | MenuTabCore             | `FXMenuTab` root skeleton, permanent tab registration, active-tab switching, arrow-key tab navigation | -              |
| IP-02 | ContextualTabs (COMPLETED) | Temporary/contextual tabs, incl. named/coloured context groups                    | IP-01          |
| IP-03 | TabStripScrolling (COMPLETED) | Horizontal scrolling of the tab strip once tabs exceed the available width     | IP-01          |
| IP-04 | FileMenuTab             | Distinguished first file tab: identification hook, backstage content slot            | IP-01          |
| IP-05 | BackstageOverlay        | File tab activation/deactivation, Escape/outside-click dismissal, overlay contract   | IP-04          |
| IP-06 | Groups                  | Group container within a tab: title, content hosting, ordering                       | IP-01          |
| IP-07 | GroupLayout             | Standard group layout variants (large / stacked small / columns) for controls        | IP-06          |
| IP-08 | GroupLauncher           | Optional per-group launcher button that opens an application dialog                  | IP-06          |
| IP-09 | GroupOverflow           | Chevron-triggered overflow menu for groups exceeding the available width             | IP-06, IP-07   |
| IP-10 | DisabledState           | Disabled state (with visual) for tabs and groups                                     | IP-01, IP-06   |
| IP-11 | ChromeDocking           | Dedicated band placement of `FXMenuTab` below `ChromePane`'s caption bar             | IP-01          |
| IP-12 | ChromeOverlayHook       | `ChromePane` overlay layer so the backstage paints above ribbon and content          | IP-05, IP-11   |
| IP-13 | CollapseAndExpand       | Ribbon collapse/expand: double-click, toggle button, transient peek                  | IP-01, IP-11   |
| IP-14 | RibbonContextMenu       | Right-click ribbon context menu with a minimize/expand toggle entry                  | IP-13          |
| IP-15 | StylingAndCssApi        | Style classes, pseudo-classes, styleable properties, default stylesheet              | IP-01..IP-14   |
| IP-16 | TestHarnessAndCoverage  | TestFX headless coverage for every plan above                                        | IP-01..IP-15   |

## 7. Implementation Plans

### IP-01: MenuTabCore

**Objective**

Establish `FXMenuTab` as the ribbon root with a working tab strip skeleton, permanent tab
registration/ordering, active-tab switching and arrow-key tab navigation.

**Scope**

* In: `FXMenuTab` component skeleton (MVVM-fx triple, `<fx:root>`), permanent tab registration and
  ordering, active-tab selection API and observable, arrow-key switching between tabs when the tab
  strip has focus.
* Out: contextual tabs (IP-02), tab-strip scrolling (IP-03), file tab/backstage (IP-04/IP-05),
  groups (IP-06), disabled state (IP-10), docking (IP-11), collapse/expand (IP-13), styling
  (IP-14).

**Dependencies**

`-` (independent, foundation).

**Interfaces to Other Plans**

Provides the tab model, the active-tab contract and the content-area placeholder every other plan
attaches to.

### IP-02: ContextualTabs (COMPLETED)

**Objective**

Let tabs be registered as temporary/contextual, shown only while relevant, without disturbing the
permanent tab order, and optionally bundled into a named, colour-highlighted context group.

**Scope**

* In: temporary/contextual tab add/remove API, merge/ordering rules against the permanent tabs from
  IP-01, active-tab handling when a contextual tab is removed while active, grouping several
  contextual tabs under a shared named/coloured context-group header (e.g. "Table Tools").
* Out: the actual colour styling/rendering of the context group (IP-14).

**Dependencies**

IP-01.

**Delivered**

Built directly under `org.pcsoft.framework.panelium.menutab` (not `.../chrome/menutab`),
consistent with the IP-01 package decision. `contextualTabs` is a second `ObservableList` on
`FXMenuTab`, merged with `tabs` into `FXMenuTabViewModel.visibleTabs` (permanent first, then
contextual). `ContextTabGroup(name, color)` groups contextual tabs; `color` is carried as a raw
string only - actual rendering stays deferred, as scoped. The demo
(`MenuTabShowcaseWindowController`) got a toggleable "Table Tools" contextual group.

**Interfaces to Other Plans**

Extends the tab model from IP-01; exposes the contextual-tab and context-group style hooks that
IP-14 styles.

### IP-03: TabStripScrolling (COMPLETED)

**Objective**

Keep every tab reachable when the tab strip is wider than the available space, by scrolling it
horizontally instead of shrinking or hiding tabs.

**Scope**

* In: horizontal scroll viewport for the tab strip, mouse-wheel and/or scroll-button interaction,
  keeping the active tab visible when it changes (incl. via the arrow-key navigation from IP-01).
* Out: any tab-overflow menu (explicitly rejected in favour of scrolling), styling (IP-14).

**Dependencies**

IP-01.

**Delivered**

Built as planned: the tab-strip `HBox` sits inside a `ScrollPane` (`menu-tab-strip-scroll-pane`)
with both scrollbars hidden. Vertical mouse-wheel delta is mapped onto `ScrollPane.hvalue`; a
scroll-button pair was not added, as wheel scrolling alone already satisfied the scope. The active
tab is scrolled into view on click, code activation, arrow-key navigation and tab-list changes
alike.

**Interfaces to Other Plans**

Wraps the tab strip from IP-01; no other plan depends on it beyond styling (IP-14) and tests
(IP-15).

### IP-04: FileMenuTab

**Objective**

Give the ribbon its distinguished first tab and the content slot its backstage panel will occupy.

**Scope**

* In: file-tab identification/marker within the tab model, its fixed first position, a backstage
  content slot API (set/get the application-supplied panel), without yet wiring show/hide behaviour.
* Out: activation/deactivation logic, dismissal and the overlay contract (IP-05), styling (IP-14).

**Dependencies**

IP-01.

**Interfaces to Other Plans**

Extends the tab model from IP-01 with the file-tab marker and content slot that IP-05 wires up.

### IP-05: BackstageOverlay

**Objective**

Wire the file tab's activation/deactivation to show and hide its backstage content, dismiss it via
Escape or an outside click, and define the overlay contract the host chrome must satisfy to paint
it above everything else.

**Scope**

* In: activation shows the backstage content, deactivation (via Escape, outside click, or switching
  to a regular tab) restores the previously active regular tab and its prior collapse state, the
  overlay content contract (what `FXMenuTab` hands the host for full-window painting).
* Out: the concrete `ChromePane` overlay layer that consumes this contract (IP-12).

**Dependencies**

IP-04.

**Interfaces to Other Plans**

Consumes the file-tab marker/content slot from IP-04; defines the overlay contract that IP-12
implements against `ChromePane`, and the collapse-state restore hook that IP-13 provides.

### IP-06: Groups

**Objective**

Let regular tabs host ordered groups of arbitrary action controls.

**Scope**

* In: group container (title + content host) within a tab, group ordering/registration API within
  a tab from IP-01.
* Out: layout variants (IP-07), dialog launcher (IP-08), overflow handling (IP-09), disabled state
  (IP-10), styling (IP-14).

**Dependencies**

IP-01.

**Interfaces to Other Plans**

Consumes the tab model/content-area placeholder from IP-01; provides the group node structure that
IP-07, IP-08, IP-09, IP-10 and IP-14 build on.

### IP-07: GroupLayout

**Objective**

Give a group's content host a small set of standard layout variants so controls can be arranged
like a real ribbon - not just a plain flow of same-sized controls.

**Scope**

* In: layout variants for arranging controls within a group (a large control spanning the group's
  full height, several small controls stacked vertically, controls arranged in columns), an API to
  pick a variant per control/sub-area, mixing variants within one group.
* Out: overflow handling once controls do not fit (IP-09), styling specifics (IP-14).

**Dependencies**

IP-06.

**Interfaces to Other Plans**

Extends the group node structure from IP-06 with the layout variants that IP-09 accounts for when
computing overflow and that IP-14 styles.

### IP-08: GroupLauncher

**Objective**

Add an optional per-group launcher button that opens an application-supplied dialog for the
group's extended options.

**Scope**

* In: launcher button slot on a group, visibility toggle (present only when configured), callback
  API invoked on activation.
* Out: the dialog itself (application-supplied, out of component scope), styling (IP-14).

**Dependencies**

IP-06.

**Interfaces to Other Plans**

Extends the group node structure from IP-06 with the launcher hook that IP-14 styles.

### IP-09: GroupOverflow

**Objective**

Keep a group usable when its controls exceed the available width by moving the overflow into a
chevron-triggered menu.

**Scope**

* In: width measurement/overflow detection for a group's content across the layout variants from
  IP-07, chevron control at the group's trailing edge, overflow menu listing the controls that no
  longer fit.
* Out: styling specifics (IP-14).

**Dependencies**

IP-06, IP-07.

**Interfaces to Other Plans**

Extends the group node structure from IP-06/IP-07 with the overflow chevron/menu that IP-14
styles.

### IP-10: DisabledState

**Objective**

Let tabs and groups be disabled as a whole, with a distinct visual state, independent of the
standard per-control disabling JavaFX already provides.

**Scope**

* In: disabled flag/API on a tab (IP-01's model) and on a group (IP-06's model), the resulting
  interaction behaviour (a disabled tab cannot become active; a disabled group's controls stop
  receiving input), the pseudo-class hook consumed by styling.
* Out: the visual styling itself (IP-14).

**Dependencies**

IP-01, IP-06.

**Interfaces to Other Plans**

Extends the tab model from IP-01 and the group model from IP-06 with the disabled flag and
pseudo-class hook that IP-14 styles.

### IP-11: ChromeDocking

**Objective**

Place `FXMenuTab` as a dedicated band directly below `ChromePane`'s caption bar, not routed through
the caption bar's slots.

**Scope**

* In: band placement between `ChromePane`'s caption bar and content area, sizing/layout interaction
  with the rest of the chrome, public API/entry point to attach a `FXMenuTab` to a `ChromePane`.
* Out: the backstage overlay hook (IP-12), collapse/expand (IP-13).

**Dependencies**

IP-01.

**Interfaces to Other Plans**

Provides the docked band placement that IP-12 and IP-13 build their behaviour on top of.

### IP-12: ChromeOverlayHook

**Objective**

Give `ChromePane` an overlay layer so the file tab's backstage panel (IP-05) can paint above both
the ribbon band and the window content.

**Scope**

* In: overlay layer/hook on `ChromePane` satisfying the contract from IP-05, wiring so
  `FXMenuTab`'s backstage activation (from the docked band, IP-11) shows/hides it correctly.
* Out: backstage content itself (IP-05), collapse/expand (IP-13).

**Dependencies**

IP-05, IP-11.

**Interfaces to Other Plans**

Implements the overlay contract from IP-05 against the docked band from IP-11; no other plan
depends on it.

### IP-13: CollapseAndExpand

**Objective**

Let the ribbon collapse to just the tab strip and expand again, via double-click, an explicit
toggle button, and a transient single-click "peek" while collapsed.

**Scope**

* In: collapse/expand state and API on `FXMenuTab`, double-click-on-active-tab trigger, explicit
  toggle button, transient peek reveal on single click while collapsed, restoring prior collapse
  state after the backstage (IP-05) closes.
* Out: styling of the collapsed state (IP-14).

**Dependencies**

IP-01, IP-11.

**Interfaces to Other Plans**

Consumes the tab model from IP-01 and the docked band from IP-11; provides the collapse-state
restore hook that IP-05 calls into and the collapsed-state style hooks that IP-15 styles.

### IP-14: RibbonContextMenu

**Objective**

Offer a right-click context menu on the ribbon surface with a minimize/expand toggle entry,
mirroring Office's ribbon context menu.

**Scope**

* In: context menu triggered by a right-click on the tab strip or the group strip, a single
  minimize/expand-ribbon entry delegating to the collapse controller from IP-13, label/state
  reflecting the current collapse state.
* Out: any "customize the ribbon" or "add to Quick Access Toolbar" entry (out of scope, no such
  concepts exist), styling of the menu itself beyond the platform default (IP-15).

**Dependencies**

IP-13.

**Interfaces to Other Plans**

Consumes the collapse/expand API from IP-13; no other plan depends on it beyond styling (IP-15)
and tests (IP-16).

### IP-15: StylingAndCssApi

**Objective**

Make every part of the ribbon stylable and ship a default look, following the `ChromePane` CSS
conventions.

**Scope**

* In: style classes for root/tab-strip/tab/file-tab/contextual-tab/context-group/group-strip/
  group/group-layout-variants/launcher/overflow-chevron/collapsed-state/disabled-state,
  pseudo-classes for active/contextual/disabled/collapsed state, styleable properties via
  `CssMetaData`, a bundled default stylesheet, colour styling for context groups (IP-02).
* Out: new behaviour - styling only.

**Dependencies**

IP-01, IP-02, IP-03, IP-04, IP-05, IP-06, IP-07, IP-08, IP-09, IP-10, IP-11, IP-12, IP-13, IP-14.

**Interfaces to Other Plans**

Depends on the node structure and state hooks delivered by every preceding plan; no other plan
depends on it.

### IP-16: TestHarnessAndCoverage

**Objective**

Verify tab switching, tab-strip scrolling, contextual tabs/context groups, backstage behaviour
(incl. dismissal), groups/layout/launchers/overflow, disabled state, docking, collapse/expand,
the ribbon context menu and styling headlessly, mirroring the `CustomWindowChrome` test harness.

**Scope**

* In: TestFX headless tests (reusing/extending `AbstractChromeUiTest` where practical) for all
  behaviours delivered by IP-01 through IP-15.
* Out: production behaviour changes.

**Dependencies**

IP-01, IP-02, IP-03, IP-04, IP-05, IP-06, IP-07, IP-08, IP-09, IP-10, IP-11, IP-12, IP-13, IP-14,
IP-15.

**Interfaces to Other Plans**

Consumes the public API and observable state of all other plans.

## 8. Dependency Graph

```text
IP-01
├── IP-02 (COMPLETED)
├── IP-03 (COMPLETED)
├── IP-04
│   └── IP-05
│       └── IP-12
├── IP-06
│   ├── IP-07
│   │   └── IP-09
│   ├── IP-08
│   ├── IP-09
│   └── IP-10
├── IP-10
├── IP-11
│   ├── IP-12
│   └── IP-13
│       └── IP-14
└── IP-13
    └── IP-14

IP-01..IP-14 ── IP-15 ── IP-16
```

Parallelizable after IP-01: IP-02, IP-03, IP-04, IP-06, IP-11 run independently of each other.
IP-07, IP-08 run in parallel after IP-06, with IP-09 following IP-07; IP-10 needs IP-01+IP-06;
IP-05 after IP-04; IP-13 after IP-01+IP-11; IP-12 after IP-05+IP-11; IP-14 after IP-13. IP-15 needs
every behavioural plan finished; IP-16 needs IP-15.

## 9. Risks and Open Questions

All previously open questions have been resolved with the user:

* Docking position: `FXMenuTab` is a dedicated band directly below `ChromePane`'s caption bar, not
  routed through the caption bar's slots. Settled in IP-11.
* Backstage rendering: a full-window overlay above both ribbon and content, matching Office's
  backstage view, dismissible via Escape or an outside click. Settled in IP-05/IP-12; the exact
  overlay-layer mechanism on `ChromePane` (new public overlay layer vs. reusing an existing
  structure) is a detailed design decision for IP-12.
* Collapse/expand trigger: double-click on the active tab plus an explicit toggle button, with a
  transient single-click "peek" reveal while collapsed. Settled in IP-13.
* Group overflow: a chevron-triggered overflow menu at the group's trailing edge, matching Office.
  Settled in IP-09.
* Tab overflow (too many tabs for the available width): horizontal scrolling of the tab strip, not
  a tab overflow menu. Settled in IP-03.
* Backstage dismissal: closes on Escape and on a click outside its content, in addition to
  switching back to a regular tab. Settled in IP-05.
* Disabled state: tabs and groups can be disabled as a whole with a dedicated visual state;
  individual controls keep using standard JavaFX disabling. Settled in IP-10.
* Keyboard navigation: arrow keys switch tabs when the tab strip has focus; standard JavaFX focus
  traversal reaches groups/controls - no dedicated Alt-key access-key system. Settled in IP-01.
* Quick Access Toolbar: not part of `FXMenuTab` - `ChromeCaptionBar`'s existing left/center/right
  slots already cover this need. Settled, no plan added.
* Group control sizing/layout: standard layout variants (large / stacked small / columns) are
  in scope, covering the general layouting question the user raised. Settled in IP-07.
* Contextual tab grouping: contextual tabs can be bundled into a named, colour-highlighted context
  group. Settled in IP-02 (behaviour) / IP-14 (colour styling).
* ScreenTips: standard JavaFX `Tooltip` on application-supplied controls is sufficient; no
  dedicated ScreenTip API. Settled, no plan added.
* Keyboard access keys (Alt-key tips, as in Office ribbons) remain out of scope - not requested by
  the user.
* Ribbon context menu: a right-click menu on the ribbon offers a minimize/expand toggle, mirroring
  Office; no "customize the ribbon" or "add to Quick Access Toolbar" entry, since neither concept
  exists in this component. Settled in IP-14.

## 10. Feature Completion Criteria

* `FXMenuTab` can be used standalone or docked into `ChromePane` (as the band below the caption bar)
  with tabs, groups and the file tab all functioning together.
* The file tab reliably opens and closes its full-window backstage overlay (via Escape, outside
  click or tab switch) without losing the previously active regular tab or its prior collapse
  state.
* Groups display arbitrary content through the standard layout variants, open their dialog launcher
  correctly where configured, and move overflowing controls into the chevron menu when the
  available width is too narrow.
* Temporary/contextual tabs can be added and removed at runtime without disturbing permanent tabs,
  and can be bundled into a coloured, named context group.
* The tab strip scrolls horizontally instead of hiding tabs when it exceeds the available width;
  arrow keys switch tabs when it has focus.
* Tabs and groups can be disabled with a distinct visual state, independent of individual control
  disabling.
* The ribbon collapses to the tab strip and expands again via double-click, the toggle button, the
  transient peek-on-click and the ribbon's right-click context menu, with a complete default look
  and every documented style class/pseudo-class overridable via CSS.
* Documentation and CI reflect the delivered state.
* The build passes with `build` and the headless test suite is green.
