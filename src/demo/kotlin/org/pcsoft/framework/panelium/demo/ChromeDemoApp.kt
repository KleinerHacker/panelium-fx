package org.pcsoft.framework.panelium.demo

import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.PaneliumChrome
import org.pcsoft.framework.panelium.chrome.PaneliumStage

class ChromeDemoApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = PaneliumStage()
        stage.title = "PaneliumFX Chrome Demo"
        stage.width = 480.0
        stage.height = 320.0

        val installButton = Button("Open via PaneliumChrome.install(stage)")
        installButton.setOnAction { openInstalledWindow() }

        val content = VBox(
            16.0,
            Label("PaneliumStage demo window"),
            Label("Undecorated, transparent, with a shadow / caption placeholder frame."),
            installButton,
        )
        content.alignment = Pos.CENTER
        content.style = "-fx-background-color: white; -fx-padding: 24;"

        stage.content = content
        stage.show()
    }

    private fun openInstalledWindow() {
        val stage = Stage()
        stage.width = 380.0
        stage.height = 220.0

        val label = Label("Opened via PaneliumChrome.install(stage)")
        val root = VBox(label)
        root.alignment = Pos.CENTER
        root.style = "-fx-background-color: white; -fx-padding: 24;"
        stage.scene = Scene(root)

        PaneliumChrome.install(stage)
        stage.show()
    }
}

fun main() {
    Application.launch(ChromeDemoApp::class.java)
}
