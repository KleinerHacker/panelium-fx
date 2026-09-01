package org.pcsoft.framework.panelium.demo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.image.WritableImage
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.ChromeOs
import org.pcsoft.framework.panelium.chrome.ChromePane
import org.pcsoft.framework.panelium.chrome.PaneliumChrome
import org.pcsoft.framework.panelium.chrome.PaneliumStage

class ChromeDemoApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = PaneliumStage()
        stage.title = "PaneliumFX Chrome Demo"
        stage.icons.add(solidIcon(Color.web("#3b82f6")))
        stage.width = 520.0
        stage.height = 380.0

        // Caption slots: a status label in the center, an action button on the right.
        stage.chromePane.captionCenterItems.add(Label("center slot"))
        stage.chromePane.captionRightItems.add(Button("right slot"))

        val titleToggle = ToggleButton("Default title visible").apply {
            isSelected = true
            stage.chromePane.defaultTitleVisibleProperty().bind(selectedProperty())
        }
        val osRow = captionOsRow(stage.chromePane)
        val fxmlButton = Button("Open FXML window").apply {
            setOnAction { openFxmlWindow() }
        }
        val installButton = Button("Open via PaneliumChrome.install(stage)").apply {
            setOnAction { openInstalledWindow() }
        }

        val content = VBox(
            16.0,
            Label("PaneliumStage demo window"),
            Label("Undecorated, transparent, with a composable caption bar."),
            titleToggle,
            osRow,
            fxmlButton,
            installButton,
        )
        content.alignment = Pos.CENTER
        content.style = "-fx-background-color: white; -fx-padding: 24;"

        stage.content = content
        stage.show()
    }

    /**
     * A picker that switches [ChromePane.captionOs] live, so the Windows / macOS / Linux / other
     * caption button designs and placements can be compared without restarting the demo.
     */
    private fun captionOsRow(chromePane: ChromePane): HBox {
        val selector = ComboBox<ChromeOs>().apply {
            items.setAll(ChromeOs.entries)
            value = chromePane.captionOs
            valueProperty().addListener { _, _, os -> os?.let { chromePane.captionOs = it } }
        }
        return HBox(8.0, Label("Caption OS design:"), selector).apply { alignment = Pos.CENTER }
    }

    private fun openFxmlWindow() {
        val stage = Stage()
        stage.title = "FXML Chrome Window"
        stage.width = 420.0
        stage.height = 240.0

        val pane = FXMLLoader.load<ChromePane>(
            ChromeDemoApp::class.java.getResource("FxmlChromeWindow.fxml"),
        )
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT)
        stage.scene = Scene(pane).apply { fill = Color.TRANSPARENT }
        pane.attachStage(stage)
        pane.captionRightItems.add(Label("try the Caption OS picker in the main window"))
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

        val chrome = PaneliumChrome.install(stage)
        chrome.captionCenterItems.add(captionOsRow(chrome))
        stage.show()
    }

    private fun solidIcon(color: Color): WritableImage {
        val size = 16
        val image = WritableImage(size, size)
        val writer = image.pixelWriter
        for (y in 0 until size) {
            for (x in 0 until size) {
                writer.setColor(x, y, color)
            }
        }
        return image
    }
}

fun main() {
    Application.launch(ChromeDemoApp::class.java)
}
