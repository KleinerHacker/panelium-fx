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
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers the strip-wide behaviour of [MenuGroupStripOverflowCoordinator]: groups organise themselves
 * by box priority instead of shrinking evenly - low-priority non-anchor boxes across the whole strip
 * collapse before higher-priority ones, an untouched group keeps its exact width and control sizes,
 * a group's anchor box is never collapsed, widening restores boxes in reverse priority order, and a
 * strip that overflows even after every non-anchor box is gone stays scrollable via the mouse wheel.
 */
class MenuGroupStripOverflowTest : AbstractMenuPaneUiTest() {

    private var stage: Stage? = null
    private lateinit var strip: HBox
    private lateinit var scroll: ScrollPane
    private lateinit var coordinator: MenuGroupStripOverflowCoordinator

    @AfterEach
    fun closeStage() {
        onFx { stage?.close() }
        pumpFx()
    }

    /**
     * Use case: with one collapse needed the coordinator takes a `LOW` box from the rightmost group;
     * with two collapses it takes both `LOW` boxes and leaves `MEDIUM`, `HIGH` and the anchors.
     */
    @Test
    fun `boxes collapse in ascending priority then rightmost group first`() {
        val anchorA = box("AnchorA", FXMenuGroupBoxPriority.MEDIUM)
        val a1 = box("A1", FXMenuGroupBoxPriority.HIGH)
        val a2 = box("A2", FXMenuGroupBoxPriority.LOW)
        val anchorB = box("AnchorB", FXMenuGroupBoxPriority.MEDIUM)
        val b1 = box("B1", FXMenuGroupBoxPriority.MEDIUM)
        val b2 = box("B2", FXMenuGroupBoxPriority.LOW)
        val groupA = group("A", anchorA, a1, a2)
        val groupB = group("B", anchorB, b1, b2)
        show(groupA, groupB)

        grant(750.0)
        assertEquals(listOf<Node>(anchorB, b1), visibleContent(groupB))
        assertEquals(listOf<Node>(anchorA, a1, a2), visibleContent(groupA))
        assertTrue(onFx { groupB.isOverflowActive })
        assertFalse(onFx { groupA.isOverflowActive })

        grant(640.0)
        assertEquals(listOf<Node>(anchorA, a1), visibleContent(groupA))
        assertEquals(listOf<Node>(anchorB, b1), visibleContent(groupB))
    }

    /**
     * Use case: a group holding only `HIGH` boxes must not change at all while a neighbouring
     * `LOW`-only group collapses down to its anchor - same width to the pixel, same button widths.
     */
    @Test
    fun `an untouched group keeps its width and control sizes`() {
        val keepAnchor = box("KeepAnchor", FXMenuGroupBoxPriority.HIGH)
        val keep1 = box("Keep1", FXMenuGroupBoxPriority.HIGH)
        val keep2 = box("Keep2", FXMenuGroupBoxPriority.HIGH)
        val dropAnchor = box("DropAnchor", FXMenuGroupBoxPriority.LOW)
        val drop1 = box("Drop1", FXMenuGroupBoxPriority.LOW)
        val drop2 = box("Drop2", FXMenuGroupBoxPriority.LOW)
        val keepGroup = group("Keep", keepAnchor, keep1, keep2)
        val dropGroup = group("Drop", dropAnchor, drop1, drop2)
        show(keepGroup, dropGroup)
        grant(1000.0)

        val unconstrainedWidth = onFx { keepGroup.width }

        grant(620.0)

        assertTrue(onFx { dropGroup.isOverflowActive })
        assertFalse(onFx { keepGroup.isOverflowActive })
        assertEquals(unconstrainedWidth, onFx { keepGroup.width }, 0.5)
        assertEquals(listOf<Node>(keepAnchor, keep1, keep2), visibleContent(keepGroup))
        assertEquals(listOf<Node>(dropAnchor), visibleContent(dropGroup))
        onFx {
            keepGroup.lookupAll(".button")
                .filterIsInstance<Button>()
                .filter { "menu-group-overflow-button" !in it.styleClass && "menu-group-launcher" !in it.styleClass }
                .forEach { button -> assertEquals(140.0, button.width, 0.5) }
        }
    }

    /**
     * Use case: however little width a group is granted, its anchor box is never collapsed - even
     * when every other box has the lowest priority and there is almost no room.
     */
    @Test
    fun `the anchor box is never collapsed`() {
        val anchor = box("Anchor", FXMenuGroupBoxPriority.LOW)
        val b1 = box("B1", FXMenuGroupBoxPriority.LOW)
        val b2 = box("B2", FXMenuGroupBoxPriority.LOW)
        val b3 = box("B3", FXMenuGroupBoxPriority.LOW)
        val group = group("G", anchor, b1, b2, b3)
        show(group)

        grant(60.0)

        assertEquals(listOf<Node>(anchor), visibleContent(group))
        assertTrue(onFx { group.isOverflowActive })
    }

    /**
     * Use case: after boxes were collapsed, widening the strip must restore them in reverse of the
     * collapse order - `HIGH` before `MEDIUM` before `LOW` - until everything is shown again.
     */
    @Test
    fun `widening restores boxes in reverse priority order`() {
        val anchor = box("Anchor", FXMenuGroupBoxPriority.HIGH)
        val high = box("High", FXMenuGroupBoxPriority.HIGH)
        val medium = box("Medium", FXMenuGroupBoxPriority.MEDIUM)
        val low = box("Low", FXMenuGroupBoxPriority.LOW)
        val group = group("G", anchor, high, medium, low)
        show(group)

        grant(200.0)
        assertEquals(listOf<Node>(anchor), visibleContent(group))

        grant(400.0)
        assertEquals(listOf<Node>(anchor, high), visibleContent(group))

        grant(1200.0)
        assertEquals(listOf<Node>(anchor, high, medium, low), visibleContent(group))
        assertFalse(onFx { group.isOverflowActive })
    }

    /**
     * Use case: an `FXMenuPane` whose active tab carries wide groups made only of an anchor box
     * cannot collapse anything; the group strip must overflow and the mouse wheel must scroll it
     * horizontally.
     */
    @Test
    fun `mouse wheel scrolls the group strip when nothing can be collapsed`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            repeat(4) { index ->
                val onlyBox = FXMenuGroupLargeBox(Button("Wide $index").apply { prefWidth = 220.0 })
                home.groups.add(FXMenuGroup(onlyBox, anchor = onlyBox).apply { title = "Group $index" })
            }
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        repeat(6) { pumpFx() }

        val groupScroll = menuPane.lookup(".menu-pane-group-strip-scroll-pane") as ScrollPane
        assertEquals(0.0, onFx { groupScroll.hvalue })
        assertTrue(onFx { groupScroll.content.boundsInLocal.width > groupScroll.viewportBounds.width })

        onFx {
            groupScroll.fireEvent(
                ScrollEvent(
                    ScrollEvent.SCROLL, 0.0, 0.0, 0.0, 0.0, false, false, false, false, true, false,
                    0.0, -50.0, 0.0, -50.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0.0,
                    ScrollEvent.VerticalTextScrollUnits.NONE, 0.0, 0, null,
                ),
            )
        }
        pumpFx()

        assertTrue(onFx { groupScroll.hvalue } > 0.0)
    }

    private fun show(vararg groups: FXMenuGroup) {
        onFx {
            strip = HBox(4.0).apply {
                alignment = Pos.CENTER_LEFT
                children.addAll(groups)
            }
            scroll = ScrollPane(strip).apply {
                isFitToHeight = true
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            }
            coordinator = MenuGroupStripOverflowCoordinator(scroll, strip)
            stage = Stage().apply {
                scene = Scene(StackPane(scroll), 1400.0, 260.0)
                show()
            }
            coordinator.setGroups(groups.toList())
        }
        settle()
    }

    private fun grant(width: Double) {
        onFx {
            scroll.minWidth = width
            scroll.prefWidth = width
            scroll.maxWidth = width
            scroll.parent?.requestLayout()
        }
        settle()
    }

    private fun settle() = repeat(6) { pumpFx() }

    private fun box(text: String, priority: FXMenuGroupBoxPriority): FXMenuGroupLargeBox =
        FXMenuGroupLargeBox(Button(text).apply { prefWidth = 140.0 }, priority)

    private fun group(title: String, anchor: FXMenuGroupBox, vararg rest: FXMenuGroupBox): FXMenuGroup =
        FXMenuGroup(anchor, *rest, anchor = anchor).apply { this.title = title }

    private fun visibleContent(group: FXMenuGroup): List<Node> {
        val contentHost = group.lookup(".menu-group-content") as HBox
        return onFx { contentHost.children.toList() }
    }
}
