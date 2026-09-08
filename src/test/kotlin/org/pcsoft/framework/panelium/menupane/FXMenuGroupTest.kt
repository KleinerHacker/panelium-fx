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

    /**
     * Use case: an application binds against the group's property accessors; `titleProperty`,
     * `anchorProperty`, `onLauncherActionProperty` and `overflowActiveProperty` must each expose the
     * live backing property, so a value set through the plain accessor is visible through the
     * property and vice versa.
     */
    @Test
    fun `group property accessors expose the live backing properties`() {
        val box = FXMenuGroupLargeBox(Button("Paste"))
        val group = FXMenuGroup(box, anchor = box)

        group.titleProperty().set("Clipboard")
        assertEquals("Clipboard", group.title)

        org.junit.jupiter.api.Assertions.assertSame(box, group.anchorProperty().get())

        val handler = javafx.event.EventHandler<javafx.event.ActionEvent> { }
        group.onLauncherAction = handler
        org.junit.jupiter.api.Assertions.assertSame(handler, group.onLauncherActionProperty().get())

        org.junit.jupiter.api.Assertions.assertFalse(group.overflowActiveProperty().get())
        assertEquals(group.isOverflowActive, group.overflowActiveProperty().get())
    }

    /**
     * Use case: the anchor must always be one of the content boxes; assigning an anchor that is not
     * in `content` through the setter must fail fast with an [IllegalArgumentException].
     */
    @Test
    fun `setting an anchor that is not in the content is rejected`() {
        val box = FXMenuGroupLargeBox(Button("Paste"))
        val stray = FXMenuGroupLargeBox(Button("Copy"))
        val group = FXMenuGroup(box, anchor = box)

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            group.anchor = stray
        }
    }

    /**
     * Use case: the mandatory constructor must reject an anchor box that was not passed in the
     * content varargs, so a group can never be built with a dangling anchor.
     */
    @Test
    fun `constructor rejects an anchor that is not one of the content boxes`() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            FXMenuGroup(FXMenuGroupLargeBox(Button("Paste")), anchor = FXMenuGroupLargeBox(Button("Copy")))
        }
    }

    /**
     * Use case: removing the anchor box from `content` is forbidden - the group must always keep its
     * anchor. JavaFX routes list-listener failures to the thread's uncaught-exception handler, so the
     * test captures the [IllegalStateException] there.
     */
    @Test
    fun `removing the anchor box from the content is reported`() {
        val anchor = FXMenuGroupLargeBox(Button("Paste"))
        val other = FXMenuGroupLargeBox(Button("Copy"))
        val group = FXMenuGroup(anchor, other, anchor = anchor)

        val captured = onFx {
            val thread = Thread.currentThread()
            val previous = thread.uncaughtExceptionHandler
            var seen: Throwable? = null
            thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e -> seen = e }
            try {
                group.content.remove(anchor)
            } finally {
                thread.uncaughtExceptionHandler = previous
            }
            seen
        }

        assertTrue(captured is IllegalStateException)
    }

    private fun menuGroup(title: String): FXMenuGroup = FXMenuGroup().apply { this.title = title }

    private fun groupStrip(menuPane: FXMenuPane): HBox = menuPane.lookup(".menu-pane-group-strip") as HBox

    private fun groupTitles(menuPane: FXMenuPane): List<String> = onFx {
        groupStrip(menuPane).children.filterIsInstance<FXMenuGroup>().map { it.title }
    }
}
