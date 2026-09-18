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

import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers the [FXBackstageMenuPane] IP-03 quick action footer: registered [FXBackstageQuickAction]
 * entries are rendered as icon-only buttons, clicking one invokes its
 * [FXBackstageQuickAction.onAction] callback directly, and the footer stays in sync with the
 * [FXBackstageMenuPane.quickActions] list without ever touching the menu selection or content
 * area.
 */
class FXBackstageMenuPaneQuickActionsTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: registered [FXBackstageQuickAction] entries are rendered as one button each in the
     * quick action footer, in registration order.
     */
    @Test
    fun `quick action footer shows one button per registered action`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        val first = FXBackstageQuickAction()
        val second = FXBackstageQuickAction()

        onFx { backstageMenuPane.quickActions.addAll(first, second) }
        pumpFx()

        val buttons = onFx { quickActionArea(backstageMenuPane).children.toList() }
        assertEquals(2, buttons.size)
        assertTrue(buttons.all { it is Button })
    }

    /**
     * Use case: clicking a quick action's button invokes its [FXBackstageQuickAction.onAction]
     * callback directly.
     */
    @Test
    fun `clicking a quick action button invokes its callback`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        var invoked = false
        val quickAction = FXBackstageQuickAction(onAction = { invoked = true })
        onFx { backstageMenuPane.quickActions.add(quickAction) }
        pumpFx()

        onFx { (quickActionArea(backstageMenuPane).children[0] as Button).fire() }
        pumpFx()

        assertTrue(invoked)
    }

    /**
     * Use case: clicking a quick action's button does not select any [FXBackstageMenuItem] and
     * leaves [FXBackstageMenuPane.selectedItem] at `null`.
     */
    @Test
    fun `clicking a quick action button does not change the selection`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        onFx { backstageMenuPane.items.add(FXBackstageMenuItem(text = "Info", content = Label("Info content"))) }
        val quickAction = FXBackstageQuickAction(onAction = {})
        onFx { backstageMenuPane.quickActions.add(quickAction) }
        pumpFx()

        onFx { (quickActionArea(backstageMenuPane).children[0] as Button).fire() }
        pumpFx()

        assertNull(onFx { backstageMenuPane.selectedItem })
    }

    /**
     * Use case: clicking a quick action's button leaves the content area untouched - it stays
     * empty, since no menu item has been selected.
     */
    @Test
    fun `clicking a quick action button does not change the content area`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        onFx { backstageMenuPane.items.add(FXBackstageMenuItem(text = "Info", content = Label("Info content"))) }
        val quickAction = FXBackstageQuickAction(onAction = {})
        onFx { backstageMenuPane.quickActions.add(quickAction) }
        pumpFx()

        onFx { (quickActionArea(backstageMenuPane).children[0] as Button).fire() }
        pumpFx()

        val contentArea = onFx { contentArea(backstageMenuPane) }
        assertTrue(onFx { contentArea.children.isEmpty() })
    }

    /**
     * Use case: adding a new [FXBackstageQuickAction] to an already-shown [FXBackstageMenuPane]
     * grows the footer by one button.
     */
    @Test
    fun `adding a quick action after showing updates the footer`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        onFx { backstageMenuPane.quickActions.add(FXBackstageQuickAction()) }
        pumpFx()

        onFx { backstageMenuPane.quickActions.add(FXBackstageQuickAction()) }
        pumpFx()

        assertEquals(2, onFx { quickActionArea(backstageMenuPane).children.size })
    }

    /**
     * Use case: removing a [FXBackstageQuickAction] from an already-shown [FXBackstageMenuPane]
     * shrinks the footer by one button.
     */
    @Test
    fun `removing a quick action after showing updates the footer`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        val quickAction = FXBackstageQuickAction()
        onFx { backstageMenuPane.quickActions.add(quickAction) }
        pumpFx()

        onFx { backstageMenuPane.quickActions.remove(quickAction) }
        pumpFx()

        assertTrue(onFx { quickActionArea(backstageMenuPane).children.isEmpty() })
    }

    private fun quickActionArea(backstageMenuPane: FXBackstageMenuPane): HBox =
        backstageMenuPane.lookup(".backstage-menu-pane-quick-action-area") as HBox

    private fun contentArea(backstageMenuPane: FXBackstageMenuPane): StackPane =
        backstageMenuPane.lookup(".backstage-menu-pane-content-area") as StackPane
}
