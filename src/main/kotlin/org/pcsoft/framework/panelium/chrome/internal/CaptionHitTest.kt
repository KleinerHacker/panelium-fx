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

package org.pcsoft.framework.panelium.chrome.internal

import javafx.scene.Node
import javafx.scene.control.Control
import org.pcsoft.framework.panelium.chrome.ChromeCaptionBar

/**
 * Decides whether a pressed point inside the caption bar drags the window or passes through to the
 * node under the pointer. Walking from the picked node up to the caption bar, the first explicit
 * [ChromeCaptionBar.getDragRegion] flag wins; without a flag an interactive node makes the point a
 * passthrough; if neither applies the caption background drags the window.
 */
internal object CaptionHitTest {

    fun isDragZone(picked: Node?, captionBar: Node): Boolean {
        var node: Node? = picked
        while (node != null) {
            ChromeCaptionBar.getDragRegion(node)?.let { return it }
            if (node === captionBar) return true
            if (isInteractive(node)) return false
            node = node.parent
        }
        return true
    }

    fun isInteractive(node: Node): Boolean =
        node is Control ||
            node.isFocusTraversable ||
            node.onMousePressed != null ||
            node.onMouseClicked != null ||
            node.onMouseReleased != null ||
            node.onMouseDragged != null
}
