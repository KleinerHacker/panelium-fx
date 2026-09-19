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

import javafx.geometry.Point2D
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.HBox
import javafx.stage.Window
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Covers the ribbon right-click menu on [FXMenuPane]: right-clicking the tab-strip row or the group
 * strip opens a [ContextMenu] whose built-in entry toggles the collapse state and whose label
 * mirrors that state, optionally preceded by host entries ([FXMenuPane.contextMenuItems]) and
 * gated by [FXMenuPane.isContextMenuEnabled].
 *
 * The default locale is pinned to English for this class so the label assertions test the base
 * bundle regardless of the machine's locale.
 */
class FXMenuPaneContextMenuTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: right-clicking the tab-strip row opens the ribbon context menu with exactly one
     * entry - the collapse/expand toggle - labelled for the current (expanded) state.
     */
    @Test
    fun `right clicking the tab strip opens the one-entry ribbon menu`() {
        val menuPane = showMenuPaneStage()
        onFx { menuPane.tabs.add(FXMenuTab("home", "Home")) }
        pumpFx()

        onFx { tabStripRow(menuPane).fireEvent(contextMenuRequest()) }
        pumpFx()

        val menu = shownRibbonMenu()
        assertEquals(1, menu.items.size)
        assertEquals("Collapse", menu.items.first().text)
    }

    /**
     * Use case: a right-click on the group strip opens the same menu, so the gesture works over the
     * whole ribbon band, not just the tab row.
     */
    @Test
    fun `right clicking the group strip opens the ribbon menu`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            home.groups.add(FXMenuGroup().apply { title = "Clipboard" })
            menuPane.tabs.add(home)
            menuPane.activate(home)
        }
        pumpFx()

        onFx { groupStrip(menuPane).fireEvent(contextMenuRequest()) }
        pumpFx()

        assertEquals(1, shownRibbonMenu().items.size)
    }

    /**
     * Use case: activating the menu entry collapses the expanded ribbon; opening the menu again and
     * activating the entry expands it, and the entry label follows the state on each pass.
     */
    @Test
    fun `the menu entry toggles the collapse state and its label follows it`() {
        val menuPane = showMenuPaneStage()
        onFx { menuPane.tabs.add(FXMenuTab("home", "Home")) }
        pumpFx()

        onFx { tabStripRow(menuPane).fireEvent(contextMenuRequest()) }
        pumpFx()
        assertFalse(onFx { menuPane.isCollapsed })

        onFx { shownRibbonMenu().items.first().fire() }
        pumpFx()
        assertTrue(onFx { menuPane.isCollapsed })

        onFx { tabStripRow(menuPane).fireEvent(contextMenuRequest()) }
        pumpFx()
        assertEquals("Expand", onFx { shownRibbonMenu().items.first().text })

        onFx { shownRibbonMenu().items.first().fire() }
        pumpFx()
        assertFalse(onFx { menuPane.isCollapsed })
    }

    /**
     * Use case: setting the collapse state from code while the menu is open updates the entry label,
     * so a right-click always shows the label matching the live state.
     */
    @Test
    fun `the entry label reacts to a collapse state change from code`() {
        val menuPane = showMenuPaneStage()
        onFx { menuPane.tabs.add(FXMenuTab("home", "Home")) }
        pumpFx()

        onFx { tabStripRow(menuPane).fireEvent(contextMenuRequest()) }
        pumpFx()
        val menu = shownRibbonMenu()
        assertEquals("Collapse", onFx { menu.items.first().text })

        onFx { menuPane.isCollapsed = true }
        pumpFx()
        assertEquals("Expand", onFx { menu.items.first().text })
    }

    /**
     * Use case: while collapsing is switched off (`isCollapsible = false`) and no host entries are
     * registered, a right-click on the ribbon must not open the context menu at all - it would have
     * nothing to show.
     */
    @Test
    fun `no ribbon menu opens while collapsing is disabled and no host entries exist`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.isCollapsible = false
        }
        pumpFx()

        onFx { tabStripRow(menuPane).fireEvent(contextMenuRequest()) }
        pumpFx()

        assertFalse(
            onFx {
                Window.getWindows()
                    .filterIsInstance<ContextMenu>()
                    .any { it.styleClass.contains("menu-pane-context-menu") && it.isShowing }
            },
        )
    }

    /**
     * Use case: host-supplied entries ([FXMenuPane.contextMenuItems]) appear first, in insertion
     * order, followed by a separator and finally the built-in collapse/expand toggle as the last
     * entry.
     */
    @Test
    fun `host entries appear before a separator and the built-in toggle`() {
        val menuPane = showMenuPaneStage()
        val hostItem = MenuItem("Custom Action")
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.contextMenuItems.add(hostItem)
        }
        pumpFx()

        onFx { tabStripRow(menuPane).fireEvent(contextMenuRequest()) }
        pumpFx()

        val items = onFx { shownRibbonMenu().items.toList() }
        assertEquals(3, items.size)
        assertEquals("Custom Action", items[0].text)
        assertTrue(items[1] is SeparatorMenuItem)
        assertEquals("Collapse", items[2].text)
    }

    /**
     * Use case: while collapsing is switched off but a host entry is registered, the menu still
     * opens for that entry - only the built-in toggle and its separator drop out.
     */
    @Test
    fun `host entries keep the menu open while collapsing is disabled`() {
        val menuPane = showMenuPaneStage()
        val hostItem = MenuItem("Custom Action")
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.isCollapsible = false
            menuPane.contextMenuItems.add(hostItem)
        }
        pumpFx()

        onFx { tabStripRow(menuPane).fireEvent(contextMenuRequest()) }
        pumpFx()

        val items = onFx { shownRibbonMenu().items.toList() }
        assertEquals(1, items.size)
        assertEquals("Custom Action", items[0].text)
    }

    /**
     * Use case: `isContextMenuEnabled = false` suppresses the ribbon context menu entirely, even
     * with host entries registered and collapsing available.
     */
    @Test
    fun `no ribbon menu opens while the context menu is disabled`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.contextMenuItems.add(MenuItem("Custom Action"))
            menuPane.isContextMenuEnabled = false
        }
        pumpFx()

        onFx { tabStripRow(menuPane).fireEvent(contextMenuRequest()) }
        pumpFx()

        assertFalse(
            onFx {
                Window.getWindows()
                    .filterIsInstance<ContextMenu>()
                    .any { it.styleClass.contains("menu-pane-context-menu") && it.isShowing }
            },
        )
    }

    private fun tabStripRow(menuPane: FXMenuPane): HBox = menuPane.lookup("#tabStripRow") as HBox

    private fun groupStrip(menuPane: FXMenuPane): HBox = menuPane.lookup("#groupStrip") as HBox

    private fun shownRibbonMenu(): ContextMenu = onFx {
        Window.getWindows()
            .filterIsInstance<ContextMenu>()
            .firstOrNull { it.styleClass.contains("menu-pane-context-menu") && it.isShowing }
            ?: error("Ribbon context menu is not showing")
    }

    private fun contextMenuRequest(): ContextMenuEvent {
        val screen = Point2D(10.0, 10.0)
        return ContextMenuEvent(
            ContextMenuEvent.CONTEXT_MENU_REQUESTED,
            5.0, 5.0, screen.x, screen.y,
            false, null,
        )
    }

    companion object {

        private var savedLocale: Locale = Locale.getDefault()

        @BeforeAll
        @JvmStatic
        fun pinEnglishLocale() {
            savedLocale = Locale.getDefault()
            Locale.setDefault(Locale.ENGLISH)
            ResourceBundle.clearCache()
        }

        @AfterAll
        @JvmStatic
        fun restoreLocale() {
            Locale.setDefault(savedLocale)
            ResourceBundle.clearCache()
        }
    }
}
