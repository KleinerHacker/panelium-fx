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

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleButton
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.TimeUnit

/**
 * Covers the file-tab backstage on [FXMenuPane]: opening it from the file-tab button, the fade into
 * the local overlay slot, dismissal via Escape, an outside click and strip-tab selection, keeping
 * the previously active tab untouched, delegating to a [BackstageOverlayHost] when one is set, the
 * [FXMenuPane.onBackstageClosed] callback, and hiding the group strip while the backstage is open.
 */
class FXMenuPaneBackstageTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: clicking the file-tab button opens the backstage - `isFileTabActive` becomes `true`
     * and the overlay slot turns visible so the panel can be seen.
     */
    @Test
    fun `clicking the file tab button opens the backstage`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = Label("Backstage")
        }
        pumpFx()

        onFx { fileTabButton(menuPane).fire() }
        pumpFx()

        assertTrue(onFx { menuPane.isFileTabActive })
        assertTrue(onFx { backstageSlot(menuPane).isVisible })
    }

    /**
     * Use case: the backstage layer must stay unmanaged so opening it never grows the ribbon band;
     * its top edge sits at or below the bottom of the tab-strip row, leaving the pressed file-tab
     * button uncovered. The band may only shrink - the group strip is hidden while the backstage is
     * open - never grow.
     */
    @Test
    fun `opening the backstage never grows the band and does not cover the tab strip`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.backstageContent = VBox(Label("Backstage")).apply { minHeight = 400.0 }
        }
        pumpFx()
        val bandHeightBefore = onFx { menuPane.prefHeight(400.0) }

        onFx { menuPane.isFileTabActive = true }
        pumpFx()

        val slot = onFx { backstageSlot(menuPane) }
        assertFalse(onFx { slot.isManaged })
        assertTrue(onFx { menuPane.prefHeight(400.0) } <= bandHeightBefore + 0.5)
        assertTrue(onFx { slot.boundsInParent.minY + 0.5 >= tabStripRow(menuPane).boundsInParent.maxY })
    }

    /**
     * Use case: opening the backstage while the ribbon is expanded hides and unmanages the group
     * strip, so the band collapses down to the tab-strip row instead of leaving an empty group strip
     * band behind the backstage. Closing it brings the group strip back.
     */
    @Test
    fun `opening the backstage hides the group strip while the ribbon is expanded`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            home.groups.add(FXMenuGroup().apply { title = "Clipboard" })
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.backstageContent = Label("Backstage")
        }
        pumpFx()
        assertFalse(onFx { menuPane.isCollapsed })
        assertTrue(onFx { groupStripScrollPane(menuPane).isManaged })

        onFx { menuPane.isFileTabActive = true }
        pumpFx()
        assertFalse(onFx { groupStripScrollPane(menuPane).isVisible })
        assertFalse(onFx { groupStripScrollPane(menuPane).isManaged })

        onFx { menuPane.isFileTabActive = false }
        pumpFx()
        assertFalse(onFx { menuPane.isCollapsed })
        assertTrue(onFx { groupStripScrollPane(menuPane).isVisible })
        assertTrue(onFx { groupStripScrollPane(menuPane).isManaged })
    }

    /**
     * Use case: setting [FXMenuPane.isFileTabActive] back to `false` fades the overlay slot out; once
     * the 0.3s fade has finished the slot is hidden again.
     */
    @Test
    fun `closing the backstage fades the overlay slot back out`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = Label("Backstage")
            menuPane.isFileTabActive = true
        }
        pumpFx()

        onFx { menuPane.isFileTabActive = false }
        waitForFade()

        assertFalse(onFx { backstageSlot(menuPane).isVisible })
    }

    /**
     * Use case: pressing Escape while the backstage is open closes it, mirroring the platform
     * convention for dismissible overlays.
     */
    @Test
    fun `escape closes the backstage`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = Label("Backstage")
            menuPane.isFileTabActive = true
        }
        pumpFx()

        onFx {
            backstageSlot(menuPane).fireEvent(
                KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false),
            )
        }
        pumpFx()

        assertFalse(onFx { menuPane.isFileTabActive })
    }

    /**
     * Use case: a mouse press outside the backstage content (here on the tab strip) closes the
     * backstage, matching Office's click-away behaviour.
     */
    @Test
    fun `clicking outside the backstage content closes it`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.backstageContent = Label("Backstage")
            menuPane.isFileTabActive = true
        }
        pumpFx()

        onFx { menuPane.lookup(".menu-pane-strip").fireEvent(mousePress()) }
        pumpFx()

        assertFalse(onFx { menuPane.isFileTabActive })
    }

    /**
     * Use case: selecting a regular strip tab while the backstage is open closes the backstage and
     * activates the chosen tab in one step.
     */
    @Test
    fun `selecting a strip tab closes the backstage and activates that tab`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val edit = FXMenuTab("edit", "Edit")
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.tabs.addAll(home, edit)
            menuPane.activate(home)
            menuPane.backstageContent = Label("Backstage")
            menuPane.isFileTabActive = true
        }
        pumpFx()

        onFx { stripButtons(menuPane)[1].fire() }
        pumpFx()

        assertFalse(onFx { menuPane.isFileTabActive })
        assertEquals(edit, onFx { menuPane.activeTab })
    }

    /**
     * Use case: opening and closing the backstage must leave [FXMenuPane.activeTab] pointing at the
     * strip tab that was active before, so the ribbon returns to exactly where the user left it.
     */
    @Test
    fun `opening and closing the backstage keeps the previously active tab`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.tabs.add(home)
            menuPane.activate(home)
            menuPane.backstageContent = Label("Backstage")
        }
        pumpFx()

        onFx { menuPane.isFileTabActive = true }
        pumpFx()
        assertEquals(home, onFx { menuPane.activeTab })

        onFx { menuPane.isFileTabActive = false }
        pumpFx()
        assertEquals(home, onFx { menuPane.activeTab })
    }

    /**
     * Use case: with a [BackstageOverlayHost] set, [FXMenuPane] hands the panel to the host on open
     * and asks it to hide on close, and never lights up its own local overlay slot.
     */
    @Test
    fun `an overlay host receives the panel instead of the local slot`() {
        val menuPane = showMenuPaneStage()
        val panel = Label("Backstage")
        val host = RecordingOverlayHost()
        onFx {
            menuPane.overlayHost = host
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = panel
            menuPane.isFileTabActive = true
        }
        pumpFx()

        assertSame(panel, host.shown)
        assertFalse(onFx { backstageSlot(menuPane).isVisible })

        onFx { menuPane.isFileTabActive = false }
        pumpFx()
        assertTrue(host.hidden)
    }

    /**
     * Use case: [FXMenuPane.onBackstageClosed] runs once the backstage has closed - and only then -
     * so a host can restore the ribbon's prior collapse state.
     */
    @Test
    fun `onBackstageClosed fires only after the backstage closes`() {
        val menuPane = showMenuPaneStage()
        var closedCount = 0
        onFx {
            menuPane.onBackstageClosed = { closedCount++ }
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = Label("Backstage")
            menuPane.isFileTabActive = true
        }
        pumpFx()
        assertEquals(0, closedCount)

        onFx { menuPane.isFileTabActive = false }
        pumpFx()
        assertEquals(1, closedCount)
    }

    /**
     * Use case: clearing [FXMenuPane.fileTab] while its backstage is open closes the backstage, so
     * no overlay is left without a tab to dismiss it from.
     */
    @Test
    fun `clearing the file tab closes an open backstage`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = Label("Backstage")
            menuPane.isFileTabActive = true
        }
        pumpFx()

        onFx { menuPane.fileTab = null }
        pumpFx()

        assertFalse(onFx { menuPane.isFileTabActive })
    }

    private fun waitForFade() {
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS)
        pumpFx()
    }

    private fun mousePress(): MouseEvent = MouseEvent(
        MouseEvent.MOUSE_PRESSED, 0.0, 0.0, 0.0, 0.0, MouseButton.PRIMARY, 1,
        false, false, false, false, true, false, false, false, false, false, null,
    )

    private fun fileTabButton(menuPane: FXMenuPane): ToggleButton =
        menuPane.lookupAll(".menu-pane-strip-file-button").filterIsInstance<ToggleButton>().first()

    private fun stripButtons(menuPane: FXMenuPane): List<ToggleButton> {
        val strip = menuPane.lookup(".menu-pane-strip") as HBox
        return strip.children.filterIsInstance<ToggleButton>()
    }

    private fun tabStripRow(menuPane: FXMenuPane): HBox =
        menuPane.lookup("#tabStripRow") as HBox

    private fun backstageSlot(menuPane: FXMenuPane): StackPane =
        menuPane.lookup("#backstageContentSlot") as StackPane

    private fun groupStripScrollPane(menuPane: FXMenuPane): ScrollPane =
        menuPane.lookup("#groupStripScrollPane") as ScrollPane

    /** Minimal [BackstageOverlayHost] that just records the last show/hide calls. */
    private class RecordingOverlayHost : BackstageOverlayHost {
        var shown: Node? = null
        var hidden: Boolean = false

        override fun showOverlay(node: Node) {
            shown = node
        }

        override fun hideOverlay() {
            hidden = true
        }
    }
}
