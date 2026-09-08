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

import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest
import org.pcsoft.framework.panelium.menupane.FXMenuPane
import org.pcsoft.framework.panelium.menupane.FXMenuTab
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.TimeUnit

/**
 * Headless coverage for IP-12 (ChromeOverlayHook): [MenuChromePane] docks an [FXMenuPane] below the
 * caption bar, wires an internal overlay host into it, and paints the file-tab backstage as a
 * full-width overlay above the window body.
 */
class MenuChromePaneTest : AbstractChromeUiTest() {

    private fun menuChrome(): MenuChromePane {
        val stage = showChromeStage(title = "Menu Chrome Window", factory = { MenuChromePane() })
        return stage.pane as MenuChromePane
    }

    /**
     * Assigning a [FXMenuPane] to [MenuChromePane.menuPane] puts it into the internal border pane's
     * top slot and wires an internal overlay host into it; clearing it releases the hook again.
     */
    @Test
    fun `assigning the menu tab docks it and wires the overlay host`() {
        val pane = menuChrome()
        val menuPane = FXMenuPane()

        onFx { pane.menuPane = menuPane }
        pumpFx()

        onFx {
            assertSame(menuPane, (menuPane.parent as BorderPane).top, "menu tab is the top node")
            assertNotNull(menuPane.overlayHost, "overlay host is wired")
        }

        onFx { pane.menuPane = null }
        pumpFx()
        onFx { assertNull(menuPane.overlayHost, "overlay host released when the menu tab is cleared") }
    }

    /**
     * [MenuChromePane.body] is laid out below the docked [FXMenuPane]: its top edge in scene
     * coordinates is at or below the ribbon's bottom edge.
     */
    @Test
    fun `body is laid out below the docked menu tab`() {
        val pane = menuChrome()
        val menuPane = FXMenuPane().apply { tabs.add(FXMenuTab("home", "Home")) }
        val body = Label("Body")

        onFx {
            pane.menuPane = menuPane
            pane.body = body
        }
        pumpFx()

        onFx {
            val ribbonBottom = menuPane.localToScene(menuPane.boundsInLocal).maxY
            val bodyTop = body.localToScene(body.boundsInLocal).minY
            assertTrue(menuPane.boundsInLocal.height > 0.0, "ribbon has a real height")
            assertTrue(bodyTop + 0.5 >= ribbonBottom, "body top ($bodyTop) is below ribbon ($ribbonBottom)")
        }
    }

    /**
     * Opening the file-tab backstage hands the panel to the pane's overlay layer: the
     * `.chrome-backstage-overlay` layer becomes visible with the panel as its child while the menu
     * tab's own `#backstageContentSlot` stays hidden. Closing it fades the layer back out.
     */
    @Test
    fun `opening the backstage shows it in the pane overlay layer`() {
        val pane = menuChrome()
        val menuPane = FXMenuPane()
        val panel = Label("Backstage")

        onFx {
            pane.menuPane = menuPane
            pane.body = Label("Body")
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = panel
            menuPane.isFileTabActive = true
        }
        pumpFx()

        val overlay = onFx { pane.lookup(".chrome-backstage-overlay") as StackPane }
        onFx {
            assertTrue(overlay.isVisible, "overlay layer is visible")
            assertSame(overlay, panel.parent, "panel is hosted by the overlay layer")
            assertFalse(
                (menuPane.lookup("#backstageContentSlot") as StackPane).isVisible,
                "local backstage slot stays hidden",
            )
        }

        onFx { menuPane.isFileTabActive = false }
        waitForFade()
        onFx { assertFalse(overlay.isVisible, "overlay layer is hidden again after closing") }
    }

    /**
     * The backstage overlay covers only the body: the docked [FXMenuPane] (with its File button)
     * stays above the overlay, so re-clicking the File button still closes the backstage.
     */
    @Test
    fun `the docked menu tab stays outside the backstage overlay`() {
        val pane = menuChrome()
        val menuPane = FXMenuPane()

        onFx {
            pane.menuPane = menuPane
            pane.body = Label("Body")
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = Label("Backstage")
            menuPane.isFileTabActive = true
        }
        pumpFx()

        val overlay = onFx { pane.lookup(".chrome-backstage-overlay") as StackPane }
        onFx {
            val ribbonBottom = menuPane.localToScene(menuPane.boundsInLocal).maxY
            val overlayTop = overlay.localToScene(overlay.boundsInLocal).minY
            assertTrue(overlayTop + 0.5 >= ribbonBottom, "overlay ($overlayTop) starts below the ribbon ($ribbonBottom)")
        }

        val fileButton = onFx {
            menuPane.lookupAll(".menu-pane-strip-file-button").filterIsInstance<ToggleButton>().first()
        }
        onFx { fileButton.fire() }
        pumpFx()
        onFx { assertFalse(menuPane.isFileTabActive, "re-clicking the File button closes the backstage") }
    }

    /**
     * A mouse press inside the hosted backstage panel must not close the backstage, while a press
     * elsewhere (here the caption bar) does.
     */
    @Test
    fun `clicking inside the hosted panel keeps the backstage open`() {
        val pane = menuChrome()
        val menuPane = FXMenuPane()
        val panel = Label("Backstage")

        onFx {
            pane.menuPane = menuPane
            pane.body = Label("Body")
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = panel
            menuPane.isFileTabActive = true
        }
        pumpFx()

        onFx { panel.fireEvent(mousePress()) }
        pumpFx()
        onFx { assertTrue(menuPane.isFileTabActive, "click inside the panel keeps it open") }

        onFx { pane.captionBar.fireEvent(mousePress()) }
        pumpFx()
        onFx { assertFalse(menuPane.isFileTabActive, "click on the caption bar closes it") }
    }

    /**
     * Pressing Escape while the hosted backstage is open closes it.
     */
    @Test
    fun `escape closes the hosted backstage`() {
        val pane = menuChrome()
        val menuPane = FXMenuPane()

        onFx {
            pane.menuPane = menuPane
            pane.body = Label("Body")
            menuPane.fileTab = FXMenuTab("file", "File")
            menuPane.backstageContent = Label("Backstage")
            menuPane.isFileTabActive = true
        }
        pumpFx()

        onFx {
            pane.captionBar.fireEvent(
                KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false),
            )
        }
        pumpFx()
        onFx { assertFalse(menuPane.isFileTabActive, "Escape closes the backstage") }
    }

    /**
     * `MenuChromePane` loads as an FXML root element with `menuPane` and `body` as property
     * elements: the menu tab is docked and its overlay host is wired, and the body is placed.
     */
    @Test
    fun `loads as an FXML root with menuPane and body`() {
        val url = javaClass.getResource("/org/pcsoft/framework/panelium/chrome/MenuChromePaneRoot.fxml")
        assertNotNull(url, "the test FXML resource must be on the classpath")

        val pane = onFx { FXMLLoader.load<MenuChromePane>(url) }

        onFx {
            val menuPane = pane.menuPane
            assertNotNull(menuPane, "menu tab loaded from FXML")
            assertNotNull(menuPane!!.overlayHost, "overlay host wired from FXML")
            assertSame(menuPane, (menuPane.parent as BorderPane).top, "menu tab docked in the top slot")
            assertTrue(pane.body is Label, "body loaded from FXML")
        }
    }

    /**
     * Use case: opening the file-tab backstage while the ribbon is expanded collapses the ribbon
     * band down to the tab-strip row (the group strip is hidden), and it does so without moving or
     * resizing the caption bar - so no window chrome is shifted or clipped.
     */
    @Test
    fun `opening the backstage collapses the expanded ribbon band without disturbing the chrome`() {
        val stage = showChromeStage(width = 760.0, height = 480.0, factory = { MenuChromePane() })
        trackStage(stage.stage)
        val pane = stage.pane as MenuChromePane
        val menuPane = FXMenuPane().apply {
            val home = FXMenuTab("home", "Home")
            home.groups.add(org.pcsoft.framework.panelium.menupane.FXMenuGroup().apply { title = "Clipboard" })
            tabs.add(home)
            activeTab = home
            fileTab = FXMenuTab("file", "File")
            backstageContent = Label("Backstage")
        }
        onFx {
            pane.menuPane = menuPane
            pane.body = Label("Body")
        }
        pumpFx()

        val bandBefore = onFx { menuPane.boundsInLocal.height }
        val captionYBefore = onFx { pane.captionBar.localToScene(pane.captionBar.boundsInLocal).minY }
        val captionHBefore = onFx { pane.captionBar.height }

        onFx { menuPane.isFileTabActive = true }
        pumpFx()

        val bandAfter = onFx { menuPane.boundsInLocal.height }
        onFx {
            assertTrue(bandAfter < bandBefore - 40.0, "ribbon band collapsed ($bandBefore -> $bandAfter)")
            assertFalse(
                (menuPane.lookup("#groupStripScrollPane") as javafx.scene.control.ScrollPane).isManaged,
                "group strip is unmanaged while the backstage is open",
            )
            val captionYAfter = pane.captionBar.localToScene(pane.captionBar.boundsInLocal).minY
            assertTrue(kotlin.math.abs(captionYAfter - captionYBefore) < 0.5, "caption bar did not move")
            assertTrue(kotlin.math.abs(pane.captionBar.height - captionHBefore) < 0.5, "caption bar height unchanged")
        }

        onFx { menuPane.isFileTabActive = false }
        pumpFx()
        onFx {
            assertTrue(
                menuPane.boundsInLocal.height > bandAfter + 40.0,
                "ribbon band expands again after closing the backstage",
            )
        }
    }

    private fun waitForFade() {
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS)
        pumpFx()
    }

    private fun mousePress(): MouseEvent = MouseEvent(
        MouseEvent.MOUSE_PRESSED, 0.0, 0.0, 0.0, 0.0, MouseButton.PRIMARY, 1,
        false, false, false, false, true, false, false, false, false, false, null,
    )
}
