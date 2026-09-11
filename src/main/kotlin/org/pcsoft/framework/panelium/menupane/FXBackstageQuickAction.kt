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
 * A single icon-only quick action rendered in an [FXBackstageMenuPane]'s footer. [icon] is the
 * button's graphic; clicking it invokes [onAction] directly, without selecting any
 * [FXBackstageMenuItem] or changing the pane's content area.
 *
 * Instantiable from FXML through the no-arg constructor, with `icon` as a property element and
 * `onAction` wired from a controller method.
 */
class FXBackstageQuickAction(
    var icon: Node? = null,
    var onAction: (() -> Unit)? = null,
)
