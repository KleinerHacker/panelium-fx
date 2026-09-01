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
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import org.pcsoft.framework.panelium.chrome.internal.ChromeConfig
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [ChromeCaptionBarViewModel]: an HBox row of leading slot, growing center slot, trailing
 * slot and the caption-button box. The button box is a real row child, so it reserves its own
 * width and the trailing slot never slides underneath it. Its side and the side of the default
 * icon / title follow `captionOs` - leading on Windows / Linux / other, trailing on macOS.
 */
internal class ChromeCaptionBarView : FxmlView<ChromeCaptionBarViewModel>, Initializable {

    @FXML
    private lateinit var root: StackPane

    @FXML
    private lateinit var row: HBox

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
    private val leftItemsBox: HBox = HBox(4.0)
    private val rightItemsBox: HBox = HBox(4.0)

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        root.minHeight = ChromeConfig.CAPTION_MIN_HEIGHT
        root.prefHeight = Region.USE_COMPUTED_SIZE

        iconView.isPreserveRatio = true
        iconView.fitHeight = ICON_SIZE
        iconView.isMouseTransparent = true
        iconView.imageProperty().bind(viewModel.iconImage)
        iconView.visibleProperty().bind(viewModel.defaultIconVisible.and(viewModel.iconImage.isNotNull))
        iconView.managedProperty().bind(iconView.visibleProperty())

        titleLabel.textProperty().bind(viewModel.titleText)
        titleLabel.isMouseTransparent = true
        titleLabel.visibleProperty().bind(viewModel.defaultTitleVisible)
        titleLabel.managedProperty().bind(titleLabel.visibleProperty())

        HBox.setHgrow(centerBox, Priority.ALWAYS)
        HBox.setHgrow(leftItemsBox, Priority.NEVER)
        HBox.setHgrow(rightItemsBox, Priority.NEVER)

        mirror(leftItemsBox, viewModel.leftItems)
        mirror(centerBox, viewModel.centerItems)
        mirror(rightItemsBox, viewModel.rightItems)

        applyOsLayout(viewModel.captionOs.get())
        viewModel.captionOs.addListener { _, _, os -> applyOsLayout(os) }

        bindButtonSlot()
    }

    private fun applyOsLayout(os: ChromeOs) {
        if (os == ChromeOs.MAC) {
            leftBox.children.setAll(leftItemsBox)
            rightBox.children.setAll(rightItemsBox, iconView, titleLabel)
            row.children.setAll(buttonSlot, leftBox, centerBox, rightBox)
        } else {
            leftBox.children.setAll(iconView, titleLabel, leftItemsBox)
            rightBox.children.setAll(rightItemsBox)
            row.children.setAll(leftBox, centerBox, rightBox, buttonSlot)
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
