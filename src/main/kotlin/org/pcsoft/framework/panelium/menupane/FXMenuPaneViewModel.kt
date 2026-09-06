package org.pcsoft.framework.panelium.menupane

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
 * Holds no scene graph - the [FXMenuPaneView] renders it.
 */
internal class FXMenuPaneViewModel : ViewModel {

    val tabs: ObservableList<FXMenuTab> = FXCollections.observableArrayList()

    val contextualTabs: ObservableList<FXMenuTab> = FXCollections.observableArrayList()

    val visibleTabs: ObservableList<FXMenuTab> = FXCollections.observableArrayList()

    val activeTab: ObjectProperty<FXMenuTab?> = SimpleObjectProperty(this, "activeTab", null)

    val fileTab: ObjectProperty<FXMenuTab?> = SimpleObjectProperty(this, "fileTab", null)

    val backstageContent: ObjectProperty<Node?> = SimpleObjectProperty(this, "backstageContent", null)

    val fileTabActive: BooleanProperty = SimpleBooleanProperty(this, "fileTabActive", false)

    val groupByTab: MutableMap<FXMenuTab, FXMenuContextTabGroup> = mutableMapOf()

    var previousPermanentTab: FXMenuTab? = null

    var previousActiveTab: FXMenuTab? = null

    var hasOverlayHost: Boolean = false

    init {
        val listener = ListChangeListener<FXMenuTab> { rebuildVisibleTabs() }
        tabs.addListener(listener)
        contextualTabs.addListener(listener)
    }

    fun rebuildVisibleTabs() {
        visibleTabs.setAll(tabs + contextualTabs)
    }
}
