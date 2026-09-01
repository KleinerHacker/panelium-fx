package org.pcsoft.framework.panelium.chrome

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.internal.ResizeEdge
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest

/**
 * Covers the window operations that [org.pcsoft.framework.panelium.chrome.internal.WindowOps]
 * performs for a framed stage: move, edge and corner resize inside the size constraints, the
 * `resizable = false` lock, minimize, maximize onto the work area, restore and the full-screen
 * caption toggle.
 */
class WindowOperationsTest : AbstractChromeUiTest() {

    /**
     * Use case: the user drags the caption, so the window origin follows the pointer delta while
     * width and height stay untouched.
     */
    @Test
    fun `move shifts the window origin by the pointer delta`() {
        val (pane, stage) = showChromeStage()
        val ops = pane.windowOps!!

        val startX = onFx { stage.x }
        val startY = onFx { stage.y }
        val width = onFx { stage.width }

        onFx {
            ops.startMove(500.0, 500.0)
            ops.moveTo(560.0, 540.0)
        }

        assertEquals(startX + 60.0, onFx { stage.x }, 0.5)
        assertEquals(startY + 40.0, onFx { stage.y }, 0.5)
        assertEquals(width, onFx { stage.width }, 0.5)
    }

    /**
     * Use case: dragging the east edge widens the window; dragging past the minimum width clamps
     * the width at `Stage.minWidth` instead of collapsing the window.
     */
    @Test
    fun `east edge resize grows the window and honours the minimum width`() {
        val (pane, stage) = showChromeStage()
        val ops = pane.windowOps!!
        onFx { stage.minWidth = 300.0 }

        onFx {
            ops.startResize(400.0, 400.0)
            ops.resize(ResizeEdge.E, 520.0, 400.0)
        }
        assertEquals(600.0, onFx { stage.width }, 0.5)

        onFx {
            ops.startResize(400.0, 400.0)
            ops.resize(ResizeEdge.E, 0.0, 400.0)
        }
        assertEquals(300.0, onFx { stage.width }, 0.5)
    }

    /**
     * Use case: dragging the north-west corner moves the top-left corner while keeping the
     * bottom-right corner of the window anchored.
     */
    @Test
    fun `north-west corner resize keeps the opposite corner anchored`() {
        val (pane, stage) = showChromeStage()
        val ops = pane.windowOps!!
        onFx {
            stage.minWidth = 100.0
            stage.minHeight = 100.0
        }

        val right = onFx { stage.x + stage.width }
        val bottom = onFx { stage.y + stage.height }

        onFx {
            ops.startResize(stage.x, stage.y)
            ops.resize(ResizeEdge.NW, stage.x - 40.0, stage.y - 20.0)
        }

        assertEquals(right, onFx { stage.x + stage.width }, 0.5)
        assertEquals(bottom, onFx { stage.y + stage.height }, 0.5)
    }

    /**
     * Use case: a non-resizable window ignores every resize drag, so its bounds stay exactly as
     * they were.
     */
    @Test
    fun `resize is a no-op while the stage is not resizable`() {
        val (pane, stage) = showChromeStage()
        val ops = pane.windowOps!!
        onFx { stage.isResizable = false }

        val width = onFx { stage.width }
        val height = onFx { stage.height }

        onFx {
            ops.startResize(400.0, 400.0)
            ops.resize(ResizeEdge.SE, 480.0, 470.0)
        }

        assertEquals(width, onFx { stage.width }, 0.5)
        assertEquals(height, onFx { stage.height }, 0.5)
    }

    /**
     * Use case: pressing the minimize button iconifies the stage.
     */
    @Test
    fun `minimize iconifies the stage`() {
        val (pane, stage) = showChromeStage()
        onFx { pane.windowOps!!.minimize() }
        assertTrue(onFx { stage.isIconified })
    }

    /**
     * Use case: maximize expands the window onto the current screen's visual bounds (the work
     * area, excluding the task bar) and restore returns it to the previous rectangle.
     */
    @Test
    fun `maximize fills the work area and restore returns the previous bounds`() {
        val (pane, stage) = showChromeStage()
        val ops = pane.windowOps!!

        val originX = onFx { stage.x }
        val originWidth = onFx { stage.width }
        val visual = onFx { javafx.stage.Screen.getPrimary().visualBounds }

        onFx { ops.maximize() }
        assertTrue(onFx { stage.isMaximized })
        assertTrue(ops.isMaximized)
        assertEquals(visual.width, onFx { stage.width }, 1.0)
        assertEquals(visual.height, onFx { stage.height }, 1.0)

        onFx { ops.restore() }
        assertFalse(onFx { stage.isMaximized })
        assertFalse(ops.isMaximized)
        assertEquals(originX, onFx { stage.x }, 0.5)
        assertEquals(originWidth, onFx { stage.width }, 0.5)
    }

    /**
     * Use case: entering full screen hides the caption bar entirely; leaving it brings the caption
     * back and shows it again.
     */
    @Test
    fun `full screen hides the caption bar and leaving it shows the caption again`() {
        val (pane, stage) = showChromeStage()

        onFx { stage.isFullScreen = true }
        pumpFx()
        assertFalse(onFx { pane.captionBar.isVisible }, "caption must be hidden in full screen")
        assertFalse(onFx { pane.captionBar.isManaged })

        onFx { stage.isFullScreen = false }
        pumpFx()
        assertTrue(onFx { pane.captionBar.isVisible }, "caption must return after full screen")
        assertTrue(onFx { pane.captionBar.isManaged })
    }
}
