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
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Node

/**
 * State of a single menu group: its [title] and the ordered [content] nodes it hosts. Holds no
 * scene graph - the [FXMenuGroupView] renders it.
 */
internal class FXMenuGroupViewModel : ViewModel {

    val title: StringProperty = SimpleStringProperty(this, "title", "")

    val content: ObservableList<Node> = FXCollections.observableArrayList()
}
