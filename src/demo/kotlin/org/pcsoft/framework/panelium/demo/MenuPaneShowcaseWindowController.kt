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
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import org.pcsoft.framework.panelium.menupane.FXMenuContextTabGroup
import org.pcsoft.framework.panelium.menupane.FXMenuGroup
import org.pcsoft.framework.panelium.menupane.FXMenuPane
import org.pcsoft.framework.panelium.menupane.FXMenuTab
import java.net.URL
import java.util.ResourceBundle

/**
 * Controller for `MenuPaneShowcaseWindow.fxml`. The whole ribbon - tabs, groups, layout boxes,
 * per-group anchor, file tab and backstage panel - is declared in the FXML. This controller only
 * wires the dynamic behaviour: the checkbox that toggles the contextual "Table Tools" tabs in and
 * out, the checkbox that toggles the collapse feature, the checkbox that shows or hides the
 * collapse/expand chevron, and the status label that reflects the active tab (and the open
 * backstage).
 */
class MenuPaneShowcaseWindowController : Initializable {

    @FXML
    private lateinit var menuPane: FXMenuPane

    @FXML
    private lateinit var activeTabLabel: Label

    @FXML
    private lateinit var showTableToolsCheckBox: CheckBox

    @FXML
    private lateinit var collapsibleCheckBox: CheckBox

    @FXML
    private lateinit var collapseButtonCheckBox: CheckBox

    @FXML
    private lateinit var launcherLabel: Label

    @FXML
    private lateinit var collapsedLabel: Label

    @FXML
    private lateinit var fontGroup: FXMenuGroup

    @FXML
    private lateinit var tableDesign: FXMenuTab

    @FXML
    private lateinit var tableLayout: FXMenuTab

    private val tableToolsGroup = FXMenuContextTabGroup("Table Tools", "#4a90d9")

    private var fontLauncherCount = 0

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        menuPane.assignToGroup(tableDesign, tableToolsGroup)
        menuPane.assignToGroup(tableLayout, tableToolsGroup)

        fontGroup.onLauncherAction = EventHandler<ActionEvent> {
            fontLauncherCount++
            launcherLabel.text = "Font launcher: opened $fontLauncherCount time(s)"
        }

        collapsedLabel.textProperty().bind(
            Bindings.createStringBinding(
                {
                    if (menuPane.isCollapsed) {
                        "Ribbon: collapsed (double-click the active tab or use the chevron to expand)"
                    } else {
                        "Ribbon: expanded (double-click the active tab or use the chevron to collapse)"
                    }
                },
                menuPane.collapsedProperty(),
            ),
        )

        menuPane.isCollapsible = collapsibleCheckBox.isSelected
        collapsibleCheckBox.selectedProperty().addListener { _, _, selected ->
            menuPane.isCollapsible = selected
        }

        // collapseButtonVisible defaults to false; the showcase opts the chevron in so it can be
        // demonstrated, and the checkbox lets it be toggled back off at runtime.
        menuPane.isCollapseButtonVisible = collapseButtonCheckBox.isSelected
        collapseButtonCheckBox.selectedProperty().addListener { _, _, selected ->
            menuPane.isCollapseButtonVisible = selected
        }

        showTableToolsCheckBox.selectedProperty().addListener { _, _, selected ->
            if (selected) {
                menuPane.contextualTabs.addAll(tableDesign, tableLayout)
                menuPane.activate(tableDesign)
            } else {
                menuPane.contextualTabs.removeAll(tableDesign, tableLayout)
            }
        }

        activeTabLabel.textProperty().bind(
            Bindings.createStringBinding(
                {
                    if (menuPane.isFileTabActive) {
                        return@createStringBinding "Active tab: File (backstage open)"
                    }
                    val active = menuPane.activeTab
                    val kind = when {
                        active == null -> ""
                        menuPane.contextualTabs.contains(active) -> " (contextual)"
                        else -> " (permanent)"
                    }
                    "Active tab: ${active?.title ?: "none"}$kind"
                },
                menuPane.activeTabProperty(),
                menuPane.fileTabActiveProperty(),
            ),
        )
    }
}
