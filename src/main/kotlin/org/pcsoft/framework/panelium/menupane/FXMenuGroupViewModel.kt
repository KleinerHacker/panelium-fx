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

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler

/**
 * State of a single menu group: its [title], the ordered [content] layout boxes it hosts, the
 * mandatory [anchor] box that is never collapsed, and whether the content row currently overflows
 * into its chevron popup ([overflowActive]). Holds no scene graph - the [FXMenuGroupView] renders it
 * and, through [overflowController], lets the strip-wide [MenuGroupStripOverflowCoordinator] measure
 * and drive the group's overflow.
 */
internal class FXMenuGroupViewModel : ViewModel {

    val title: StringProperty = SimpleStringProperty(this, "title", "")

    val content: ObservableList<FXMenuGroupBox> = FXCollections.observableArrayList()

    /** The layout box that always stays visible; must be one of [content]. Null only before setup. */
    val anchor: ObjectProperty<FXMenuGroupBox?> = SimpleObjectProperty(this, "anchor", null)

    /** Handler behind the group's launcher button; `null` means the group shows no launcher button. */
    val onLauncherAction: ObjectProperty<EventHandler<ActionEvent>?> =
        SimpleObjectProperty(this, "onLauncherAction", null)

    val overflowActive: BooleanProperty = SimpleBooleanProperty(this, "overflowActive", false)

    /** Set by [FXMenuGroupView] once its scene graph exists; used by the overflow coordinator. */
    var overflowController: MenuGroupOverflowController? = null
}
