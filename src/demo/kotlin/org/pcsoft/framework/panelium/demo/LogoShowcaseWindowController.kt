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
import javafx.scene.control.Label
import org.pcsoft.framework.panelium.menupane.FXBackstageMenuPane
import org.pcsoft.framework.panelium.menupane.FXBackstageQuickAction
import org.pcsoft.framework.panelium.menupane.FXMenuPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Controller for `LogoShowcaseWindow.fxml`; wires the backstage's quick action footer (a
 * "Refresh" icon button that jumps the selection back to the first [FXBackstageMenuPane] entry,
 * and a "Close" icon button that closes the backstage overlay).
 */
class LogoShowcaseWindowController : Initializable {

    @FXML
    private lateinit var menuPane: FXMenuPane

    @FXML
    private lateinit var backstageMenuPane: FXBackstageMenuPane

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
    }
}
