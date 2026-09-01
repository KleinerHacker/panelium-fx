package org.pcsoft.framework.panelium.demo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.Separator
import javafx.scene.control.Slider
import javafx.scene.control.SplitPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.ChromeOs
import org.pcsoft.framework.panelium.chrome.ChromePane
import org.pcsoft.framework.panelium.chrome.PaneliumChrome
import org.pcsoft.framework.panelium.chrome.PaneliumStage

/**
 * Showcase window for [PaneliumStage]. Every caption slot and the window body are filled with real
 * JavaFX controls; the icons are a PaneliumFX-styled set derived from the project logo and live in
 * `resources/.../demo/icons`.
 */
class ChromeDemoApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = PaneliumStage()
        stage.title = "PaneliumFX Chrome Demo"
        stage.icons.setAll(
            appImage(16), appImage(32), appImage(48), appImage(64), appImage(128),
        )
        stage.width = 960.0
        stage.height = 640.0
        stage.minWidth = 640.0
        stage.minHeight = 440.0

        fillCaption(stage.chromePane)

        stage.content = buildBody(stage.chromePane)
        stage.show()
    }

    // ------------------------------------------------------------------ caption

    /** Puts icon controls into the leading, center and trailing caption slots. */
    private fun fillCaption(chromePane: ChromePane) {
        val back = captionIconButton("folder", "Open location")
        val newDoc = captionIconButton("new-doc", "New document")
        val view = captionIconButton("view", "Toggle preview")
        chromePane.captionLeftItems.addAll(Separator().apply { padding = Insets(0.0, 2.0, 0.0, 6.0) }, back, newDoc, view)

        val brand = HBox(6.0, iconView("swoosh", 16), Label("Showcase workspace")).apply {
            alignment = Pos.CENTER
            styleClass.add("demo-brand")
        }
        val osSelector = ComboBox<ChromeOs>().apply {
            items.setAll(ChromeOs.entries)
            value = chromePane.captionOs
            tooltip = Tooltip("Caption OS design")
            valueProperty().addListener { _, _, os -> os?.let { chromePane.captionOs = it } }
        }
        val center = HBox(12.0, brand, Label("OS:"), osSelector).apply {
            alignment = Pos.CENTER
        }
        chromePane.captionCenterItems.add(center)

        val grid = captionIconButton("grid", "Layout")
        val settings = captionIconButton("settings", "Settings")
        chromePane.captionRightItems.addAll(grid, settings)
    }

    private fun captionIconButton(icon: String, tip: String): Button =
        Button(null, iconView(icon, 16)).apply {
            styleClass.add("demo-caption-button")
            style = "-fx-background-color: transparent; -fx-padding: 3 6 3 6; -fx-background-radius: 4;"
            tooltip = Tooltip(tip)
            focusTraversableProperty().set(false)
        }

    // --------------------------------------------------------------------- body

    private fun buildBody(chromePane: ChromePane): Node {
        val toolBar = ToolBar(
            toolButton("new-doc", "New"),
            toolButton("folder", "Open"),
            Separator(),
            toolButton("view", "Preview"),
            toolButton("list", "List"),
            toolButton("grid", "Grid"),
            spacer(),
            toolButton("tools", "Tools"),
            toolButton("settings", "Settings"),
        )

        val pages = listOf(
            NavPage("Start", "home") { startPage() },
            NavPage("Documents", "folder") { documentsPage() },
            NavPage("Chrome options", "tools") { chromeOptionsPage(chromePane) },
            NavPage("Settings", "settings") { settingsPage() },
        )
        val detail = StackPane().apply { padding = Insets(20.0) }
        val nav = ListView<NavPage>().apply {
            items.setAll(pages)
            minWidth = 190.0
            maxWidth = 240.0
            cellFactory = javafx.util.Callback { navCell() }
            selectionModel.selectedItemProperty().addListener { _, _, page ->
                detail.children.setAll(page?.build?.invoke() ?: Region())
            }
            selectionModel.selectFirst()
        }

        val split = SplitPane(nav, detail).apply {
            setDividerPositions(0.24)
            SplitPane.setResizableWithParent(nav, false)
        }
        VBox.setVgrow(split, Priority.ALWAYS)

        val status = HBox(
            8.0,
            iconView("swoosh", 16),
            Label("Ready"),
            spacer(),
            Label("PaneliumFX • undecorated stage"),
        ).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(6.0, 12.0, 6.0, 12.0)
            styleClass.add("demo-status-bar")
            style = "-fx-background-color: #f2f5fb; -fx-border-color: #d9e2f2; -fx-border-width: 1 0 0 0;"
        }

        return VBox(toolBar, split, status).apply {
            style = "-fx-background-color: white;"
        }
    }

    private fun startPage(): Node = VBox(
        14.0,
        ImageView(appImage(64)),
        Label("PaneliumStage demo window").apply { style = "-fx-font-size: 16; -fx-font-weight: bold;" },
        Label("Undecorated, transparent, with a composable caption bar."),
        Label("Icons in every caption slot, the tool bar, the navigation list and the status bar."),
    ).apply { alignment = Pos.TOP_LEFT }

    private fun documentsPage(): Node {
        val list = ListView<NavPage>().apply {
            items.setAll(
                NavPage("Quarterly report.pdf", "new-doc") { Region() },
                NavPage("Assets", "folder") { Region() },
                NavPage("Preview cache", "view") { Region() },
                NavPage("Build tools", "tools") { Region() },
            )
            cellFactory = javafx.util.Callback { navCell() }
            prefHeight = 220.0
        }
        return VBox(10.0, Label("Recent items").apply { style = "-fx-font-weight: bold;" }, list)
    }

    private fun chromeOptionsPage(chromePane: ChromePane): Node {
        val titleToggle = ToggleButton("Default title visible").apply {
            isSelected = true
            chromePane.defaultTitleVisibleProperty().bind(selectedProperty())
        }
        val iconToggle = ToggleButton("Default icon visible").apply {
            isSelected = true
            chromePane.defaultIconVisibleProperty().bind(selectedProperty())
        }
        val overrideToggle = ToggleButton("Apply CSS override stylesheet").apply {
            val sheet = ChromeDemoApp::class.java.getResource("chrome-override.css")!!.toExternalForm()
            selectedProperty().addListener { _, _, on ->
                val sheets = this@ChromeDemoApp.scene(this)?.stylesheets ?: return@addListener
                if (on) sheets.add(sheet) else sheets.remove(sheet)
            }
        }
        val signatureToggle = ToggleButton("Apply signature window theme").apply {
            val sheet = ChromeDemoApp::class.java.getResource("chrome-signature.css")!!.toExternalForm()
            selectedProperty().addListener { _, _, on ->
                val sheets = this@ChromeDemoApp.scene(this)?.stylesheets ?: return@addListener
                if (on) sheets.add(sheet) else sheets.remove(sheet)
            }
        }
        val fxmlButton = Button("Open FXML window", iconView("new-doc", 16)).apply {
            setOnAction { openFxmlWindow() }
        }
        val installButton = Button("Open via PaneliumChrome.install(stage)", iconView("tools", 16)).apply {
            setOnAction { openInstalledWindow() }
        }

        return VBox(
            12.0,
            Label("Caption bar").apply { style = "-fx-font-weight: bold;" },
            titleToggle,
            iconToggle,
            Label("The caption OS design is switched from the ComboBox in the center caption slot."),
            overrideToggle,
            signatureToggle,
            Separator(),
            Label("Extra windows").apply { style = "-fx-font-weight: bold;" },
            fxmlButton,
            installButton,
        )
    }

    private fun settingsPage(): Node {
        val opacity = Slider(0.2, 1.0, 1.0).apply { isShowTickMarks = true }
        return VBox(
            12.0,
            Label("Appearance").apply { style = "-fx-font-weight: bold;" },
            CheckBox("Show status bar").apply { isSelected = true },
            CheckBox("Show tool bar labels").apply { isSelected = true },
            HBox(8.0, Label("Window opacity"), opacity).apply { alignment = Pos.CENTER_LEFT },
        )
    }

    // ----------------------------------------------------------------- helpers

    private fun scene(node: Node): Scene? = node.scene

    private fun toolButton(icon: String, text: String): Button =
        Button(text, iconView(icon, 20)).apply { contentDisplay = ContentDisplay.LEFT }

    private fun spacer(): Region = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }

    private fun navCell(): ListCell<NavPage> = object : ListCell<NavPage>() {
        override fun updateItem(item: NavPage?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty || item == null) {
                text = null
                graphic = null
            } else {
                text = item.title
                graphic = iconView(item.icon, 20)
            }
        }
    }

    private fun iconView(name: String, size: Int): ImageView =
        ImageView(image("icons/$name-$size.png")).apply {
            fitWidth = size.toDouble()
            fitHeight = size.toDouble()
            isPreserveRatio = true
        }

    private fun appImage(size: Int): Image = image("icons/app-$size.png")

    private fun image(path: String): Image =
        Image(ChromeDemoApp::class.java.getResourceAsStream(path) ?: error("missing demo resource: $path"))

    // ------------------------------------------------------------ extra windows

    private fun openFxmlWindow() {
        val stage = Stage()
        stage.title = "FXML Chrome Window"
        stage.icons.setAll(appImage(32), appImage(64))
        stage.width = 460.0
        stage.height = 260.0

        val pane = FXMLLoader.load<ChromePane>(
            ChromeDemoApp::class.java.getResource("FxmlChromeWindow.fxml"),
        )
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT)
        stage.scene = Scene(pane).apply { fill = Color.TRANSPARENT }
        pane.attachStage(stage)
        pane.captionRightItems.add(
            HBox(6.0, iconView("view", 16), Label("FXML caption slot")).apply { alignment = Pos.CENTER },
        )
        stage.show()
    }

    private fun openInstalledWindow() {
        val stage = Stage()
        stage.icons.setAll(appImage(32), appImage(64))
        stage.width = 420.0
        stage.height = 240.0

        val root = VBox(
            10.0,
            ImageView(appImage(48)),
            Label("Opened via PaneliumChrome.install(stage)"),
        )
        root.alignment = Pos.CENTER
        root.style = "-fx-background-color: white; -fx-padding: 24;"
        stage.scene = Scene(root)

        val chrome = PaneliumChrome.install(stage)
        chrome.captionRightItems.add(captionIconButton("settings", "Settings"))
        stage.show()
    }

    private class NavPage(val title: String, val icon: String, val build: () -> Node)
}

fun main() {
    Application.launch(ChromeDemoApp::class.java)
}
