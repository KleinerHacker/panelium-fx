# Getting Started

## Requirements

- Java 25 or newer
- JavaFX 25 (`javafx.controls`)

## Add the dependency

The artifacts are published to Maven Central under the group
`org.pcsoft.framework`.

=== "Gradle (Kotlin DSL)"

    ```kotlin
    dependencies {
        implementation("org.pcsoft.framework:panelium:<version>")
    }
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>org.pcsoft.framework</groupId>
        <artifactId>panelium</artifactId>
        <version>VERSION</version>
    </dependency>
    ```

JavaFX is exposed transitively by the library, so no additional JavaFX
dependency is required for the public API types.

## Next steps

Continue with [Usage](usage.md).
