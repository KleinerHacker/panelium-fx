package org.pcsoft.framework.panelium.menutab

import javafx.scene.Node

/**
 * Contract the host chrome implements so [FXMenuTab] can paint its file-tab backstage panel above
 * the whole window (the ribbon band and the window content alike).
 *
 * [FXMenuTab.overlayHost] is wired up by the chrome integration. While it is `null`, [FXMenuTab]
 * falls back to showing the backstage panel in its own overlay slot instead.
 */
interface BackstageOverlayHost {

    /** Shows [node] as a full-window overlay above everything else. */
    fun showOverlay(node: Node)

    /** Hides the overlay previously shown through [showOverlay]. */
    fun hideOverlay()
}
