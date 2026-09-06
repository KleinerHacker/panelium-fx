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
 * Contract the host chrome implements so [FXMenuPane] can paint its file-tab backstage panel above
 * the whole window (the ribbon band and the window content alike).
 *
 * Module-internal wiring only, not part of the public API: [FXMenuPane.overlayHost] is set by the
 * chrome integration in this module (see `MenuChromePane`). While it is `null`, [FXMenuPane] falls
 * back to showing the backstage panel in its own overlay slot instead.
 */
internal interface BackstageOverlayHost {

    /** Shows [node] as a full-window overlay above everything else. */
    fun showOverlay(node: Node)

    /** Hides the overlay previously shown through [showOverlay]. */
    fun hideOverlay()
}
