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

import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest

/**
 * Headless coverage for the single-stage binding contract of [ChromePane.attachStage]: a pane binds
 * to exactly one stage and rejects every further attach attempt instead of leaking a second
 * window-operations service, drag handler and stage listener set.
 */
class ChromePaneAttachStageTest : AbstractChromeUiTest() {

    /**
     * Use case: after a [ChromePane] has been attached to a stage, a second [ChromePane.attachStage]
     * call with a different stage fails fast with an [IllegalStateException] and leaves the original
     * binding (its [ChromePane.windowOps]) untouched.
     */
    @Test
    fun `attachStage rejects a second stage`() {
        val (pane, _) = showChromeStage()

        onFx {
            val secondStage = Stage().apply {
                initStyle(StageStyle.TRANSPARENT)
                scene = Scene(Region(), 400.0, 300.0)
            }

            assertThrows(IllegalStateException::class.java) { pane.attachStage(secondStage) }
            assertNotNull(pane.windowOps, "the rejected call keeps the first stage binding intact")
        }
    }

    /**
     * Use case: re-attaching the identical stage is refused as well - the guard triggers on the pane
     * already being bound, regardless of stage identity.
     */
    @Test
    fun `attachStage rejects re-attaching the same stage`() {
        val (pane, stage) = showChromeStage()

        onFx {
            assertThrows(IllegalStateException::class.java) { pane.attachStage(stage) }
        }
    }
}
