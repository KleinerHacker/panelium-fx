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
import org.pcsoft.framework.panelium.menutab.ContextTabGroup
import org.pcsoft.framework.panelium.menutab.FXMenuGroup
import org.pcsoft.framework.panelium.menutab.FXMenuTab
import org.pcsoft.framework.panelium.menutab.MenuTab
import java.net.URL
import java.util.ResourceBundle

/**
 * Controller for `MenuTabShowcaseWindow.fxml`; registers the demo tabs, gives the "Home" and
 * "View" tabs a few `FXMenuGroup`s to show the group strip switching with the active tab, wires the
 * checkbox that toggles the "Table" contextual tab group in and out, shows the active tab plus
 * whether it is permanent or contextual, and reflects whether the file tab's backstage is open.
 */
class MenuTabShowcaseWindowController : Initializable {

    @FXML
    private lateinit var menuTab: FXMenuTab

    @FXML
    private lateinit var activeTabLabel: Label

    @FXML
    private lateinit var showTableToolsCheckBox: CheckBox

    private val tableDesign = MenuTab("table-design", "Design")
    private val tableLayout = MenuTab("table-layout", "Layout")
    private val tableToolsGroup = ContextTabGroup("Table Tools", "#4a90d9")

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        menuTab.fileTab = MenuTab("file", "File")
        menuTab.backstageContent = VBox(8.0).apply {
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

        val home = MenuTab("home", "Home")
        val view = MenuTab("view", "View")
        val tools = MenuTab("tools", "Tools")
        val disabled = MenuTab("disabled", "Disabled").apply { isDisabled = true }
        menuTab.tabs.addAll(home, view, tools, disabled)

        home.groups.addAll(
            group("Clipboard", Button("Paste"), Button("Cut"), Button("Copy")),
            group("Font", Button("Bold"), Button("Italic"), Button("Underline")),
            group("Paragraph", Button("Bullets"), Button("Numbering")),
        )
        view.groups.addAll(
            group("Views", ToggleButton("Read"), ToggleButton("Print"), ToggleButton("Web")),
            group("Show", CheckBox("Ruler"), CheckBox("Gridlines")),
        )

        menuTab.activeTab = home

        menuTab.assignToGroup(tableDesign, tableToolsGroup)
        menuTab.assignToGroup(tableLayout, tableToolsGroup)

        showTableToolsCheckBox.selectedProperty().addListener { _, _, selected ->
            if (selected) {
                menuTab.contextualTabs.addAll(tableDesign, tableLayout)
                menuTab.activate(tableDesign)
            } else {
                menuTab.contextualTabs.removeAll(tableDesign, tableLayout)
            }
        }

        activeTabLabel.textProperty().bind(
            Bindings.createStringBinding(
                {
                    if (menuTab.isFileTabActive) {
                        return@createStringBinding "Active tab: File (backstage open)"
                    }
                    val active = menuTab.activeTab
                    val kind = when {
                        active == null -> ""
                        menuTab.contextualTabs.contains(active) -> " (contextual)"
                        else -> " (permanent)"
                    }
                    "Active tab: ${active?.title ?: "none"}$kind"
                },
                menuTab.activeTabProperty(),
                menuTab.fileTabActiveProperty(),
            ),
        )
    }

    private fun group(title: String, vararg controls: javafx.scene.Node): FXMenuGroup =
        FXMenuGroup().apply {
            this.title = title
            content.addAll(controls)
        }
}
