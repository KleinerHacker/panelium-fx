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
