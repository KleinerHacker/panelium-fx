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

import javafx.scene.control.Button
import javafx.scene.control.ToggleButton
import javafx.scene.layout.HBox
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers [FXMenuPane.trailingItems]: host-supplied nodes shown at the trailing edge of the
 * tab-strip row, after the scrolling tab strip and before the built-in collapse/expand chevron.
 */
class FXMenuPaneTrailingItemsTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: nodes added to [FXMenuPane.trailingItems] are rendered in the same insertion order
     * inside the dedicated trailing-items box.
     */
    @Test
    fun `added trailing items are rendered in insertion order`() {
        val menuPane = showMenuPaneStage()
        val first = Button("Comments")
        val second = Button("Share")

        onFx {
            menuPane.trailingItems.add(first)
            menuPane.trailingItems.add(second)
        }
        pumpFx()

        val children = onFx { trailingItemsBox(menuPane).children.toList() }
        assertEquals(listOf<Any>(first, second), children)
    }

    /**
     * Use case: removing a node from [FXMenuPane.trailingItems] removes it from the rendered box
     * too, leaving the remaining items in place.
     */
    @Test
    fun `removed trailing items disappear from the box`() {
        val menuPane = showMenuPaneStage()
        val first = Button("Comments")
        val second = Button("Share")
        onFx {
            menuPane.trailingItems.addAll(first, second)
        }
        pumpFx()

        onFx { menuPane.trailingItems.remove(first) }
        pumpFx()

        val children = onFx { trailingItemsBox(menuPane).children.toList() }
        assertEquals(listOf<Any>(second), children)
    }

    /**
     * Use case: the trailing-items box sits in the tab-strip row, after the scrolling tab strip and
     * before the collapse/expand chevron - the fixed position relative to the built-in chevron.
     */
    @Test
    fun `trailing items box sits before the collapse chevron in the tab strip row`() {
        val menuPane = showMenuPaneStage()
        pumpFx()

        val row = onFx { tabStripRow(menuPane) }
        val children = onFx { row.children.toList() }
        val trailingIndex = children.indexOf(onFx { trailingItemsBox(menuPane) })
        val chevronIndex = children.indexOfFirst { it is ToggleButton && it.styleClass.contains("menu-pane-collapse-toggle") }

        assertTrue(trailingIndex in children.indices)
        assertTrue(chevronIndex in children.indices)
        assertTrue(trailingIndex < chevronIndex)
    }

    /**
     * Use case: [FXMenuPane.trailingItems] is empty by default, so no host is forced to opt out of
     * the feature.
     */
    @Test
    fun `trailing items are empty by default`() {
        val menuPane = showMenuPaneStage()
        pumpFx()

        assertTrue(onFx { menuPane.trailingItems.isEmpty() })
        assertTrue(onFx { trailingItemsBox(menuPane).children.isEmpty() })
    }

    /**
     * Use case: the same [FXMenuPane.trailingItems] list instance is returned on repeated access, so
     * a host can hold onto it and mutate it later.
     */
    @Test
    fun `trailing items list identity is stable across accesses`() {
        val menuPane = showMenuPaneStage()
        pumpFx()

        assertSame(onFx { menuPane.trailingItems }, onFx { menuPane.trailingItems })
    }

    private fun tabStripRow(menuPane: FXMenuPane): HBox = menuPane.lookup("#tabStripRow") as HBox

    private fun trailingItemsBox(menuPane: FXMenuPane): HBox = menuPane.lookup("#trailingItemsBox") as HBox
}
