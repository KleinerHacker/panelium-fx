package org.pcsoft.framework.panelium.menupane

import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleButton
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.StackPane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers the [FXMenuPane] core: registering tabs, activating them from code and by click, the
 * `active` pseudo-class on the tab-strip buttons, and left/right arrow-key navigation. Also
 * covers contextual tabs: merge order with permanent tabs, the activation fallback when the
 * active contextual tab is removed, and context-group headers.
 */
class FXMenuPaneTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: an application registers tabs on [FXMenuPane.tabs]; one tab-strip button per tab
     * must appear in the live scene graph, in registration order.
     */
    @Test
    fun `registered tabs each get a tab-strip button in order`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val edit = FXMenuTab("edit", "Edit")

        onFx { menuPane.tabs.addAll(home, edit) }
        pumpFx()

        val buttons = onFx { tabStripButtons(menuPane) }
        assertEquals(listOf("Home", "Edit"), buttons.map { it.text })
    }

    /**
     * Use case: no tab is active initially; [FXMenuPane.activeTab] is `null` until one is
     * explicitly activated.
     */
    @Test
    fun `no tab is active by default`() {
        val menuPane = showMenuPaneStage()
        onFx { menuPane.tabs.add(FXMenuTab("home", "Home")) }
        pumpFx()

        assertNull(onFx { menuPane.activeTab })
    }

    /**
     * Use case: activating a registered tab from code updates [FXMenuPane.activeTab] and marks the
     * matching tab-strip button with the `active` pseudo-class, clearing it from the previous one.
     */
    @Test
    fun `activating a tab updates the active property and button state`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val edit = FXMenuTab("edit", "Edit")
        onFx { menuPane.tabs.addAll(home, edit) }
        pumpFx()

        onFx { menuPane.activate(home) }
        pumpFx()
        assertEquals(home, onFx { menuPane.activeTab })
        assertTrue(onFx { tabStripButtons(menuPane)[0].isSelected })
        assertFalse(onFx { tabStripButtons(menuPane)[1].isSelected })

        onFx { menuPane.activate(edit) }
        pumpFx()
        assertEquals(edit, onFx { menuPane.activeTab })
        assertFalse(onFx { tabStripButtons(menuPane)[0].isSelected })
        assertTrue(onFx { tabStripButtons(menuPane)[1].isSelected })
    }

    /**
     * Use case: activating a tab that was never added to [FXMenuPane.tabs] or
     * [FXMenuPane.contextualTabs] must be rejected, so the active tab always stays consistent with
     * the registered tabs.
     */
    @Test
    fun `activating an unregistered tab is rejected`() {
        val menuPane = showMenuPaneStage()
        val stray = FXMenuTab("stray", "Stray")

        assertTrue(onFx {
            runCatching { menuPane.activate(stray) }.isFailure
        })
    }

    /**
     * Use case: clicking a tab-strip button activates the corresponding tab, mirroring what a user
     * does with the mouse.
     */
    @Test
    fun `clicking a tab-strip button activates that tab`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val edit = FXMenuTab("edit", "Edit")
        onFx { menuPane.tabs.addAll(home, edit) }
        pumpFx()

        onFx { tabStripButtons(menuPane)[1].fire() }
        pumpFx()

        assertEquals(edit, onFx { menuPane.activeTab })
    }

    /**
     * Use case: with the tab strip focused, the right arrow key advances to the next tab and wraps
     * around from the last tab to the first.
     */
    @Test
    fun `right arrow activates the next tab and wraps around`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val edit = FXMenuTab("edit", "Edit")
        val view = FXMenuTab("view", "View")
        onFx { menuPane.tabs.addAll(home, edit, view) }
        pumpFx()
        onFx { menuPane.activate(view) }
        pumpFx()

        fireArrowKey(menuPane, KeyCode.RIGHT)
        assertEquals(home, onFx { menuPane.activeTab })
    }

    /**
     * Use case: with the tab strip focused, the left arrow key moves to the previous tab and wraps
     * around from the first tab to the last.
     */
    @Test
    fun `left arrow activates the previous tab and wraps around`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val edit = FXMenuTab("edit", "Edit")
        onFx { menuPane.tabs.addAll(home, edit) }
        pumpFx()
        onFx { menuPane.activate(home) }
        pumpFx()

        fireArrowKey(menuPane, KeyCode.LEFT)
        assertEquals(edit, onFx { menuPane.activeTab })
    }

    /**
     * Use case: a disabled tab's button must be disabled in the UI so the user cannot select it
     * with the mouse.
     */
    @Test
    fun `disabling a tab disables its tab-strip button`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx { menuPane.tabs.add(home) }
        pumpFx()

        onFx { home.isDisabled = true }
        pumpFx()

        assertTrue(onFx { tabStripButtons(menuPane)[0].isDisable })
    }

    /**
     * Use case: contextual tabs added via [FXMenuPane.contextualTabs] must render after all
     * permanent tabs, in their own insertion order.
     */
    @Test
    fun `contextual tabs render after permanent tabs in insertion order`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val edit = FXMenuTab("edit", "Edit")
        val design = FXMenuTab("design", "Design")
        val layout = FXMenuTab("layout", "Layout")
        onFx {
            menuPane.tabs.addAll(home, edit)
            menuPane.contextualTabs.addAll(design, layout)
        }
        pumpFx()

        val buttons = onFx { tabStripButtons(menuPane) }
        assertEquals(listOf("Home", "Edit", "Design", "Layout"), buttons.map { it.text })
    }

    /**
     * Use case: removing the active contextual tab falls back to the permanent tab that was
     * active before the contextual tab was activated.
     */
    @Test
    fun `removing the active contextual tab falls back to the previous permanent tab`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val design = FXMenuTab("design", "Design")
        onFx {
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.contextualTabs.add(design)
            menuPane.activate(design)
        }
        pumpFx()
        assertEquals(design, onFx { menuPane.activeTab })

        onFx { menuPane.contextualTabs.remove(design) }
        pumpFx()

        assertEquals(home, onFx { menuPane.activeTab })
    }

    /**
     * Use case: removing the active contextual tab when no permanent tab was ever active leaves
     * no active tab at all, rather than pointing at a stale reference.
     */
    @Test
    fun `removing the active contextual tab without a prior permanent tab clears the active tab`() {
        val menuPane = showMenuPaneStage()
        val design = FXMenuTab("design", "Design")
        onFx {
            menuPane.contextualTabs.add(design)
            menuPane.activate(design)
        }
        pumpFx()

        onFx { menuPane.contextualTabs.remove(design) }
        pumpFx()

        assertNull(onFx { menuPane.activeTab })
    }

    /**
     * Use case: contextual tabs assigned to the same [FXMenuContextTabGroup] get a single group-header
     * label rendered directly above the first of them in the tab strip.
     */
    @Test
    fun `grouped contextual tabs get a single group header`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val design = FXMenuTab("design", "Design")
        val layout = FXMenuTab("layout", "Layout")
        val group = FXMenuContextTabGroup("Table Tools", "#4a90d9")
        onFx {
            menuPane.tabs.add(home)
            menuPane.assignToGroup(design, group)
            menuPane.assignToGroup(layout, group)
            menuPane.contextualTabs.addAll(design, layout)
        }
        pumpFx()

        val headers = onFx { groupHeaders(menuPane) }
        assertEquals(listOf("Table Tools"), headers.map { it.text })
    }

    /**
     * Use case: when the tab strip's content is wider than its viewport, scrolling the mouse
     * wheel over the strip must move the horizontal scroll position instead of doing nothing.
     */
    @Test
    fun `mouse wheel scrolls the tab strip horizontally when it overflows`() {
        val menuPane = showMenuPaneStage()
        onFx { menuPane.tabs.addAll(manyTabs()) }
        pumpFx()

        val scrollPane = onFx { tabStripScrollPane(menuPane) }
        assertEquals(0.0, onFx { scrollPane.hvalue })

        onFx {
            scrollPane.fireEvent(
                ScrollEvent(
                    ScrollEvent.SCROLL, 0.0, 0.0, 0.0, 0.0, false, false, false, false, true, false,
                    0.0, -50.0, 0.0, -50.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0.0,
                    ScrollEvent.VerticalTextScrollUnits.NONE, 0.0, 0, null
                )
            )
        }
        pumpFx()

        assertTrue(onFx { scrollPane.hvalue } > 0.0)
    }

    /**
     * Use case: activating a tab that is scrolled out of view on the right must scroll the tab
     * strip so its button becomes visible again.
     */
    @Test
    fun `activating an off-screen tab scrolls it into view`() {
        val menuPane = showMenuPaneStage()
        val tabs = manyTabs()
        onFx { menuPane.tabs.addAll(tabs) }
        pumpFx()

        onFx { menuPane.activate(tabs.last()) }
        pumpFx()

        val scrollPane = onFx { tabStripScrollPane(menuPane) }
        assertTrue(onFx { scrollPane.hvalue } > 0.0)
    }

    /**
     * Use case: setting [FXMenuPane.fileTab] renders exactly one file-tab button, labelled with the
     * tab title, without adding the file tab to the regular tab-strip buttons.
     */
    @Test
    fun `setting the file tab renders a dedicated file-tab button outside the strip`() {
        val menuPane = showMenuPaneStage()
        onFx { menuPane.tabs.add(FXMenuTab("home", "Home")) }
        pumpFx()

        onFx { menuPane.fileTab = FXMenuTab("file", "File") }
        pumpFx()

        val fileButton = onFx { fileTabButton(menuPane) }
        assertEquals("File", fileButton.text)
        assertTrue(onFx { fileButton.isVisible && fileButton.isManaged })
        assertEquals(listOf("Home"), onFx { tabStripButtons(menuPane) }.map { it.text })
    }

    /**
     * Use case: the file tab is a separate slot, so it never shows up in
     * [FXMenuPane.tabs] or [FXMenuPane.contextualTabs].
     */
    @Test
    fun `the file tab is kept out of the tab lists`() {
        val menuPane = showMenuPaneStage()
        val file = FXMenuTab("file", "File")

        onFx { menuPane.fileTab = file }
        pumpFx()

        assertFalse(onFx { menuPane.tabs.contains(file) })
        assertFalse(onFx { menuPane.contextualTabs.contains(file) })
        assertEquals(file, onFx { menuPane.fileTab })
    }

    /**
     * Use case: clearing [FXMenuPane.fileTab] hides its button again and unmanages it so it no
     * longer takes up layout space.
     */
    @Test
    fun `clearing the file tab hides its button`() {
        val menuPane = showMenuPaneStage()
        onFx { menuPane.fileTab = FXMenuTab("file", "File") }
        pumpFx()

        onFx { menuPane.fileTab = null }
        pumpFx()

        val fileButton = onFx { fileTabButton(menuPane) }
        assertFalse(onFx { fileButton.isVisible })
        assertFalse(onFx { fileButton.isManaged })
    }

    /**
     * Use case: disabling the file tab's [FXMenuTab] disables its button in the UI, mirroring the
     * behaviour of a regular tab-strip button.
     */
    @Test
    fun `disabling the file tab disables its button`() {
        val menuPane = showMenuPaneStage()
        val file = FXMenuTab("file", "File")
        onFx { menuPane.fileTab = file }
        pumpFx()

        onFx { file.isDisabled = true }
        pumpFx()

        assertTrue(onFx { fileTabButton(menuPane).isDisable })
    }

    /**
     * Use case: arrow-key navigation only walks the regular tabs and never lands on the file tab,
     * because the file tab is not part of the navigable strip.
     */
    @Test
    fun `arrow-key navigation ignores the file tab`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val edit = FXMenuTab("edit", "Edit")
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.tabs.addAll(home, edit)
            menuPane.activate(home)
        }
        pumpFx()

        fireArrowKey(menuPane, KeyCode.LEFT)
        assertEquals(edit, onFx { menuPane.activeTab })
    }

    /**
     * Use case: [FXMenuPane.backstageContent] round-trips through its property so an application can
     * set and read back the backstage panel.
     */
    @Test
    fun `backstage content round-trips through the property`() {
        val menuPane = showMenuPaneStage()
        val panel = Label("Backstage")

        onFx { menuPane.backstageContent = panel }
        pumpFx()

        assertEquals(panel, onFx { menuPane.backstageContent })
        assertEquals(panel, onFx { menuPane.backstageContentProperty().get() })
    }

    /**
     * Use case: the backstage panel is parked in the overlay slot but stays invisible and
     * unmanaged, since no plan wires up its display yet.
     */
    @Test
    fun `backstage content is parked invisible and unmanaged in the overlay slot`() {
        val menuPane = showMenuPaneStage()
        val panel = Label("Backstage")

        onFx { menuPane.backstageContent = panel }
        pumpFx()

        val slot = onFx { backstageSlot(menuPane) }
        assertEquals(listOf<Any>(panel), onFx { slot.children.toList() })
        assertFalse(onFx { slot.isVisible })
        assertFalse(onFx { slot.isManaged })
    }

    private fun manyTabs(): List<FXMenuTab> = (1..30).map { FXMenuTab("tab-$it", "Menu Tab Number $it") }

    private fun fileTabButton(menuPane: FXMenuPane): ToggleButton =
        menuPane.lookupAll(".menu-pane-strip-file-button").filterIsInstance<ToggleButton>().first()

    private fun backstageSlot(menuPane: FXMenuPane): StackPane =
        menuPane.lookup("#backstageContentSlot") as StackPane

    private fun tabStripButtons(menuPane: FXMenuPane): List<ToggleButton> =
        menuPane.lookupAll(".menu-pane-strip-button").filterIsInstance<ToggleButton>()
            .sortedBy { menuPane.lookupAll(".menu-pane-strip-button").indexOf(it) }

    private fun groupHeaders(menuPane: FXMenuPane): List<Label> =
        menuPane.lookupAll(".menu-pane-context-group-header").filterIsInstance<Label>()

    private fun tabStripScrollPane(menuPane: FXMenuPane): ScrollPane =
        menuPane.lookup(".menu-pane-strip-scroll-pane") as ScrollPane

    private fun fireArrowKey(menuPane: FXMenuPane, code: KeyCode) {
        onFx {
            val strip = menuPane.lookup(".menu-pane-strip")
            strip.fireEvent(KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false))
        }
        pumpFx()
    }
}
