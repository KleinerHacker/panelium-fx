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

### Fixed

- Nothing yet.
