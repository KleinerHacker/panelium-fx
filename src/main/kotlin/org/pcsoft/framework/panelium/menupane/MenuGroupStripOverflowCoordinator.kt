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

import javafx.application.Platform
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox

/**
 * Coordinates the overflow of every [FXMenuGroup] currently in the group strip so the groups
 * organise themselves as a whole instead of shrinking evenly.
 *
 * Each group sits at its preferred width (it is not `HBox.hgrow`), so a group the coordinator does
 * not touch keeps its exact width and control sizes. When the groups together need more than the
 * [viewport]'s width, the coordinator collapses whole layout boxes into their group's chevron popup
 * following a retention matrix: ascending [FXMenuGroupBoxPriority] (`LOW` first), then the rightmost
 * group, then the rightmost box within that group. A group's mandatory anchor box is never a
 * candidate, so at least one component always stays visible in every group. Widening restores boxes
 * in the reverse order while they still fit.
 *
 * If every collapsible box is already collapsed and the strip still does not fit, it is left
 * overflowing - the host wraps [groupStrip] in [viewport] with hidden scrollbars and redirects the
 * mouse wheel to scroll it horizontally.
 */
internal class MenuGroupStripOverflowCoordinator(
    private val viewport: ScrollPane,
    private val groupStrip: HBox,
) {

    private var groups: List<FXMenuGroup> = emptyList()
    private var pending: Boolean = false

    init {
        viewport.viewportBoundsProperty().addListener { _, _, _ -> schedule() }
        groupStrip.widthProperty().addListener { _, _, _ -> schedule() }
    }

    /** Sets the groups currently rendered in the strip and recomputes. Pass an empty list to reset. */
    fun setGroups(groups: List<FXMenuGroup>) {
        this.groups.forEach { it.overflowController.onContentChanged = null }
        this.groups = groups.toList()
        // Validate eagerly and synchronously: a group holding layout boxes must have an anchor.
        // eligibleBoxes() throws IllegalStateException here (on the caller's FX call) rather than
        // later from the debounced recompute, so the failure is attributable and does not leak.
        this.groups.forEach { it.overflowController.eligibleBoxes() }
        this.groups.forEach { group -> group.overflowController.onContentChanged = { schedule() } }
        schedule()
    }

    private fun schedule() {
        if (pending) {
            return
        }
        pending = true
        Platform.runLater {
            pending = false
            recompute()
        }
    }

    private fun availableWidth(): Double {
        val vp = viewport.viewportBounds.width
        return if (vp > 0.0) vp else groupStrip.width
    }

    private fun recompute() {
        val groups = groups
        if (groups.isEmpty()) {
            return
        }
        val available = availableWidth()
        if (available <= 0.0) {
            return
        }

        val collapsed: Map<FXMenuGroup, MutableSet<FXMenuGroupBox>> =
            groups.associateWith { HashSet<FXMenuGroupBox>() }

        fun total(): Double =
            groups.sumOf { it.overflowController.groupWidthWithout(collapsed.getValue(it)) } +
                groupStrip.spacing * (groups.size - 1).coerceAtLeast(0)

        // Retention matrix: ascending priority, then rightmost group, then rightmost box. The
        // anchor box of each group is not in eligibleBoxes(), so it is never a candidate - that is
        // what guarantees at least one visible component per group.
        val removalOrder: List<Removable> = buildList {
            groups.forEachIndexed { groupIndex, group ->
                group.overflowController.eligibleBoxes().forEach { box ->
                    add(Removable(group, groupIndex, box.node, group.content.indexOf(box.node), box.priority))
                }
            }
        }.sortedWith(
            compareBy<Removable> { it.priority.ordinal }
                .thenByDescending { it.groupIndex }
                .thenByDescending { it.boxIndex },
        )

        val applied = ArrayList<Removable>()
        var index = 0
        while (total() > available + TOLERANCE && index < removalOrder.size) {
            val next = removalOrder[index]
            index++
            collapsed.getValue(next.group).add(next.node)
            applied.add(next)
        }
        // Restore in reverse of the applied order (highest priority first) while it still fits.
        while (applied.isNotEmpty()) {
            val candidate = applied.last()
            val set = collapsed.getValue(candidate.group)
            set.remove(candidate.node)
            if (total() > available + TOLERANCE) {
                set.add(candidate.node)
                break
            }
            applied.removeAt(applied.lastIndex)
        }

        groups.forEach { it.overflowController.applyCollapsed(collapsed.getValue(it)) }
    }

    private data class Removable(
        val group: FXMenuGroup,
        val groupIndex: Int,
        val node: FXMenuGroupBox,
        val boxIndex: Int,
        val priority: FXMenuGroupBoxPriority,
    )

    private companion object {

        /** Rounding slack (px) so a strip matching its width to the pixel does not oscillate. */
        const val TOLERANCE: Double = 0.5
    }
}
