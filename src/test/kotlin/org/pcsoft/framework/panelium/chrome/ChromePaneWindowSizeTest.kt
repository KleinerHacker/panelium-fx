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
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest
import org.pcsoft.framework.panelium.menupane.FXMenuPane
import org.pcsoft.framework.panelium.menupane.FXMenuTab

/**
 * Headless coverage for the window-sizing contract of [ChromePane]: an explicit `prefWidth` /
 * `prefHeight` set on the pane forces the hosting window to that size and keeps it there, so a
 * docked, collapsible [FXMenuPane] can no longer shrink the window when its ribbon collapses.
 */
class ChromePaneWindowSizeTest : AbstractChromeUiTest() {

    private fun menuPane(): FXMenuPane = FXMenuPane().apply {
        tabs.addAll(FXMenuTab("home", "Home"), FXMenuTab("view", "View"))
        activeTab = tabs.first()
        isCollapsible = true
    }

    /**
     * A [ChromePane] carrying an explicit `prefWidth` / `prefHeight` and attached to a stage that was
     * never sized itself opens the window at exactly that preferred size: [ChromePane.attachStage]
     * pins the preference onto the stage.
     */
    @Test
    fun `preferred size forces the window size`() {
        lateinit var stage: Stage

        onFx {
            val pane = ChromePane(Label("Body")).apply {
                prefWidth = 640.0
                prefHeight = 400.0
            }
            stage = Stage().apply {
                initStyle(StageStyle.UNDECORATED)
                scene = Scene(pane)
                x = 120.0
                y = 90.0
            }
            pane.attachStage(stage)
            stage.show()
        }
        trackStage(stage)
        pumpFx()

        onFx {
            assertEquals(640.0, stage.width, 1.0, "window width follows the pane prefWidth")
            assertEquals(400.0, stage.height, 1.0, "window height follows the pane prefHeight")
        }
    }

    /**
     * With the preferred size pinned, collapsing the ribbon of a docked [FXMenuPane] - which shrinks
     * the framed content's own preferred height - leaves the window size unchanged.
     */
    @Test
    fun `collapsing a docked ribbon does not resize the window`() {
        lateinit var stage: Stage
        lateinit var docked: FXMenuPane

        onFx {
            docked = menuPane()
            val pane = ChromePane(
                BorderPane().apply {
                    top = docked
                    center = Label("Body")
                },
            ).apply {
                prefWidth = 640.0
                prefHeight = 400.0
            }
            stage = Stage().apply {
                initStyle(StageStyle.UNDECORATED)
                scene = Scene(pane)
                x = 120.0
                y = 90.0
            }
            pane.attachStage(stage)
            stage.show()
        }
        trackStage(stage)
        pumpFx()

        val widthBefore = onFx { stage.width }
        val heightBefore = onFx { stage.height }

        onFx { docked.isCollapsed = true }
        pumpFx()

        onFx {
            assertEquals(widthBefore, stage.width, 0.5, "collapsing keeps the window width")
            assertEquals(heightBefore, stage.height, 0.5, "collapsing keeps the window height")
        }
    }

    /**
     * A stage that already carries an explicit size is not overridden by [ChromePane.attachStage]:
     * the caller's window size wins over an unset pane preference.
     */
    @Test
    fun `explicit stage size is left untouched when the pane has no preference`() {
        lateinit var stage: Stage

        onFx {
            val pane = ChromePane(Label("Body"))
            stage = Stage().apply {
                initStyle(StageStyle.UNDECORATED)
                scene = Scene(pane)
                x = 120.0
                y = 90.0
                width = 500.0
                height = 360.0
            }
            pane.attachStage(stage)
            stage.show()
        }
        trackStage(stage)
        pumpFx()

        onFx {
            assertEquals(500.0, stage.width, 1.0, "caller width is kept")
            assertEquals(360.0, stage.height, 1.0, "caller height is kept")
        }
    }
}
