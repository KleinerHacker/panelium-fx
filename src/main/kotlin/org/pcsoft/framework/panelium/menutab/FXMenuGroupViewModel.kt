package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Node

/**
 * State of a single menu group: its [title] and the ordered [content] nodes it hosts. Holds no
 * scene graph - the [FXMenuGroupView] renders it.
 */
internal class FXMenuGroupViewModel : ViewModel {

    val title: StringProperty = SimpleStringProperty(this, "title", "")

    val content: ObservableList<Node> = FXCollections.observableArrayList()
}
