plugins {
    kotlin("jvm") version "2.4.10"
    `java-library`
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.pcsoft.framework"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(25)
    explicitApi()
}

javafx {
    version = "25"
    modules("javafx.controls")
    // Expose JavaFX transitively: the public API of this library uses JavaFX types.
    configuration = "api"
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
