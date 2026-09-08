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
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority

/**
 * Common base class of the ribbon layout boxes that make up an [FXMenuGroup]'s content row -
 * [FXMenuGroupLargeBox] and [FXMenuGroupSmallBox]. It is the element type of [FXMenuGroup.content]
 * and [FXMenuGroup.anchor], so a group hosts only these boxes and never loose controls.
 *
 * Every box carries a retention [priority] that the strip-wide overflow coordinator uses to decide
 * which non-anchor boxes collapse into the chevron popup first. An [FXMenuGroup]'s mandatory anchor
 * element must be one of these boxes.
 *
 * Every box carries equal `HBox` grow weight ([Priority.ALWAYS]) and an unbounded `maxWidth`, so the
 * group's content row divides its width evenly across all of its boxes. Every box also has a fixed
 * [MIN_WIDTH] floor, so a small or large box never shrinks below a usable width.
 */
sealed class FXMenuGroupBox : Pane() {

    private val priorityProperty: ObjectProperty<FXMenuGroupBoxPriority> =
        SimpleObjectProperty(this, "priority", FXMenuGroupBoxPriority.MEDIUM)

    init {
        // Every box in a group carries equal HBox weight and an unbounded max width, so the group's
        // content row divides its width evenly across the boxes.
        maxWidth = Double.MAX_VALUE
        minWidth = MIN_WIDTH
        HBox.setHgrow(this, Priority.ALWAYS)
    }

    /** Retention priority when the group strip runs out of width. Ignored for a group's anchor box. */
    var priority: FXMenuGroupBoxPriority
        get() = priorityProperty.get()
        set(value) = priorityProperty.set(value)

    fun priorityProperty(): ObjectProperty<FXMenuGroupBoxPriority> = priorityProperty

    companion object {

        /** Fixed minimum width of every ribbon layout box, in pixels. */
        const val MIN_WIDTH: Double = 60.0
    }
}
