package org.pcsoft.framework.panelium.demo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.ChromePane

/**
 * Standalone showcase window for [org.pcsoft.framework.panelium.menutab.FXMenuTab]. The whole
 * window content is declared in `MenuTabShowcaseWindow.fxml` and wired by
 * [MenuTabShowcaseWindowController]; this class only bootstraps the [Stage]. The FXML root is a
 * [ChromePane] whose content is a `BorderPane(top = FXMenuTab)`, showing the ribbon docked below
 * the caption bar without any dedicated `ChromePane` API, mirroring [ChromeDemoApp].
 */
class MenuTabShowcaseApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = Stage()
        stage.title = "PaneliumFX FXMenuTab Showcase"
        stage.width = 720.0
        stage.height = 420.0
        stage.minWidth = 480.0
        stage.minHeight = 320.0

        val pane = FXMLLoader.load<ChromePane>(
            MenuTabShowcaseApp::class.java.getResource("MenuTabShowcaseWindow.fxml"),
        )
        stage.scene = Scene(pane).apply { fill = Color.TRANSPARENT }
        stage.initStyle(StageStyle.TRANSPARENT)
        pane.attachStage(stage)

        stage.show()
    }
}

fun main() {
    Application.launch(MenuTabShowcaseApp::class.java)
}
