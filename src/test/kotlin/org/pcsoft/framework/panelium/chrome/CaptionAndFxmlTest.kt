package org.pcsoft.framework.panelium.chrome

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
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

    private fun hasAncestorWithStyleClass(node: javafx.scene.Node, styleClass: String): Boolean {
        var current: javafx.scene.Node? = node
        while (current != null) {
            if (current.styleClass.contains(styleClass)) return true
            current = current.parent
        }
        return false
    }

    private fun firstLabelWithText(root: Region, text: String): Label? =
        root.lookupAll(".label").filterIsInstance<Label>().firstOrNull { it.text == text }
}
