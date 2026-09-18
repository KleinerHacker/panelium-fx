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

/**
 * A single entry in an [FXBackstageMenuPane]'s menu list. [icon] is an optional leading graphic,
 * [text] the label shown beside it, and [content] the node displayed in the pane's content area
 * while this item is the selected one.
 *
 * Instantiable from FXML through the no-arg constructor: `<FXBackstageMenuItem text="Info">` with
 * `icon` and `content` as property elements.
 */
class FXBackstageMenuItem(
    var icon: Node? = null,
    var text: String = "",
    var content: Node? = null,
)
