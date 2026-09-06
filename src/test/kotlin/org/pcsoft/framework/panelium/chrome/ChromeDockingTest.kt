package org.pcsoft.framework.panelium.chrome

import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest
import org.pcsoft.framework.panelium.menutab.FXMenuTab
import org.pcsoft.framework.panelium.menutab.MenuTab

/**
 * Headless coverage for IP-11 (ChromeDocking): `FXMenuTab` is docked below `ChromePane`'s caption
 * bar purely by composition - it becomes the `top` of a `BorderPane` used as the `ChromePane`
 * content - with no dedicated `ChromePane` API involved.
 */
class ChromeDockingTest : AbstractChromeUiTest() {

    private val home = MenuTab("home", "Home")
    private val view = MenuTab("view", "View")

    private fun dockedMenuTab(): FXMenuTab = FXMenuTab().apply {
        tabs.addAll(home, view)
        activeTab = home
    }

    /**
     * A `ChromePane` whose content is a `BorderPane(top = FXMenuTab)` shows without error and the
     * `FXMenuTab` ends up attached to the same scene as the frame, in the border pane's top slot.
     */
    @Test
    fun `menu tab docks into the border pane top slot`() {
        lateinit var menuTab: FXMenuTab
        lateinit var borderPane: BorderPane

        val chrome = showChromeStage(
            title = "Docking Test Window",
            content = {
                menuTab = dockedMenuTab()
                borderPane = BorderPane().apply {
                    top = menuTab
                    center = Label("Body")
                }
                borderPane
            },
        )

        onFx {
            assertSame(borderPane, chrome.pane.content, "border pane is the chrome content")
            assertSame(menuTab, borderPane.top, "menu tab sits in the top slot")
            assertNotNull(menuTab.scene, "menu tab is attached to a scene")
            assertSame(chrome.pane.scene, menuTab.scene, "menu tab shares the frame's scene")
        }
    }

    /**
     * The docked `FXMenuTab` is laid out above the `center` content: its bottom edge in scene
     * coordinates is at or above the top edge of the center node, and it has a non-zero height.
     */
    @Test
    fun `docked menu tab is laid out above the content area`() {
        lateinit var menuTab: FXMenuTab
        lateinit var body: Label

        showChromeStage(
            title = "Docking Layout Window",
            content = {
                menuTab = dockedMenuTab()
                body = Label("Body")
                BorderPane().apply {
                    top = menuTab
                    center = body
                }
            },
        )

        onFx {
            val ribbonBottom = menuTab.localToScene(menuTab.boundsInLocal).maxY
            val bodyTop = body.localToScene(body.boundsInLocal).minY
            assertTrue(menuTab.boundsInLocal.height > 0.0, "ribbon has a real height")
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
        lateinit var menuTab: FXMenuTab

        showChromeStage(
            title = "Docking Switch Window",
            content = {
                menuTab = dockedMenuTab()
                BorderPane().apply {
                    top = menuTab
                    center = Label("Body")
                }
            },
        )

        onFx {
            assertEquals(home, menuTab.activeTab, "home is active initially")
            menuTab.activate(view)
            assertEquals(view, menuTab.activeTab, "view is active after activation")
        }
    }
}
