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
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane

/**
 * A layout box for a single, prominent control inside an [FXMenuGroup] - the ribbon "large button"
 * slot. Put instances of this box (and [FXMenuGroupSmallBox]) into [FXMenuGroup.content]; the group
 * arranges its content horizontally, so each box forms one column of the group.
 *
 * The box stretches to the full height of the group's content row (its `maxHeight` is unbounded),
 * and its hosted control is stretched to fill the box in both directions - a `Region` child has its
 * `maxWidth` / `maxHeight` widened to unbounded, so a plain `Button` fills the whole slot. An
 * application stylesheet can still cap the control through `-fx-max-width` / `-fx-max-height`.
 * Nesting further layout boxes is possible but not the intended use - a large box holds exactly one
 * control.
 *
 * Every box in a group carries equal `HBox` grow weight ([Priority.ALWAYS]) and an unbounded
 * `maxWidth`, so the group's content row divides its width evenly across all of its boxes.
 *
 * [priority] is the box's retention priority when the group strip runs out of width: the overflow
 * coordinator collapses lower-priority boxes into the chevron popup before higher-priority ones. The
 * group's anchor box is never collapsed regardless of priority. Defaults to
 * [FXMenuGroupBoxPriority.MEDIUM].
 *
 * Style class: `menu-group-large-box`. Usable from FXML as a plain element with its child control
 * nested inside.
 */
class FXMenuGroupLargeBox() : StackPane(), FXMenuGroupBox {

    private val priorityProperty: ObjectProperty<FXMenuGroupBoxPriority> =
        SimpleObjectProperty(this, "priority", FXMenuGroupBoxPriority.MEDIUM)

    init {
        styleClass.add("menu-group-large-box")
        maxHeight = Double.MAX_VALUE
        // Every box in a group carries equal HBox weight and an unbounded max width, so the group's
        // content row divides its width evenly across the boxes.
        maxWidth = Double.MAX_VALUE
        HBox.setHgrow(this, Priority.ALWAYS)
        children.addListener(ListChangeListener { stretchChildren() })
        stretchChildren()
    }

    /** Creates the box already holding [control] as its single child. */
    constructor(control: Node) : this() {
        children.add(control)
    }

    /** Creates the box holding [control] with an explicit retention [priority]. */
    constructor(control: Node, priority: FXMenuGroupBoxPriority) : this(control) {
        this.priority = priority
    }

    override fun priorityProperty(): ObjectProperty<FXMenuGroupBoxPriority> = priorityProperty

    override var priority: FXMenuGroupBoxPriority
        get() = priorityProperty.get()
        set(value) = priorityProperty.set(value)

    /** Widens every `Region` child so the stacking pane grows it to fill the whole box. */
    private fun stretchChildren() {
        for (child in children) {
            if (child is Region) {
                child.maxWidth = Double.MAX_VALUE
                child.maxHeight = Double.MAX_VALUE
            }
        }
    }
}
