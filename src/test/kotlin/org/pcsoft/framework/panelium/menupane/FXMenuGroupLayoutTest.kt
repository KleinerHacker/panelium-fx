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

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers the group layout boxes [FXMenuGroupLargeBox] and [FXMenuGroupSmallBox]: the large box
 * stretches its control to the full height of the group's content row, the small box stacks its
 * controls vertically in order and rejects a fourth control, and a group mixing both box types lays
 * them out left to right in content order.
 */
class FXMenuGroupLayoutTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: an application drops an [FXMenuGroupLargeBox] into a group's content; the box must
     * fill the full height of the `menu-group-content` row so the hosted control spans the group.
     */
    @Test
    fun `large box fills the full height of the group content row`() {
        val menuPane = showMenuPaneStage()
        onFx { (menuPane.scene.window as Stage).apply { width = 600.0; height = 240.0 } }
        pumpFx()

        val home = FXMenuTab("home", "Home")
        val largeBox = FXMenuGroupLargeBox(Button("Paste"))
        val group = FXMenuGroup(largeBox, anchor = largeBox).apply { title = "Clipboard" }

        onFx {
            home.groups.add(group)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        val contentHost = group.lookup(".menu-group-content") as HBox
        assertEquals(onFx { contentHost.height }, onFx { largeBox.height }, 0.5)
        assertTrue(onFx { largeBox.height } > 0.0)
    }

    /**
     * Use case: an [FXMenuGroupSmallBox] receives three controls; it must keep them in insertion
     * order, stack them vertically (each control below the previous one) and align the stack to the
     * top of the box - never centre it vertically in the group's content row.
     */
    @Test
    fun `small box stacks its controls vertically in content order`() {
        val menuPane = showMenuPaneStage()
        onFx { (menuPane.scene.window as Stage).apply { width = 600.0; height = 240.0 } }
        pumpFx()

        val home = FXMenuTab("home", "Home")
        val cut = Button("Cut")
        val copy = Button("Copy")
        val paste = Button("Paste")
        val smallBox = FXMenuGroupSmallBox(cut, copy, paste)
        val group = FXMenuGroup(smallBox, anchor = smallBox).apply { title = "Clipboard" }

        onFx {
            home.groups.add(group)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        assertEquals(listOf<Any>(cut, copy, paste), onFx { smallBox.children.toList() })
        onFx {
            assertTrue(cut.boundsInParent.minY < copy.boundsInParent.minY)
            assertTrue(copy.boundsInParent.minY < paste.boundsInParent.minY)
            // Top-aligned: the first control starts at the top edge of the box, even when the box is
            // stretched taller than the stack by the group's content row.
            assertEquals(Pos.TOP_LEFT, smallBox.alignment)
            assertTrue(cut.boundsInParent.minY < 1.0)
        }
    }

    /**
     * Use case: adding a fourth control to an [FXMenuGroupSmallBox] that already holds three must be
     * reported as an [IllegalStateException]. JavaFX routes list-listener failures to the thread's
     * uncaught-exception handler, so the test captures it there.
     */
    @Test
    fun `small box reports a fourth control added to its children`() {
        val captured = onFx {
            val smallBox = FXMenuGroupSmallBox(Button("Cut"), Button("Copy"), Button("Paste"))
            val thread = Thread.currentThread()
            val previous = thread.uncaughtExceptionHandler
            var seen: Throwable? = null
            thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e -> seen = e }
            try {
                smallBox.children.add(Button("Format"))
            } finally {
                thread.uncaughtExceptionHandler = previous
            }
            seen
        }

        assertTrue(captured is IllegalStateException)
    }

    /**
     * Use case: constructing an [FXMenuGroupSmallBox] with more than three controls must fail fast
     * with an [IllegalArgumentException].
     */
    @Test
    fun `small box constructor rejects more than three controls`() {
        assertThrows(IllegalArgumentException::class.java) {
            FXMenuGroupSmallBox(Button("Cut"), Button("Copy"), Button("Paste"), Button("Format"))
        }
    }

    /**
     * Use case: a group mixes an [FXMenuGroupLargeBox] with two [FXMenuGroupSmallBox] columns; the
     * boxes must keep their content order and be laid out left to right.
     */
    @Test
    fun `group lays out mixed layout boxes left to right in content order`() {
        val menuPane = showMenuPaneStage()
        onFx { (menuPane.scene.window as Stage).apply { width = 600.0; height = 240.0 } }
        pumpFx()

        val home = FXMenuTab("home", "Home")
        val large = FXMenuGroupLargeBox(Button("Paste"))
        val column1 = FXMenuGroupSmallBox(Button("Cut"), Button("Copy"))
        val column2 = FXMenuGroupSmallBox(Button("Bold"), Button("Italic"), Button("Underline"))
        val group = FXMenuGroup(large, column1, column2, anchor = large).apply { title = "Clipboard" }

        onFx {
            home.groups.add(group)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        assertEquals(listOf<Any>(large, column1, column2), onFx { group.content.toList() })
        onFx {
            assertTrue(large.boundsInParent.minX < column1.boundsInParent.minX)
            assertTrue(column1.boundsInParent.minX < column2.boundsInParent.minX)
        }
    }
}
