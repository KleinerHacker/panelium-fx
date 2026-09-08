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

import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers [FXMenuGroup] and [FXMenuTab.groups]: the group strip renders the active regular tab's
 * groups in order, follows live edits to that list, swaps its content when the active tab changes,
 * surfaces each group's title and content nodes, and empties while the file-tab backstage is open.
 */
class FXMenuGroupTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: an application fills `FXMenuTab.groups` of the active tab; the group strip must show
     * one container per group, in registration order.
     */
    @Test
    fun `active tab groups render in the group strip in order`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")

        onFx {
            home.groups.addAll(menuGroup("Clipboard"), menuGroup("Font"), menuGroup("Paragraph"))
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        assertEquals(listOf("Clipboard", "Font", "Paragraph"), groupTitles(menuPane))
    }

    /**
     * Use case: groups are added, removed and reordered on the already-active tab; the group strip
     * must reflect every change without a tab switch.
     */
    @Test
    fun `group strip follows live edits of the active tab's group list`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val clipboard = menuGroup("Clipboard")
        val font = menuGroup("Font")

        onFx {
            menuPane.tabs.add(home)
            menuPane.activeTab = home
            home.groups.addAll(clipboard, font)
        }
        pumpFx()
        assertEquals(listOf("Clipboard", "Font"), groupTitles(menuPane))

        onFx { home.groups.remove(clipboard) }
        pumpFx()
        assertEquals(listOf("Font"), groupTitles(menuPane))

        onFx { home.groups.add(clipboard) }
        pumpFx()
        assertEquals(listOf("Font", "Clipboard"), groupTitles(menuPane))
    }

    /**
     * Use case: two regular tabs carry different groups; activating the other tab must replace the
     * group strip with its groups.
     */
    @Test
    fun `switching the active tab swaps the group strip`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val view = FXMenuTab("view", "View")

        onFx {
            home.groups.add(menuGroup("Clipboard"))
            view.groups.addAll(menuGroup("Views"), menuGroup("Show"))
            menuPane.tabs.addAll(home, view)
            menuPane.activeTab = home
        }
        pumpFx()
        assertEquals(listOf("Clipboard"), groupTitles(menuPane))

        onFx { menuPane.activeTab = view }
        pumpFx()
        assertEquals(listOf("Views", "Show"), groupTitles(menuPane))
    }

    /**
     * Use case: a group carries a title and layout boxes; both must reach the rendered group - the
     * title as its `menu-group-title` label, the boxes inside its `menu-group-content` host with
     * their controls nested in them.
     */
    @Test
    fun `group exposes its title and content boxes`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val paste = Button("Paste")
        val copy = Button("Copy")
        val pasteBox = FXMenuGroupLargeBox(paste)
        val copyBox = FXMenuGroupLargeBox(copy)
        val clipboard = FXMenuGroup(pasteBox, copyBox, anchor = pasteBox).apply {
            title = "Clipboard"
        }

        onFx {
            home.groups.add(clipboard)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        val titleLabel = clipboard.lookupAll(".menu-group-title").filterIsInstance<Label>().first()
        assertEquals("Clipboard", titleLabel.text)

        val contentHost = clipboard.lookup(".menu-group-content") as HBox
        assertEquals(listOf<Any>(pasteBox, copyBox), onFx { contentHost.children.toList() })
        assertEquals(listOf<Any>(paste), onFx { pasteBox.children.toList() })
        assertEquals(listOf<Any>(copy), onFx { copyBox.children.toList() })
    }

    /**
     * Use case: disabling a whole group through the inherited `disable` state must disable the group
     * node itself and, by JavaFX disable propagation, every control nested in its `content` boxes.
     */
    @Test
    fun `disabling a group disables it and its content nodes`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")
        val paste = Button("Paste")
        val pasteBox = FXMenuGroupLargeBox(paste)
        val clipboard = FXMenuGroup(pasteBox, anchor = pasteBox).apply {
            title = "Clipboard"
        }

        onFx {
            home.groups.add(clipboard)
            menuPane.tabs.add(home)
            menuPane.activeTab = home
        }
        pumpFx()

        onFx { clipboard.isDisable = true }
        pumpFx()

        assertTrue(onFx { clipboard.isDisable })
        assertTrue(onFx { paste.isDisabled })
    }

    /**
     * Use case: while the file tab's backstage is open the ribbon shows no group strip; closing it
     * again must restore the active tab's groups.
     */
    @Test
    fun `open backstage clears the group strip and closing restores it`() {
        val menuPane = showMenuPaneStage()
        val home = FXMenuTab("home", "Home")

        onFx {
            home.groups.addAll(menuGroup("Clipboard"), menuGroup("Font"))
            menuPane.tabs.add(home)
            menuPane.activeTab = home
            menuPane.fileTab = FXMenuTab("file", "File")
        }
        pumpFx()
        assertEquals(listOf("Clipboard", "Font"), groupTitles(menuPane))

        onFx { menuPane.isFileTabActive = true }
        pumpFx()
        assertTrue(groupTitles(menuPane).isEmpty())

        onFx { menuPane.isFileTabActive = false }
        pumpFx()
        assertEquals(listOf("Clipboard", "Font"), groupTitles(menuPane))
    }

    private fun menuGroup(title: String): FXMenuGroup = FXMenuGroup().apply { this.title = title }

    private fun groupStrip(menuPane: FXMenuPane): HBox = menuPane.lookup(".menu-pane-group-strip") as HBox

    private fun groupTitles(menuPane: FXMenuPane): List<String> = onFx {
        groupStrip(menuPane).children.filterIsInstance<FXMenuGroup>().map { it.title }
    }
}
