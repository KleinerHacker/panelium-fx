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
import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [FXBackstageMenuPaneViewModel]: a [BorderPane] with the [menuArea] (a [menuListView] of
 * [FXBackstageMenuItem] entries plus the [quickActionArea] footer of icon-only quick action
 * buttons) on the left, sized to [FXBackstageMenuPaneViewModel.menuWidth], and the [contentArea]
 * filling the rest. Selecting an entry in [menuListView] sets
 * [FXBackstageMenuPaneViewModel.selectedItem], and the currently selected item's
 * [FXBackstageMenuItem.content] node is shown as the sole child of [contentArea] - empty while
 * nothing is selected. Clicking a button in [quickActionArea] invokes its
 * [FXBackstageQuickAction.onAction] callback directly, without touching the selection or
 * [contentArea].
 */
internal class FXBackstageMenuPaneView : FxmlView<FXBackstageMenuPaneViewModel>, Initializable {

    @FXML
    private lateinit var root: StackPane

    @FXML
    private lateinit var layout: BorderPane

    @FXML
    private lateinit var menuArea: VBox

    @FXML
    private lateinit var menuListView: ListView<FXBackstageMenuItem>

    @FXML
    private lateinit var quickActionArea: HBox

    @FXML
    private lateinit var contentArea: StackPane

    @InjectViewModel
    private lateinit var viewModel: FXBackstageMenuPaneViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        menuArea.prefWidthProperty().bind(viewModel.menuWidth)
        menuArea.minWidthProperty().bind(viewModel.menuWidth)
        menuArea.maxWidthProperty().bind(viewModel.menuWidth)

        Bindings.bindContent(menuListView.items, viewModel.items)
        menuListView.setCellFactory { MenuItemCell() }
        // The list has no keyboard focus concept of its own - selection is driven entirely by
        // clicks and by FXBackstageMenuPane.selectedItem - so it must never take focus itself;
        // otherwise ":selected:focused" cells would render differently from ":selected" ones.
        menuListView.isFocusTraversable = false

        menuListView.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            viewModel.selectedItem.set(selected)
        }
        viewModel.selectedItem.addListener { _, _, selected ->
            if (menuListView.selectionModel.selectedItem !== selected) {
                menuListView.selectionModel.select(selected)
            }
            contentArea.children.setAll(listOfNotNull(selected?.content))
        }

        rebuildQuickActionButtons()
        viewModel.quickActions.addListener(ListChangeListener { rebuildQuickActionButtons() })
    }

    /** Rebuilds [quickActionArea]'s buttons from [FXBackstageMenuPaneViewModel.quickActions]. */
    private fun rebuildQuickActionButtons() {
        quickActionArea.children.setAll(viewModel.quickActions.map { quickAction ->
            Button().apply {
                styleClass.add("backstage-menu-pane-quick-action-button")
                graphic = quickAction.icon
                setOnAction { quickAction.onAction?.invoke() }
                // Like menuListView, this button has no keyboard focus concept of its own -
                // without this, a mouse click still focuses it and modena's default
                // ".button:focused" ring/background shows through around our own styling.
                isFocusTraversable = false
            }
        })
    }

    /** Cell showing an [FXBackstageMenuItem]'s optional [FXBackstageMenuItem.icon] beside its text. */
    private class MenuItemCell : ListCell<FXBackstageMenuItem>() {

        private val iconArea = StackPane().apply { styleClass.add("backstage-menu-pane-item-icon") }
        private val label = Label()
        private val box = HBox(8.0, iconArea, label).apply { styleClass.add("backstage-menu-pane-item") }

        override fun updateItem(item: FXBackstageMenuItem?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty || item == null) {
                graphic = null
                return
            }

            iconArea.children.setAll(listOfNotNull(item.icon))
            iconArea.isVisible = item.icon != null
            iconArea.isManaged = item.icon != null
            label.text = item.text
            graphic = box
        }
    }
}
