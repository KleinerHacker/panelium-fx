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
