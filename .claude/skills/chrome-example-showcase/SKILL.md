---
name: chrome-example-showcase
description: Keep the Platinum Chrome "Complex example" MkDocs sections and their runnable demo (ChromeExampleShowcaseApp / chrome-example-showcase.css) in sync. Load before editing any of these files - the two MkDocs "Complex example" sections, the showcase runner, its stylesheet, or the runChromeExampleShowcase task.
---

# Chrome Example Showcase - sync rule

The "Complex example" sections of the Platinum Chrome MkDocs pages and the demo that
runs them are ONE unit. Editing either side without the other is a defect.

## The unit

| Documentation | Demo counterpart |
| --- | --- |
| `docs/docs/platinum-chrome/implementation.md` + `implementation.de.md`, section "Complex example" | `src/demo/kotlin/org/pcsoft/framework/panelium/demo/ChromeExampleShowcaseApp.kt` |
| `docs/docs/platinum-chrome/customize-styles.md` + `customize-styles.de.md`, section "Complex example" | `src/demo/resources/org/pcsoft/framework/panelium/demo/chrome-example-showcase.css` |
| - | Gradle task `runChromeExampleShowcase` in `build.gradle.kts` |

The runner exists to produce the documentation preview images, so what it shows on
screen MUST be what the documentation snippet describes.

## Rules

* Sync in BOTH directions, in the SAME change:
    * Change a "Complex example" section -> update the runner / stylesheet.
    * Change the runner / stylesheet -> update the "Complex example" section.
* Every language variant of the page (`*.md` and `*.de.md`) and every code block in the
  section (Kotlin and FXML) MUST be updated together.
* The Kotlin body of the runner's `start(...)` MUST match the Kotlin snippet line for
  line, except for the demo-only scaffolding listed below.
* The CSS file MUST match the `customize-styles.md` CSS block rule for rule.
* After any change: `./gradlew build`, then `./gradlew runChromeExampleShowcase` for a
  visual check.

## Allowed divergence (demo-only scaffolding)

Only lines that are NOT the API being demonstrated. Each MUST carry a short
`// Demo-only: ...` comment in the runner and MUST NOT appear in the doc snippet:

* icon resource paths (`icons/app-*.png` instead of `/app/icon.png`)
* the `Application` subclass wrapper and the `main()` function
* the stylesheet / resource lookup paths (the demo resource name instead of `/app/...`)

Window `width` / `height` ARE part of the example (they keep the caption from being
squeezed in the preview) and MUST be present in both the runner and the doc snippet.

## Adding another "Complex example"

If a new "Complex example" is added to any MkDocs page, create its own runner +
stylesheet + Gradle task, extend the table above, and mirror this sync rule for it.
