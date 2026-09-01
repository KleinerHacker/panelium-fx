package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.stage.Stage

/**
 * State of the caption bar: the three content slots, the default title / icon and the reserved
 * caption-button slot. Holds no scene graph - the [ChromeCaptionBarView] renders it.
 */
internal class ChromeCaptionBarViewModel : ViewModel {

    val leftItems: ObservableList<Node> = FXCollections.observableArrayList()
    val centerItems: ObservableList<Node> = FXCollections.observableArrayList()
    val rightItems: ObservableList<Node> = FXCollections.observableArrayList()

    val titleText: StringProperty = SimpleStringProperty(this, "titleText", "")
    val iconImage: ObjectProperty<Image?> = SimpleObjectProperty(this, "iconImage", null)

    val defaultTitleVisible: BooleanProperty = SimpleBooleanProperty(this, "defaultTitleVisible", true)
    val defaultIconVisible: BooleanProperty = SimpleBooleanProperty(this, "defaultIconVisible", true)

    /** Filled by IP-05 with the OS-specific caption buttons; rendered on top of the slots. */
    val captionButtonSlot: ObjectProperty<Node?> = SimpleObjectProperty(this, "captionButtonSlot", null)

    var onMoveStart: ((screenX: Double, screenY: Double) -> Unit)? = null
    var onMove: ((screenX: Double, screenY: Double) -> Unit)? = null

    private var boundStage: Stage? = null
    private var iconListener: ListChangeListener<Image>? = null

    /** Binds the default title / icon to [stage]; they keep following its `title` and `icons`. */
    fun bindStage(stage: Stage) {
        boundStage?.let { previous -> iconListener?.let(previous.icons::removeListener) }

        titleText.bind(stage.titleProperty())
        updateIcon(stage)
        val listener = ListChangeListener<Image> { updateIcon(stage) }
        stage.icons.addListener(listener)

        boundStage = stage
        iconListener = listener
    }

    private fun updateIcon(stage: Stage) {
        iconImage.set(stage.icons.firstOrNull())
    }

    fun moveStart(screenX: Double, screenY: Double) {
        onMoveStart?.invoke(screenX, screenY)
    }

    fun move(screenX: Double, screenY: Double) {
        onMove?.invoke(screenX, screenY)
    }
}
