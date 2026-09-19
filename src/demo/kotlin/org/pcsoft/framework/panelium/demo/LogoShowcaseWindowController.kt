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

package org.pcsoft.framework.panelium.demo

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import org.pcsoft.framework.panelium.menupane.FXBackstageMenuPane
import org.pcsoft.framework.panelium.menupane.FXBackstageQuickAction
import org.pcsoft.framework.panelium.menupane.FXMenuPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Controller for `LogoShowcaseWindow.fxml`; wires the backstage's quick action footer (a
 * "Refresh" icon button that jumps the selection back to the first [FXBackstageMenuPane] entry,
 * and a "Close" icon button that closes the backstage overlay), a host entry added to
 * [FXMenuPane.contextMenuItems] to demonstrate extending the ribbon's context menu, and the
 * Sync/Share buttons wired to [FXMenuPane.trailingItems].
 */
class LogoShowcaseWindowController : Initializable {

    @FXML
    private lateinit var menuPane: FXMenuPane

    @FXML
    private lateinit var backstageMenuPane: FXBackstageMenuPane

    @FXML
    private lateinit var syncButton: Button

    @FXML
    private lateinit var shareButton: Button

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        backstageMenuPane.quickActions.addAll(
            FXBackstageQuickAction(
                icon = Label("⟳"),
                onAction = { backstageMenuPane.selectedItem = backstageMenuPane.items.firstOrNull() },
            ),
            FXBackstageQuickAction(
                icon = Label("✕"),
                onAction = { menuPane.isFileTabActive = false },
            ),
        )

        menuPane.contextMenuItems.add(
            MenuItem("About").apply {
                setOnAction { menuPane.isFileTabActive = true }
            },
        )

        // Trailing items: Sync/Share buttons already declared in FXML under <trailingItems> -
        // only their behaviour is wired here.
        syncButton.setOnAction { syncButton.text = "Synced" }
        shareButton.setOnAction { menuPane.isFileTabActive = true }
    }
}
