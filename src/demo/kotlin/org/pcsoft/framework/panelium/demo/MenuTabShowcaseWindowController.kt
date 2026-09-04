package org.pcsoft.framework.panelium.demo

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import org.pcsoft.framework.panelium.menutab.FXMenuTab
import org.pcsoft.framework.panelium.menutab.MenuTab
import java.net.URL
import java.util.ResourceBundle

/** Controller for `MenuTabShowcaseWindow.fxml`; registers the demo tabs and shows the active one. */
class MenuTabShowcaseWindowController : Initializable {

    @FXML
    private lateinit var menuTab: FXMenuTab

    @FXML
    private lateinit var activeTabLabel: Label

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val home = MenuTab("home", "Home")
        val view = MenuTab("view", "View")
        val tools = MenuTab("tools", "Tools")
        val disabled = MenuTab("disabled", "Disabled").apply { isDisabled = true }
        menuTab.tabs.addAll(home, view, tools, disabled)
        menuTab.activeTab = home

        activeTabLabel.textProperty().bind(
            Bindings.createStringBinding(
                { "Active tab: ${menuTab.activeTab?.title ?: "none"}" },
                menuTab.activeTabProperty(),
            ),
        )
    }
}
