package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * State of the menu tab strip: the registered [tabs] and the currently [activeTab]. Holds no
 * scene graph - the [FXMenuTabView] renders it.
 */
internal class FXMenuTabViewModel : ViewModel {

    val tabs: ObservableList<MenuTab> = FXCollections.observableArrayList()

    val activeTab: ObjectProperty<MenuTab?> = SimpleObjectProperty(this, "activeTab", null)
}
