package org.pcsoft.framework.panelium.menutab

import javafx.scene.Node
import javafx.scene.control.Label
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
import org.pcsoft.framework.panelium.menutab.support.AbstractMenuTabUiTest
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.TimeUnit

/**
 * Covers the file-tab backstage on [FXMenuTab]: opening it from the file-tab button, the fade into
 * the local overlay slot, dismissal via Escape, an outside click and strip-tab selection, keeping
 * the previously active tab untouched, delegating to a [BackstageOverlayHost] when one is set, and
 * the [FXMenuTab.onBackstageClosed] callback.
 */
class FXMenuTabBackstageTest : AbstractMenuTabUiTest() {

    /**
     * Use case: clicking the file-tab button opens the backstage - `isFileTabActive` becomes `true`
     * and the overlay slot turns visible so the panel can be seen.
     */
    @Test
    fun `clicking the file tab button opens the backstage`() {
        val menuTab = showMenuTabStage()
        onFx {
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = Label("Backstage")
        }
        pumpFx()

        onFx { fileTabButton(menuTab).fire() }
        pumpFx()

        assertTrue(onFx { menuTab.isFileTabActive })
        assertTrue(onFx { backstageSlot(menuTab).isVisible })
    }

    /**
     * Use case: the backstage layer must stay unmanaged so opening it never grows the ribbon band;
     * its top edge sits at or below the bottom of the tab-strip row, leaving the pressed file-tab
     * button uncovered.
     */
    @Test
    fun `opening the backstage neither resizes the band nor covers the tab strip`() {
        val menuTab = showMenuTabStage()
        onFx {
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.tabs.add(MenuTab("home", "Home"))
            menuTab.backstageContent = VBox(Label("Backstage")).apply { minHeight = 400.0 }
        }
        pumpFx()
        val bandHeightBefore = onFx { menuTab.prefHeight(400.0) }

        onFx { menuTab.isFileTabActive = true }
        pumpFx()

        val slot = onFx { backstageSlot(menuTab) }
        assertFalse(onFx { slot.isManaged })
        assertEquals(bandHeightBefore, onFx { menuTab.prefHeight(400.0) })
        assertTrue(onFx { slot.boundsInParent.minY + 0.5 >= tabStripRow(menuTab).boundsInParent.maxY })
    }

    /**
     * Use case: setting [FXMenuTab.isFileTabActive] back to `false` fades the overlay slot out; once
     * the 0.3s fade has finished the slot is hidden again.
     */
    @Test
    fun `closing the backstage fades the overlay slot back out`() {
        val menuTab = showMenuTabStage()
        onFx {
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = Label("Backstage")
            menuTab.isFileTabActive = true
        }
        pumpFx()

        onFx { menuTab.isFileTabActive = false }
        waitForFade()

        assertFalse(onFx { backstageSlot(menuTab).isVisible })
    }

    /**
     * Use case: pressing Escape while the backstage is open closes it, mirroring the platform
     * convention for dismissible overlays.
     */
    @Test
    fun `escape closes the backstage`() {
        val menuTab = showMenuTabStage()
        onFx {
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = Label("Backstage")
            menuTab.isFileTabActive = true
        }
        pumpFx()

        onFx {
            backstageSlot(menuTab).fireEvent(
                KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false),
            )
        }
        pumpFx()

        assertFalse(onFx { menuTab.isFileTabActive })
    }

    /**
     * Use case: a mouse press outside the backstage content (here on the tab strip) closes the
     * backstage, matching Office's click-away behaviour.
     */
    @Test
    fun `clicking outside the backstage content closes it`() {
        val menuTab = showMenuTabStage()
        onFx {
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.tabs.add(MenuTab("home", "Home"))
            menuTab.backstageContent = Label("Backstage")
            menuTab.isFileTabActive = true
        }
        pumpFx()

        onFx { menuTab.lookup(".menu-tab-strip").fireEvent(mousePress()) }
        pumpFx()

        assertFalse(onFx { menuTab.isFileTabActive })
    }

    /**
     * Use case: selecting a regular strip tab while the backstage is open closes the backstage and
     * activates the chosen tab in one step.
     */
    @Test
    fun `selecting a strip tab closes the backstage and activates that tab`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val edit = MenuTab("edit", "Edit")
        onFx {
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.tabs.addAll(home, edit)
            menuTab.activate(home)
            menuTab.backstageContent = Label("Backstage")
            menuTab.isFileTabActive = true
        }
        pumpFx()

        onFx { stripButtons(menuTab)[1].fire() }
        pumpFx()

        assertFalse(onFx { menuTab.isFileTabActive })
        assertEquals(edit, onFx { menuTab.activeTab })
    }

    /**
     * Use case: opening and closing the backstage must leave [FXMenuTab.activeTab] pointing at the
     * strip tab that was active before, so the ribbon returns to exactly where the user left it.
     */
    @Test
    fun `opening and closing the backstage keeps the previously active tab`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        onFx {
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.tabs.add(home)
            menuTab.activate(home)
            menuTab.backstageContent = Label("Backstage")
        }
        pumpFx()

        onFx { menuTab.isFileTabActive = true }
        pumpFx()
        assertEquals(home, onFx { menuTab.activeTab })

        onFx { menuTab.isFileTabActive = false }
        pumpFx()
        assertEquals(home, onFx { menuTab.activeTab })
    }

    /**
     * Use case: with a [BackstageOverlayHost] set, [FXMenuTab] hands the panel to the host on open
     * and asks it to hide on close, and never lights up its own local overlay slot.
     */
    @Test
    fun `an overlay host receives the panel instead of the local slot`() {
        val menuTab = showMenuTabStage()
        val panel = Label("Backstage")
        val host = RecordingOverlayHost()
        onFx {
            menuTab.overlayHost = host
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = panel
            menuTab.isFileTabActive = true
        }
        pumpFx()

        assertSame(panel, host.shown)
        assertFalse(onFx { backstageSlot(menuTab).isVisible })

        onFx { menuTab.isFileTabActive = false }
        pumpFx()
        assertTrue(host.hidden)
    }

    /**
     * Use case: [FXMenuTab.onBackstageClosed] runs once the backstage has closed - and only then -
     * so a host can restore the ribbon's prior collapse state.
     */
    @Test
    fun `onBackstageClosed fires only after the backstage closes`() {
        val menuTab = showMenuTabStage()
        var closedCount = 0
        onFx {
            menuTab.onBackstageClosed = { closedCount++ }
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = Label("Backstage")
            menuTab.isFileTabActive = true
        }
        pumpFx()
        assertEquals(0, closedCount)

        onFx { menuTab.isFileTabActive = false }
        pumpFx()
        assertEquals(1, closedCount)
    }

    /**
     * Use case: clearing [FXMenuTab.fileTab] while its backstage is open closes the backstage, so
     * no overlay is left without a tab to dismiss it from.
     */
    @Test
    fun `clearing the file tab closes an open backstage`() {
        val menuTab = showMenuTabStage()
        onFx {
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = Label("Backstage")
            menuTab.isFileTabActive = true
        }
        pumpFx()

        onFx { menuTab.fileTab = null }
        pumpFx()

        assertFalse(onFx { menuTab.isFileTabActive })
    }

    private fun waitForFade() {
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS)
        pumpFx()
    }

    private fun mousePress(): MouseEvent = MouseEvent(
        MouseEvent.MOUSE_PRESSED, 0.0, 0.0, 0.0, 0.0, MouseButton.PRIMARY, 1,
        false, false, false, false, true, false, false, false, false, false, null,
    )

    private fun fileTabButton(menuTab: FXMenuTab): ToggleButton =
        menuTab.lookupAll(".menu-tab-strip-file-button").filterIsInstance<ToggleButton>().first()

    private fun stripButtons(menuTab: FXMenuTab): List<ToggleButton> {
        val strip = menuTab.lookup(".menu-tab-strip") as HBox
        return strip.children.filterIsInstance<ToggleButton>()
    }

    private fun tabStripRow(menuTab: FXMenuTab): HBox =
        menuTab.lookup("#tabStripRow") as HBox

    private fun backstageSlot(menuTab: FXMenuTab): StackPane =
        menuTab.lookup("#backstageContentSlot") as StackPane

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
