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

- Custom, undecorated window frame (`ChromePane`) with three entry points:
  `PaneliumChrome.install(stage)`, `PaneliumStage`, and direct use of `ChromePane`.
- Window operations on the custom frame: move by dragging the caption area, resize on
  all edges and corners within the stage size constraints (disabled when the stage is
  not resizable), minimize, and maximize/restore to the current screen's work area on
  multi-monitor setups.
- Full-screen support: the caption area is hidden while full screen and restored on
  exit.
- Drop shadow with rounded corners on the frame, switchable via
  `ChromePane.isShadowEnabled` and dropped automatically while maximized or full
  screen.
- Composable caption bar: insert nodes into the leading, center and trailing slots
  (`ChromePane.captionLeftItems` / `captionCenterItems` / `captionRightItems`).
- Default caption title and icon that follow `Stage.title` and `Stage.icons` and can
  be switched off (`ChromePane.isDefaultTitleVisible` / `isDefaultIconVisible`).
- `ChromePane` and `ChromeCaptionBar` can be used from FXML, including as an FXML root
  element (`ChromePane` exposes `content` as its default property).
- Caption hit-testing: interactive controls placed in the caption slots no longer drag the
  window, while the caption background still does. Override per node with
  `ChromeCaptionBar.setDragRegion(node, true/false/null)`.
- Window buttons (minimize, maximize/restore, close) added to the caption automatically,
  wired to the frame's window operations, with per-OS placement and a native look: Windows
  and Linux keep them on the trailing edge (minimize, maximize, close), macOS mirrors the
  caption with traffic-light buttons on the leading edge (close, minimize, zoom) and the
  default title/icon on the trailing edge. The maximize button reflects the maximized
  state and is disabled while the stage is not resizable. Override the detected platform
  with `ChromePane.captionOs` / `captionOsProperty()`.
- Double-click on the caption drag zone toggles maximize/restore (resizable stages only).
- Secondary click on the caption drag zone opens a window menu (restore, move, size,
  minimize, maximize, close) split into sections and showing the host operating system's
  window shortcuts.
- Full CSS styling API: a bundled user-agent stylesheet gives every frame a complete
  default look with no application stylesheet, and a scene stylesheet overrides it
  through normal CSS precedence. Style classes `chrome-pane`, `chrome-caption-bar`,
  `chrome-caption-left` / `-center` / `-right`, `chrome-caption-buttons` and
  `chrome-button`; pseudo-classes `:maximized`, `:fullscreen`, `:active` and `:inactive`
  on `chrome-pane`; and the styleable properties `-panelium-shadow-radius`,
  `-panelium-shadow-color`, `-panelium-corner-radius`, `-panelium-resize-border` and
  `-panelium-caption-min-height`.

### Fixed

- Nothing yet.
