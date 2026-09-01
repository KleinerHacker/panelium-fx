# PaneliumFX

<p align="center">
  <img src="docs/docs/assets/images/logo.png" alt="PaneliumFX" width="320">
</p>

A JavaFX menu pane library with a custom window frame.

📖 **Documentation:** https://kleinerhacker.github.io/panelium-fx/latest/

## What it is

`panelium-fx` is a JavaFX UI library that provides a custom, undecorated window
frame (Platinum Chrome) and, in a later release, a MenuPane control that groups
actions into tabs and groups similar to the menu panes known from common office
applications. JavaFX is exposed transitively, so consumers do not need to
declare a separate JavaFX dependency for the public API types.

## AI disclosure

In accordance with EU transparency requirements, please note that this project -
including its source code, tests, documentation and configuration - was created
entirely with the assistance of artificial intelligence.

## Features

- Custom window frame (`ChromePane`, `PaneliumChrome.install`, `PaneliumStage`) for
  undecorated, transparent windows. See
  [Platinum Chrome docs](docs/docs/platinum-chrome/implementation.md).
- Window operations on the custom frame: move, edge/corner resize within the stage
  size constraints, minimize, maximize/restore, full screen, optional drop shadow.
- Composable caption bar with leading / center / trailing content slots, a default
  title and icon bound to the `Stage`, and drag vs. interactive hit-testing with a
  per-node drag-region override.
- OS-specific caption buttons: minimize / maximize-restore / close added
  automatically, wired to the window operations, with per-OS placement and a
  native-looking default style (Windows, macOS traffic lights, GNOME/Adwaita);
  override the detected platform with `ChromePane.captionOs`.
- Full CSS styling API for the frame: a bundled user-agent stylesheet for a complete
  default look, style classes and `:maximized` / `:fullscreen` / `:active` / `:inactive`
  pseudo-classes, and `-panelium-*` styleable properties for shadow, corners, resize
  border and caption height. See
  [Customize styles](docs/docs/platinum-chrome/customize-styles.md).
- MenuPane control arranging content into tabs, groups and action controls (planned).

## Requirements

- JDK 25 or newer
- JavaFX 25 (`javafx.controls`) - provided transitively by the library

## Check out, build and run

```bash
git clone https://github.com/KleinerHacker/panelium-fx.git
cd panelium-fx

# Build the library and run the tests
./gradlew build
```

This is a library; it has no runnable application. Use the test suite and your
own JavaFX application to exercise the controls.

### Useful Gradle tasks

- `./gradlew build` - compile, test and assemble the library
- `./gradlew dokkaGeneratePublicationHtml` - generate the API documentation
- `./gradlew generateLicenseReport` - generate the dependency licence report
- `./gradlew koverHtmlReport` - generate the test coverage report
- `./gradlew runDocs` - serve the MkDocs site locally
- `./gradlew buildDocs` - build the MkDocs site (strict, acts as a docs test)

## Consuming the artifacts

The artifacts are published to GitHub Packages under the group
`org.pcsoft.framework` with the artifact id `panelium`. GitHub Packages requires
an authenticated request even for public repositories, so consumers need a
GitHub username and a personal access token with `read:packages` scope.

Gradle (Kotlin DSL):

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/KleinerHacker/panelium-fx")
        credentials {
            username = "<github-username>"
            password = "<github-token-with-read:packages-scope>"
        }
    }
}

dependencies {
    implementation("org.pcsoft.framework:panelium:0.1.0")
}
```

Maven (`~/.m2/settings.xml` server entry with id `github`):

```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/KleinerHacker/panelium-fx</url>
</repository>
```

```xml
<dependency>
    <groupId>org.pcsoft.framework</groupId>
    <artifactId>panelium</artifactId>
    <version>0.1.0</version>
</dependency>
```

A sources jar is published alongside the main artifact.

## Documentation

- User documentation (MkDocs, GitHub Pages): https://kleinerhacker.github.io/panelium-fx/latest/
- API documentation (Dokka): published under the MkDocs site at `API Docs`
- Dependency licence report: published under the MkDocs site at `Licences`

## Implementation status

| Feature | State |
| --- | --- |
| MenuPane control (tabs, groups, action controls) | Planned |
| Control reference and customization API | Planned |
| Custom window frame core (`ChromePane`, `PaneliumChrome`, `PaneliumStage`) | Done |
| Window operations (move, resize, min/max/restore, full screen, shadow) | Done |
| Composable caption bar (content slots, default title/icon, drag hit-testing) | Done |
| OS-specific caption buttons (per-OS placement, native default style) | Done |
| Full CSS styling API and default user-agent stylesheet | Done |
| Headless UI test harness and coverage | Done |

The public API is under active development.

## Licence

See [LICENSE](LICENSE).
