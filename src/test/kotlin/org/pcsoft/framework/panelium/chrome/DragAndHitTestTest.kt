package org.pcsoft.framework.panelium.chrome

import javafx.scene.control.Button
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.PickResult
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.internal.CaptionHitTest
import org.pcsoft.framework.panelium.chrome.internal.WindowMenu
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest

/**
 * Covers the caption drag / hit-test model: a plain caption background is a drag zone while an
 * interactive control passes through, the explicit `dragRegion` override wins over the heuristic,
 * a primary double-click on the caption maximizes the window and a secondary click opens the
 * window system menu.
 */
class DragAndHitTestTest : AbstractChromeUiTest() {

    /**
     * Use case: pressing empty caption background drags the window, but pressing a button in the
     * caption must reach the button instead of starting a window move.
     */
    @Test
    fun `plain background is a drag zone and an interactive control passes through`() {
        val captionBar = Pane()
        val background = Region()
        captionBar.children.add(background)
        val button = Button("Action")
        captionBar.children.add(button)

        assertTrue(CaptionHitTest.isDragZone(background, captionBar))
        assertFalse(CaptionHitTest.isDragZone(button, captionBar))
    }

    /**
     * Use case: a filled, interactive strip is forced to drag the window with
     * `setDragRegion(node, true)`, and an otherwise inert node is opted out with `false`.
     */
    @Test
    fun `explicit drag-region override wins over the interactivity heuristic`() {
        val captionBar = Pane()
        val interactive = Button("Draggable")
        val inert = Region()
        captionBar.children.addAll(interactive, inert)

        ChromeCaptionBar.setDragRegion(interactive, true)
        ChromeCaptionBar.setDragRegion(inert, false)

        assertTrue(CaptionHitTest.isDragZone(interactive, captionBar))
        assertFalse(CaptionHitTest.isDragZone(inert, captionBar))

        ChromeCaptionBar.setDragRegion(interactive, null)
        assertFalse(CaptionHitTest.isDragZone(interactive, captionBar))
    }

    /**
     * Use case: double-clicking the caption background maximizes a resizable window and toggles it
     * back to the previous bounds on the next double-click.
     */
    @Test
    fun `primary double-click on the caption toggles maximize`() {
        val (pane, stage) = showChromeStage()
        val captionBar = pane.captionBar

        onFx { fireCaptionClick(captionBar, MouseButton.PRIMARY, clickCount = 2) }
        pumpFx()
        assertTrue(onFx { stage.isMaximized }, "the first double-click must maximize")

        onFx { fireCaptionClick(captionBar, MouseButton.PRIMARY, clickCount = 2) }
        pumpFx()
        assertFalse(onFx { stage.isMaximized }, "the second double-click must restore")
    }

    /**
     * Use case: a double-click on a non-resizable window's caption must not maximize it.
     */
    @Test
    fun `double-click does not maximize a non-resizable window`() {
        val (pane, stage) = showChromeStage()
        onFx { stage.isResizable = false }

        onFx { fireCaptionClick(pane.captionBar, MouseButton.PRIMARY, clickCount = 2) }
        pumpFx()

        assertFalse(onFx { stage.isMaximized })
    }

    /**
     * Use case: the window system menu is rebuilt on every open so its entries reflect the current
     * state - it is not showing before [WindowMenu.show] and is showing afterwards.
     */
    @Test
    fun `window menu opens on demand`() {
        val (pane, stage) = showChromeStage()
        val menu = WindowMenu(pane.windowOps!!, stage)

        assertFalse(menu.isShowing)
        onFx { menu.show(pane.captionBar, 150.0, 120.0) }
        pumpFx()
        assertTrue(menu.isShowing)

        onFx { menu.hide() }
        pumpFx()
        assertFalse(menu.isShowing)
    }

    private fun fireCaptionClick(captionBar: javafx.scene.Node, button: MouseButton, clickCount: Int) {
        val pick = PickResult(captionBar, 12.0, 8.0)
        val event = MouseEvent(
            MouseEvent.MOUSE_PRESSED,
            12.0, 8.0, 12.0, 8.0,
            button, clickCount,
            false, false, false, false,
            button == MouseButton.PRIMARY, false, button == MouseButton.SECONDARY,
            false, false, true,
            pick,
        )
        captionBar.fireEvent(event)
    }
}
