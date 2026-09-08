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

import javafx.beans.property.BooleanProperty
import javafx.geometry.Side
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Region

/**
 * One overflow-eligible layout box of an [FXMenuGroup] together with its retention [priority].
 */
internal data class MenuGroupBox(val node: FXMenuGroupBox, val priority: FXMenuGroupBoxPriority)

/**
 * Renders the overflow state of a single [FXMenuGroup] on behalf of the strip-wide
 * [MenuGroupStripOverflowCoordinator]. This controller does not decide *what* to collapse - it
 * measures the group for the coordinator ([groupWidthWithout]) and applies the coordinator's verdict
 * ([applyCollapsed]) by moving whole layout boxes ([FXMenuGroupLargeBox] / [FXMenuGroupSmallBox])
 * between [groupContent] and [overflowMenu].
 *
 * The chevron [overflowButton] is shown and managed only while at least one box is collapsed;
 * clicking it opens the collapsed boxes as [CustomMenuItem]s in their original order. [active]
 * mirrors "at least one box is collapsed".
 */
internal class MenuGroupOverflowController(
    private val root: Region,
    private val overflowRow: HBox,
    private val groupContent: HBox,
    private val groupTitle: Label,
    private val overflowButton: Button,
    private val overflowMenu: ContextMenu,
    private val active: BooleanProperty,
    private val anchorSupplier: () -> FXMenuGroupBox?,
) {

    /** Invoked whenever the group's content list changes, so the coordinator can recompute. */
    var onContentChanged: (() -> Unit)? = null

    /**
     * The full ordered box list the group wants to show. Held immutable and swapped wholesale so a
     * concurrent [setContent] never invalidates an iterator a running measurement holds.
     */
    private var allBoxes: List<FXMenuGroupBox> = emptyList()

    /** The boxes currently collapsed into [overflowMenu], as decided by the coordinator. */
    private var collapsed: Set<FXMenuGroupBox> = emptySet()

    init {
        overflowButton.isFocusTraversable = false
        overflowButton.setOnAction {
            if (overflowMenu.items.isNotEmpty()) {
                overflowMenu.show(overflowButton, Side.BOTTOM, 0.0, 0.0)
            }
        }
    }

    /** Replaces the managed box list, drops the collapse state and asks for a recompute. */
    fun setContent(boxes: List<FXMenuGroupBox>) {
        allBoxes = boxes.toList()
        collapsed = emptySet()
        rebuild()
        onContentChanged?.invoke()
    }

    /**
     * The overflow-eligible layout boxes of this group in content order - every box except the
     * mandatory anchor. Throws if the group holds boxes but no anchor was designated.
     */
    fun eligibleBoxes(): List<MenuGroupBox> {
        val anchor = anchorSupplier()
        if (allBoxes.isNotEmpty()) {
            checkNotNull(anchor) { "FXMenuGroup with layout boxes requires an anchor element" }
        }
        return allBoxes.filter { it !== anchor }.map { MenuGroupBox(it, it.priority) }
    }

    /**
     * The width the whole group would take with exactly [hidden] boxes collapsed - the maximum of
     * the content row (visible boxes + spacing + chevron when anything is hidden) and the title
     * label, plus the group's own horizontal insets.
     */
    fun groupWidthWithout(hidden: Set<FXMenuGroupBox>): Double {
        val visible = allBoxes.filter { it !in hidden }
        val contentWidth = if (visible.isEmpty()) {
            horizontalInsets()
        } else {
            visible.sumOf { it.prefWidth(-1.0) } + groupContent.spacing * (visible.size - 1) + horizontalInsets()
        }
        val chevron = if (hidden.any { it in allBoxes }) overflowRow.spacing + overflowButton.prefWidth(-1.0) else 0.0
        val rowWidth = contentWidth + chevron
        return maxOf(rowWidth, groupTitle.prefWidth(-1.0)) + root.insets.left + root.insets.right
    }

    /** Applies the coordinator's verdict: collapse exactly [hidden] (restricted to this group's boxes). */
    fun applyCollapsed(hidden: Set<FXMenuGroupBox>) {
        val effective = hidden.filterTo(HashSet()) { it in allBoxes }
        if (effective == collapsed) {
            return
        }
        collapsed = effective
        rebuild()
    }

    private fun rebuild() {
        val visible = allBoxes.filter { it !in collapsed }
        val overflow = allBoxes.filter { it in collapsed }
        // Release the boxes held by the menu first so they have no parent while being re-added.
        overflowMenu.items.clear()
        groupContent.children.setAll(visible)
        overflowMenu.items.setAll(overflow.map { CustomMenuItem(it, false) })
        val hasOverflow = overflow.isNotEmpty()
        overflowButton.isVisible = hasOverflow
        overflowButton.isManaged = hasOverflow
        active.set(hasOverflow)
    }

    private fun horizontalInsets(): Double = groupContent.insets.left + groupContent.insets.right
}
