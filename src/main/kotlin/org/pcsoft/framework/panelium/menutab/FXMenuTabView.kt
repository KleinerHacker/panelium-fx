package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.collections.ListChangeListener
import javafx.css.PseudoClass
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleButton
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [FXMenuTabViewModel]: an HBox of one toggle button per visible tab (permanent, then
 * contextual), with a group-header label inserted before the first button of each context group.
 * Clicking a button, or pressing left/right arrow while the strip is focused, activates the
 * corresponding tab. The strip is embedded in a horizontally scrolling [ScrollPane] so an
 * overflowing set of tabs stays reachable without shrinking the buttons.
 */
internal class FXMenuTabView : FxmlView<FXMenuTabViewModel>, Initializable {

    @FXML
    private lateinit var root: StackPane

    @FXML
    private lateinit var tabStripScrollPane: ScrollPane

    @FXML
    private lateinit var tabStrip: HBox

    @InjectViewModel
    private lateinit var viewModel: FXMenuTabViewModel

    private val buttonsByTab: MutableMap<MenuTab, ToggleButton> = mutableMapOf()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        rebuildButtons()
        viewModel.visibleTabs.addListener(ListChangeListener { rebuildButtons() })
        viewModel.activeTab.addListener { _, _, active ->
            updateActiveStyle(active)
            scrollToTab(active)
        }

        tabStrip.addEventFilter(KeyEvent.KEY_PRESSED, ::onKeyPressed)
        tabStripScrollPane.addEventFilter(ScrollEvent.SCROLL, ::onScroll)
    }

    private fun rebuildButtons() {
        buttonsByTab.clear()
        val children = mutableListOf<Node>()
        var lastGroup: ContextTabGroup? = null
        for (tab in viewModel.visibleTabs) {
            val group = viewModel.groupByTab[tab]
            if (group != null && group !== lastGroup) {
                children.add(createGroupHeader(group))
            }
            lastGroup = group

            val button = ToggleButton(tab.title)
            button.styleClass.add("menu-tab-strip-button")
            button.disableProperty().bind(tab.disabled)
            button.setOnAction { viewModel.activeTab.set(tab) }
            buttonsByTab[tab] = button
            children.add(button)
        }
        tabStrip.children.setAll(children)
        updateActiveStyle(viewModel.activeTab.get())
        scrollToTab(viewModel.activeTab.get())
    }

    private fun createGroupHeader(group: ContextTabGroup): Label {
        val header = Label(group.name)
        header.styleClass.add("menu-tab-context-group-header")
        return header
    }

    private fun updateActiveStyle(active: MenuTab?) {
        buttonsByTab.forEach { (tab, button) ->
            val isActive = tab == active
            button.isSelected = isActive
            button.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, isActive)
        }
    }

    private fun onKeyPressed(event: KeyEvent) {
        val tabs = viewModel.visibleTabs
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

    private fun onScroll(event: ScrollEvent) {
        val contentWidth = tabStrip.width
        val viewportWidth = tabStripScrollPane.viewportBounds.width
        val scrollableWidth = contentWidth - viewportWidth
        if (scrollableWidth <= 0) {
            return
        }

        val deltaValue = -event.deltaY / scrollableWidth
        tabStripScrollPane.hvalue = (tabStripScrollPane.hvalue + deltaValue).coerceIn(0.0, 1.0)
        event.consume()
    }

    private fun scrollToTab(tab: MenuTab?) {
        val button = buttonsByTab[tab] ?: return
        val contentWidth = tabStrip.width
        val viewportWidth = tabStripScrollPane.viewportBounds.width
        val scrollableWidth = contentWidth - viewportWidth
        if (scrollableWidth <= 0) {
            return
        }

        val buttonMinX = button.boundsInParent.minX
        val buttonMaxX = button.boundsInParent.maxX
        val visibleMinX = tabStripScrollPane.hvalue * scrollableWidth
        val visibleMaxX = visibleMinX + viewportWidth

        val targetMinX = when {
            buttonMinX < visibleMinX -> buttonMinX
            buttonMaxX > visibleMaxX -> buttonMaxX - viewportWidth
            else -> return
        }
        tabStripScrollPane.hvalue = (targetMinX / scrollableWidth).coerceIn(0.0, 1.0)
    }

    private companion object {
        val ACTIVE_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("active")
    }
}
