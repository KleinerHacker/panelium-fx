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

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Button
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers the [FXMenuGroup] launcher button: it is only present while [FXMenuGroup.onLauncherAction]
 * is set, a click fires an ActionEvent to that handler, and clearing it removes the button again.
 */
class FXMenuGroupLauncherTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: a group without a launcher handler must not show a launcher button - it stays
     * invisible and unmanaged so it takes no room in the title row.
     */
    @Test
    fun `group without launcher action hides the launcher button`() {
        val group = renderGroup()

        val launcher = launcherButton(group)
        assertFalse(onFx { launcher.isVisible })
        assertFalse(onFx { launcher.isManaged })
    }

    /**
     * Use case: setting a launcher action must reveal the launcher button; clearing it back to
     * `null` must hide it again.
     */
    @Test
    fun `launcher button visibility follows the launcher action`() {
        val group = renderGroup()
        val launcher = launcherButton(group)

        onFx { group.onLauncherAction = EventHandler {} }
        pumpFx()
        assertTrue(onFx { launcher.isVisible })
        assertTrue(onFx { launcher.isManaged })

        onFx { group.onLauncherAction = null }
        pumpFx()
        assertFalse(onFx { launcher.isVisible })
        assertFalse(onFx { launcher.isManaged })
    }

    /**
     * Use case: clicking the launcher button must fire an ActionEvent to the configured handler
     * exactly once per click.
     */
    @Test
    fun `clicking the launcher button runs the launcher action`() {
        val group = renderGroup()
        val launcher = launcherButton(group)
        var runs = 0

        onFx { group.onLauncherAction = EventHandler<ActionEvent> { runs++ } }
        pumpFx()

        onFx { launcher.fire() }
        onFx { launcher.fire() }
        pumpFx()

        assertEquals(2, runs)
    }

    private fun renderGroup(): FXMenuGroup {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val group = FXMenuGroup().apply { title = "Clipboard" }

        onFx {
            home.groups.add(group)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()
        return group
    }

    private fun launcherButton(group: FXMenuGroup): Button =
        group.lookupAll(".menu-group-launcher").filterIsInstance<Button>().first()
}
