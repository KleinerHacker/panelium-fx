package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.collections.ListChangeListener
import javafx.css.PseudoClass
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ToggleButton
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [FXMenuTabViewModel]: an HBox of one toggle button per registered tab. Clicking a
 * button, or pressing left/right arrow while the strip is focused, activates the corresponding
 * tab.
 */
internal class FXMenuTabView : FxmlView<FXMenuTabViewModel>, Initializable {

    @FXML
    private lateinit var root: StackPane

    @FXML
    private lateinit var tabStrip: HBox

    @InjectViewModel
    private lateinit var viewModel: FXMenuTabViewModel

    private val buttonsByTab: MutableMap<MenuTab, ToggleButton> = mutableMapOf()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        rebuildButtons()
        viewModel.tabs.addListener(ListChangeListener { rebuildButtons() })
        viewModel.activeTab.addListener { _, _, active -> updateActiveStyle(active) }

        tabStrip.addEventFilter(KeyEvent.KEY_PRESSED, ::onKeyPressed)
    }

    private fun rebuildButtons() {
        buttonsByTab.clear()
        val buttons = viewModel.tabs.map { tab ->
            val button = ToggleButton(tab.title)
            button.styleClass.add("menu-tab-strip-button")
            button.disableProperty().bind(tab.disabled)
            button.setOnAction { viewModel.activeTab.set(tab) }
            buttonsByTab[tab] = button
            button
        }
        tabStrip.children.setAll(buttons)
        updateActiveStyle(viewModel.activeTab.get())
    }

    private fun updateActiveStyle(active: MenuTab?) {
        buttonsByTab.forEach { (tab, button) ->
            val isActive = tab == active
            button.isSelected = isActive
            button.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, isActive)
        }
    }

    private fun onKeyPressed(event: KeyEvent) {
        val tabs = viewModel.tabs
        if (tabs.isEmpty()) {
            return
        }

        val delta = when (event.code) {
            KeyCode.LEFT -> -1
            KeyCode.RIGHT -> 1
            else -> return
        }

        val currentIndex = tabs.indexOf(viewModel.activeTab.get()).takeIf { it >= 0 } ?: 0
        val nextIndex = (currentIndex + delta + tabs.size) % tabs.size
        viewModel.activeTab.set(tabs[nextIndex])
        event.consume()
    }

    private companion object {
        val ACTIVE_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("active")
    }
}
