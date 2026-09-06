package org.pcsoft.framework.panelium.menutab

import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menutab.support.AbstractMenuTabUiTest

/**
 * Covers [FXMenuGroup] and [MenuTab.groups]: the group strip renders the active regular tab's
 * groups in order, follows live edits to that list, swaps its content when the active tab changes,
 * surfaces each group's title and content nodes, and empties while the file-tab backstage is open.
 */
class FXMenuGroupTest : AbstractMenuTabUiTest() {

    /**
     * Use case: an application fills `MenuTab.groups` of the active tab; the group strip must show
     * one container per group, in registration order.
     */
    @Test
    fun `active tab groups render in the group strip in order`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")

        onFx {
            home.groups.addAll(menuGroup("Clipboard"), menuGroup("Font"), menuGroup("Paragraph"))
            menuTab.tabs.add(home)
            menuTab.activeTab = home
        }
        pumpFx()

        assertEquals(listOf("Clipboard", "Font", "Paragraph"), groupTitles(menuTab))
    }

    /**
     * Use case: groups are added, removed and reordered on the already-active tab; the group strip
     * must reflect every change without a tab switch.
     */
    @Test
    fun `group strip follows live edits of the active tab's group list`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val clipboard = menuGroup("Clipboard")
        val font = menuGroup("Font")

        onFx {
            menuTab.tabs.add(home)
            menuTab.activeTab = home
            home.groups.addAll(clipboard, font)
        }
        pumpFx()
        assertEquals(listOf("Clipboard", "Font"), groupTitles(menuTab))

        onFx { home.groups.remove(clipboard) }
        pumpFx()
        assertEquals(listOf("Font"), groupTitles(menuTab))

        onFx { home.groups.add(clipboard) }
        pumpFx()
        assertEquals(listOf("Font", "Clipboard"), groupTitles(menuTab))
    }

    /**
     * Use case: two regular tabs carry different groups; activating the other tab must replace the
     * group strip with its groups.
     */
    @Test
    fun `switching the active tab swaps the group strip`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val view = MenuTab("view", "View")

        onFx {
            home.groups.add(menuGroup("Clipboard"))
            view.groups.addAll(menuGroup("Views"), menuGroup("Show"))
            menuTab.tabs.addAll(home, view)
            menuTab.activeTab = home
        }
        pumpFx()
        assertEquals(listOf("Clipboard"), groupTitles(menuTab))

        onFx { menuTab.activeTab = view }
        pumpFx()
        assertEquals(listOf("Views", "Show"), groupTitles(menuTab))
    }

    /**
     * Use case: a group carries a title and control nodes; both must reach the rendered group -
     * the title as its `menu-group-title` label, the controls inside its `menu-group-content` host.
     */
    @Test
    fun `group exposes its title and content nodes`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")
        val paste = Button("Paste")
        val copy = Button("Copy")
        val clipboard = FXMenuGroup().apply {
            title = "Clipboard"
            content.addAll(paste, copy)
        }

        onFx {
            home.groups.add(clipboard)
            menuTab.tabs.add(home)
            menuTab.activeTab = home
        }
        pumpFx()

        val titleLabel = clipboard.lookupAll(".menu-group-title").filterIsInstance<Label>().first()
        assertEquals("Clipboard", titleLabel.text)

        val contentHost = clipboard.lookup(".menu-group-content") as HBox
        assertEquals(listOf<Any>(paste, copy), onFx { contentHost.children.toList() })
    }

    /**
     * Use case: while the file tab's backstage is open the ribbon shows no group strip; closing it
     * again must restore the active tab's groups.
     */
    @Test
    fun `open backstage clears the group strip and closing restores it`() {
        val menuTab = showMenuTabStage()
        val home = MenuTab("home", "Home")

        onFx {
            home.groups.addAll(menuGroup("Clipboard"), menuGroup("Font"))
            menuTab.tabs.add(home)
            menuTab.activeTab = home
            menuTab.fileTab = MenuTab("file", "File")
        }
        pumpFx()
        assertEquals(listOf("Clipboard", "Font"), groupTitles(menuTab))

        onFx { menuTab.isFileTabActive = true }
        pumpFx()
        assertTrue(groupTitles(menuTab).isEmpty())

        onFx { menuTab.isFileTabActive = false }
        pumpFx()
        assertEquals(listOf("Clipboard", "Font"), groupTitles(menuTab))
    }

    private fun menuGroup(title: String): FXMenuGroup = FXMenuGroup().apply { this.title = title }

    private fun groupStrip(menuTab: FXMenuTab): HBox = menuTab.lookup(".menu-tab-group-strip") as HBox

    private fun groupTitles(menuTab: FXMenuTab): List<String> = onFx {
        groupStrip(menuTab).children.filterIsInstance<FXMenuGroup>().map { it.title }
    }
}
