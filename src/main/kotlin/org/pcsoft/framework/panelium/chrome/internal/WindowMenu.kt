package org.pcsoft.framework.panelium.chrome.internal

import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.ChromeOs

/**
 * The window system menu shown on a secondary click in the caption drag zone. Rebuilt on every
 * [show] so each entry's enabled state matches the current window state; entries delegate to
 * [WindowOps] and carry the host operating system's window shortcut where one exists. `Move` and
 * `Size` are listed for parity but stay disabled - the one-shot menu cannot host their interactive
 * drag loop.
 */
internal class WindowMenu(
    private val windowOps: WindowOps,
    private val stage: Stage,
) {

    private val menu = ContextMenu()

    val isShowing: Boolean
        get() = menu.isShowing

    fun show(anchor: Node, screenX: Double, screenY: Double) {
        menu.items.setAll(buildItems())
        menu.show(anchor, screenX, screenY)
    }

    fun hide() {
        if (menu.isShowing) menu.hide()
    }

    private fun buildItems(): List<MenuItem> {
        val fullScreen = stage.isFullScreen
        val maximized = windowOps.isMaximized

        return listOf(
            item("Restore", enabled = maximized && !fullScreen) { windowOps.restore() },
            item("Move", enabled = false) {},
            item("Size", enabled = false) {},
            item("Minimize", enabled = !fullScreen, accelerator = minimizeAccelerator()) { windowOps.minimize() },
            item("Maximize", enabled = stage.isResizable && !maximized && !fullScreen) { windowOps.maximize() },
            SeparatorMenuItem(),
            item("Close", enabled = true, accelerator = closeAccelerator()) { windowOps.close() },
        )
    }

    private fun item(
        text: String,
        enabled: Boolean,
        accelerator: KeyCombination? = null,
        action: () -> Unit,
    ): MenuItem = MenuItem(text).apply {
        isDisable = !enabled
        accelerator?.let { this.accelerator = it }
        setOnAction { action() }
    }

    private fun closeAccelerator(): KeyCombination? = when (OS) {
        ChromeOs.WINDOWS, ChromeOs.LINUX -> KeyCombination.valueOf("Alt+F4")
        ChromeOs.MAC -> KeyCombination.valueOf("Meta+W")
        ChromeOs.OTHER -> null
    }

    private fun minimizeAccelerator(): KeyCombination? =
        if (OS == ChromeOs.MAC) KeyCombination.valueOf("Meta+M") else null

    private companion object {
        val OS: ChromeOs = ChromeOs.detect()
    }
}
