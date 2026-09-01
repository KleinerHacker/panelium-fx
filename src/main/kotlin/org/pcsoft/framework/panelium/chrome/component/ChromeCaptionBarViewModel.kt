package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
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
 * State of the caption bar: the three content slots, the default title / icon, the reserved
 * caption-button slot and the [captionOs] that decides which side that slot and the default
 * title / icon sit on. Holds no scene graph - the [ChromeCaptionBarView] renders it.
 */
internal class ChromeCaptionBarViewModel : ViewModel {

    val leftItems: ObservableList<Node> = FXCollections.observableArrayList()
    val centerItems: ObservableList<Node> = FXCollections.observableArrayList()
    val rightItems: ObservableList<Node> = FXCollections.observableArrayList()

    val titleText: StringProperty = SimpleStringProperty(this, "titleText", "")
    val iconImage: ObjectProperty<Image?> = SimpleObjectProperty(this, "iconImage", null)

    val defaultTitleVisible: BooleanProperty = SimpleBooleanProperty(this, "defaultTitleVisible", true)
    val defaultIconVisible: BooleanProperty = SimpleBooleanProperty(this, "defaultIconVisible", true)

    /** Minimum caption height; driven by `ChromePane`'s `-panelium-caption-min-height` property. */
    val captionMinHeight: DoubleProperty = SimpleDoubleProperty(this, "captionMinHeight", 32.0)

    /** Filled by IP-05 with the OS-specific caption buttons; rendered on top of the slots. */
    val captionButtonSlot: ObjectProperty<Node?> = SimpleObjectProperty(this, "captionButtonSlot", null)

    /**
     * The OS whose native button placement / look the bar follows. Defaults to the detected OS and
     * can be overridden (via [ChromePane.captionOsProperty]) for tests and demos.
     */
    val captionOs: ObjectProperty<ChromeOs> = SimpleObjectProperty(this, "captionOs", ChromeOs.detect())

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
}
