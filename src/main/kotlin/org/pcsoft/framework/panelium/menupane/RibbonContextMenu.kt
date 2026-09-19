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

import javafx.beans.value.ObservableBooleanValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import org.pcsoft.framework.panelium.internal.PaneliumI18n

/**
 * The right-click menu of the menu pane's tab strip and group strip. It always carries the
 * host-supplied [extraItems] first, in insertion order, then - while [collapsible] is `true` - a
 * separator followed by the built-in collapse/expand toggle as the last entry. That toggle's label
 * follows [collapsedState] (the localised equivalent of `"Expand"` while collapsed, `"Collapse"`
 * while expanded); activating it runs [onToggleCollapsed] and closes the menu.
 *
 * While [collapsible] is `false` the toggle entry and its separator are both removed, leaving only
 * [extraItems] - the menu still opens for those. The separator is likewise omitted whenever
 * [extraItems] is empty, so it never appears with nothing to separate on either side. [extraItems]
 * is observed live: additions, removals and reordering are reflected on the next open without
 * recreating the menu.
 *
 * Labels are resolved through [PaneliumI18n] for the current default locale and fall back to
 * English when no translation is bundled.
 *
 * Style class `menu-pane-context-menu` on the [ContextMenu] itself for stylesheet targeting.
 */
internal class RibbonContextMenu(
    private val collapsedState: ObservableBooleanValue,
    private val collapsible: ObservableBooleanValue,
    private val extraItems: ObservableList<MenuItem>,
    private val onToggleCollapsed: () -> Unit,
) : ContextMenu() {

    private val separator = SeparatorMenuItem()

    private val toggleCollapsedItem = MenuItem().apply {
        setOnAction {
            onToggleCollapsed()
            hide()
        }
    }

    init {
        styleClass.add("menu-pane-context-menu")
        updateToggleLabel(collapsedState.get())
        collapsedState.addListener { _, _, collapsed -> updateToggleLabel(collapsed) }
        collapsible.addListener { _, _, _ -> rebuildItems() }
        extraItems.addListener(ListChangeListener { rebuildItems() })
        rebuildItems()
    }

    private fun rebuildItems() {
        val newItems = mutableListOf<MenuItem>()
        newItems.addAll(extraItems)
        if (collapsible.get()) {
            if (extraItems.isNotEmpty()) {
                newItems.add(separator)
            }
            newItems.add(toggleCollapsedItem)
        }
        items.setAll(newItems)
    }

    private fun updateToggleLabel(collapsed: Boolean) {
        toggleCollapsedItem.text = if (collapsed) {
            PaneliumI18n.string(EXPAND_KEY, EXPAND_FALLBACK)
        } else {
            PaneliumI18n.string(COLLAPSE_KEY, COLLAPSE_FALLBACK)
        }
    }

    private companion object {
        const val COLLAPSE_KEY: String = "menupane.contextmenu.collapse"
        const val EXPAND_KEY: String = "menupane.contextmenu.expand"
        const val COLLAPSE_FALLBACK: String = "Collapse"
        const val EXPAND_FALLBACK: String = "Expand"
    }
}
