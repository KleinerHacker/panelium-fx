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

import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest

/**
 * Covers the composable caption area and the FXML entry: inserting nodes into the leading, center
 * and trailing slots, the default title / icon binding to the owning stage and switching it off,
 * and loading a [ChromePane] as an FXML root element.
 */
class CaptionAndFxmlTest : AbstractChromeUiTest() {

    /**
     * Use case: an application adds toolbar nodes to each caption slot; every node must land under
     * the matching slot container in the live scene graph.
     */
    @Test
    fun `nodes added to the three caption slots appear in their slot containers`() {
        val (pane, _) = showChromeStage()
        val left = Label("left")
        val center = Button("center")
        val right = Label("right")

        onFx {
            pane.captionLeftItems.add(left)
            pane.captionCenterItems.add(center)
            pane.captionRightItems.add(right)
        }
        pumpFx()

        assertTrue(onFx { hasAncestorWithStyleClass(left, "chrome-caption-left") })
        assertTrue(onFx { hasAncestorWithStyleClass(center, "chrome-caption-center") })
        assertTrue(onFx { hasAncestorWithStyleClass(right, "chrome-caption-right") })
    }

    /**
     * Use case: the default caption title follows `Stage.title`, and hiding the default title
     * removes its label from layout without breaking the binding.
     */
    @Test
    fun `default title follows the stage title and can be switched off`() {
        val (pane, stage) = showChromeStage(title = "Initial Title")

        assertEquals("Initial Title", onFx { pane.captionTitleProperty().get() })
        onFx { stage.title = "Renamed Window" }
        assertEquals("Renamed Window", onFx { pane.captionTitleProperty().get() })

        onFx { pane.isDefaultTitleVisible = false }
        pumpFx()
        val label = onFx { firstLabelWithText(pane, "Renamed Window") }
        assertNotNull(label)
        assertFalse(onFx { label!!.isManaged }, "the hidden default title must leave layout")
    }

    /**
     * Use case: the default caption icon mirrors the first stage icon and disappears when the
     * default icon is switched off.
     */
    @Test
    fun `default icon visibility follows the switch`() {
        val (pane, _) = showChromeStage()

        onFx { pane.isDefaultIconVisible = false }
        pumpFx()
        assertFalse(onFx { pane.isDefaultIconVisible })

        onFx { pane.isDefaultIconVisible = true }
        pumpFx()
        assertTrue(onFx { pane.isDefaultIconVisible })
    }

    /**
     * Use case: a layout is authored in FXML with `<ChromePane>` as the root element and a single
     * child; the loader must build the pane and adopt that child as the framed content.
     */
    @Test
    fun `ChromePane loads as an FXML root with its child as content`() {
        val url = javaClass.getResource("/org/pcsoft/framework/panelium/chrome/ChromePaneRoot.fxml")
        assertNotNull(url, "the test FXML resource must be on the classpath")

        val pane = onFx { FXMLLoader.load<ChromePane>(url) }
        assertNotNull(pane)

        val content = onFx { pane.content }
        assertTrue(content is Label)
        assertEquals("FXML Content", onFx { (pane.content as Label).text })

        val stage = onFx {
            Stage().apply {
                scene = Scene(pane, 400.0, 300.0)
                pane.attachStage(this)
                show()
            }
        }
        trackStage(stage)
        pumpFx()
        assertSame(pane, onFx { stage.scene.root })
    }

    /**
     * Use case: switching `captionTitlePosition` moves the default title before or after the nodes
     * in `captionLeftItems` within the caption's leading slot, while the default icon stays put at
     * the very leading edge in both positions.
     */
    @Test
    fun `caption title position switches the title before or after the left items but keeps the icon in place`() {
        val (pane, _) = showChromeStage(title = "Positioned Title")
        val leftItem = Label("left-item")

        onFx { pane.captionLeftItems.add(leftItem) }
        pumpFx()

        val iconView = onFx { firstImageView(pane) }
        assertNotNull(iconView, "the default caption icon's ImageView must be present")

        onFx { pane.captionTitlePosition = ChromeCaptionTitlePosition.NEXT_TO_LOGO }
        pumpFx()
        val titleLabel = onFx { firstLabelWithText(pane, "Positioned Title") }
        assertNotNull(titleLabel)
        assertTrue(
            onFx { indexInLeadingSlot(pane, titleLabel!!) < indexInLeadingSlot(pane, leftItem) },
            "with NEXT_TO_LOGO the title must sit before the left items",
        )
        assertEquals(0, onFx { indexInLeadingSlot(pane, iconView!!) }, "the icon must stay at the leading edge")

        onFx { pane.captionTitlePosition = ChromeCaptionTitlePosition.AFTER_LEFT_ITEMS }
        pumpFx()
        assertTrue(
            onFx { indexInLeadingSlot(pane, titleLabel!!) > indexInLeadingSlot(pane, leftItem) },
            "with AFTER_LEFT_ITEMS the title must sit after the left items",
        )
        assertEquals(0, onFx { indexInLeadingSlot(pane, iconView!!) }, "the icon must stay at the leading edge")
    }

    private fun firstImageView(root: Node): ImageView? {
        if (root is ImageView) return root
        if (root !is Pane) return null
        return root.childrenUnmodifiable.firstNotNullOfOrNull { firstImageView(it) }
    }

    private fun indexInLeadingSlot(root: Region, node: Node): Int {
        val leftBox = root.lookupAll(".chrome-caption-left").first() as Pane
        return leftBox.childrenUnmodifiable.indexOfFirst { it === node || hasDescendant(it, node) }
    }

    private fun hasDescendant(root: Node, target: Node): Boolean {
        if (root === target) return true
        if (root !is Pane) return false
        return root.childrenUnmodifiable.any { hasDescendant(it, target) }
    }

    private fun hasAncestorWithStyleClass(node: Node, styleClass: String): Boolean {
        var current: Node? = node
        while (current != null) {
            if (current.styleClass.contains(styleClass)) return true
            current = current.parent
        }
        return false
    }

    private fun firstLabelWithText(root: Region, text: String): Label? =
        root.lookupAll(".label").filterIsInstance<Label>().firstOrNull { it.text == text }
}
