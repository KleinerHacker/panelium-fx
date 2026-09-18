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
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * State of an [FXBackstageMenuPane]: the ordered [items] shown in the menu list, the [quickActions]
 * shown in its footer, the currently [selectedItem] (`null` until an item is clicked, see IP-02),
 * and the [menuWidth] of the left-hand menu area.
 *
 * Holds no scene graph - the [FXBackstageMenuPaneView] renders it.
 */
internal class FXBackstageMenuPaneViewModel : ViewModel {

    val items: ObservableList<FXBackstageMenuItem> = FXCollections.observableArrayList()

    val quickActions: ObservableList<FXBackstageQuickAction> = FXCollections.observableArrayList()

    val selectedItem: ObjectProperty<FXBackstageMenuItem?> = SimpleObjectProperty(this, "selectedItem", null)

    val menuWidth: DoubleProperty = SimpleDoubleProperty(this, "menuWidth", 300.0)
}
