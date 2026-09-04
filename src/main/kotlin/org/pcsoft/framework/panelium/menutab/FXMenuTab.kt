package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
import javafx.scene.layout.StackPane

/**
 * A row of tabs rendered as a tab strip. Tabs are registered through [tabs]; [activeTab] tracks
 * which one is currently selected and can be changed either from code or by the user (click, or
 * left/right arrow while the strip is focused). Usable from FXML through the `<fx:root>` pattern.
 *
 * Style classes: `menu-tab` on the component itself, `menu-tab-strip-button` on each tab button.
 */
public class FXMenuTab : StackPane() {

    private val viewModel: FXMenuTabViewModel

    init {
        val tuple = FluentViewLoader.fxmlView(FXMenuTabView::class.java)
            .root(this)
            .load()
        viewModel = tuple.viewModel

        styleClass.add("menu-tab")
    }

    /** The permanent tabs shown in the strip. */
    public val tabs: ObservableList<MenuTab> get() = viewModel.tabs

    /** The currently active tab, or `null` when none is active. */
    public fun activeTabProperty(): ObjectProperty<MenuTab?> = viewModel.activeTab

    public var activeTab: MenuTab?
        get() = viewModel.activeTab.get()
        set(value) = viewModel.activeTab.set(value)

    /** Activates [tab]. [tab] MUST already be registered in [tabs]. */
    public fun activate(tab: MenuTab) {
        require(viewModel.tabs.contains(tab)) { "Tab is not registered: ${tab.id}" }
        viewModel.activeTab.set(tab)
    }
}
