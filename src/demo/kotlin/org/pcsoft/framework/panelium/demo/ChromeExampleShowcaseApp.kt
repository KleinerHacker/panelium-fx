package org.pcsoft.framework.panelium.demo

import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.ChromeCaptionBar
import org.pcsoft.framework.panelium.chrome.ChromeOs
import org.pcsoft.framework.panelium.chrome.PaneliumStage

/**
 * Standalone runner for the "Complex example" shown in the Platinum Chrome MkDocs pages.
 *
 * It mirrors the editor-style window from `docs/docs/platinum-chrome/implementation.md`
 * one to one - [PaneliumStage] as the entry point, all three caption slots filled, a
 * draggable breadcrumb strip in the center, interactive controls on the trailing edge, a
 * forced Windows look for a cross-platform preview and a runtime content swap - and attaches
 * the full replacement theme from `docs/docs/platinum-chrome/customize-styles.md`
 * (`chrome-example-showcase.css`). Used to produce the documentation preview images.
 *
 * Keep this runner and its stylesheet in sync with those two documentation pages.
 */
class ChromeExampleShowcaseApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = PaneliumStage()
        stage.title = "PaneliumFX Editor"
        stage.icons.setAll(
            appIcon(16), appIcon(32), appIcon(48), appIcon(64), appIcon(128),
        )
        stage.width = 900.0
        stage.height = 600.0
        stage.minWidth = 640.0
        stage.minHeight = 420.0

        val chrome = stage.chromePane

        // Force the Windows layout regardless of the host OS (demo / preview).
        chrome.captionOs = ChromeOs.WINDOWS

        // Leading slot: a menu-like button next to the default icon and title.
        chrome.captionLeftItems.add(
            MenuButton(
                "File", null,
                MenuItem("New"), MenuItem("Open…"), MenuItem("Save"),
            ),
        )

        // Center slot: a breadcrumb that still drags the window.
        val breadcrumb = HBox(
            6.0,
            Label("workspace"), Label("›"), Label("docs"), Label("›"),
            Label("implementation.md"),
        ).apply {
            alignment = Pos.CENTER
            styleClass.add("breadcrumb")
        }
        ChromeCaptionBar.setDragRegion(breadcrumb, true)
        chrome.captionCenterItems.add(breadcrumb)

        // Trailing slot: live controls, kept clickable by the heuristic.
        val dirty = Label("●")
        chrome.captionRightItems.addAll(dirty, Button("Share"))

        // Content, swapped at runtime.
        val welcome = Label("Open a file to start editing")
        val editor = TextArea()
        stage.content = welcome

        editor.textProperty().addListener { _, _, _ -> dirty.text = "● unsaved" }

        Platform.runLater {
            stage.content = editor
            editor.requestFocus()
        }

        stage.scene.stylesheets.add(
            ChromeExampleShowcaseApp::class.java.getResource("chrome-example-showcase.css")!!
                .toExternalForm(),
        )

        stage.show()
    }

    private fun appIcon(size: Int): Image =
        Image(
            ChromeExampleShowcaseApp::class.java.getResourceAsStream("icons/app-$size.png")
                ?: error("missing demo resource: icons/app-$size.png"),
        )
}

fun main() {
    Application.launch(ChromeExampleShowcaseApp::class.java)
}
