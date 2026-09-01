import com.github.jk1.license.render.ReportRenderer

plugins {
    kotlin("jvm") version "2.4.10"
    `java-library`
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.jetbrains.dokka") version "2.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.9"
    id("com.github.jk1.dependency-license-report") version "3.1.4"
    id("org.cyclonedx.bom") version "3.4.1"
    id("app.cash.licensee") version "1.14.1"
}

group = "org.pcsoft.framework"

// A release passes the tag as -PreleaseVersion=<tag>; a local build stays on the snapshot.
version = (project.findProperty("releaseVersion") as String?)?.takeIf { it.isNotBlank() } ?: "1.0-SNAPSHOT"

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
    // javafx.fxml is needed for FXMLLoader / <fx:root> (the caption bar view and the ChromePane view).
    modules("javafx.controls", "javafx.fxml")
    // Expose JavaFX transitively: the public API of this library uses JavaFX types.
    configuration = "api"
}

sourceSets {
    create("demo") {
        kotlin.srcDir("src/demo/kotlin")
        resources.srcDir("src/demo/resources")
    }
}

val demoImplementation by configurations.getting {
    extendsFrom(configurations["implementation"], configurations["api"])
}

// The demo source set names src/demo/resources explicitly on top of the convention dir of the
// same name, so every demo resource is seen twice; keep the last copy instead of failing.
tasks.named<Copy>("processDemoResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

dependencies {
    // MVVM wiring for the chrome components; kept internal, never exposed in the public API.
    implementation("de.saxsys:mvvmfx:1.8.0")

    testImplementation(kotlin("test"))

    demoImplementation(sourceSets["main"].output)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runDemo") {
    group = "demo"
    description = "Run the ChromePane demo application (visual, manual check)"
    mainClass.set("org.pcsoft.framework.panelium.demo.ChromeDemoAppKt")
    classpath = sourceSets["demo"].runtimeClasspath
}

// The licence report is written into the root build dir, so the MkDocs tasks find one place.
licenseReport {
    outputDir = layout.buildDirectory.dir("licences").get().asFile.absolutePath
    renderers = arrayOf<ReportRenderer>(
        com.github.jk1.license.render.JsonReportRenderer(),
        com.github.jk1.license.render.SimpleHtmlReportRenderer()
    )
}

licensee {
    listOf(
        "Apache-2.0",
        // ControlsFX and other BSD licensed FX helpers
        "BSD-2-Clause",
        // SLF4J
        "MIT",
    ).forEach(::allow)

    // JavaFX publishes GPL-2.0 with Classpath Exception, but only as a URL - it carries no SPDX id
    // in its POM, so it has to be allowed by that URL.
    allowUrl("https://openjdk.java.net/legal/gplv2+ce.html") {
        because("GPL-2.0 with Classpath Exception")
    }

    // typetools names Apache-2.0 only as a plain http URL in its POM, without an SPDX id.
    allowUrl("http://apache.org/licenses/LICENSE-2.0") {
        because("Apache-2.0")
    }

    // SLF4J names MIT only by the URL of the licence text in its POM, without an SPDX id.
    allowUrl("https://opensource.org/license/mit") {
        because("MIT")
    }

    // eu.lestard:doc-annotations (transitive via mvvmfx) carries the MIT licence URL wrapped in
    // single quotes in its POM, so it matches neither the SPDX id nor the plain URL.
    allowUrl("'http://opensource.org/licenses/mit-license'") {
        because("MIT")
    }
}

tasks {
    //region Dokka
    register<Copy>("copyDokka") {
        group = "dokka"
        description = "Copy all Dokka to MkDocs"
        from(layout.buildDirectory.dir("dokka"))
        into(File("docs/docs/dokka"))
        dependsOn("dokkaGeneratePublicationHtml")
    }

    register<Delete>("deleteDokka") {
        group = "dokka"
        description = "Delete Dokka"
        delete(File("docs/docs/dokka"))
    }
    //endregion

    //region Licencing
    register<Copy>("copyLicenceReport") {
        group = "licencing"
        description = "Copy licence report to MkDocs"
        from(layout.buildDirectory.dir("licences"))
        into(File("docs/docs/licences"))
        dependsOn("generateLicenseReport")
    }

    register<Delete>("deleteLicenceReport") {
        group = "licencing"
        description = "Delete licence report"
        delete(File("docs/docs/licences"))
    }
    //endregion

    //region MkDocs
    // mike spawns `mkdocs` as a subprocess; on Windows the Python Scripts dir
    // (where mkdocs.exe lives) is often not on PATH. Resolve it once and prepend
    // it to PATH for the mike tasks. In CI (setup-python) it is already on PATH.
    val pythonScriptsDir: String? by lazy {
        runCatching {
            providers.exec {
                commandLine("python", "-c", "import sysconfig; print(sysconfig.get_path('scripts'))")
            }.standardOutput.asText.get().trim().ifEmpty { null }
        }.getOrNull()
    }

    fun Exec.withMikePath() {
        pythonScriptsDir?.let { dir ->
            environment("PATH", dir + File.pathSeparator + System.getenv("PATH"))
        }
    }

    register<Exec>("installMkDocs") {
        group = null
        description = "Install mkdocs"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "mkdocs")
    }

    register<Exec>("installMkDocsMaterial") {
        group = null
        description = "Install mkdocs-material"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "mkdocs-material")
    }

    register<Exec>("installGitHubPages") {
        group = null
        description = "Install ghp-import"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "ghp-import")
    }

    register<Exec>("installMike") {
        group = null
        description = "Install mike for versioned docs deployment"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "mike")
    }

    register("installDocs") {
        group = "MKDocs"
        description = "Install mkdocs and dependencies"
        dependsOn("installMkDocs")
        dependsOn("installMkDocsMaterial")
        dependsOn("installGitHubPages")
        dependsOn("installMike")
    }

    register<Exec>("runDocs") {
        group = "MKDocs"
        description = "Run mkdocs serve and open browser (no version selector - that only appears on the deployed site)"
        workingDir = file("docs")
        commandLine("python", "-m", "mkdocs", "serve", "-o", "-w", ".", "-w", "./docs")
        dependsOn("installDocs", "copyDokka", "copyLicenceReport")
        finalizedBy("deleteDokka", "deleteLicenceReport")
    }

    register<Exec>("buildDocs") {
        group = "MKDocs"
        description =
            "Build the mkdocs site into build/docs (per mkdocs.yml site_dir; no serve, no deploy) - usable as a generation test"
        workingDir = file("docs")
        // --strict fails the build on warnings (broken links, missing pages ...) so it acts as a test;
        // --clean wipes the previous output first.
        commandLine("python", "-m", "mkdocs", "build", "--clean", "--strict")
        dependsOn("installDocs", "copyDokka", "copyLicenceReport")
        finalizedBy("deleteDokka", "deleteLicenceReport")
    }

    register<Exec>("deployDocs") {
        group = "MKDocs"
        description =
            "Deploy a versioned docs snapshot via mike. Pass -PdocsVersion=<tag>; falls back to \"snapshot\" if no tag is given. Requires a pre-configured git push target."
        workingDir = file("docs")
        val ver = (project.findProperty("docsVersion") as String?)?.takeIf { it.isNotBlank() }
            ?: "snapshot"
        val setLatest = ver != "snapshot" && (project.findProperty("setLatest") as String?) != "false"
        val args = buildList {
            add("python"); add("-c"); add("from mike.driver import main; main()"); add("deploy"); add("--push")
            if (setLatest) {
                add("--update-aliases"); add(ver); add("latest")
            } else add(ver)
        }
        commandLine(args)
        withMikePath()
        dependsOn("installDocs", "copyDokka", "copyLicenceReport")
        finalizedBy("deleteDokka", "deleteLicenceReport")
    }

    register<Exec>("setDefaultDocs") {
        group = "MKDocs"
        description =
            "Set the default docs version shown at the root URL via mike (run once after the first release deploy)."
        workingDir = file("docs")
        commandLine("python", "-c", "from mike.driver import main; main()", "set-default", "--push", "latest")
        withMikePath()
        dependsOn("installDocs")
    }
    //endregion
}
