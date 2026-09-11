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
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [FXBackstageMenuPaneViewModel]: a [BorderPane] with the [menuArea] (menu list and footer,
 * built in IP-02/IP-03) on the left, sized to [FXBackstageMenuPaneViewModel.menuWidth], and the
 * [contentArea] filling the rest, showing the [FXBackstageMenuItem.content] of the currently
 * selected item once selection is wired up (IP-02).
 */
internal class FXBackstageMenuPaneView : FxmlView<FXBackstageMenuPaneViewModel>, Initializable {

    @FXML
    private lateinit var root: StackPane

    @FXML
    private lateinit var layout: BorderPane

    @FXML
    private lateinit var menuArea: VBox

    @FXML
    private lateinit var contentArea: StackPane

    @InjectViewModel
    private lateinit var viewModel: FXBackstageMenuPaneViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        menuArea.prefWidthProperty().bind(viewModel.menuWidth)
        menuArea.minWidthProperty().bind(viewModel.menuWidth)
        menuArea.maxWidthProperty().bind(viewModel.menuWidth)
    }
}
