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

import javafx.fxml.FXMLLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers building the whole ribbon from FXML: `FXMenuTab` via `@NamedArg`, `FXMenuPane` with a
 * `<tabs>` / `<fileTab>` / `<backstageContent>` / `<activeTab>` property elements, an `FXMenuGroup`
 * whose `<content>` holds layout boxes and whose `<anchor>` is an `<fx:reference>` to one of them,
 * `priority` as a box attribute and `disabled` as a tab attribute - the exact constructs the
 * MenuPane showcase FXML uses.
 */
class FXMenuGroupFxmlTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: loading `menu-pane-fxml-test.fxml` must produce a fully wired `FXMenuPane` - two
     * tabs (the second disabled), the file tab set, the "Home" tab active, and its "Clipboard"
     * group holding both layout boxes with the large box registered as the anchor.
     */
    @Test
    fun `the ribbon builds from FXML including the group anchor`() {
        val pane = onFx {
            FXMLLoader.load<FXMenuPane>(
                javaClass.getResource("/org/pcsoft/framework/panelium/menupane/menu-pane-fxml-test.fxml"),
            )
        }

        assertEquals(listOf("Home", "Disabled"), onFx { pane.tabs.map { it.title } })
        assertTrue(onFx { pane.tabs[1].isDisabled })
        assertNotNull(onFx { pane.fileTab })
        assertSame(onFx { pane.tabs[0] }, onFx { pane.activeTab })

        val group = onFx { pane.tabs[0].groups.first() }
        assertEquals("Clipboard", onFx { group.title })
        assertEquals(2, onFx { group.content.size })
        val anchor = onFx { group.anchor }
        assertTrue(anchor is FXMenuGroupLargeBox)
        assertTrue(onFx { anchor in group.content })

        val protectedGroup = onFx { pane.tabs[0].groups[1] }
        assertTrue(onFx { protectedGroup.isDisable })
    }
}
