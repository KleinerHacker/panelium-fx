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
