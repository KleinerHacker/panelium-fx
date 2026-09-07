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
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem

/**
 * The right-click menu of the ribbon's tab strip and group strip. It carries exactly one entry that
 * collapses or expands the ribbon: its label follows [collapsedState] (`"Expand Ribbon"` while
 * collapsed, `"Collapse Ribbon"` while expanded) and activating it runs [onToggleCollapsed] and
 * closes the menu.
 *
 * Style class `menu-pane-context-menu` on the [ContextMenu] itself for stylesheet targeting.
 */
internal class RibbonContextMenu(
    private val collapsedState: ObservableBooleanValue,
    private val onToggleCollapsed: () -> Unit,
) : ContextMenu() {

    private val toggleCollapsedItem = MenuItem().apply {
        setOnAction {
            onToggleCollapsed()
            hide()
        }
    }

    init {
        styleClass.add("menu-pane-context-menu")
        items.add(toggleCollapsedItem)
        updateToggleLabel(collapsedState.get())
        collapsedState.addListener { _, _, collapsed -> updateToggleLabel(collapsed) }
    }

    private fun updateToggleLabel(collapsed: Boolean) {
        toggleCollapsedItem.text = if (collapsed) EXPAND_TEXT else COLLAPSE_TEXT
    }

    private companion object {
        const val COLLAPSE_TEXT: String = "Collapse Ribbon"
        const val EXPAND_TEXT: String = "Expand Ribbon"
    }
}
