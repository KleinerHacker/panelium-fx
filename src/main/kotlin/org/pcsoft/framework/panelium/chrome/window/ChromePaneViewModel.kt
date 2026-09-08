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

package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node

/**
 * State of a [ChromePane]: the hosted content node and whether the drop shadow is drawn. Window
 * operations stay in [ChromePane] / `WindowOps`, not here.
 */
internal class ChromePaneViewModel : ViewModel {

    val content: ObjectProperty<Node?> = SimpleObjectProperty(this, "content", null)

    val shadowEnabled: BooleanProperty = SimpleBooleanProperty(this, "shadowEnabled", true)
}
