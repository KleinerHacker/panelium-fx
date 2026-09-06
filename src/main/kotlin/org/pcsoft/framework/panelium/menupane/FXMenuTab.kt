package org.pcsoft.framework.panelium.menupane

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * A single tab registered on an [FXMenuPane]. Identified by [id], labelled by [title]; [disabled]
 * disables its tab-strip button and blocks activation - a disabled tab is skipped by
 * [FXMenuPane.activate], by setting [FXMenuPane.activeTab] and by arrow-key navigation. [groups]
 * holds the ordered [FXMenuGroup]s shown in the group strip while this tab is the active regular
 * tab; add, remove or reorder them through the list directly.
 */
class FXMenuTab(
    val id: String,
    val title: String,
) {

    val disabled: BooleanProperty = SimpleBooleanProperty(this, "disabled", false)

    var isDisabled: Boolean
        get() = disabled.get()
        set(value) = disabled.set(value)

    val groups: ObservableList<FXMenuGroup> = FXCollections.observableArrayList()
}
