package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node

/**
 * State of a [ChromePane]: the hosted content node and whether the drop shadow is drawn. Window
 * operations stay in [ChromePane] / `WindowOps`, not here.
 */
internal class ChromePaneViewModel : ViewModel {

    val content: ObjectProperty<Node?> = SimpleObjectProperty(this, "content", null)

    val shadowEnabled: BooleanProperty = SimpleBooleanProperty(this, "shadowEnabled", true)
}
