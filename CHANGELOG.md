# Changelog

All notable end-user visible changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Only changes an end user of the library can see or notice belong here.
Tests, refactorings, renamings, moved code, build and CI changes, changes to the
rules under `.claude` and changes to the documentation itself are intentionally
excluded.

## [UNRELEASED]

### Added

- Window system menu (`Restore` / `Move` / `Size` / `Minimize` / `Maximize` / `Close`) and the
  `FXMenuPane` context menu (`Collapse` / `Expand`) are now localised via `Locale.getDefault()`.
  Bundled translations cover around 70 of the world's most widely used languages, with English as
  the base bundle and the fallback for any unknown locale.
- `FXMenuPane` context-menu entry now reads `Collapse` / `Expand` instead of `Collapse Ribbon` /
  `Expand Ribbon`.
- `FXMenuPane.isCollapseButtonVisible` / `collapseButtonVisibleProperty()`: show or hide only the
  collapse/expand chevron button. Defaults to `false`, so out of the box the ribbon is collapsed
  only by double-clicking the active tab or via the ribbon context menu; those gestures are
  unaffected. The chevron appears only while both `isCollapsible` and `isCollapseButtonVisible` are
  `true`.

### Fixed

- `ChromePane` now reports the framed content's preferred and minimum size (grown by the shadow
  inset) instead of `0`, so `Scene.sizeToScene()` and layouts that size a `ChromePane` by
  preference no longer collapse it.
- `ChromePane.attachStage` now pins an explicit `prefWidth` / `prefHeight` set on the pane (e.g.
  from FXML) onto a not-yet-sized stage as its initial window size. The window then keeps that size
  instead of following the framed content, so a docked, collapsible `FXMenuPane` no longer shrinks
  the window when its ribbon collapses (visible in the `FXMenuPane` showcase).
- `FXMenuPane` group strip now keeps a constant height across tab switches; the ribbon band no
  longer jumps when the active tab's groups differ in size. The fixed height can be overridden from
  a scene stylesheet via `-fx-pref-height` / `-fx-min-height` on `.menu-pane-group-strip`.
- `FXMenuPane` file-tab backstage docked in a `MenuChromePane` now closes on Escape and on a click
  outside its content again, not only by re-clicking the File button.
- `FXMenuPane` group strip is restored when the file-tab backstage closes (it stayed empty after the
  first open/close cycle).
- `FXMenuPane` tab strip keeps exactly one tab selected; clicking the already-active tab no longer
  clears the selection and leaves the strip with no active tab.
- `FXMenuPane` active tab no longer shows the platform toggle-button selection/focus highlight on
  top of the flat ribbon styling while the window is focused.
- `FXMenuPane` now hides the group strip while the file-tab backstage is open even when the ribbon
  is expanded, so the band collapses down to the tab-strip row instead of leaving an empty group
  strip band above the backstage.

### Changed

- `FXMenuPane` default look now leans on the platform theme (Modena): the bundled `menu-pane.css`
  takes its colours from the standard looked-up colours (`-fx-background`, `-fx-body-color`,
  `-fx-accent`, `-fx-text-base-color`, ...) instead of fixed Office-style values, so the ribbon
  follows the application's `-fx-base`. The tab strip now mirrors a JavaFX `TabPane` header:
  top-rounded tabs on a shared baseline, the active tab in the theme selection colour (`-fx-accent`)
  merging into the group strip below it. The `File` tab stays a solid accent button.
- `FXMenuPane` collapse/expand chevron is now hidden by default; opt in with
  `isCollapseButtonVisible = true` (double-click and the context menu still collapse the ribbon).

- `FXMenuPane` collapse/expand chevron and `FXMenuGroup` launcher button now draw their glyph from
  a scalable `-fx-shape` icon region (style classes `menu-pane-collapse-toggle-icon` and
  `menu-group-launcher-icon`) instead of a fixed text character, so both icons can be restyled from
  a scene stylesheet. The collapse chevron flips direction purely via the `collapsed` pseudo-class
  and stays centred when it switches.

### Added

- `FXMenuPane.isCollapsible` / `collapsibleProperty()`: switch ribbon collapsing off entirely.
  While `false` the ribbon is forced expanded (`isCollapsed = true` is ignored, an already collapsed
  ribbon expands at once), the collapse/expand chevron is hidden, the double-click-the-active-tab
  gesture is inert and the ribbon context menu does not open. Defaults to `true`.
- `FXMenuPane` CSS styling API: `getUserAgentStylesheet()` now returns a bundled default stylesheet
  (`menu-pane.css`), so the ribbon is fully styled without an application stylesheet. Tab-strip
  buttons carry the `contextual` pseudo-class while their tab is a contextual tab (alongside the
  existing `active` and the standard `disabled`). The new `-panelium-menu-pane-accent-color`
  styleable property (also `FXMenuPane.accentColor` / `accentColorProperty()`) sets the accent
  applied to contextual tabs, and an `FXMenuContextTabGroup`'s `color` is now applied to its header
  and tab-button accent.
- `FXMenuPane` ribbon context menu: right-clicking the tab-strip row or the group strip opens a
  one-entry context menu (style class `menu-pane-context-menu`) at the cursor that collapses or
  expands the ribbon; its label follows the collapse state (`Collapse Ribbon` / `Expand Ribbon`).
- `FXMenuPane.isCollapsed` / `collapsedProperty()`: collapse the ribbon down to just the tab-strip
  row and expand it again. The user toggles it by double-clicking the active tab or with the chevron
  button at the trailing edge of the tab-strip row (style class `menu-pane-collapse-toggle`). While
  collapsed, a single click on a tab reveals that tab's groups temporarily (a "peek") without
  expanding; the peek closes on a click outside the ribbon or by clicking the tab again. The collapse
  state is preserved across opening and closing the file-tab backstage. The `collapsed` pseudo-class
  is set on the component while collapsed.
- `FXMenuGroup.onLauncherAction` / `onLauncherActionProperty()`: set an `EventHandler<ActionEvent>`
  (JavaFX `onXxx` event convention, also FXML-settable) to show a launcher button (style class
  `menu-group-launcher`) in the group's title row, following the ribbon dialog launcher convention;
  clicking it fires an `ActionEvent`, `null` hides the button.
- `FXMenuGroup` layout boxes and group overflow: `FXMenuGroup.content` and `FXMenuGroup.anchor`
  accept only the ribbon layout boxes `FXMenuGroupLargeBox` / `FXMenuGroupSmallBox` (both extend the
  sealed `FXMenuGroupBox` base class, a JavaFX `Pane`, and carry an `FXMenuGroupBoxPriority` of
  `LOW` / `MEDIUM` / `HIGH`, default `MEDIUM`) - wrap every control in one of these boxes, a group
  hosts no loose controls. A large box stretches its control to fill the box in both directions; a
  small box stretches every row to the box width and pins each row to one third of the box height, so
  a box with one or two controls keeps ribbon-sized rows. All boxes in a group carry equal `HBox`
  weight and an unbounded max width, so the group's content row divides its width evenly across them.
  Each non-empty group must name one box as its `anchor` (a normal member of `content`, positioned by
  its index there) - the mandatory `FXMenuGroup(vararg content, anchor = …)` constructor, or
  `<anchor><fx:reference/></anchor>` from FXML. When the group strip cannot fit every group a
  strip-wide coordinator keeps each group at its preferred width (untouched groups do not change) and
  collapses whole non-anchor boxes into a per-group chevron popup lowest-priority first, then
  rightmost group, then rightmost box; the anchor always stays visible, so at least one component per
  group is always shown. Widening restores boxes in reverse order. `FXMenuGroup.isOverflowActive` /
  `overflowActiveProperty()` report a group's state; the chevron carries the
  `menu-group-overflow-button` style class.
- `FXMenuPane` group strip now scrolls horizontally via the mouse wheel when the groups overflow it
  even after every non-anchor box is collapsed (style class `menu-pane-group-strip-scroll-pane`).
- `FXMenuPane`, `FXMenuTab` and the group / layout-box types can now be built entirely from FXML
  (`FXMenuTab` via its no-arg constructor with `id` / `title` / `disabled` attributes and a
  `<groups>` element).
- `FXMenuPane` (package `org.pcsoft.framework.panelium.menupane`): a tab-strip component with
  registered `FXMenuTab` entries, programmatic and click-driven activation, a disabled state per
  tab, and left/right arrow-key navigation with wrap-around.
- `FXMenuPane.contextualTabs`: a second, ordered list of `FXMenuTab` entries that render after the
  permanent tabs; removing the active contextual tab falls back to the previously active
  permanent tab.
- `FXMenuContextTabGroup` and `FXMenuPane.assignToGroup(tab, group)` / `groupOf(tab)`: group contextual
  tabs under a shared header rendered in the tab strip.
- `FXMenuPane` tab strip now scrolls horizontally via the mouse wheel when its tabs overflow the
  available width, and scrolls the active tab into view automatically.
- `FXMenuPane.fileTab` / `backstageContent`: a distinguished first "File" tab held in its own slot
  (kept out of `tabs` and arrow-key navigation) and rendered as a separate button before the
  strip, plus the backstage panel it shows.
- `FXMenuPane` file-tab backstage: clicking the File button opens the backstage panel
  (`isFileTabActive` / `fileTabActiveProperty()`), fading it in over 0.3 seconds below the tab
  strip without resizing the ribbon band, and closes it again on Escape, on a click outside its
  content, or when a strip tab is selected, restoring the previously active tab. When the pane is
  docked in a `MenuChromePane` the panel is painted above the whole window instead, and
  `onBackstageClosed` fires once it has closed.
- `FXMenuGroup` and `FXMenuTab.groups`: regular tabs now host an ordered list of titled control
  groups. The active regular tab's groups render in a group strip below the tab-strip row, update
  live while that tab stays active, swap when the active tab changes, and clear while the file-tab
  backstage is open.
- `FXMenuGroupLargeBox` and `FXMenuGroupSmallBox` (package `org.pcsoft.framework.panelium.menupane`):
  layout boxes for `FXMenuGroup.content` modelled after the JavaFX panes. A large box holds one
  prominent control spanning the full group height; a small box stacks up to three small controls.
  Several boxes side by side form the columns of a group and the two types can be mixed.
- Disabled state for `FXMenuTab` and `FXMenuGroup`: a disabled tab can no longer be activated from
  code, via `activeTab`, or by arrow-key navigation, and disabling a group (through the inherited
  JavaFX `disable` state) disables all of its controls. Both carry the `:disabled` pseudo-class for
  styling.
- `MenuChromePane` (package `org.pcsoft.framework.panelium.chrome`): a `ChromePane` subclass for
  MenuPane windows that docks an `FXMenuPane` (`menuPane`) directly below the caption bar, hosts the
  rest of the window in `body`, and paints the docked tab's file-tab backstage as an overlay over
  the `body` while the docked tab stays visible.

## [0.2.0]

### Added

- Fully styleable custom frame: the window surface, border stroke and effect are now driven
  by CSS, and every colour is a paint so `linear-gradient` works everywhere.
  - `-panelium-surface-color` for the surface fill.
  - Border: `-panelium-border-mode` (`flat` / `raised` / `sunken`, also `ChromePane.borderMode`),
    `-panelium-border-color`, `-panelium-border-light-color` / `-panelium-border-dark-color`
    (bevel edges), `-panelium-border-width`, `-panelium-border-style`
    (`solid` / `dashed` / `dotted`), `-panelium-border-line-cap`,
    `-panelium-border-line-join`, `-panelium-border-miter-limit`,
    `-panelium-border-dash-offset`.
  - Effect: `-panelium-effect` (any `dropshadow()` / `innershadow()`, replaces the built-in
    drop shadow) and `-panelium-shadow-inset` for the outer gutter.
- Glass caption: `-panelium-caption-backdrop-blur` renders a frosted, blurred strip behind
  the caption bar for an Aero-style translucent title bar (`0` disables it).
- The caption bar, slots and window-button glyphs set no paint in code, so an application
  stylesheet fully controls their fill, stroke, gradients and effects.

## [0.1.0]

### Added

- Custom, undecorated window frame (`ChromePane`) with three entry points:
  `PaneliumChrome.install(stage)`, `PaneliumStage`, and direct use of `ChromePane`, usable
  from FXML including as an FXML root element.
- Full window management on the custom frame: move, resize, minimize, maximize/restore
  (respecting the current screen's work area on multi-monitor setups) and full-screen,
  with a drop shadow and rounded corners that adapt automatically to the window state.
- Composable, stylable caption bar: leading/center/trailing content slots, a default
  title and icon following `Stage.title` / `Stage.icons`, automatic per-OS window buttons
  with a native look on Windows, Linux and macOS, drag-to-move with per-node opt-out for
  interactive controls, double-click to maximize/restore, and a secondary-click window
  menu with the host OS's shortcuts.
- Full CSS styling API: a bundled user-agent stylesheet gives every frame a complete
  default look out of the box, overridable through normal scene stylesheet precedence,
  with dedicated style classes, pseudo-classes and styleable properties for shadow,
  corner radius, resize border and caption sizing.
