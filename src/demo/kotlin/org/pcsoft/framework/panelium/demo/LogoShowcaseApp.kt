package org.pcsoft.framework.panelium.demo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.ChromeOs
import org.pcsoft.framework.panelium.chrome.ChromePane

/**
 * Standalone runner that recreates `docs/docs/assets/images/logo.png` as a live
 * [org.pcsoft.framework.panelium.chrome.PaneliumStage]-style window: a dark-blue caption with the
 * swoosh mark, a Home/View/Tools tab row, an icon tool bar, a list sidebar and a glossy canvas.
 * Layout is declared in `LogoShowcaseWindow.fxml`, the blue look in `logo-theme.css`.
 */
class LogoShowcaseApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = Stage()
        stage.title = "PaneliumFX"
        stage.icons.setAll(
            appIcon(16), appIcon(32), appIcon(48), appIcon(64), appIcon(128),
        )
        stage.width = 900.0
        stage.height = 620.0
        stage.minWidth = 640.0
        stage.minHeight = 460.0

        val pane = FXMLLoader.load<ChromePane>(
            LogoShowcaseApp::class.java.getResource("LogoShowcaseWindow.fxml"),
        )
        pane.captionOs = ChromeOs.WINDOWS

        stage.scene = Scene(pane).apply {
            fill = Color.TRANSPARENT
            stylesheets.add(
                LogoShowcaseApp::class.java.getResource("logo-theme.css")!!.toExternalForm(),
            )
        }
        stage.initStyle(StageStyle.TRANSPARENT)
        pane.attachStage(stage)

        stage.show()
    }

    private fun appIcon(size: Int): Image =
        Image(
            LogoShowcaseApp::class.java.getResourceAsStream("icons/app-$size.png")
                ?: error("missing demo resource: icons/app-$size.png"),
        )
}

fun main() {
    Application.launch(LogoShowcaseApp::class.java)
}
