package org.pcsoft.framework.panelium.demo

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import org.pcsoft.framework.panelium.menutab.ContextTabGroup
import org.pcsoft.framework.panelium.menutab.FXMenuTab
import org.pcsoft.framework.panelium.menutab.MenuTab
import java.net.URL
import java.util.ResourceBundle

/**
 * Controller for `MenuTabShowcaseWindow.fxml`; registers the demo tabs, wires the checkbox that
 * toggles the "Table" contextual tab group in and out, and shows the active tab plus whether it
 * is permanent or contextual.
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
        val home = MenuTab("home", "Home")
        val view = MenuTab("view", "View")
        val tools = MenuTab("tools", "Tools")
        val disabled = MenuTab("disabled", "Disabled").apply { isDisabled = true }
        menuTab.tabs.addAll(home, view, tools, disabled)
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
                    val active = menuTab.activeTab
                    val kind = when {
                        active == null -> ""
                        menuTab.contextualTabs.contains(active) -> " (contextual)"
                        else -> " (permanent)"
                    }
                    "Active tab: ${active?.title ?: "none"}$kind"
                },
                menuTab.activeTabProperty(),
            ),
        )
    }
}
