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
import javafx.scene.control.ScrollPane
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
 * controls vertically in order and rejects a fourth control, a group mixing both box types lays
 * them out left to right in content order, and both boxes stretch their child controls to fill the
 * box.
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

    /**
     * Use case: a plain control dropped into an [FXMenuGroupLargeBox] is stretched to the box's full
     * width and height, so a large ribbon button occupies the whole slot without extra layout code.
     */
    @Test
    fun `large box stretches its control to fill the box`() {
        val menuPane = showMenuPaneStage()
        onFx { (menuPane.scene.window as Stage).apply { width = 600.0; height = 240.0 } }
        pumpFx()

        val home = FXMenuTab("home", "Home")
        val button = Button("Paste")
        val largeBox = FXMenuGroupLargeBox(button)
        val group = FXMenuGroup(largeBox, anchor = largeBox).apply { title = "Clipboard" }

        onFx {
            home.groups.add(group)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        onFx {
            assertEquals(largeBox.width, button.width, 0.5)
            assertEquals(largeBox.height, button.height, 0.5)
            assertTrue(button.width > 0.0)
        }
    }

    /**
     * Use case: every control in an [FXMenuGroupSmallBox] fills its row's full width and the rows
     * share the box height evenly, so a three-button stack reads as one even column.
     */
    @Test
    fun `small box stretches every row to fill the box`() {
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

        onFx {
            listOf(cut, copy, paste).forEach { assertEquals(smallBox.width, it.width, 0.5) }
            val third = (smallBox.height - 2 * smallBox.spacing) / 3.0
            listOf(cut, copy, paste).forEach { assertEquals(third, it.height, 1.5) }
            assertTrue(third > 0.0)
        }
    }

    /**
     * Use case: an [FXMenuGroupSmallBox] holding fewer than three controls still sizes each row to
     * one third of the box height (never one half for two controls), leaving the unused rows empty
     * at the bottom; the horizontal stretch is unaffected.
     */
    @Test
    fun `small box keeps one-third row height with fewer controls`() {
        val menuPane = showMenuPaneStage()
        onFx { (menuPane.scene.window as Stage).apply { width = 600.0; height = 240.0 } }
        pumpFx()

        val home = FXMenuTab("home", "Home")
        val cut = Button("Cut")
        val copy = Button("Copy")
        val smallBox = FXMenuGroupSmallBox(cut, copy)
        val group = FXMenuGroup(smallBox, anchor = smallBox).apply { title = "Clipboard" }

        onFx {
            home.groups.add(group)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        onFx {
            val third = (smallBox.height - 2 * smallBox.spacing) / 3.0
            assertEquals(third, cut.height, 1.5)
            assertEquals(third, copy.height, 1.5)
            assertEquals(smallBox.width, cut.width, 0.5)
            assertTrue(cut.height < smallBox.height / 2.0, "row ${cut.height} should be ~1/3 of ${smallBox.height}")
        }
    }

    /**
     * Use case: repeatedly widening the window must not grow the small box's rows - the box's
     * preferred height stays tied to the child controls, never to its own laid-out height, so there
     * is no layout feedback loop.
     */
    @Test
    fun `resizing the window width does not grow the small box rows`() {
        val menuPane = showMenuPaneStage()
        val stage = onFx { menuPane.scene.window as Stage }
        onFx { stage.apply { width = 500.0; height = 240.0 } }
        pumpFx()

        val home = FXMenuTab("home", "Home")
        val cut = Button("Cut")
        val smallBox = FXMenuGroupSmallBox(cut, Button("Copy"), Button("Paste"))
        val group = FXMenuGroup(smallBox, anchor = smallBox).apply { title = "Clipboard" }

        onFx {
            home.groups.add(group)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        val initial = onFx { cut.height }
        repeat(4) { i ->
            onFx { stage.width = 500.0 + (i + 1) * 60.0 }
            pumpFx()
        }

        assertEquals(initial, onFx { cut.height }, 1.0, "row height grew from $initial on width resize")
    }

    /**
     * Use case: when a group's content row is wider than the boxes need (here forced by a long
     * title), the boxes divide that extra width evenly - equal `HBox` weight and unbounded max
     * width - instead of one box hogging it.
     */
    @Test
    fun `boxes in a group divide the content width evenly`() {
        val menuPane = showMenuPaneStage()
        onFx { (menuPane.scene.window as Stage).apply { width = 800.0; height = 240.0 } }
        pumpFx()

        val home = FXMenuTab("home", "Home")
        val boxA = FXMenuGroupSmallBox(Button("X"))
        val boxB = FXMenuGroupSmallBox(Button("X"))
        val group = FXMenuGroup(boxA, boxB, anchor = boxA).apply {
            title = "A deliberately very wide group caption exceeding the two boxes"
        }

        onFx {
            home.groups.add(group)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        onFx {
            val content = group.lookup(".menu-group-content") as HBox
            assertEquals(boxA.width, boxB.width, 1.0)
            assertEquals(content.width, boxA.width + boxB.width + content.spacing, 1.5)
        }
    }

    /**
     * Use case: three tabs whose groups differ in intrinsic height - a three-row small-box stack, a
     * single flat large-box button, and a tab with no groups at all - must all render the group strip
     * at the same fixed height, so the ribbon band never jumps when the user switches tabs.
     */
    @Test
    fun `group strip keeps a constant height across tab switches`() {
        val menuPane = showMenuPaneStage()
        onFx { (menuPane.scene.window as Stage).apply { width = 600.0; height = 260.0 } }
        pumpFx()

        val tall = FXMenuTab("home", "Home")
        val stack = FXMenuGroupSmallBox(Button("Cut"), Button("Copy"), Button("Paste"))
        tall.groups.add(FXMenuGroup(stack, anchor = stack).apply { title = "Clipboard" })

        val flat = FXMenuTab("view", "View")
        val large = FXMenuGroupLargeBox(Button("Zoom"))
        flat.groups.add(FXMenuGroup(large, anchor = large).apply { title = "Display" })

        val empty = FXMenuTab("help", "Help")

        onFx {
            menuPane.tabs.addAll(tall, flat, empty)
            menuPane.activeTab = tall
        }
        pumpFx()

        val scrollPane = menuPane.lookup(".menu-pane-group-strip-scroll-pane") as ScrollPane
        val tallHeight = onFx { scrollPane.height }
        assertTrue(tallHeight > 0.0)

        onFx { menuPane.activeTab = flat }
        pumpFx()
        assertEquals(tallHeight, onFx { scrollPane.height }, 0.5)

        onFx { menuPane.activeTab = empty }
        pumpFx()
        assertEquals(tallHeight, onFx { scrollPane.height }, 0.5)
    }
}
