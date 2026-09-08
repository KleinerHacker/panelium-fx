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
 * Covers the per-group side of the group overflow: a single [FXMenuGroup] driven by
 * [MenuGroupStripOverflowCoordinator] collapses its lowest-priority non-anchor box into the chevron
 * popup when it is granted too little width, always keeps the anchor box visible, exposes the state
 * via [FXMenuGroup.isOverflowActive], and restores boxes when the width is granted back.
 *
 * The group strip's [ScrollPane] is width-pinned so the granted width is controlled directly,
 * without depending on headless stage resizing.
 */
class FXMenuGroupOverflowTest : AbstractMenuPaneUiTest() {

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
     * Use case: a group granted less width than its boxes need must collapse only its `LOW` box into
     * the chevron popup, keep the anchor box and the higher-priority boxes visible, mark itself
     * overflowing and show its chevron button.
     */
    @Test
    fun `narrow group collapses only its lowest-priority box`() {
        val anchor = box("Anchor", FXMenuGroupBoxPriority.LOW)
        val high = box("High", FXMenuGroupBoxPriority.HIGH)
        val medium = box("Medium", FXMenuGroupBoxPriority.MEDIUM)
        val low = box("Low", FXMenuGroupBoxPriority.LOW)
        val group = group("G", anchor, high, medium, low)
        show(group)

        grant(520.0)

        assertTrue(onFx { group.isOverflowActive })
        val chevron = group.lookup(".menu-group-overflow-button")!!
        assertTrue(onFx { chevron.isVisible && chevron.isManaged })

        val visible = visibleContent(group)
        assertTrue(anchor in visible && high in visible && medium in visible)
        assertFalse(low in visible)
    }

    /**
     * Use case: the same group granted ample width shows every box, is not overflowing and hides
     * its chevron button.
     */
    @Test
    fun `wide group shows every box without a chevron`() {
        val anchor = box("Anchor", FXMenuGroupBoxPriority.LOW)
        val high = box("High", FXMenuGroupBoxPriority.HIGH)
        val medium = box("Medium", FXMenuGroupBoxPriority.MEDIUM)
        val low = box("Low", FXMenuGroupBoxPriority.LOW)
        val group = group("G", anchor, high, medium, low)
        show(group)

        grant(1000.0)

        assertFalse(onFx { group.isOverflowActive })
        assertEquals(listOf<Node>(anchor, high, medium, low), visibleContent(group))
        val chevron = group.lookup(".menu-group-overflow-button")!!
        assertFalse(onFx { chevron.isVisible || chevron.isManaged })
    }

    /**
     * Use case: taking width away collapses the `LOW` box; granting it back must restore the box
     * and clear the overflow state, while the anchor box stays put throughout.
     */
    @Test
    fun `granting the width back restores the collapsed box`() {
        val anchor = box("Anchor", FXMenuGroupBoxPriority.LOW)
        val high = box("High", FXMenuGroupBoxPriority.HIGH)
        val medium = box("Medium", FXMenuGroupBoxPriority.MEDIUM)
        val low = box("Low", FXMenuGroupBoxPriority.LOW)
        val group = group("G", anchor, high, medium, low)
        show(group)

        grant(460.0)
        assertTrue(onFx { group.isOverflowActive })
        assertFalse(low in visibleContent(group))
        assertTrue(anchor in visibleContent(group))

        grant(1000.0)
        assertFalse(onFx { group.isOverflowActive })
        assertEquals(listOf<Node>(anchor, high, medium, low), visibleContent(group))
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
                scene = Scene(StackPane(scroll), 1200.0, 260.0)
                show()
            }
            coordinator.setGroups(groups.toList())
        }
        settle()
    }

    /** Pins the group strip viewport to [width] and lets the debounced recompute settle. */
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

    /** [anchor] is rendered first; the rest follow in order. */
    private fun group(title: String, anchor: FXMenuGroupBox, vararg rest: FXMenuGroupBox): FXMenuGroup =
        FXMenuGroup(anchor, *rest, anchor = anchor).apply { this.title = title }

    private fun visibleContent(group: FXMenuGroup): List<Node> {
        val contentHost = group.lookup(".menu-group-content") as HBox
        return onFx { contentHost.children.toList() }
    }
}
