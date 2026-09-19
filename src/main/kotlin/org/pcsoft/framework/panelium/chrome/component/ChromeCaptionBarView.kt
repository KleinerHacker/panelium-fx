/*
 * Copyright (c) KleinerHacker alias Pfeiffer C Soft 2026.
 * This work is licensed under the Apache License, Version 2.0.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations.
 */

package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [ChromeCaptionBarViewModel]: an HBox row of leading slot, growing center slot, trailing
 * slot and the caption-button box. The button box is a real row child, so it reserves its own
 * width and the trailing slot never slides underneath it. The default icon / title stay in the
 * leading slot on every OS; only the caption-button box changes side - trailing on Windows / Linux
 * / other, leading on macOS. Within the leading slot, the icon always stays first; the title sits
 * either right after the icon or after `captionLeftItems`, depending on
 * [ChromeCaptionBarViewModel.captionTitlePosition].
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
    private val leftItemsBox: HBox = HBox(4.0).apply { alignment = Pos.CENTER }
    private val rightItemsBox: HBox = HBox(4.0).apply { alignment = Pos.CENTER }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        root.minHeightProperty().bind(viewModel.captionMinHeight)
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
        viewModel.captionTitlePosition.addListener { _, _, _ -> applyOsLayout(viewModel.captionOs.get()) }

        bindButtonSlot()
    }

    private fun applyOsLayout(os: ChromeOs) {
        val leadingChildren = when (viewModel.captionTitlePosition.get()) {
            ChromeCaptionTitlePosition.NEXT_TO_LOGO -> listOf(iconView, titleLabel, leftItemsBox)
            ChromeCaptionTitlePosition.AFTER_LEFT_ITEMS -> listOf(iconView, leftItemsBox, titleLabel)
        }
        if (os == ChromeOs.MAC) {
            leftBox.children.setAll(leadingChildren)
            rightBox.children.setAll(rightItemsBox)
            row.children.setAll(buttonSlot, leftBox, centerBox, rightBox)
        } else {
            leftBox.children.setAll(leadingChildren)
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
