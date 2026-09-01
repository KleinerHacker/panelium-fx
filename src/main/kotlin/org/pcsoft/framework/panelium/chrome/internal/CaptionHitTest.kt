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
