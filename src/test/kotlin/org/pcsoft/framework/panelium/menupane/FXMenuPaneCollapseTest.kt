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

import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleButton
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers ribbon collapse/expand on [FXMenuPane]: the double-click-on-active-tab and explicit
 * chevron-button triggers, the `collapsed` pseudo-class, hiding and showing the group strip, the
 * transient single-click peek while collapsed (start, outside-click end, re-click end, end on
 * expand), preserving the collapse state across the file-tab backstage, and the
 * `collapseButtonVisible` opt-in that shows or hides only the chevron button.
 */
class FXMenuPaneCollapseTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: double-clicking the active tab button collapses the ribbon; a second double-click
     * expands it again, mirroring the Office ribbon gesture.
     */
    @Test
    fun `double clicking the active tab collapses and expands the ribbon`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.tabs.add(home)
            menuPane.activate(home)
        }
        pumpFx()

        onFx { stripButtons(menuPane).first().fireEvent(doubleClick()) }
        pumpFx()
        assertTrue(onFx { menuPane.isCollapsed })

        onFx { stripButtons(menuPane).first().fireEvent(doubleClick()) }
        pumpFx()
        assertFalse(onFx { menuPane.isCollapsed })
    }

    /**
     * Use case: the dedicated chevron button at the trailing edge of the tab-strip row toggles the
     * collapse state on every click, without needing a selected tab.
     */
    @Test
    fun `the collapse toggle button toggles the collapsed state`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.isCollapseButtonVisible = true
        }
        pumpFx()

        onFx { collapseToggle(menuPane).fire() }
        pumpFx()
        assertTrue(onFx { menuPane.isCollapsed })

        onFx { collapseToggle(menuPane).fire() }
        pumpFx()
        assertFalse(onFx { menuPane.isCollapsed })
    }

    /**
     * Use case: the toggle button stays in sync with the collapse state regardless of what changed
     * it - clicking the button, double-clicking the active tab, then clicking the button again must
     * each flip the state. The button is selected ("pinned") while the ribbon is shown and released
     * while it is collapsed - the inverse of `isCollapsed`.
     */
    @Test
    fun `the toggle button stays in sync when the state is changed elsewhere`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.isCollapseButtonVisible = true
        }
        pumpFx()
        assertTrue(onFx { collapseToggle(menuPane).isSelected })

        onFx { collapseToggle(menuPane).fire() }
        pumpFx()
        assertTrue(onFx { menuPane.isCollapsed })
        assertFalse(onFx { collapseToggle(menuPane).isSelected })

        onFx { stripButtons(menuPane).first().fireEvent(doubleClick()) }
        pumpFx()
        assertFalse(onFx { menuPane.isCollapsed })
        assertTrue(onFx { collapseToggle(menuPane).isSelected })

        onFx { collapseToggle(menuPane).fire() }
        pumpFx()
        assertTrue(onFx { menuPane.isCollapsed })
        assertFalse(onFx { collapseToggle(menuPane).isSelected })

        onFx { menuPane.isCollapsed = false }
        pumpFx()
        assertTrue(onFx { collapseToggle(menuPane).isSelected })
    }

    /**
     * Use case: collapsing hides the group strip and takes it out of the layout so the band
     * shrinks; expanding brings it back.
     */
    @Test
    fun `collapsing hides the group strip and expanding shows it again`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            home.groups.add(FXMenuGroup().apply { title = "Clipboard" })
            menuPane.tabs.add(home)
            menuPane.activate(home)
        }
        pumpFx()
        assertTrue(onFx { groupStripScrollPane(menuPane).isVisible })

        onFx { menuPane.isCollapsed = true }
        pumpFx()
        assertFalse(onFx { groupStripScrollPane(menuPane).isVisible })
        assertFalse(onFx { groupStripScrollPane(menuPane).isManaged })

        onFx { menuPane.isCollapsed = false }
        pumpFx()
        assertTrue(onFx { groupStripScrollPane(menuPane).isVisible })
        assertTrue(onFx { groupStripScrollPane(menuPane).isManaged })
    }

    /**
     * Use case: the `collapsed` pseudo-class is present on the component exactly while it is
     * collapsed, so a stylesheet can react to the state.
     */
    @Test
    fun `the collapsed pseudo-class tracks the collapse state`() {
        val menuPane = showMenuPaneStage()
        assertFalse(onFx { hasCollapsedPseudoClass(menuPane) })

        onFx { menuPane.isCollapsed = true }
        pumpFx()
        assertTrue(onFx { hasCollapsedPseudoClass(menuPane) })

        onFx { menuPane.isCollapsed = false }
        pumpFx()
        assertFalse(onFx { hasCollapsedPseudoClass(menuPane) })
    }

    /**
     * Use case: while collapsed, a single click on a tab activates it and reveals its groups
     * temporarily (peek) - the group strip becomes visible again but `isCollapsed` stays `true`.
     */
    @Test
    fun `a single click on a tab while collapsed peeks the groups without expanding`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val edit = FXMenuTab("edit", "Edit")
        onFx {
            menuPane.tabs.addAll(home, edit)
            menuPane.activate(home)
            menuPane.isCollapsed = true
        }
        pumpFx()
        assertFalse(onFx { groupStripScrollPane(menuPane).isVisible })

        onFx { stripButtons(menuPane)[1].fire() }
        pumpFx()

        assertEquals(edit, onFx { menuPane.activeTab })
        assertTrue(onFx { menuPane.isCollapsed })
        assertTrue(onFx { groupStripScrollPane(menuPane).isVisible })
    }

    /**
     * Use case: a mouse press outside the tab-strip row and the group strip ends an active peek,
     * hiding the group strip again while the ribbon stays collapsed.
     */
    @Test
    fun `clicking outside the ribbon ends an active peek`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.isCollapsed = true
        }
        pumpFx()
        onFx { stripButtons(menuPane).first().fire() }
        pumpFx()
        assertTrue(onFx { groupStripScrollPane(menuPane).isVisible })

        onFx { menuPane.lookup("#backstageContentSlot").fireEvent(mousePress()) }
        pumpFx()

        assertTrue(onFx { menuPane.isCollapsed })
        assertFalse(onFx { groupStripScrollPane(menuPane).isVisible })
    }

    /**
     * Use case: clicking the already-peeking active tab a second time ends the peek, so the same
     * gesture that opened it also closes it.
     */
    @Test
    fun `clicking the peeking tab again ends the peek`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.isCollapsed = true
        }
        pumpFx()

        onFx { stripButtons(menuPane).first().fire() }
        pumpFx()
        assertTrue(onFx { groupStripScrollPane(menuPane).isVisible })

        onFx { stripButtons(menuPane).first().fire() }
        pumpFx()
        assertFalse(onFx { groupStripScrollPane(menuPane).isVisible })
    }

    /**
     * Use case: expanding the ribbon while a peek is active ends the peek in the same step, leaving
     * the group strip shown for the now-expanded ribbon.
     */
    @Test
    fun `expanding the ribbon ends an active peek`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.isCollapsed = true
        }
        pumpFx()
        onFx { stripButtons(menuPane).first().fire() }
        pumpFx()

        onFx { menuPane.isCollapsed = false }
        pumpFx()

        assertFalse(onFx { menuPane.isCollapsed })
        assertTrue(onFx { groupStripScrollPane(menuPane).isVisible })

        // Re-collapsing must land in the plain collapsed state, not a lingering peek.
        onFx { menuPane.isCollapsed = true }
        pumpFx()
        assertFalse(onFx { groupStripScrollPane(menuPane).isVisible })
    }

    /**
     * Use case: a collapsed ribbon is still collapsed after the file-tab backstage is opened and
     * closed again, even if the collapse state was changed while the backstage was open.
     */
    @Test
    fun `the collapse state is restored after the file-tab backstage closes`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = Label("Backstage")
            menuPane.isCollapsed = true
            menuPane.isFileTabActive = true
        }
        pumpFx()

        onFx { menuPane.isCollapsed = false }
        pumpFx()
        onFx { menuPane.isFileTabActive = false }
        pumpFx()

        assertTrue(onFx { menuPane.isCollapsed })
    }

    /**
     * Use case: the mirror of the restore case - an expanded ribbon stays expanded after the
     * backstage closes, even if it was collapsed while the backstage was open.
     */
    @Test
    fun `an expanded ribbon stays expanded after the backstage closes`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = Label("Backstage")
            menuPane.isFileTabActive = true
        }
        pumpFx()

        onFx { menuPane.isCollapsed = true }
        pumpFx()
        onFx { menuPane.isFileTabActive = false }
        pumpFx()

        assertFalse(onFx { menuPane.isCollapsed })
    }

    /**
     * Use case: the chevron is a styleable `-fx-shape` icon, not button text - the toggle button
     * carries no text, is `GRAPHIC_ONLY`, and its graphic is the `menu-pane-collapse-toggle-icon`
     * region. Toggling collapse/expand only flips the region's shape through CSS, so the very same
     * graphic instance stays in place and never shifts.
     */
    @Test
    fun `the collapse toggle shows a styleable shape icon that stays put when it flips`() {
        val menuPane = showMenuPaneStage()
        onFx { menuPane.tabs.add(FXMenuTab("home", "Home")) }
        pumpFx()

        val toggle = collapseToggle(menuPane)
        assertTrue(onFx { toggle.text.isNullOrEmpty() })
        assertEquals(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY, onFx { toggle.contentDisplay })
        val icon = onFx { toggle.graphic }
        assertTrue(icon is javafx.scene.layout.Region)
        assertTrue((icon as javafx.scene.layout.Region).styleClass.contains("menu-pane-collapse-toggle-icon"))

        onFx { menuPane.isCollapsed = true }
        pumpFx()
        org.junit.jupiter.api.Assertions.assertSame(icon, onFx { toggle.graphic })

        onFx { menuPane.isCollapsed = false }
        pumpFx()
        org.junit.jupiter.api.Assertions.assertSame(icon, onFx { toggle.graphic })
    }

    /**
     * Use case: collapsing is enabled out of the box - `isCollapsible` defaults to `true` - but the
     * chevron button is opt-in: `isCollapseButtonVisible` defaults to `false`, so out of the box the
     * ribbon is collapsed only by double-clicking the active tab or via the context menu.
     */
    @Test
    fun `collapsing is enabled by default but the chevron is hidden`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.tabs.add(home)
            menuPane.activate(home)
        }
        pumpFx()

        assertTrue(onFx { menuPane.isCollapsible })
        assertFalse(onFx { menuPane.isCollapseButtonVisible })
        assertFalse(onFx { collapseToggle(menuPane).isVisible })
        assertFalse(onFx { collapseToggle(menuPane).isManaged })

        onFx { stripButtons(menuPane).first().fireEvent(doubleClick()) }
        pumpFx()
        assertTrue(onFx { menuPane.isCollapsed })
    }

    /**
     * Use case: setting `isCollapseButtonVisible = true` reveals the collapse/expand chevron button
     * and lays it out in the tab-strip row.
     */
    @Test
    fun `opting in with collapseButtonVisible shows the chevron button`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.isCollapseButtonVisible = true
        }
        pumpFx()

        assertTrue(onFx { collapseToggle(menuPane).isVisible })
        assertTrue(onFx { collapseToggle(menuPane).isManaged })

        onFx { menuPane.isCollapseButtonVisible = false }
        pumpFx()
        assertFalse(onFx { collapseToggle(menuPane).isVisible })
        assertFalse(onFx { collapseToggle(menuPane).isManaged })
    }

    /**
     * Use case: the chevron button needs both switches - it stays hidden when `isCollapsible` is
     * `false` even though `isCollapseButtonVisible` is `true`.
     */
    @Test
    fun `the chevron button stays hidden while collapsing is disabled`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.isCollapseButtonVisible = true
            menuPane.isCollapsible = false
        }
        pumpFx()

        assertFalse(onFx { collapseToggle(menuPane).isVisible })
        assertFalse(onFx { collapseToggle(menuPane).isManaged })
    }

    /**
     * Use case: switching `isCollapsible` off hides the collapse/expand chevron button and unmanages
     * it, so it takes no room in the tab-strip row - even when the chevron was opted in.
     */
    @Test
    fun `disabling collapsing hides the chevron button`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.isCollapseButtonVisible = true
            menuPane.isCollapsible = false
        }
        pumpFx()

        assertFalse(onFx { collapseToggle(menuPane).isVisible })
        assertFalse(onFx { collapseToggle(menuPane).isManaged })
    }

    /**
     * Use case: switching `isCollapsible` off while the ribbon is collapsed forces it back to
     * expanded immediately.
     */
    @Test
    fun `disabling collapsing expands an already collapsed ribbon`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.isCollapsed = true
        }
        pumpFx()
        assertTrue(onFx { menuPane.isCollapsed })

        onFx { menuPane.isCollapsible = false }
        pumpFx()

        assertFalse(onFx { menuPane.isCollapsed })
    }

    /**
     * Use case: while `isCollapsible` is `false`, setting `isCollapsed = true` from code is ignored,
     * so the ribbon can never be collapsed behind the user's back.
     */
    @Test
    fun `setting collapsed from code is ignored while collapsing is disabled`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.isCollapsible = false
        }
        pumpFx()

        onFx { menuPane.isCollapsed = true }
        pumpFx()

        assertFalse(onFx { menuPane.isCollapsed })
    }

    /**
     * Use case: while `isCollapsible` is `false`, the double-click-the-active-tab gesture no longer
     * collapses the ribbon.
     */
    @Test
    fun `double clicking the active tab does nothing while collapsing is disabled`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.isCollapsible = false
        }
        pumpFx()

        onFx { stripButtons(menuPane).first().fireEvent(doubleClick()) }
        pumpFx()

        assertFalse(onFx { menuPane.isCollapsed })
    }

    /**
     * Use case: while a peek is active the component carries the `peeking` pseudo-class on top of
     * `collapsed`, and drops it again once the peek ends, so a stylesheet can react to the transient
     * reveal.
     */
    @Test
    fun `the peeking pseudo-class tracks an active peek`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.isCollapsed = true
        }
        pumpFx()
        assertFalse(onFx { hasPeekingPseudoClass(menuPane) })

        onFx { stripButtons(menuPane).first().fire() }
        pumpFx()
        assertTrue(onFx { hasPeekingPseudoClass(menuPane) })

        onFx { stripButtons(menuPane).first().fire() }
        pumpFx()
        assertFalse(onFx { hasPeekingPseudoClass(menuPane) })
    }

    /**
     * Use case: a peek reveals the active tab's groups at the group strip's full body height - the
     * `menu-pane:collapsed:peeking` stylesheet rule undoes the height collapse that `collapsed`
     * alone forces, so the group contents are no longer clipped to a zero-height band.
     */
    @Test
    fun `a peek restores the group strip body height instead of clipping it`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            home.groups.add(FXMenuGroup().apply { title = "Clipboard" })
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.isCollapsed = true
        }
        pumpFx()

        onFx { stripButtons(menuPane).first().fire() }
        pumpFx()
        onFx {
            menuPane.applyCss()
            menuPane.layout()
        }
        pumpFx()

        val strip = onFx { menuPane.lookup(".menu-pane-group-strip") as HBox }
        assertTrue(onFx { strip.prefHeight(-1.0) } >= 100.0)

        onFx { stripButtons(menuPane).first().fire() }
        pumpFx()
        onFx {
            menuPane.applyCss()
            menuPane.layout()
        }
        pumpFx()
        assertEquals(0.0, onFx { strip.prefHeight(-1.0) })
    }

    private fun hasPeekingPseudoClass(menuPane: FXMenuPane): Boolean =
        menuPane.pseudoClassStates.any { it.pseudoClassName == "peeking" }

    private fun hasCollapsedPseudoClass(menuPane: FXMenuPane): Boolean =
        menuPane.pseudoClassStates.any { it.pseudoClassName == "collapsed" }

    private fun stripButtons(menuPane: FXMenuPane): List<ToggleButton> {
        val strip = menuPane.lookup(".menu-pane-strip") as HBox
        return strip.children.filterIsInstance<ToggleButton>()
    }

    private fun collapseToggle(menuPane: FXMenuPane): ToggleButton =
        menuPane.lookup(".menu-pane-collapse-toggle") as ToggleButton

    private fun groupStripScrollPane(menuPane: FXMenuPane): ScrollPane =
        menuPane.lookup("#groupStripScrollPane") as ScrollPane

    private fun mousePress(): MouseEvent = MouseEvent(
        MouseEvent.MOUSE_PRESSED, 0.0, 0.0, 0.0, 0.0, MouseButton.PRIMARY, 1,
        false, false, false, false, true, false, false, false, false, false, null,
    )

    private fun doubleClick(): MouseEvent = MouseEvent(
        MouseEvent.MOUSE_CLICKED, 0.0, 0.0, 0.0, 0.0, MouseButton.PRIMARY, 2,
        false, false, false, false, true, false, false, false, false, false, null,
    )
}
