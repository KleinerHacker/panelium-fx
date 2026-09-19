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

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import org.pcsoft.framework.panelium.chrome.ChromeOs
import org.pcsoft.framework.panelium.chrome.MenuChromePane
import org.pcsoft.framework.panelium.menupane.FXBackstageMenuPane
import org.pcsoft.framework.panelium.menupane.FXBackstageQuickAction
import org.pcsoft.framework.panelium.menupane.FXMenuPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Controller for `ChromeDemoWindow.fxml`; wires the caption OS selector, the included pages, the
 * status bar label that reflects the selected [FXBackstageMenuPane] entry, the backstage's
 * quick action footer (a "Refresh" and a "Close" icon button), a host entry added to
 * [FXMenuPane.contextMenuItems] to demonstrate extending the ribbon's context menu, and the
 * Preview/Share buttons wired to [FXMenuPane.trailingItems].
 */
class ChromeDemoWindowController : Initializable {

    @FXML
    private lateinit var chromePane: MenuChromePane

    @FXML
    private lateinit var menuPane: FXMenuPane

    @FXML
    private lateinit var osSelector: ComboBox<ChromeOs>

    @FXML
    private lateinit var backstageMenuPane: FXBackstageMenuPane

    @FXML
    private lateinit var backstageSelectionLabel: Label

    @FXML
    private lateinit var backstageQuickActionLabel: Label

    @FXML
    private lateinit var chromeOptionsPageController: ChromeOptionsPageController

    @FXML
    private lateinit var previewButton: Button

    @FXML
    private lateinit var shareButton: Button

    @FXML
    private lateinit var trailingItemActionLabel: Label

    private var refreshQuickActionCount = 0

    private var trailingItemActionCount = 0

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        osSelector.value = chromePane.captionOs
        chromePane.captionOsProperty().bindBidirectional(osSelector.valueProperty())

        backstageSelectionLabel.textProperty().bind(
            Bindings.createStringBinding(
                { "Backstage selection: ${backstageMenuPane.selectedItem?.text ?: "none"}" },
                backstageMenuPane.selectedItemProperty(),
            ),
        )

        backstageMenuPane.quickActions.addAll(
            FXBackstageQuickAction(
                icon = Label("⟳"),
                onAction = {
                    refreshQuickActionCount++
                    backstageQuickActionLabel.text = "Backstage quick action: refreshed $refreshQuickActionCount time(s)"
                },
            ),
            FXBackstageQuickAction(
                icon = Label("✕"),
                onAction = { menuPane.isFileTabActive = false },
            ),
        )

        chromeOptionsPageController.chromePane = chromePane

        menuPane.contextMenuItems.add(
            MenuItem("Reset OS Selector").apply {
                setOnAction { osSelector.value = ChromeOs.WINDOWS }
            },
        )

        // Trailing items: Preview/Share buttons already declared in FXML under <trailingItems> -
        // only their behaviour is wired here.
        val trailingActionHandler: (String) -> Unit = { name ->
            trailingItemActionCount++
            trailingItemActionLabel.text = "Trailing item action: \"$name\" fired $trailingItemActionCount time(s)"
        }
        previewButton.setOnAction { trailingActionHandler("Preview") }
        shareButton.setOnAction { trailingActionHandler("Share") }
    }
}
