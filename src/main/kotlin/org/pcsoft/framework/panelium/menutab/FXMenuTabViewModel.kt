package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

/**
 * State of the menu tab strip: the permanent [tabs], the [contextualTabs], the merged
 * [visibleTabs] (permanent first, then contextual, each in insertion order), the currently
 * [activeTab], the group assignments in [groupByTab], and the [previousPermanentTab] used to fall
 * back to when the active contextual tab is removed. Holds no scene graph - the [FXMenuTabView]
 * renders it.
 */
internal class FXMenuTabViewModel : ViewModel {

    val tabs: ObservableList<MenuTab> = FXCollections.observableArrayList()

    val contextualTabs: ObservableList<MenuTab> = FXCollections.observableArrayList()

    val visibleTabs: ObservableList<MenuTab> = FXCollections.observableArrayList()

    val activeTab: ObjectProperty<MenuTab?> = SimpleObjectProperty(this, "activeTab", null)

    val groupByTab: MutableMap<MenuTab, ContextTabGroup> = mutableMapOf()

    var previousPermanentTab: MenuTab? = null

    init {
        val listener = ListChangeListener<MenuTab> { rebuildVisibleTabs() }
        tabs.addListener(listener)
        contextualTabs.addListener(listener)
    }

    fun rebuildVisibleTabs() {
        visibleTabs.setAll(tabs + contextualTabs)
    }
}
