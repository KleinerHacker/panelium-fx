package org.pcsoft.framework.panelium.chrome

import javafx.scene.control.Button
import javafx.scene.layout.Pane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest

/**
 * Covers the OS-specific caption buttons: their placement order per [ChromeOs], the
 * minimize / maximize / close actions they trigger on the window and the `maximized` state that
 * the max-restore button follows.
 */
class CaptionButtonsTest : AbstractChromeUiTest() {

    /**
     * Use case: on Windows / Linux / other the caption buttons read minimize, max-restore, close
     * from leading to trailing.
     */
    @Test
    fun `non-mac layout orders the buttons minimize, max-restore, close`() {
        val (pane, _) = showChromeStage()
        onFx { pane.captionOs = ChromeOs.WINDOWS }
        pumpFx()

        assertEquals(listOf("minimize", "max-restore", "close"), onFx { buttonRoles(pane) })
    }

    /**
     * Use case: on macOS the caption buttons read close, minimize, zoom from leading to trailing.
     */
    @Test
    fun `mac layout orders the buttons close, minimize, zoom`() {
        val (pane, _) = showChromeStage()
        onFx { pane.captionOs = ChromeOs.MAC }
        pumpFx()

        assertEquals(listOf("close", "minimize", "max-restore"), onFx { buttonRoles(pane) })
    }

    /**
     * Use case: clicking the minimize button iconifies the window.
     */
    @Test
    fun `minimize button iconifies the window`() {
        val (pane, stage) = showChromeStage()
        onFx { pane.captionOs = ChromeOs.WINDOWS }
        pumpFx()

        onFx { button(pane, "minimize").fire() }
        assertTrue(onFx { stage.isIconified })
    }

    /**
     * Use case: clicking the max-restore button maximizes the window and clicking it again
     * restores it.
     */
    @Test
    fun `max-restore button toggles maximize`() {
        val (pane, stage) = showChromeStage()
        onFx { pane.captionOs = ChromeOs.WINDOWS }
        pumpFx()

        onFx { button(pane, "max-restore").fire() }
        pumpFx()
        assertTrue(onFx { stage.isMaximized })

        onFx { button(pane, "max-restore").fire() }
        pumpFx()
        assertFalse(onFx { stage.isMaximized })
    }

    /**
     * Use case: clicking the close button closes the window.
     */
    @Test
    fun `close button closes the window`() {
        val (pane, stage) = showChromeStage()
        onFx { pane.captionOs = ChromeOs.WINDOWS }
        pumpFx()

        onFx { button(pane, "close").fire() }
        pumpFx()
        assertFalse(onFx { stage.isShowing })
    }

    /**
     * Use case: when the window becomes maximized, the max-restore button reflects it through its
     * `maximized` pseudo-class; restoring clears it again.
     */
    @Test
    fun `max-restore button follows the maximized state`() {
        val (pane, _) = showChromeStage()
        onFx { pane.captionOs = ChromeOs.WINDOWS }
        pumpFx()
        val ops = pane.windowOps!!

        onFx { ops.maximize() }
        pumpFx()
        assertTrue(onFx { button(pane, "max-restore").pseudoClassStates.any { it.pseudoClassName == "maximized" } })

        onFx { ops.restore() }
        pumpFx()
        assertFalse(onFx { button(pane, "max-restore").pseudoClassStates.any { it.pseudoClassName == "maximized" } })
    }

    /**
     * Use case: a non-resizable window disables the max-restore button.
     */
    @Test
    fun `max-restore button is disabled when the stage is not resizable`() {
        val (pane, stage) = showChromeStage()
        onFx {
            pane.captionOs = ChromeOs.WINDOWS
            stage.isResizable = false
        }
        pumpFx()

        assertTrue(onFx { button(pane, "max-restore").isDisable })
    }

    private fun buttonBox(pane: ChromePane): Pane =
        onFx { pane.lookupAll(".chrome-caption-buttons").firstOrNull() as? Pane }
            .also { assertNotNull(it, "the caption button box must be in the scene") }!!

    private fun buttonRoles(pane: ChromePane): List<String> =
        buttonBox(pane).children.filterIsInstance<Button>()
            .map { b -> b.styleClass.first { it != "chrome-button" } }

    private fun button(pane: ChromePane, role: String): Button =
        buttonBox(pane).children.filterIsInstance<Button>().first { it.styleClass.contains(role) }
}
