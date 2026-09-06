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
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.ChromeCaptionBar

/**
 * Routes mouse gestures on the [ChromeCaptionBar] to [WindowOps]: dragging a drag zone moves the
 * window, a primary double-click toggles maximize (resizable stages only) and a secondary click
 * opens the [WindowMenu]. Presses on interactive content or on nodes marked `dragRegion = false`
 * pass through untouched. Installed as an event filter so it runs before the caption's children.
 */
internal class CaptionDragHandler(
    private val captionBar: ChromeCaptionBar,
    private val windowOps: WindowOps,
    private val stage: Stage,
) {

    private val windowMenu = WindowMenu(windowOps, stage)
    private var dragging = false

    fun install() {
        captionBar.addEventFilter(MouseEvent.MOUSE_PRESSED, ::onPressed)
        captionBar.addEventFilter(MouseEvent.MOUSE_DRAGGED, ::onDragged)
    }

    private fun onPressed(event: MouseEvent) {
        dragging = false
        windowMenu.hide()
        if (!CaptionHitTest.isDragZone(pickedNode(event), captionBar)) return

        when (event.button) {
            MouseButton.SECONDARY -> {
                windowMenu.show(captionBar, event.screenX, event.screenY)
                event.consume()
            }

            MouseButton.PRIMARY -> {
                if (event.clickCount == 2) {
                    if (stage.isResizable) windowOps.toggleMaximize()
                } else {
                    dragging = true
                    windowOps.startMove(event.screenX, event.screenY)
                }
                event.consume()
            }

            else -> Unit
        }
    }

    private fun onDragged(event: MouseEvent) {
        if (!dragging) return
        windowOps.moveTo(event.screenX, event.screenY)
        event.consume()
    }

    private fun pickedNode(event: MouseEvent): Node? =
        event.pickResult?.intersectedNode ?: event.target as? Node
}
