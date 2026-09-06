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

import javafx.scene.Node
import javafx.scene.layout.StackPane

/**
 * A layout box for a single, prominent control inside an [FXMenuGroup] - the ribbon "large button"
 * slot. Put instances of this box (and [FXMenuGroupSmallBox]) into [FXMenuGroup.content]; the group
 * arranges its content horizontally, so each box forms one column of the group.
 *
 * The box stretches to the full height of the group's content row (its `maxHeight` is unbounded),
 * so the hosted control spans the whole group vertically. Nesting further layout boxes is possible
 * but not the intended use - a large box holds exactly one control.
 *
 * Style class: `menu-group-large-box`. Usable from FXML as a plain element with its child control
 * nested inside.
 */
class FXMenuGroupLargeBox() : StackPane() {

    init {
        styleClass.add("menu-group-large-box")
        maxHeight = Double.MAX_VALUE
    }

    /** Creates the box already holding [control] as its single child. */
    constructor(control: Node) : this() {
        children.add(control)
    }
}
