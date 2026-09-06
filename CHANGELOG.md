# Changelog

All notable end-user visible changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Only changes an end user of the library can see or notice belong here.
Tests, refactorings, renamings, moved code, build and CI changes, changes to the
rules under `.claude` and changes to the documentation itself are intentionally
excluded.

## [UNRELEASED]

### Fixed

- `FXMenuPane` file-tab backstage docked in a `MenuChromePane` now closes on Escape and on a click
  outside its content again, not only by re-clicking the File button.
- `FXMenuPane` group strip is restored when the file-tab backstage closes (it stayed empty after the
  first open/close cycle).

### Added

- `FXMenuGroup` layout boxes and group overflow: wrap controls in `FXMenuGroupLargeBox` /
  `FXMenuGroupSmallBox` (both implement `FXMenuGroupBox` and carry an `FXMenuGroupBoxPriority` of
  `LOW` / `MEDIUM` / `HIGH`, default `MEDIUM`). Each group that holds boxes must name one as its
  `anchor` (a normal member of `content`, positioned by its index there) - the mandatory
  `FXMenuGroup(vararg content, anchor = …)` constructor, or `<anchor><fx:reference/></anchor>` from
  FXML. When the group strip cannot fit every group a strip-wide coordinator keeps each group at its
  preferred width (untouched groups do not change) and collapses whole non-anchor boxes into a
  per-group chevron popup lowest-priority first, then rightmost group, then rightmost box; the anchor
  and loose nodes always stay visible, so at least one component per group is always shown. Widening
  restores boxes in reverse order. `FXMenuGroup.isOverflowActive` / `overflowActiveProperty()` report
  a group's state; the chevron carries the `menu-group-overflow-button` style class.
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
