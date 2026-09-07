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

package org.pcsoft.framework.panelium.menupane

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [FXMenuGroupViewModel]: an HBox of the group's [FXMenuGroupViewModel.content] nodes with
 * the group [FXMenuGroupViewModel.title] shown as a label below them, following the ribbon
 * convention of a caption under the group body.
 *
 * The overflow itself is driven from outside by [MenuGroupStripOverflowCoordinator] through the
 * [MenuGroupOverflowController] published on the view model: the coordinator collapses whole layout
 * boxes into the [overflowButton]'s popup by priority, and [FXMenuGroupViewModel.overflowActive]
 * mirrors whether this group currently has any.
 */
internal class FXMenuGroupView : FxmlView<FXMenuGroupViewModel>, Initializable {

    @FXML
    private lateinit var root: StackPane

    @FXML
    private lateinit var overflowRow: HBox

    @FXML
    private lateinit var groupContent: HBox

    @FXML
    private lateinit var groupTitle: Label

    @FXML
    private lateinit var overflowButton: Button

    @FXML
    private lateinit var launcherButton: Button

    @InjectViewModel
    private lateinit var viewModel: FXMenuGroupViewModel

    private val overflowMenu = ContextMenu()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        groupTitle.textProperty().bind(viewModel.title)

        launcherButton.visibleProperty().bind(viewModel.onLauncherAction.isNotNull())
        launcherButton.managedProperty().bind(viewModel.onLauncherAction.isNotNull())
        launcherButton.onActionProperty().bind(viewModel.onLauncherAction)

        val controller = MenuGroupOverflowController(
            root = root,
            overflowRow = overflowRow,
            groupContent = groupContent,
            groupTitle = groupTitle,
            overflowButton = overflowButton,
            overflowMenu = overflowMenu,
            active = viewModel.overflowActive,
            anchorSupplier = { viewModel.anchor.get() },
        )
        viewModel.overflowController = controller

        controller.setContent(viewModel.content)
        viewModel.content.addListener(ListChangeListener { controller.setContent(viewModel.content) })
    }
}
