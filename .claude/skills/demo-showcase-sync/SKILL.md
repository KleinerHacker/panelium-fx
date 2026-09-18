---
name: demo-showcase-sync
description: Keep the demo/showcase modules under src/demo in sync with production features. Load whenever a new component, window, public API member, or user-visible behaviour is added or changed in src/main - the matching showcase must demonstrate it in the same change set.
---

# Demo / Showcase sync rule

The runnable showcases under `src/demo` exist to demonstrate every production feature.
Adding or changing a feature in `src/main` without updating its showcase is a defect.

## Scope

Applies to every change in `src/main` that a user of the library could call or see:

* a new component or window class
* a new public property, method, factory, or FXML hook on an existing type
* a new or changed user-visible behaviour (new interaction, new visual state)

Pure internals (private helpers, refactors, renames with no API change) are exempt.

## The units

| Production area | Showcase counterpart |
| --- | --- |
| `org.pcsoft.framework.panelium.menupane.*` (`FXMenuPane`, `FXMenuTab`, `FXMenuContextTabGroup`, ...) | `src/demo/kotlin/org/pcsoft/framework/panelium/demo/MenuPaneShowcaseWindowController.kt` + `MenuPaneShowcaseApp.kt` |
| Panelium Chrome "Complex example" | governed by the `chrome-example-showcase` skill - load that instead |
| other Chrome demo surface | `ChromeDemoApp.kt` / `ChromeDemoWindowController.kt` / `ChromeDemoWindow.fxml` |
| logo rendering | `LogoShowcaseApp.kt` |

If a production area has no showcase counterpart yet, add one (runner + controller,
mirroring the nearest existing showcase) and extend this table in the same change.

## Rules

* Update the showcase in the SAME change set as the production feature.
* The showcase must actually exercise the new API - set the new property, call the new
  method, trigger the new behaviour - not just compile against it.
* If the feature is staged (visible API now, behaviour in a later plan), the showcase
  wires up what exists now and states the gap in a short on-screen or comment note.
* Keep every language / FXML / Kotlin variant of a showcase consistent with each other.
* After any change: `./gradlew build`. Run the affected showcase app for a visual check
  when practical.

## Plan integration

* An implementation plan that touches `src/main` lists the showcase file(s) under its
  "Betroffene Dateien" and carries a task for the showcase update.
* Verification of such a plan names starting the showcase app.
