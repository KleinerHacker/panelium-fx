package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import org.pcsoft.framework.panelium.chrome.internal.ChromeConfig
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [ChromeCaptionBarViewModel]: an HBox row (left / growing center / right) with the
 * reserved caption-button slot stacked on top, plus the default icon and title in the left slot.
 */
internal class ChromeCaptionBarView : FxmlView<ChromeCaptionBarViewModel>, Initializable {

    @FXML
    private lateinit var root: StackPane

    @FXML
    private lateinit var leftBox: HBox

    @FXML
    private lateinit var centerBox: HBox

    @FXML
    private lateinit var rightBox: HBox

    @FXML
    private lateinit var buttonSlot: HBox

    @InjectViewModel
    private lateinit var viewModel: ChromeCaptionBarViewModel

    private val iconView: ImageView = ImageView()
    private val titleLabel: Label = Label()
    private val leftItemsBox: HBox = HBox()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        root.minHeight = ChromeConfig.CAPTION_MIN_HEIGHT
        root.prefHeight = Region.USE_COMPUTED_SIZE

        iconView.isPreserveRatio = true
        iconView.fitHeight = ICON_SIZE
        iconView.imageProperty().bind(viewModel.iconImage)
        iconView.visibleProperty().bind(viewModel.defaultIconVisible.and(viewModel.iconImage.isNotNull))
        iconView.managedProperty().bind(iconView.visibleProperty())

        titleLabel.textProperty().bind(viewModel.titleText)
        titleLabel.visibleProperty().bind(viewModel.defaultTitleVisible)
        titleLabel.managedProperty().bind(titleLabel.visibleProperty())

        HBox.setHgrow(leftItemsBox, javafx.scene.layout.Priority.NEVER)
        leftBox.children.setAll(iconView, titleLabel, leftItemsBox)

        mirror(leftItemsBox, viewModel.leftItems)
        mirror(centerBox, viewModel.centerItems)
        mirror(rightBox, viewModel.rightItems)

        bindButtonSlot()

        listOf(root, leftBox, centerBox, rightBox).forEach { target ->
            target.addEventHandler(MouseEvent.MOUSE_PRESSED) { e -> viewModel.moveStart(e.screenX, e.screenY) }
            target.addEventHandler(MouseEvent.MOUSE_DRAGGED) { e -> viewModel.move(e.screenX, e.screenY) }
        }
    }

    private fun mirror(box: HBox, items: ObservableList<Node>) {
        box.children.setAll(items)
        items.addListener(ListChangeListener { box.children.setAll(items) })
    }

    private fun bindButtonSlot() {
        updateButtonSlot(viewModel.captionButtonSlot.get())
        viewModel.captionButtonSlot.addListener { _, _, node -> updateButtonSlot(node) }
    }

    private fun updateButtonSlot(node: Node?) {
        buttonSlot.children.setAll(listOfNotNull(node))
        buttonSlot.isMouseTransparent = node == null
    }

    private companion object {
        const val ICON_SIZE: Double = 16.0
    }
}
