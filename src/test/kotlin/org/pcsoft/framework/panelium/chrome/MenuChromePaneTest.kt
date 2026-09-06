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
import org.pcsoft.framework.panelium.menutab.FXMenuTab
import org.pcsoft.framework.panelium.menutab.MenuTab
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.TimeUnit

/**
 * Headless coverage for IP-12 (ChromeOverlayHook): [MenuChromePane] docks an [FXMenuTab] below the
 * caption bar, wires its `overlayHost`, and paints the file-tab backstage as a full-width overlay
 * above the window body.
 */
class MenuChromePaneTest : AbstractChromeUiTest() {

    private fun menuChrome(): MenuChromePane {
        val stage = showChromeStage(title = "Menu Chrome Window", factory = { MenuChromePane() })
        return stage.pane as MenuChromePane
    }

    /**
     * Assigning a [FXMenuTab] to [MenuChromePane.menuTab] puts it into the internal border pane's
     * top slot and sets its `overlayHost` to the pane; clearing it releases the hook again.
     */
    @Test
    fun `assigning the menu tab docks it and wires the overlay host`() {
        val pane = menuChrome()
        val menuTab = FXMenuTab()

        onFx { pane.menuTab = menuTab }
        pumpFx()

        onFx {
            assertSame(menuTab, (menuTab.parent as BorderPane).top, "menu tab is the top node")
            assertSame(pane, menuTab.overlayHost, "overlay host points at the pane")
        }

        onFx { pane.menuTab = null }
        pumpFx()
        onFx { assertNull(menuTab.overlayHost, "overlay host released when the menu tab is cleared") }
    }

    /**
     * [MenuChromePane.body] is laid out below the docked [FXMenuTab]: its top edge in scene
     * coordinates is at or below the ribbon's bottom edge.
     */
    @Test
    fun `body is laid out below the docked menu tab`() {
        val pane = menuChrome()
        val menuTab = FXMenuTab().apply { tabs.add(MenuTab("home", "Home")) }
        val body = Label("Body")

        onFx {
            pane.menuTab = menuTab
            pane.body = body
        }
        pumpFx()

        onFx {
            val ribbonBottom = menuTab.localToScene(menuTab.boundsInLocal).maxY
            val bodyTop = body.localToScene(body.boundsInLocal).minY
            assertTrue(menuTab.boundsInLocal.height > 0.0, "ribbon has a real height")
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
        val menuTab = FXMenuTab()
        val panel = Label("Backstage")

        onFx {
            pane.menuTab = menuTab
            pane.body = Label("Body")
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = panel
            menuTab.isFileTabActive = true
        }
        pumpFx()

        val overlay = onFx { pane.lookup(".chrome-backstage-overlay") as StackPane }
        onFx {
            assertTrue(overlay.isVisible, "overlay layer is visible")
            assertSame(overlay, panel.parent, "panel is hosted by the overlay layer")
            assertFalse(
                (menuTab.lookup("#backstageContentSlot") as StackPane).isVisible,
                "local backstage slot stays hidden",
            )
        }

        onFx { menuTab.isFileTabActive = false }
        waitForFade()
        onFx { assertFalse(overlay.isVisible, "overlay layer is hidden again after closing") }
    }

    /**
     * The backstage overlay covers only the body: the docked [FXMenuTab] (with its File button)
     * stays above the overlay, so re-clicking the File button still closes the backstage.
     */
    @Test
    fun `the docked menu tab stays outside the backstage overlay`() {
        val pane = menuChrome()
        val menuTab = FXMenuTab()

        onFx {
            pane.menuTab = menuTab
            pane.body = Label("Body")
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = Label("Backstage")
            menuTab.isFileTabActive = true
        }
        pumpFx()

        val overlay = onFx { pane.lookup(".chrome-backstage-overlay") as StackPane }
        onFx {
            val ribbonBottom = menuTab.localToScene(menuTab.boundsInLocal).maxY
            val overlayTop = overlay.localToScene(overlay.boundsInLocal).minY
            assertTrue(overlayTop + 0.5 >= ribbonBottom, "overlay ($overlayTop) starts below the ribbon ($ribbonBottom)")
        }

        val fileButton = onFx {
            menuTab.lookupAll(".menu-tab-strip-file-button").filterIsInstance<ToggleButton>().first()
        }
        onFx { fileButton.fire() }
        pumpFx()
        onFx { assertFalse(menuTab.isFileTabActive, "re-clicking the File button closes the backstage") }
    }

    /**
     * A mouse press inside the hosted backstage panel must not close the backstage, while a press
     * elsewhere (here the caption bar) does.
     */
    @Test
    fun `clicking inside the hosted panel keeps the backstage open`() {
        val pane = menuChrome()
        val menuTab = FXMenuTab()
        val panel = Label("Backstage")

        onFx {
            pane.menuTab = menuTab
            pane.body = Label("Body")
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = panel
            menuTab.isFileTabActive = true
        }
        pumpFx()

        onFx { panel.fireEvent(mousePress()) }
        pumpFx()
        onFx { assertTrue(menuTab.isFileTabActive, "click inside the panel keeps it open") }

        onFx { pane.captionBar.fireEvent(mousePress()) }
        pumpFx()
        onFx { assertFalse(menuTab.isFileTabActive, "click on the caption bar closes it") }
    }

    /**
     * Pressing Escape while the hosted backstage is open closes it.
     */
    @Test
    fun `escape closes the hosted backstage`() {
        val pane = menuChrome()
        val menuTab = FXMenuTab()

        onFx {
            pane.menuTab = menuTab
            pane.body = Label("Body")
            menuTab.fileTab = MenuTab("file", "File")
            menuTab.backstageContent = Label("Backstage")
            menuTab.isFileTabActive = true
        }
        pumpFx()

        onFx {
            pane.captionBar.fireEvent(
                KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false),
            )
        }
        pumpFx()
        onFx { assertFalse(menuTab.isFileTabActive, "Escape closes the backstage") }
    }

    /**
     * `MenuChromePane` loads as an FXML root element with `menuTab` and `body` as property
     * elements: the menu tab is docked and its `overlayHost` is wired, and the body is placed.
     */
    @Test
    fun `loads as an FXML root with menuTab and body`() {
        val url = javaClass.getResource("/org/pcsoft/framework/panelium/chrome/MenuChromePaneRoot.fxml")
        assertNotNull(url, "the test FXML resource must be on the classpath")

        val pane = onFx { FXMLLoader.load<MenuChromePane>(url) }

        onFx {
            val menuTab = pane.menuTab
            assertNotNull(menuTab, "menu tab loaded from FXML")
            assertSame(pane, menuTab!!.overlayHost, "overlay host wired from FXML")
            assertSame(menuTab, (menuTab.parent as BorderPane).top, "menu tab docked in the top slot")
            assertTrue(pane.body is Label, "body loaded from FXML")
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
