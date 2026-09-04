package org.pcsoft.framework.panelium.menutab

import javafx.scene.control.ToggleButton
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menutab.support.AbstractMenuTabUiTest

/**
 * Covers the [FXMenuTab] core: registering tabs, activating them from code and by click, the
 * `active` pseudo-class on the tab-strip buttons, and left/right arrow-key navigation.
 */
class FXMenuTabTest : AbstractMenuTabUiTest() {

    /**
     * Use case: an application registers tabs on [FXMenuTab.tabs]; one tab-strip button per tab
     * must appear in the live scene graph, in registration order.
     */
    @Test
    fun `registered tabs each get a tab-strip button in order`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val edit = MenuTab("edit", "Edit")

        onFx { menuTab.tabs.addAll(home, edit) }
        pumpFx()

        val buttons = onFx { tabStripButtons(menuTab) }
        assertEquals(listOf("Home", "Edit"), buttons.map { it.text })
    }

    /**
     * Use case: no tab is active initially; [FXMenuTab.activeTab] is `null` until one is
     * explicitly activated.
     */
    @Test
    fun `no tab is active by default`() {
        val menuTab = showMenuTabStage()
        onFx { menuTab.tabs.add(MenuTab("home", "Home")) }
        pumpFx()

        assertNull(onFx { menuTab.activeTab })
    }

    /**
     * Use case: activating a registered tab from code updates [FXMenuTab.activeTab] and marks the
     * matching tab-strip button with the `active` pseudo-class, clearing it from the previous one.
     */
    @Test
    fun `activating a tab updates the active property and button state`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val edit = MenuTab("edit", "Edit")
        onFx { menuTab.tabs.addAll(home, edit) }
        pumpFx()

        onFx { menuTab.activate(home) }
        pumpFx()
        assertEquals(home, onFx { menuTab.activeTab })
        assertTrue(onFx { tabStripButtons(menuTab)[0].isSelected })
        assertFalse(onFx { tabStripButtons(menuTab)[1].isSelected })

        onFx { menuTab.activate(edit) }
        pumpFx()
        assertEquals(edit, onFx { menuTab.activeTab })
        assertFalse(onFx { tabStripButtons(menuTab)[0].isSelected })
        assertTrue(onFx { tabStripButtons(menuTab)[1].isSelected })
    }

    /**
     * Use case: activating a tab that was never added to [FXMenuTab.tabs] must be rejected, so the
     * active tab always stays consistent with the registered list.
     */
    @Test
    fun `activating an unregistered tab is rejected`() {
        val menuTab = showMenuTabStage()
        val stray = MenuTab("stray", "Stray")

        assertTrue(onFx {
            runCatching { menuTab.activate(stray) }.isFailure
        })
    }

    /**
     * Use case: clicking a tab-strip button activates the corresponding tab, mirroring what a user
     * does with the mouse.
     */
    @Test
    fun `clicking a tab-strip button activates that tab`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val edit = MenuTab("edit", "Edit")
        onFx { menuTab.tabs.addAll(home, edit) }
        pumpFx()

        onFx { tabStripButtons(menuTab)[1].fire() }
        pumpFx()

        assertEquals(edit, onFx { menuTab.activeTab })
    }

    /**
     * Use case: with the tab strip focused, the right arrow key advances to the next tab and wraps
     * around from the last tab to the first.
     */
    @Test
    fun `right arrow activates the next tab and wraps around`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val edit = MenuTab("edit", "Edit")
        val view = MenuTab("view", "View")
        onFx { menuTab.tabs.addAll(home, edit, view) }
        pumpFx()
        onFx { menuTab.activate(view) }
        pumpFx()

        fireArrowKey(menuTab, KeyCode.RIGHT)
        assertEquals(home, onFx { menuTab.activeTab })
    }

    /**
     * Use case: with the tab strip focused, the left arrow key moves to the previous tab and wraps
     * around from the first tab to the last.
     */
    @Test
    fun `left arrow activates the previous tab and wraps around`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val edit = MenuTab("edit", "Edit")
        onFx { menuTab.tabs.addAll(home, edit) }
        pumpFx()
        onFx { menuTab.activate(home) }
        pumpFx()

        fireArrowKey(menuTab, KeyCode.LEFT)
        assertEquals(edit, onFx { menuTab.activeTab })
    }

    /**
     * Use case: a disabled tab's button must be disabled in the UI so the user cannot select it
     * with the mouse.
     */
    @Test
    fun `disabling a tab disables its tab-strip button`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        onFx { menuTab.tabs.add(home) }
        pumpFx()

        onFx { home.isDisabled = true }
        pumpFx()

        assertTrue(onFx { tabStripButtons(menuTab)[0].isDisable })
    }

    private fun tabStripButtons(menuTab: FXMenuTab): List<ToggleButton> =
        menuTab.lookupAll(".menu-tab-strip-button").filterIsInstance<ToggleButton>()
            .sortedBy { menuTab.lookupAll(".menu-tab-strip-button").indexOf(it) }

    private fun fireArrowKey(menuTab: FXMenuTab, code: KeyCode) {
        onFx {
            val strip = menuTab.lookup(".menu-tab-strip")
            strip.fireEvent(KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false))
        }
        pumpFx()
    }
}
