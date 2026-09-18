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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers the [FXBackstageMenuPane] IP-01 skeleton: default state of [FXBackstageMenuPane.menuWidth],
 * [FXBackstageMenuPane.items] and [FXBackstageMenuPane.quickActions], and that [menuWidth] can be
 * overridden from outside.
 */
class FXBackstageMenuPaneTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: a freshly created [FXBackstageMenuPane] has no menu items registered yet.
     */
    @Test
    fun `items is empty by default`() {
        val backstageMenuPane = showBackstageMenuPaneStage()

        assertTrue(onFx { backstageMenuPane.items }.isEmpty())
    }

    /**
     * Use case: a freshly created [FXBackstageMenuPane] has no quick actions registered yet.
     */
    @Test
    fun `quickActions is empty by default`() {
        val backstageMenuPane = showBackstageMenuPaneStage()

        assertTrue(onFx { backstageMenuPane.quickActions }.isEmpty())
    }

    /**
     * Use case: no menu item is selected initially; [FXBackstageMenuPane.selectedItem] is `null`
     * until an item is explicitly selected.
     */
    @Test
    fun `no item is selected by default`() {
        val backstageMenuPane = showBackstageMenuPaneStage()

        assertNull(onFx { backstageMenuPane.selectedItem })
    }

    /**
     * Use case: an application that does not customise [FXBackstageMenuPane.menuWidth] gets the
     * documented default of `300.0`.
     */
    @Test
    fun `menuWidth defaults to 300`() {
        val backstageMenuPane = showBackstageMenuPaneStage()

        assertEquals(300.0, onFx { backstageMenuPane.menuWidth })
    }

    /**
     * Use case: an application sets [FXBackstageMenuPane.menuWidth] from code; the new value is
     * reflected both by the Kotlin property and by [FXBackstageMenuPane.menuWidthProperty].
     */
    @Test
    fun `menuWidth can be overridden from outside`() {
        val backstageMenuPane = showBackstageMenuPaneStage()

        onFx { backstageMenuPane.menuWidth = 220.0 }

        assertEquals(220.0, onFx { backstageMenuPane.menuWidth })
        assertEquals(220.0, onFx { backstageMenuPane.menuWidthProperty().get() })
    }

    /**
     * Use case: [FXBackstageMenuItem] entries added to [FXBackstageMenuPane.items] show up in the
     * live list, in registration order.
     */
    @Test
    fun `registered items are kept in registration order`() {
        val backstageMenuPane = showBackstageMenuPaneStage()
        val info = FXBackstageMenuItem(text = "Info")
        val settings = FXBackstageMenuItem(text = "Settings")

        onFx { backstageMenuPane.items.addAll(info, settings) }

        assertEquals(listOf("Info", "Settings"), onFx { backstageMenuPane.items.map { it.text } })
    }
}
