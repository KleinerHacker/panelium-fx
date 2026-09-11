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

import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.StackPane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers the [FXBackstageMenuPane] IP-02 menu list and selection wiring: the menu `ListView` shows
 * every registered [FXBackstageMenuItem], selecting an entry shows its
 * [FXBackstageMenuItem.content] node in the content area, and clearing the selection empties the
 * content area again.
 */
class FXBackstageMenuPaneSelectionTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: registered [FXBackstageMenuItem] entries are rendered as entries of the menu
     * `ListView`, in registration order.
     */
    @Test
    fun `menu list shows registered items in order`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        val info = FXBackstageMenuItem(text = "Info")
        val settings = FXBackstageMenuItem(text = "Settings")

        onFx { backstageMenuPane.items.addAll(info, settings) }
        pumpFx()

        val menuList = onFx { menuListView(backstageMenuPane) }
        assertEquals(listOf("Info", "Settings"), onFx { menuList.items.map { it.text } })
    }

    /**
     * Use case: selecting an entry in the menu `ListView` (as a click on it would) sets
     * [FXBackstageMenuPane.selectedItem] to that entry.
     */
    @Test
    fun `selecting a list entry updates selectedItem`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        val info = FXBackstageMenuItem(text = "Info", content = Label("Info content"))
        onFx { backstageMenuPane.items.add(info) }
        pumpFx()

        onFx { menuListView(backstageMenuPane).selectionModel.select(info) }
        pumpFx()

        assertEquals(info, onFx { backstageMenuPane.selectedItem })
    }

    /**
     * Use case: once an entry is selected, its [FXBackstageMenuItem.content] node is shown as the
     * sole child of the pane's content area.
     */
    @Test
    fun `selecting an item shows its content node in the content area`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        val content = Label("Info content")
        val info = FXBackstageMenuItem(text = "Info", content = content)
        onFx { backstageMenuPane.items.add(info) }
        pumpFx()

        onFx { menuListView(backstageMenuPane).selectionModel.select(info) }
        pumpFx()

        val contentArea = onFx { contentArea(backstageMenuPane) }
        assertEquals(listOf<Any>(content), onFx { contentArea.children.toList() })
    }

    /**
     * Use case: without any selection, the content area stays empty - no
     * [FXBackstageMenuItem.content] node is shown before an entry has been clicked.
     */
    @Test
    fun `content area is empty while no item is selected`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        onFx { backstageMenuPane.items.add(FXBackstageMenuItem(text = "Info", content = Label("Info content"))) }
        pumpFx()

        val contentArea = onFx { contentArea(backstageMenuPane) }
        assertTrue(onFx { contentArea.children.isEmpty() })
    }

    /**
     * Use case: clearing [FXBackstageMenuPane.selectedItem] back to `null` (no more entry selected)
     * empties the content area again.
     */
    @Test
    fun `clearing the selection empties the content area`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        val info = FXBackstageMenuItem(text = "Info", content = Label("Info content"))
        onFx { backstageMenuPane.items.add(info) }
        pumpFx()
        onFx { backstageMenuPane.selectedItem = info }
        pumpFx()

        onFx { backstageMenuPane.selectedItem = null }
        pumpFx()

        val contentArea = onFx { contentArea(backstageMenuPane) }
        assertFalse(onFx { contentArea.children.isNotEmpty() })
    }

    @Suppress("UNCHECKED_CAST")
    private fun menuListView(backstageMenuPane: FXBackstageMenuPane): ListView<FXBackstageMenuItem> =
        backstageMenuPane.lookup(".backstage-menu-pane-list") as ListView<FXBackstageMenuItem>

    private fun contentArea(backstageMenuPane: FXBackstageMenuPane): StackPane =
        backstageMenuPane.lookup(".backstage-menu-pane-content-area") as StackPane
}
