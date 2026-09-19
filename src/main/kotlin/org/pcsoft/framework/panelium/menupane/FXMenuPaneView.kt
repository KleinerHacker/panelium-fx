/*
 * Copyright (c) KleinerHacker alias Pfeiffer C Soft 2026.
 * This work is licensed under the Apache License, Version 2.0.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations.
 */

package org.pcsoft.framework.panelium.menupane

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.animation.FadeTransition
import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
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
import javafx.scene.input.ContextMenuEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.util.Duration
import java.net.URL
import java.util.*
import kotlin.math.roundToInt

/**
 * Renders [FXMenuPaneViewModel]: an HBox of one toggle button per visible tab (permanent, then
 * contextual), with a group-header label inserted before the first button of each context group.
 * Clicking a button, or pressing left/right arrow while the strip is focused, activates the
 * corresponding tab. Disabled tabs ([FXMenuTab.disabledProperty]) are skipped by click and arrow-key
 * navigation alike. The strip is embedded in a horizontally scrolling [ScrollPane] so an
 * overflowing set of tabs stays reachable without shrinking the buttons.
 *
 * Below the tab-strip row sits the group strip: the [FXMenuGroup]s of the active regular tab
 * ([FXMenuTab.groups]), rebuilt on every tab switch and kept in sync while that tab stays active. It
 * is emptied while the backstage is open and restored when it closes. The tab-strip row and the
 * group strip share the `bandColumn` VBox; the backstage layer is anchored to its bottom edge. The
 * group strip has a fixed height (`menu-pane-group-strip` in `menu-pane.css`), so the band keeps the
 * same height regardless of which tab's groups it currently shows. The `groupStrip` HBox is held at
 * least as wide as its scroll-pane viewport, so the `menu-pane-group-strip` background spans the
 * full ribbon width even when the groups themselves are narrower; on real overflow it still grows
 * past the viewport and scrolls.
 *
 * The ribbon collapses to just the tab-strip row when [FXMenuPaneViewModel.collapsed] is set: the
 * group strip is hidden and unmanaged, so the band shrinks. A double-click on the active tab button
 * flips it ([toggleCollapsed]); the `menu-pane-collapse-toggle` toggle button is kept in sync with
 * the inverse of [FXMenuPaneViewModel.collapsed] (selected while the ribbon is shown, released while
 * it is collapsed), both ways, so it always reflects the current state. Its chevron icon
 * (`menu-pane-collapse-toggle-icon`, an `-fx-shape` region) flips direction purely through the
 * `collapsed` pseudo-class on the component. While collapsed a single click on a tab starts a
 * transient peek ([FXMenuPaneViewModel.peekActive]) that shows that tab's groups again ([startPeek]);
 * a scene-level mouse filter ends the peek on an outside click, and re-clicking the active tab ends
 * it too. Opening the file-tab backstage saves the collapse state and closing it restores that saved
 * value.
 *
 * A right-click on the tab-strip row or the group strip opens the [RibbonContextMenu] at the cursor,
 * unless [FXMenuPaneViewModel.contextMenuEnabled] is `false` or the menu would have nothing to show.
 * Its built-in entry flips the collapse state ([toggleCollapsed]) and its label mirrors that state;
 * [FXMenuPaneViewModel.contextMenuItems] adds host entries ahead of it.
 *
 * Each tab button carries the `active` pseudo-class while its tab is the active tab and the
 * `contextual` pseudo-class while its tab is one of [FXMenuPaneViewModel.contextualTabs]. A
 * contextual tab in a coloured [FXMenuContextTabGroup] gets that colour as its accent; a contextual
 * tab without one falls back to the host's `-panelium-menu-pane-accent-color`.
 *
 * The file tab from [FXMenuPaneViewModel.fileTab] is rendered as a separate button pinned before
 * the scrolling strip. Clicking it toggles [FXMenuPaneViewModel.fileTabActive]. While active and no
 * [FXMenuPane.overlayHost] is set, the `#backstageContentSlot` is faded in over 0.3 seconds as an
 * unmanaged layer that starts just below the band - so it never enlarges the ribbon band and never
 * covers the pressed file-tab button. A scene-level Escape / outside-click filter closes it again,
 * as does selecting a strip tab; that filter is installed in both modes, so an external overlay
 * host does not have to re-implement dismissal.
 */
internal class FXMenuPaneView : FxmlView<FXMenuPaneViewModel>, Initializable {

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
    private lateinit var collapseToggleButton: ToggleButton

    @FXML
    private lateinit var groupStrip: HBox

    @FXML
    private lateinit var groupStripScrollPane: ScrollPane

    @FXML
    private lateinit var backstageContentSlot: StackPane

    @InjectViewModel
    private lateinit var viewModel: FXMenuPaneViewModel

    private val buttonsByTab: MutableMap<FXMenuTab, ToggleButton> = mutableMapOf()

    private lateinit var groupOverflowCoordinator: MenuGroupStripOverflowCoordinator

    private lateinit var ribbonContextMenu: RibbonContextMenu

    private var backstageFade: FadeTransition? = null
    private var filteredScene: Scene? = null
    private var peekFilteredScene: Scene? = null

    /** Collapse state captured while the file-tab backstage is open, restored when it closes. */
    private var savedCollapsedForBackstage: Boolean? = null

    private val repositionListener = InvalidationListener { positionBackstageSlot() }

    private val activeGroupsListener = ListChangeListener<FXMenuGroup> { renderGroups() }
    private var observedGroupsTab: FXMenuTab? = null

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

    private val peekMouseFilter = EventHandler<MouseEvent> { event ->
        if (!viewModel.peekActive.get()) {
            return@EventHandler
        }
        val target = event.target
        if (target is Node && (isInside(target, groupStrip) || isInside(target, tabStripRow))) {
            return@EventHandler
        }
        endPeek()
    }

    private val ribbonContextMenuRequestFilter = EventHandler<ContextMenuEvent> { event ->
        // Swallow the request without showing anything while the menu is disabled outright, or
        // while it would have nothing to show (no host entries and the built-in toggle inert).
        val hasContent = viewModel.contextMenuItems.isNotEmpty() || viewModel.collapsible.get()
        if (!viewModel.contextMenuEnabled.get() || !hasContent) {
            event.consume()
            return@EventHandler
        }
        ribbonContextMenu.hide()
        ribbonContextMenu.show(root, event.screenX, event.screenY)
        event.consume()
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
            if (active) {
                // Remember the collapse state so closing the backstage restores exactly it.
                savedCollapsedForBackstage = viewModel.collapsed.get()
                endPeek()
            } else {
                savedCollapsedForBackstage?.let { viewModel.collapsed.set(it) }
                savedCollapsedForBackstage = null
            }
            applyBackstageState(active)
            // Re-render on both edges: clear the group strip when the backstage opens, restore the
            // active tab's groups when it closes.
            renderGroups()
            updateGroupStripVisibility()
        }

        // The button is "pinned" (selected) while the ribbon is shown and released while it is
        // collapsed - the inverse of `collapsed`. Kept in sync both ways; setting a boolean property
        // to its current value fires no event, so the two listeners cannot loop.
        collapseToggleButton.isSelected = !viewModel.collapsed.get()
        collapseToggleButton.selectedProperty().addListener { _, _, selected ->
            viewModel.collapsed.set(!selected)
        }
        applyCollapsedState()
        viewModel.collapsed.addListener { _, _, collapsed ->
            collapseToggleButton.isSelected = !collapsed
            if (!collapsed) {
                endPeek()
            }
            applyCollapsedState()
        }
        applyCollapseButtonState()
        viewModel.collapsible.addListener { _, _, _ -> applyCollapseButtonState() }
        viewModel.collapseButtonVisible.addListener { _, _, _ -> applyCollapseButtonState() }
        viewModel.peekActive.addListener { _, _, active ->
            installPeekSceneHook(active)
            updateGroupStripVisibility()
        }

        root.widthProperty().addListener(repositionListener)
        root.heightProperty().addListener(repositionListener)
        bandColumn.layoutBoundsProperty().addListener(repositionListener)

        tabStrip.addEventFilter(KeyEvent.KEY_PRESSED, ::onKeyPressed)
        tabStripScrollPane.addEventFilter(ScrollEvent.SCROLL, ::onScroll)
        groupStripScrollPane.addEventFilter(ScrollEvent.SCROLL, ::onGroupStripScroll)
        groupOverflowCoordinator = MenuGroupStripOverflowCoordinator(groupStripScrollPane, groupStrip)

        // Keep the group strip at least as wide as its viewport so the `menu-pane-group-strip`
        // background fills the full ribbon width instead of ending where the groups end. A real
        // overflow still pushes the strip past its viewport (prefWidth wins over this minWidth), so
        // the overflow coordinator and the last-resort horizontal scroll keep working.
        groupStrip.minWidthProperty().bind(
            Bindings.createDoubleBinding(
                { groupStripScrollPane.viewportBounds.width.coerceAtLeast(0.0) },
                groupStripScrollPane.viewportBoundsProperty(),
            ),
        )

        ribbonContextMenu = RibbonContextMenu(
            viewModel.collapsed,
            viewModel.collapsible,
            viewModel.contextMenuItems,
            ::toggleCollapsed,
        )
        tabStripRow.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, ribbonContextMenuRequestFilter)
        groupStrip.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, ribbonContextMenuRequestFilter)

        // Contextual tab buttons follow the host accent when they carry no coloured group.
        (root as? FXMenuPane)?.accentColorProperty()?.addListener { _, _, _ -> rebuildButtons() }

        syncGroupsObserver(viewModel.activeTab.get())
        renderGroups()
        updateGroupStripVisibility()
    }

    private fun rebuildButtons() {
        buttonsByTab.clear()
        val children = mutableListOf<Node>()
        var lastGroup: FXMenuContextTabGroup? = null
        // A fresh toggle group per rebuild: the strip tab buttons share it so exactly one stays
        // selected. Re-clicking the active tab would otherwise clear the group, leaving no tab
        // selected - the listener below re-selects the active tab's button in that case. The file
        // tab button is intentionally kept out of this group.
        val tabToggleGroup = javafx.scene.control.ToggleGroup()
        for (tab in viewModel.visibleTabs) {
            val group = viewModel.groupByTab[tab]
            if (group != null && group !== lastGroup) {
                children.add(createGroupHeader(group))
            }
            lastGroup = group

            val button = ToggleButton(tab.title)
            button.styleClass.add("menu-pane-strip-button")
            button.toggleGroup = tabToggleGroup
            val contextual = viewModel.contextualTabs.contains(tab)
            button.pseudoClassStateChanged(CONTEXTUAL_PSEUDO_CLASS, contextual)
            applyTabAccent(button, group, contextual)
            button.disableProperty().bind(tab.disabledProperty())
            button.setOnAction { selectStripTab(tab) }
            button.addEventHandler(MouseEvent.MOUSE_CLICKED) { event ->
                if (event.clickCount == 2 && viewModel.activeTab.get() === tab) {
                    toggleCollapsed()
                    event.consume()
                }
            }
            buttonsByTab[tab] = button
            children.add(button)
        }
        tabToggleGroup.selectedToggleProperty().addListener { _, _, selected ->
            if (selected == null) {
                buttonsByTab[viewModel.activeTab.get()]?.let { active ->
                    if (active.toggleGroup === tabToggleGroup) {
                        active.isSelected = true
                    }
                }
            }
        }
        tabStrip.children.setAll(children)
        updateActiveStyle(viewModel.activeTab.get())
        scrollToTab(viewModel.activeTab.get())
    }

    /**
     * Activates [tab] from the strip, closing the backstage first so it never stays behind it. A
     * disabled tab is ignored. While the ribbon is collapsed, the click drives the transient peek:
     * clicking the already-active peeking tab ends the peek, any other click (re)starts it.
     */
    private fun selectStripTab(tab: FXMenuTab) {
        if (tab.isDisabled) {
            return
        }
        val wasActive = viewModel.activeTab.get() === tab
        viewModel.fileTabActive.set(false)
        viewModel.activeTab.set(tab)
        if (viewModel.collapsed.get()) {
            if (wasActive && viewModel.peekActive.get()) {
                endPeek()
            } else {
                startPeek()
            }
        }
    }

    /**
     * Flips [FXMenuPaneViewModel.collapsed] - the double-click-on-active-tab gesture and the
     * chevron/context-menu action. A no-op while [FXMenuPaneViewModel.collapsible] is `false`.
     */
    private fun toggleCollapsed() {
        if (!viewModel.collapsible.get()) {
            return
        }
        viewModel.collapsed.set(!viewModel.collapsed.get())
    }

    /**
     * Shows the collapse/expand chevron only while the ribbon may be collapsed
     * ([FXMenuPaneViewModel.collapsible]) and the chevron is opted in
     * ([FXMenuPaneViewModel.collapseButtonVisible], `false` by default). Ends any transient peek once
     * the ribbon can no longer be collapsed. Forcing the ribbon back to expanded is done by
     * [FXMenuPane] itself.
     */
    private fun applyCollapseButtonState() {
        val collapsible = viewModel.collapsible.get()
        val showButton = collapsible && viewModel.collapseButtonVisible.get()
        collapseToggleButton.isVisible = showButton
        collapseToggleButton.isManaged = showButton
        if (!collapsible) {
            endPeek()
        }
    }

    /**
     * Reveals the active tab's groups transiently while the ribbon is collapsed. A no-op when the
     * ribbon is expanded or the file-tab backstage is open.
     */
    private fun startPeek() {
        if (viewModel.collapsed.get() && !viewModel.fileTabActive.get()) {
            viewModel.peekActive.set(true)
        }
    }

    /** Hides a transient peek reveal. */
    private fun endPeek() {
        viewModel.peekActive.set(false)
    }

    /**
     * Keeps [activeGroupsListener] attached to the currently active regular tab's [FXMenuTab.groups],
     * so edits to that list while the tab stays active are reflected in the group strip.
     */
    private fun syncGroupsObserver(active: FXMenuTab?) {
        if (observedGroupsTab === active) {
            return
        }
        observedGroupsTab?.groups?.removeListener(activeGroupsListener)
        observedGroupsTab = active
        active?.groups?.addListener(activeGroupsListener)
    }

    /**
     * Fills the group strip with the active regular tab's [FXMenuTab.groups]. Empties it when there is
     * no active tab or while the file tab's backstage is open.
     */
    private fun renderGroups() {
        val active = viewModel.activeTab.get()
        if (active == null || viewModel.fileTabActive.get()) {
            groupStrip.children.clear()
            groupOverflowCoordinator.setGroups(emptyList())
            return
        }
        groupStrip.children.setAll(active.groups)
        groupOverflowCoordinator.setGroups(active.groups.toList())
    }

    /**
     * Hides the group strip (and shrinks the band) while the file-tab backstage is open, or while
     * the ribbon is collapsed and no peek is active; shows it otherwise. The tab-strip row always
     * stays visible. Hiding it while the backstage is open stops an expanded ribbon from leaving an
     * empty group strip band above the backstage instead of collapsing down to the tab row.
     */
    private fun updateGroupStripVisibility() {
        val show = !viewModel.fileTabActive.get() &&
            (!viewModel.collapsed.get() || viewModel.peekActive.get())
        groupStripScrollPane.isVisible = show
        groupStripScrollPane.isManaged = show
    }

    /**
     * Reacts to a change of [FXMenuPaneViewModel.collapsed]. The chevron icon on
     * `collapseToggleButton` flips direction through the `collapsed` pseudo-class on the component
     * (see `menu-pane.css`), so only the group strip and the backstage layer need updating here.
     */
    private fun applyCollapsedState() {
        updateGroupStripVisibility()
        positionBackstageSlot()
    }

    private fun rebuildFileTabButton(fileTab: FXMenuTab?) {
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
        fileTabButton.disableProperty().bind(fileTab.disabledProperty())
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
     *
     * The scene-level Escape / outside-click filter is installed in both modes - with the local slot
     * and with an external overlay host that paints the panel itself but still relies on this filter
     * to close the backstage.
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

    /**
     * Installs or removes the scene-level mouse filter that ends a transient peek on a click outside
     * the tab-strip row and the group strip.
     */
    private fun installPeekSceneHook(active: Boolean) {
        val scene = root.scene
        if (peekFilteredScene != null && (!active || peekFilteredScene !== scene)) {
            peekFilteredScene?.removeEventFilter(MouseEvent.MOUSE_PRESSED, peekMouseFilter)
            peekFilteredScene = null
        }
        if (active && scene != null && peekFilteredScene == null) {
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, peekMouseFilter)
            peekFilteredScene = scene
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

    private fun createGroupHeader(group: FXMenuContextTabGroup): Label {
        val header = Label(group.name)
        header.styleClass.add("menu-pane-context-group-header")
        cssColorOrNull(group.color)?.let { header.style = "-fx-text-fill: $it;" }
        return header
    }

    /**
     * Applies the context accent to a tab-strip [button]: a coloured [FXMenuContextTabGroup] wins,
     * otherwise a contextual tab falls back to the host's `-panelium-menu-pane-accent-color`. A
     * permanent tab without a group gets no accent.
     */
    private fun applyTabAccent(button: ToggleButton, group: FXMenuContextTabGroup?, contextual: Boolean) {
        val color = cssColorOrNull(group?.color) ?: if (contextual) toCssColor(hostAccent()) else null
        button.style = if (color != null) "-fx-border-color: $color; -fx-border-width: 0 0 2 0;" else ""
    }

    private fun hostAccent(): Paint? = (root as? FXMenuPane)?.accentColor

    /** A CSS colour literal for [paint] when it is a plain [Color], else `null`. */
    private fun toCssColor(paint: Paint?): String? {
        val color = paint as? Color ?: return null
        return "#%02X%02X%02X%02X".format(
            (color.red * 255).roundToInt(),
            (color.green * 255).roundToInt(),
            (color.blue * 255).roundToInt(),
            (color.opacity * 255).roundToInt(),
        )
    }

    /** [raw] itself when JavaFX can parse it as a colour, else `null`. */
    private fun cssColorOrNull(raw: String?): String? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return try {
            Color.web(raw)
            raw
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun updateActiveStyle(active: FXMenuTab?) {
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

        val startIndex = tabs.indexOf(viewModel.activeTab.get()).takeIf { it >= 0 } ?: 0
        var candidateIndex = startIndex
        for (step in tabs.indices) {
            candidateIndex = (candidateIndex + delta + tabs.size) % tabs.size
            if (!tabs[candidateIndex].isDisabled) {
                selectStripTab(tabs[candidateIndex])
                break
            }
        }
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

    /**
     * Redirects the vertical mouse wheel to horizontal scrolling of the group strip, used only when
     * the groups overflow the strip even after the overflow coordinator collapsed every collapsible
     * box.
     */
    private fun onGroupStripScroll(event: ScrollEvent) {
        val contentWidth = groupStrip.width
        val viewportWidth = groupStripScrollPane.viewportBounds.width
        val scrollableWidth = contentWidth - viewportWidth
        if (scrollableWidth <= 0) {
            return
        }

        val deltaValue = -event.deltaY / scrollableWidth
        groupStripScrollPane.hvalue = (groupStripScrollPane.hvalue + deltaValue).coerceIn(0.0, 1.0)
        event.consume()
    }

    private fun scrollToTab(tab: FXMenuTab?) {
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
        val CONTEXTUAL_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("contextual")
        val BACKSTAGE_FADE_DURATION: Duration = Duration.seconds(0.3)
    }
}
