package org.pcsoft.framework.panelium.demo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.ChromePane

/**
 * Showcase window for [org.pcsoft.framework.panelium.chrome.PaneliumStage]. The whole window
 * content — caption slots, tool bar, navigation tabs, pages and status bar — is declared in
 * `ChromeDemoWindow.fxml` and the FXML files under `pages`; this class only bootstraps the [Stage].
 */
class ChromeDemoApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = Stage()
        stage.title = "PaneliumFX Chrome Demo"
        stage.icons.setAll(
            appIcon(16), appIcon(32), appIcon(48), appIcon(64), appIcon(128),
        )
        stage.width = 960.0
        stage.height = 640.0
        stage.minWidth = 640.0
        stage.minHeight = 440.0

        val pane = FXMLLoader.load<ChromePane>(
            ChromeDemoApp::class.java.getResource("ChromeDemoWindow.fxml"),
        )
        stage.scene = Scene(pane).apply { fill = Color.TRANSPARENT }
        stage.initStyle(StageStyle.TRANSPARENT)
        pane.attachStage(stage)

        stage.show()
    }

    private fun appIcon(size: Int): Image =
        Image(
            ChromeDemoApp::class.java.getResourceAsStream("icons/app-$size.png")
                ?: error("missing demo resource: icons/app-$size.png"),
        )
}

fun main() {
    Application.launch(ChromeDemoApp::class.java)
}
