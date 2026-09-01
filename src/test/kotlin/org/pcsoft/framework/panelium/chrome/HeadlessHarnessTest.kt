package org.pcsoft.framework.panelium.chrome

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest

/**
 * Verifies the headless test harness itself: the Monocle toolkit starts, a transparent
 * [ChromePane] stage can be shown and the pane is wired to its stage. If this fails, none of the
 * behavioural chrome tests can be trusted.
 */
class HeadlessHarnessTest : AbstractChromeUiTest() {

    /**
     * Use case: a developer runs the suite on a machine without a display; showing a framed stage
     * must succeed and the pane must report the `chrome-pane` style class and a live scene.
     */
    @Test
    fun `a transparent chrome stage shows headless`() {
        val (pane, stage) = showChromeStage(title = "Harness")

        onFx {
            assertTrue(stage.isShowing, "the stage must be showing")
            assertEquals("Harness", stage.title)
            assertTrue(pane.styleClass.contains("chrome-pane"))
            assertTrue(pane.scene != null, "the pane must be part of a scene")
        }
    }
}
