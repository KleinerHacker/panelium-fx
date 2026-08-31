# PaneliumFX

A ribbon like JavaFX menu pane control.

## What it is

`panelium-fx` is a JavaFX UI library that provides a ribbon style menu pane,
grouping actions into tabs and groups similar to the ribbon UI known from common
office applications. JavaFX is exposed transitively, so consumers do not need to
declare a separate JavaFX dependency for the public API types.

## AI disclosure

In accordance with EU transparency requirements, please note that this project -
including its source code, tests, documentation and configuration - was created
entirely with the assistance of artificial intelligence.

## Features

- Ribbon style menu pane arranging content into tabs, groups and action controls.

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

The artifacts are published to Maven Central under the group
`org.pcsoft.framework` with the artifact id `panelium`.

Gradle (Kotlin DSL):

```kotlin
dependencies {
    implementation("org.pcsoft.framework:panelium:<version>")
}
```

Maven:

```xml
<dependency>
    <groupId>org.pcsoft.framework</groupId>
    <artifactId>panelium</artifactId>
    <version>VERSION</version>
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
| Ribbon menu pane control (tabs, groups, action controls) | Planned |
| Control reference and customization API | Planned |

Nothing is implemented yet; the public API is under active development.

## Licence

See [LICENSE](LICENSE).
