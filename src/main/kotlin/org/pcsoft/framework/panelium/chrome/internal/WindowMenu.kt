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
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.ChromeOs
import org.pcsoft.framework.panelium.internal.PaneliumI18n

/**
 * The window system menu shown on a secondary click in the caption drag zone. Rebuilt on every
 * [show] so each entry's enabled state matches the current window state; entries delegate to
 * [WindowOps] and carry the host operating system's window shortcut where one exists. `Move` and
 * `Size` are listed for parity but stay disabled - the one-shot menu cannot host their interactive
 * drag loop.
 *
 * Entry labels are resolved through [PaneliumI18n] for the current default locale and fall back to
 * English when no translation is bundled.
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
            item(text("window.menu.restore", "Restore"), enabled = maximized && !fullScreen) { windowOps.restore() },
            item(text("window.menu.move", "Move"), enabled = false) {},
            item(text("window.menu.size", "Size"), enabled = false) {},
            item(
                text("window.menu.minimize", "Minimize"),
                enabled = !fullScreen,
                accelerator = minimizeAccelerator(),
            ) { windowOps.minimize() },
            item(
                text("window.menu.maximize", "Maximize"),
                enabled = stage.isResizable && !maximized && !fullScreen,
            ) { windowOps.maximize() },
            SeparatorMenuItem(),
            item(
                text("window.menu.close", "Close"),
                enabled = true,
                accelerator = closeAccelerator(),
            ) { windowOps.close() },
        )
    }

    private fun text(key: String, fallback: String): String = PaneliumI18n.string(key, fallback)

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
