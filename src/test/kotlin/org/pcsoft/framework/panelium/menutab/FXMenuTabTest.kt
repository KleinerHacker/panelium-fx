package org.pcsoft.framework.panelium.menutab

import javafx.scene.control.Label
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
 * `active` pseudo-class on the tab-strip buttons, and left/right arrow-key navigation. Also
 * covers contextual tabs: merge order with permanent tabs, the activation fallback when the
 * active contextual tab is removed, and context-group headers.
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
     * Use case: activating a tab that was never added to [FXMenuTab.tabs] or
     * [FXMenuTab.contextualTabs] must be rejected, so the active tab always stays consistent with
     * the registered tabs.
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

    /**
     * Use case: contextual tabs added via [FXMenuTab.contextualTabs] must render after all
     * permanent tabs, in their own insertion order.
     */
    @Test
    fun `contextual tabs render after permanent tabs in insertion order`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val edit = MenuTab("edit", "Edit")
        val design = MenuTab("design", "Design")
        val layout = MenuTab("layout", "Layout")
        onFx {
            menuTab.tabs.addAll(home, edit)
            menuTab.contextualTabs.addAll(design, layout)
        }
        pumpFx()

        val buttons = onFx { tabStripButtons(menuTab) }
        assertEquals(listOf("Home", "Edit", "Design", "Layout"), buttons.map { it.text })
    }

    /**
     * Use case: removing the active contextual tab falls back to the permanent tab that was
     * active before the contextual tab was activated.
     */
    @Test
    fun `removing the active contextual tab falls back to the previous permanent tab`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val design = MenuTab("design", "Design")
        onFx {
            menuTab.tabs.add(home)
            menuTab.activate(home)
            menuTab.contextualTabs.add(design)
            menuTab.activate(design)
        }
        pumpFx()
        assertEquals(design, onFx { menuTab.activeTab })

        onFx { menuTab.contextualTabs.remove(design) }
        pumpFx()

        assertEquals(home, onFx { menuTab.activeTab })
    }

    /**
     * Use case: removing the active contextual tab when no permanent tab was ever active leaves
     * no active tab at all, rather than pointing at a stale reference.
     */
    @Test
    fun `removing the active contextual tab without a prior permanent tab clears the active tab`() {
        val menuTab = showMenuTabStage()
        val design = MenuTab("design", "Design")
        onFx {
            menuTab.contextualTabs.add(design)
            menuTab.activate(design)
        }
        pumpFx()

        onFx { menuTab.contextualTabs.remove(design) }
        pumpFx()

        assertNull(onFx { menuTab.activeTab })
    }

    /**
     * Use case: contextual tabs assigned to the same [ContextTabGroup] get a single group-header
     * label rendered directly above the first of them in the tab strip.
     */
    @Test
    fun `grouped contextual tabs get a single group header`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val design = MenuTab("design", "Design")
        val layout = MenuTab("layout", "Layout")
        val group = ContextTabGroup("Table Tools", "#4a90d9")
        onFx {
            menuTab.tabs.add(home)
            menuTab.assignToGroup(design, group)
            menuTab.assignToGroup(layout, group)
            menuTab.contextualTabs.addAll(design, layout)
        }
        pumpFx()

        val headers = onFx { groupHeaders(menuTab) }
        assertEquals(listOf("Table Tools"), headers.map { it.text })
    }

    private fun tabStripButtons(menuTab: FXMenuTab): List<ToggleButton> =
        menuTab.lookupAll(".menu-tab-strip-button").filterIsInstance<ToggleButton>()
            .sortedBy { menuTab.lookupAll(".menu-tab-strip-button").indexOf(it) }

    private fun groupHeaders(menuTab: FXMenuTab): List<Label> =
        menuTab.lookupAll(".menu-tab-context-group-header").filterIsInstance<Label>()

    private fun fireArrowKey(menuTab: FXMenuTab, code: KeyCode) {
        onFx {
            val strip = menuTab.lookup(".menu-tab-strip")
            strip.fireEvent(KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false))
        }
        pumpFx()
    }
}
