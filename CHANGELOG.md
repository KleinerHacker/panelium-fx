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

- `FXMenuTab` (package `org.pcsoft.framework.panelium.menutab`): a tab-strip component with
  registered `MenuTab` entries, programmatic and click-driven activation, a disabled state per
  tab, and left/right arrow-key navigation with wrap-around.
- `FXMenuTab.contextualTabs`: a second, ordered list of `MenuTab` entries that render after the
  permanent tabs; removing the active contextual tab falls back to the previously active
  permanent tab.
- `ContextTabGroup` and `FXMenuTab.assignToGroup(tab, group)` / `groupOf(tab)`: group contextual
  tabs under a shared header rendered in the tab strip.
- `FXMenuTab` tab strip now scrolls horizontally via the mouse wheel when its tabs overflow the
  available width, and scrolls the active tab into view automatically.

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
