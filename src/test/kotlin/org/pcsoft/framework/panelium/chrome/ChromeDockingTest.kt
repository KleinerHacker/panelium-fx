package org.pcsoft.framework.panelium.chrome

import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest
import org.pcsoft.framework.panelium.menupane.FXMenuPane
import org.pcsoft.framework.panelium.menupane.FXMenuTab

/**
 * Headless coverage for IP-11 (ChromeDocking): `FXMenuPane` is docked below `ChromePane`'s caption
 * bar purely by composition - it becomes the `top` of a `BorderPane` used as the `ChromePane`
 * content - with no dedicated `ChromePane` API involved.
 */
class ChromeDockingTest : AbstractChromeUiTest() {

    private val home = FXMenuTab("home", "Home")
    private val view = FXMenuTab("view", "View")

    private fun dockedMenuPane(): FXMenuPane = FXMenuPane().apply {
        tabs.addAll(home, view)
        activeTab = home
    }

    /**
     * A `ChromePane` whose content is a `BorderPane(top = FXMenuPane)` shows without error and the
     * `FXMenuPane` ends up attached to the same scene as the frame, in the border pane's top slot.
     */
    @Test
    fun `menu tab docks into the border pane top slot`() {
        lateinit var menuPane: FXMenuPane
        lateinit var borderPane: BorderPane

        val chrome = showChromeStage(
            title = "Docking Test Window",
            content = {
                menuPane = dockedMenuPane()
                borderPane = BorderPane().apply {
                    top = menuPane
                    center = Label("Body")
                }
                borderPane
            },
        )

        onFx {
            assertSame(borderPane, chrome.pane.content, "border pane is the chrome content")
            assertSame(menuPane, borderPane.top, "menu tab sits in the top slot")
            assertNotNull(menuPane.scene, "menu tab is attached to a scene")
            assertSame(chrome.pane.scene, menuPane.scene, "menu tab shares the frame's scene")
        }
    }

    /**
     * The docked `FXMenuPane` is laid out above the `center` content: its bottom edge in scene
     * coordinates is at or above the top edge of the center node, and it has a non-zero height.
     */
    @Test
    fun `docked menu tab is laid out above the content area`() {
        lateinit var menuPane: FXMenuPane
        lateinit var body: Label

        showChromeStage(
            title = "Docking Layout Window",
            content = {
                menuPane = dockedMenuPane()
                body = Label("Body")
                BorderPane().apply {
                    top = menuPane
                    center = body
                }
            },
        )

        onFx {
            val ribbonBottom = menuPane.localToScene(menuPane.boundsInLocal).maxY
            val bodyTop = body.localToScene(body.boundsInLocal).minY
            assertTrue(menuPane.boundsInLocal.height > 0.0, "ribbon has a real height")
            assertTrue(
                ribbonBottom <= bodyTop + 0.5,
                "ribbon bottom ($ribbonBottom) is above body top ($bodyTop)",
            )
        }
    }

    /**
     * Tab switching keeps working inside the framed context: activating another registered tab
     * updates `activeTab`.
     */
    @Test
    fun `tab switching works while docked`() {
        lateinit var menuPane: FXMenuPane

        showChromeStage(
            title = "Docking Switch Window",
            content = {
                menuPane = dockedMenuPane()
                BorderPane().apply {
                    top = menuPane
                    center = Label("Body")
                }
            },
        )

        onFx {
            assertEquals(home, menuPane.activeTab, "home is active initially")
            menuPane.activate(view)
            assertEquals(view, menuPane.activeTab, "view is active after activation")
        }
    }
}
