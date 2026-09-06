package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.animation.FadeTransition
import javafx.beans.InvalidationListener
import javafx.collections.ListChangeListener
import javafx.css.PseudoClass
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleButton
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [FXMenuTabViewModel]: an HBox of one toggle button per visible tab (permanent, then
 * contextual), with a group-header label inserted before the first button of each context group.
 * Clicking a button, or pressing left/right arrow while the strip is focused, activates the
 * corresponding tab. The strip is embedded in a horizontally scrolling [ScrollPane] so an
 * overflowing set of tabs stays reachable without shrinking the buttons.
 *
 * Below the tab-strip row sits the group strip: the [FXMenuGroup]s of the active regular tab
 * ([MenuTab.groups]), rebuilt on every tab switch and kept in sync while that tab stays active. It
 * is emptied while the backstage is open and restored when it closes. The tab-strip row and the
 * group strip share the `bandColumn` VBox; the backstage layer is anchored to its bottom edge.
 *
 * The file tab from [FXMenuTabViewModel.fileTab] is rendered as a separate button pinned before
 * the scrolling strip. Clicking it toggles [FXMenuTabViewModel.fileTabActive]. While active and no
 * [FXMenuTab.overlayHost] is set, the `#backstageContentSlot` is faded in over 0.3 seconds as an
 * unmanaged layer that starts just below the band - so it never enlarges the ribbon band and never
 * covers the pressed file-tab button. A scene-level Escape / outside-click filter closes it again,
 * as does selecting a strip tab.
 */
internal class FXMenuTabView : FxmlView<FXMenuTabViewModel>, Initializable {

    @FXML
    private lateinit var root: StackPane

    @FXML
    private lateinit var bandColumn: VBox

    @FXML
    private lateinit var tabStripRow: HBox

    @FXML
    private lateinit var fileTabButton: ToggleButton

    @FXML
    private lateinit var tabStripScrollPane: ScrollPane

    @FXML
    private lateinit var tabStrip: HBox

    @FXML
    private lateinit var groupStrip: HBox

    @FXML
    private lateinit var backstageContentSlot: StackPane

    @InjectViewModel
    private lateinit var viewModel: FXMenuTabViewModel

    private val buttonsByTab: MutableMap<MenuTab, ToggleButton> = mutableMapOf()

    private var backstageFade: FadeTransition? = null
    private var filteredScene: Scene? = null

    private val repositionListener = InvalidationListener { positionBackstageSlot() }

    private val activeGroupsListener = ListChangeListener<FXMenuGroup> { renderGroups() }
    private var observedGroupsTab: MenuTab? = null

    private val backstageKeyFilter = EventHandler<KeyEvent> { event ->
        if (event.code == KeyCode.ESCAPE) {
            viewModel.fileTabActive.set(false)
            event.consume()
        }
    }

    private val backstageMouseFilter = EventHandler<MouseEvent> { event ->
        if (!viewModel.fileTabActive.get()) {
            return@EventHandler
        }
        val target = event.target
        if (target !is Node) {
            viewModel.fileTabActive.set(false)
            return@EventHandler
        }
        // The panel may live in the local slot or, with an overlay host, be reparented into the
        // host's own layer - treat a click on either as "inside".
        val hostedPanel = viewModel.backstageContent.get()
        if (isInside(target, backstageContentSlot) ||
            isInside(target, fileTabButton) ||
            (hostedPanel != null && isInside(target, hostedPanel))
        ) {
            return@EventHandler
        }
        viewModel.fileTabActive.set(false)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        rebuildButtons()
        viewModel.visibleTabs.addListener(ListChangeListener { rebuildButtons() })
        viewModel.activeTab.addListener { _, _, active ->
            updateActiveStyle(active)
            scrollToTab(active)
            syncGroupsObserver(active)
            renderGroups()
        }

        rebuildFileTabButton(viewModel.fileTab.get())
        viewModel.fileTab.addListener { _, _, fileTab -> rebuildFileTabButton(fileTab) }

        updateBackstageContent(viewModel.backstageContent.get())
        viewModel.backstageContent.addListener { _, _, content -> updateBackstageContent(content) }

        fileTabButton.setOnAction { viewModel.fileTabActive.set(fileTabButton.isSelected) }
        viewModel.fileTabActive.addListener { _, _, active ->
            applyBackstageState(active)
            renderGroups()
        }

        root.widthProperty().addListener(repositionListener)
        root.heightProperty().addListener(repositionListener)
        bandColumn.layoutBoundsProperty().addListener(repositionListener)

        tabStrip.addEventFilter(KeyEvent.KEY_PRESSED, ::onKeyPressed)
        tabStripScrollPane.addEventFilter(ScrollEvent.SCROLL, ::onScroll)

        syncGroupsObserver(viewModel.activeTab.get())
        renderGroups()
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
            button.setOnAction { selectStripTab(tab) }
            buttonsByTab[tab] = button
            children.add(button)
        }
        tabStrip.children.setAll(children)
        updateActiveStyle(viewModel.activeTab.get())
        scrollToTab(viewModel.activeTab.get())
    }

    /** Activates [tab] from the strip, closing the backstage first so it never stays behind it. */
    private fun selectStripTab(tab: MenuTab) {
        viewModel.fileTabActive.set(false)
        viewModel.activeTab.set(tab)
    }

    /**
     * Keeps [activeGroupsListener] attached to the currently active regular tab's [MenuTab.groups],
     * so edits to that list while the tab stays active are reflected in the group strip.
     */
    private fun syncGroupsObserver(active: MenuTab?) {
        if (observedGroupsTab === active) {
            return
        }
        observedGroupsTab?.groups?.removeListener(activeGroupsListener)
        observedGroupsTab = active
        active?.groups?.addListener(activeGroupsListener)
    }

    /**
     * Fills the group strip with the active regular tab's [MenuTab.groups]. Empties it when there is
     * no active tab or while the file tab's backstage is open.
     */
    private fun renderGroups() {
        val active = viewModel.activeTab.get()
        if (active == null || viewModel.fileTabActive.get()) {
            groupStrip.children.clear()
            return
        }
        groupStrip.children.setAll(active.groups)
    }

    private fun rebuildFileTabButton(fileTab: MenuTab?) {
        fileTabButton.disableProperty().unbind()
        if (fileTab == null) {
            viewModel.fileTabActive.set(false)
            fileTabButton.isSelected = false
            fileTabButton.text = ""
            fileTabButton.isDisable = false
            fileTabButton.isVisible = false
            fileTabButton.isManaged = false
            return
        }
        fileTabButton.text = fileTab.title
        fileTabButton.disableProperty().bind(fileTab.disabled)
        fileTabButton.isVisible = true
        fileTabButton.isManaged = true
    }

    private fun updateBackstageContent(content: Node?) {
        if (content == null) {
            backstageContentSlot.children.clear()
        } else {
            backstageContentSlot.children.setAll(content)
        }
    }

    /**
     * Shows or hides the backstage layer with a 0.3s fade and installs the dismissal hooks. The
     * layer stays unmanaged, so toggling it never changes the ribbon band's own size; it is
     * positioned by [positionBackstageSlot].
     */
    private fun applyBackstageState(active: Boolean) {
        fileTabButton.isSelected = active
        backstageFade?.stop()
        installBackstageSceneHooks(active)

        if (viewModel.hasOverlayHost) {
            return
        }

        if (active) {
            backstageContentSlot.opacity = 0.0
            backstageContentSlot.isVisible = true
            positionBackstageSlot()
            backstageFade = FadeTransition(BACKSTAGE_FADE_DURATION, backstageContentSlot).apply {
                fromValue = 0.0
                toValue = 1.0
                play()
            }
            return
        }

        if (!backstageContentSlot.isVisible) {
            backstageContentSlot.opacity = 1.0
            return
        }
        backstageFade = FadeTransition(BACKSTAGE_FADE_DURATION, backstageContentSlot).apply {
            fromValue = backstageContentSlot.opacity
            toValue = 0.0
            setOnFinished {
                backstageContentSlot.isVisible = false
                backstageContentSlot.opacity = 1.0
            }
            play()
        }
    }

    /**
     * Lays the unmanaged backstage layer out from the bottom edge of the ribbon band down to the
     * bottom of the scene, spanning the ribbon's width. A no-op while the layer is hidden.
     */
    private fun positionBackstageSlot() {
        if (!backstageContentSlot.isVisible) {
            return
        }
        val top = bandColumn.boundsInParent.maxY
        val width = root.width
        val scene = root.scene
        val height = if (scene != null) {
            (scene.height - root.localToScene(0.0, top).y).coerceAtLeast(backstageContentSlot.prefHeight(width))
        } else {
            backstageContentSlot.prefHeight(width)
        }.coerceAtLeast(1.0)
        backstageContentSlot.resizeRelocate(0.0, top, width.coerceAtLeast(1.0), height)
    }

    private fun installBackstageSceneHooks(active: Boolean) {
        val scene = backstageContentSlot.scene
        if (filteredScene != null && (!active || filteredScene !== scene)) {
            filteredScene?.let { previous ->
                previous.removeEventFilter(KeyEvent.KEY_PRESSED, backstageKeyFilter)
                previous.removeEventFilter(MouseEvent.MOUSE_PRESSED, backstageMouseFilter)
                previous.heightProperty().removeListener(repositionListener)
                previous.widthProperty().removeListener(repositionListener)
            }
            filteredScene = null
        }
        if (active && scene != null && filteredScene == null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, backstageKeyFilter)
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, backstageMouseFilter)
            scene.heightProperty().addListener(repositionListener)
            scene.widthProperty().addListener(repositionListener)
            filteredScene = scene
        }
    }

    private fun isInside(node: Node, ancestor: Node): Boolean {
        var current: Node? = node
        while (current != null) {
            if (current === ancestor) {
                return true
            }
            current = current.parent
        }
        return false
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
        selectStripTab(tabs[nextIndex])
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
        val BACKSTAGE_FADE_DURATION: Duration = Duration.seconds(0.3)
    }
}
