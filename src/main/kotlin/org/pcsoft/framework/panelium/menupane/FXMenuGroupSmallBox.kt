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
import javafx.geometry.Pos
import javafx.scene.Node
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
 * [priority] is the box's retention priority when the group strip runs out of width: the overflow
 * coordinator collapses lower-priority boxes into the chevron popup before higher-priority ones and
 * never collapses an [FXMenuGroupBoxPriority.ALWAYS] box. Defaults to [FXMenuGroupBoxPriority.MEDIUM].
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
        children.addListener(javafx.collections.ListChangeListener {
            check(children.size <= MAX_CONTROLS) {
                "FXMenuGroupSmallBox holds at most $MAX_CONTROLS controls, got ${children.size}"
            }
        })
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

    companion object {

        /** The maximum number of controls a small box stacks - the ribbon convention of three rows. */
        const val MAX_CONTROLS: Int = 3
    }
}
