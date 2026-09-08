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

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

/**
 * A layout box that stacks up to [MAX_CONTROLS] small controls vertically inside an [FXMenuGroup] -
 * the ribbon "small button stack" slot. Put instances of this box (and [FXMenuGroupLargeBox]) into
 * [FXMenuGroup.content]; the group arranges its content horizontally, so several small boxes side by
 * side form the columns of a group.
 *
 * A ribbon stack holds at most [MAX_CONTROLS] rows: the vararg constructor rejects more with an
 * [IllegalArgumentException], and a further child added afterwards is reported as an
 * [IllegalStateException] on the FX thread's uncaught-exception handler (JavaFX routes list-listener
 * failures there rather than propagating them).
 *
 * The box always reserves [MAX_CONTROLS] equal rows: its preferred (and minimum) height is
 * [MAX_CONTROLS] times the tallest child's preferred height plus the row spacing, independent of how
 * many controls it actually holds. [layoutChildren] then splits the box height into [MAX_CONTROLS]
 * equal slots and places the controls in the top slots, each stretched to the full slot - so a box
 * with one or two controls keeps ribbon-sized rows and leaves the remaining slots empty at the
 * bottom. A `Region` child has its `maxWidth` / `maxHeight` widened so it can be stretched to the
 * slot.
 *
 * Every box in a group carries equal `HBox` grow weight ([Priority.ALWAYS]) and an unbounded
 * `maxWidth`, so the group's content row divides its width evenly across all of its boxes.
 *
 * [priority] is the box's retention priority when the group strip runs out of width: the overflow
 * coordinator collapses lower-priority boxes into the chevron popup before higher-priority ones. The
 * group's anchor box is never collapsed regardless of priority. Defaults to
 * [FXMenuGroupBoxPriority.MEDIUM].
 *
 * Style class: `menu-group-small-box`. Usable from FXML as a plain element with its child controls
 * nested inside.
 */
class FXMenuGroupSmallBox() : VBox(2.0), FXMenuGroupBox {

    private val priorityProperty: ObjectProperty<FXMenuGroupBoxPriority> =
        SimpleObjectProperty(this, "priority", FXMenuGroupBoxPriority.MEDIUM)

    init {
        styleClass.add("menu-group-small-box")
        // Controls stack from the top of the group, never centred vertically in the content row.
        alignment = Pos.TOP_LEFT
        isFillWidth = true
        // Every box in a group carries equal HBox weight and an unbounded max width, so the group's
        // content row divides its width evenly across the boxes.
        maxWidth = Double.MAX_VALUE
        HBox.setHgrow(this, Priority.ALWAYS)
        children.addListener(
            ListChangeListener {
                check(children.size <= MAX_CONTROLS) {
                    "FXMenuGroupSmallBox holds at most $MAX_CONTROLS controls, got ${children.size}"
                }
                widenChildren()
            },
        )
    }

    /** Creates the box already holding [controls] (at most [MAX_CONTROLS]) as its children. */
    constructor(vararg controls: Node) : this() {
        require(controls.size <= MAX_CONTROLS) {
            "FXMenuGroupSmallBox holds at most $MAX_CONTROLS controls, got ${controls.size}"
        }
        children.addAll(*controls)
    }

    /** Creates the box holding [controls] with an explicit retention [priority]. */
    constructor(priority: FXMenuGroupBoxPriority, vararg controls: Node) : this(*controls) {
        this.priority = priority
    }

    override fun priorityProperty(): ObjectProperty<FXMenuGroupBoxPriority> = priorityProperty

    override var priority: FXMenuGroupBoxPriority
        get() = priorityProperty.get()
        set(value) = priorityProperty.set(value)

    /**
     * Always reserves [MAX_CONTROLS] rows of the tallest child's preferred height. Deliberately
     * independent of the box's own current height, so the value never feeds back on itself through
     * the parent's layout (which would make the rows grow without bound on every resize).
     */
    override fun computePrefHeight(width: Double): Double {
        val inner = if (width < 0.0) -1.0 else (width - insets.left - insets.right).coerceAtLeast(0.0)
        val row = children.asSequence()
            .filter { it.isManaged }
            .map { it.prefHeight(inner) }
            .maxOrNull() ?: 0.0
        return insets.top + insets.bottom + MAX_CONTROLS * row + (MAX_CONTROLS - 1) * spacing
    }

    override fun computeMinHeight(width: Double): Double = computePrefHeight(width)

    /** Splits the box height into [MAX_CONTROLS] equal slots and fills the top ones. */
    override fun layoutChildren() {
        val left = insets.left
        val contentWidth = (width - left - insets.right).coerceAtLeast(0.0)
        val contentHeight = (height - insets.top - insets.bottom).coerceAtLeast(0.0)
        val rowHeight = ((contentHeight - (MAX_CONTROLS - 1) * spacing) / MAX_CONTROLS).coerceAtLeast(0.0)
        var y = insets.top
        for (child in children) {
            if (child.isManaged) {
                child.resizeRelocate(left, y, contentWidth, rowHeight)
            }
            y += rowHeight + spacing
        }
    }

    /** Widens every `Region` child so [layoutChildren] can stretch it to the full slot. */
    private fun widenChildren() {
        for (child in children) {
            if (child is Region) {
                child.maxWidth = Double.MAX_VALUE
                child.maxHeight = Double.MAX_VALUE
            }
        }
    }

    companion object {

        /** The maximum number of controls a small box stacks - the ribbon convention of three rows. */
        const val MAX_CONTROLS: Int = 3
    }
}
