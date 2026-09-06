package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.Node

/**
 * State of the menu tab strip: the permanent [tabs], the [contextualTabs], the merged
 * [visibleTabs] (permanent first, then contextual, each in insertion order), the currently
 * [activeTab], the group assignments in [groupByTab], and the [previousPermanentTab] used to fall
 * back to when the active contextual tab is removed.
 *
 * [fileTab] is the distinguished first tab. It is held in its own slot, deliberately kept out of
 * [tabs], [contextualTabs] and [visibleTabs], and rendered as a separate button before the strip.
 * [backstageContent] is the application-supplied panel the file tab's backstage occupies.
 * [fileTabActive] tracks whether the backstage is currently open; [previousActiveTab] remembers the
 * strip tab that was active when it opened so it can be restored on close. [hasOverlayHost] tells
 * the view whether an external host paints the backstage (so the local overlay slot stays unused).
 *
 * Holds no scene graph - the [FXMenuTabView] renders it.
 */
internal class FXMenuTabViewModel : ViewModel {

    val tabs: ObservableList<MenuTab> = FXCollections.observableArrayList()

    val contextualTabs: ObservableList<MenuTab> = FXCollections.observableArrayList()

    val visibleTabs: ObservableList<MenuTab> = FXCollections.observableArrayList()

    val activeTab: ObjectProperty<MenuTab?> = SimpleObjectProperty(this, "activeTab", null)

    val fileTab: ObjectProperty<MenuTab?> = SimpleObjectProperty(this, "fileTab", null)

    val backstageContent: ObjectProperty<Node?> = SimpleObjectProperty(this, "backstageContent", null)

    val fileTabActive: BooleanProperty = SimpleBooleanProperty(this, "fileTabActive", false)

    val groupByTab: MutableMap<MenuTab, ContextTabGroup> = mutableMapOf()

    var previousPermanentTab: MenuTab? = null

    var previousActiveTab: MenuTab? = null

    var hasOverlayHost: Boolean = false

    init {
        val listener = ListChangeListener<MenuTab> { rebuildVisibleTabs() }
        tabs.addListener(listener)
        contextualTabs.addListener(listener)
    }

    fun rebuildVisibleTabs() {
        visibleTabs.setAll(tabs + contextualTabs)
    }
}
