package org.pcsoft.framework.panelium.demo

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.layout.VBox
import org.pcsoft.framework.panelium.menupane.FXMenuContextTabGroup
import org.pcsoft.framework.panelium.menupane.FXMenuGroup
import org.pcsoft.framework.panelium.menupane.FXMenuPane
import org.pcsoft.framework.panelium.menupane.FXMenuTab
import java.net.URL
import java.util.ResourceBundle

/**
 * Controller for `MenuPaneShowcaseWindow.fxml`; registers the demo tabs, gives the "Home" and
 * "View" tabs a few `FXMenuGroup`s to show the group strip switching with the active tab, wires the
 * checkbox that toggles the "Table" contextual tab group in and out, shows the active tab plus
 * whether it is permanent or contextual, and reflects whether the file tab's backstage is open.
 */
class MenuPaneShowcaseWindowController : Initializable {

    @FXML
    private lateinit var menuPane: FXMenuPane

    @FXML
    private lateinit var activeTabLabel: Label

    @FXML
    private lateinit var showTableToolsCheckBox: CheckBox

    private val tableDesign = FXMenuTab("table-design", "Design")
    private val tableLayout = FXMenuTab("table-layout", "Layout")
    private val tableToolsGroup = FXMenuContextTabGroup("Table Tools", "#4a90d9")

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        menuPane.fileTab = FXMenuTab("file", "File")
        menuPane.backstageContent = VBox(8.0).apply {
            padding = Insets(24.0)
            style = "-fx-background-color: #f4f4f4; -fx-border-color: #c8c8c8; -fx-border-width: 1 0 0 0;"
            // Fill the MenuChromePane backstage overlay layer instead of sitting at its natural size.
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            children.addAll(
                Label("Backstage").apply { style = "-fx-font-size: 16; -fx-font-weight: bold;" },
                Label("Click the File tab to open this panel; press Escape or click outside to close."),
            )
        }

        val home = FXMenuTab("home", "Home")
        val view = FXMenuTab("view", "View")
        val tools = FXMenuTab("tools", "Tools")
        val disabled = FXMenuTab("disabled", "Disabled").apply { isDisabled = true }
        menuPane.tabs.addAll(home, view, tools, disabled)

        home.groups.addAll(
            group("Clipboard", Button("Paste"), Button("Cut"), Button("Copy")),
            group("Font", Button("Bold"), Button("Italic"), Button("Underline")),
            group("Paragraph", Button("Bullets"), Button("Numbering")),
        )
        view.groups.addAll(
            group("Views", ToggleButton("Read"), ToggleButton("Print"), ToggleButton("Web")),
            group("Show", CheckBox("Ruler"), CheckBox("Gridlines")),
        )

        menuPane.activeTab = home

        menuPane.assignToGroup(tableDesign, tableToolsGroup)
        menuPane.assignToGroup(tableLayout, tableToolsGroup)

        showTableToolsCheckBox.selectedProperty().addListener { _, _, selected ->
            if (selected) {
                menuPane.contextualTabs.addAll(tableDesign, tableLayout)
                menuPane.activate(tableDesign)
            } else {
                menuPane.contextualTabs.removeAll(tableDesign, tableLayout)
            }
        }

        activeTabLabel.textProperty().bind(
            Bindings.createStringBinding(
                {
                    if (menuPane.isFileTabActive) {
                        return@createStringBinding "Active tab: File (backstage open)"
                    }
                    val active = menuPane.activeTab
                    val kind = when {
                        active == null -> ""
                        menuPane.contextualTabs.contains(active) -> " (contextual)"
                        else -> " (permanent)"
                    }
                    "Active tab: ${active?.title ?: "none"}$kind"
                },
                menuPane.activeTabProperty(),
                menuPane.fileTabActiveProperty(),
            ),
        )
    }

    private fun group(title: String, vararg controls: javafx.scene.Node): FXMenuGroup =
        FXMenuGroup().apply {
            this.title = title
            content.addAll(controls)
        }
}
