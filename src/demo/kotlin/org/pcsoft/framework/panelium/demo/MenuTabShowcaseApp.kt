package org.pcsoft.framework.panelium.demo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * Standalone showcase window for [org.pcsoft.framework.panelium.menutab.FXMenuTab]. The whole
 * window content is declared in `MenuTabShowcaseWindow.fxml` and wired by
 * [MenuTabShowcaseWindowController]; this class only bootstraps the [Stage], mirroring
 * [ChromeDemoApp].
 */
class MenuTabShowcaseApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = Stage()
        stage.title = "PaneliumFX FXMenuTab Showcase"
        stage.width = 480.0
        stage.height = 200.0
        stage.minWidth = 320.0
        stage.minHeight = 180.0

        val root = FXMLLoader.load<Parent>(
            MenuTabShowcaseApp::class.java.getResource("MenuTabShowcaseWindow.fxml"),
        )
        stage.scene = Scene(root)

        stage.show()
    }
}

fun main() {
    Application.launch(MenuTabShowcaseApp::class.java)
}
