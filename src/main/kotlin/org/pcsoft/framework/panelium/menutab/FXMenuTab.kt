package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.StackPane

/**
 * A row of tabs rendered as a tab strip. Permanent tabs are registered through [tabs]; contextual
 * tabs - shown after the permanent ones, and only relevant to a particular context - through
 * [contextualTabs]. [activeTab] tracks which one is currently selected and can be changed either
 * from code or by the user (click, or left/right arrow while the strip is focused). Removing the
 * active contextual tab falls back to the previously active permanent tab. Usable from FXML
 * through the `<fx:root>` pattern.
 *
 * [fileTab] is the distinguished first tab (the "File" menu). It is NOT part of [tabs] or
 * [visibleTabs]: it lives in its own slot and is drawn as a separate button pinned before the
 * strip, so it never scrolls and is never reached by arrow-key navigation. Activating it opens the
 * backstage: [fileTabActive] flips to `true`, [backstageContent] is shown - through [overlayHost]
 * when one is set, otherwise in the component's own overlay slot with a short fade - and the
 * previously active strip tab is remembered. The backstage closes on Escape, on a click outside its
 * content, or when a strip tab is selected again, restoring that remembered tab and invoking
 * [onBackstageClosed] for a host to restore its ribbon collapse state.
 *
 * Style classes: `menu-tab` on the component itself, `menu-tab-strip-button` on each tab button,
 * `menu-tab-strip-file-button` on the file-tab button, `menu-tab-context-group-header` on each
 * context-group header.
 */
class FXMenuTab : StackPane() {

    private val viewModel: FXMenuTabViewModel

    init {
        val tuple = FluentViewLoader.fxmlView(FXMenuTabView::class.java)
            .root(this)
            .load()
        viewModel = tuple.viewModel

        styleClass.add("menu-tab")

        viewModel.tabs.addListener(ListChangeListener { onTabsChanged(it) })
        viewModel.contextualTabs.addListener(ListChangeListener { onContextualTabsChanged(it) })
        viewModel.activeTab.addListener { _, _, active ->
            if (active != null && viewModel.tabs.contains(active)) {
                viewModel.previousPermanentTab = active
            }
        }
        viewModel.fileTabActive.addListener { _, _, active -> onFileTabActiveChanged(active) }
    }

    /** The permanent tabs shown in the strip. */
    val tabs: ObservableList<MenuTab> get() = viewModel.tabs

    /** The contextual tabs shown after the permanent tabs, in insertion order. */
    val contextualTabs: ObservableList<MenuTab> get() = viewModel.contextualTabs

    /** The currently active tab, or `null` when none is active. */
    fun activeTabProperty(): ObjectProperty<MenuTab?> = viewModel.activeTab

    var activeTab: MenuTab?
        get() = viewModel.activeTab.get()
        set(value) = viewModel.activeTab.set(value)

    /**
     * The distinguished first tab, drawn as a separate button before the strip, or `null` when the
     * component has no file tab. It is kept out of [tabs] and [visibleTabs] on purpose.
     */
    fun fileTabProperty(): ObjectProperty<MenuTab?> = viewModel.fileTab

    var fileTab: MenuTab?
        get() = viewModel.fileTab.get()
        set(value) = viewModel.fileTab.set(value)

    /** The application-supplied panel the file tab's backstage shows. */
    fun backstageContentProperty(): ObjectProperty<Node?> = viewModel.backstageContent

    var backstageContent: Node?
        get() = viewModel.backstageContent.get()
        set(value) = viewModel.backstageContent.set(value)

    /** Whether the file tab's backstage is currently open. */
    fun fileTabActiveProperty(): BooleanProperty = viewModel.fileTabActive

    var isFileTabActive: Boolean
        get() = viewModel.fileTabActive.get()
        set(value) = viewModel.fileTabActive.set(value)

    /**
     * The host that paints [backstageContent] above the whole window while the backstage is open.
     * When `null`, [FXMenuTab] shows the backstage in its own overlay slot instead.
     */
    var overlayHost: BackstageOverlayHost? = null
        set(value) {
            field = value
            viewModel.hasOverlayHost = value != null
        }

    /**
     * Invoked after the backstage has closed and the previously active strip tab has been restored.
     * A host uses this to restore the ribbon's prior collapse state (wired up by a later plan).
     */
    var onBackstageClosed: (() -> Unit)? = null

    /** Activates [tab]. [tab] MUST already be registered in [tabs] or [contextualTabs]. */
    fun activate(tab: MenuTab) {
        require(viewModel.tabs.contains(tab) || viewModel.contextualTabs.contains(tab)) {
            "Tab is not registered: ${tab.id}"
        }
        viewModel.activeTab.set(tab)
    }

    /** Assigns [tab] to [group], rendering a group header above it in the tab strip. */
    fun assignToGroup(tab: MenuTab, group: ContextTabGroup) {
        viewModel.groupByTab[tab] = group
        viewModel.rebuildVisibleTabs()
    }

    /** The group [tab] is assigned to, or `null` when it is not assigned to any group. */
    fun groupOf(tab: MenuTab): ContextTabGroup? = viewModel.groupByTab[tab]

    private fun onFileTabActiveChanged(active: Boolean) {
        if (active) {
            viewModel.previousActiveTab = viewModel.activeTab.get()
            viewModel.backstageContent.get()?.let { overlayHost?.showOverlay(it) }
        } else {
            overlayHost?.hideOverlay()
            viewModel.previousActiveTab?.let { restored ->
                if (viewModel.activeTab.get() == null) {
                    viewModel.activeTab.set(restored)
                }
            }
            viewModel.previousActiveTab = null
            onBackstageClosed?.invoke()
        }
    }

    private fun onTabsChanged(change: ListChangeListener.Change<out MenuTab>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.removed.forEach { viewModel.groupByTab.remove(it) }
                if (viewModel.previousPermanentTab in change.removed) {
                    viewModel.previousPermanentTab = null
                }
                if (viewModel.previousActiveTab in change.removed) {
                    viewModel.previousActiveTab = null
                }
            }
        }
    }

    private fun onContextualTabsChanged(change: ListChangeListener.Change<out MenuTab>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.removed.forEach { viewModel.groupByTab.remove(it) }
                if (viewModel.activeTab.get() in change.removed) {
                    viewModel.activeTab.set(viewModel.previousPermanentTab)
                }
                if (viewModel.previousActiveTab in change.removed) {
                    viewModel.previousActiveTab = null
                }
            }
        }
    }
}
