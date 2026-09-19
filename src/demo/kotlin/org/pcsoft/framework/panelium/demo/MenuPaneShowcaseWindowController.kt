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
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import org.pcsoft.framework.panelium.menupane.FXBackstageMenuPane
import org.pcsoft.framework.panelium.menupane.FXBackstageQuickAction
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
 * collapse/expand chevron, the checkbox that enables or disables the ribbon context menu, a host
 * entry added to [FXMenuPane.contextMenuItems], the status label that reflects the active tab (and
 * the open backstage), the status label that reflects the selected [FXBackstageMenuPane] entry, the
 * backstage's quick action footer (a "Refresh" and a "Close" icon button), the checkbox that
 * clears `backstageContent` to demonstrate [FXMenuPane]'s lazily created default
 * [FXBackstageMenuPane], and the Comments/Edit/Share buttons wired to [FXMenuPane.trailingItems].
 */
class MenuPaneShowcaseWindowController : Initializable {

    @FXML
    private lateinit var menuPane: FXMenuPane

    @FXML
    private lateinit var backstageMenuPane: FXBackstageMenuPane

    @FXML
    private lateinit var activeTabLabel: Label

    @FXML
    private lateinit var backstageSelectionLabel: Label

    @FXML
    private lateinit var backstageQuickActionLabel: Label

    @FXML
    private lateinit var showTableToolsCheckBox: CheckBox

    @FXML
    private lateinit var collapsibleCheckBox: CheckBox

    @FXML
    private lateinit var collapseButtonCheckBox: CheckBox

    @FXML
    private lateinit var contextMenuEnabledCheckBox: CheckBox

    @FXML
    private lateinit var contextMenuActionLabel: Label

    @FXML
    private lateinit var trailingItemActionLabel: Label

    @FXML
    private lateinit var commentsButton: Button

    @FXML
    private lateinit var editButton: Button

    @FXML
    private lateinit var shareButton: Button

    @FXML
    private lateinit var customBackstageContentCheckBox: CheckBox

    @FXML
    private lateinit var backstageContentKindLabel: Label

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

    private var refreshQuickActionCount = 0

    private var contextMenuActionCount = 0

    private var trailingItemActionCount = 0

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        menuPane.assignToGroup(tableDesign, tableToolsGroup)
        menuPane.assignToGroup(tableLayout, tableToolsGroup)

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

        customBackstageContentCheckBox.selectedProperty().addListener { _, _, useCustom ->
            if (useCustom) {
                menuPane.backstageContent = backstageMenuPane
                backstageContentKindLabel.text = "Backstage content: custom"
            } else {
                menuPane.backstageContent = null
                backstageContentKindLabel.text =
                    "Backstage content: default (FXMenuPane's lazily created FXBackstageMenuPane)"
            }
        }

        menuPane.contextMenuItems.add(
            MenuItem("Refresh Ribbon").apply {
                setOnAction {
                    contextMenuActionCount++
                    contextMenuActionLabel.text = "Context menu action: fired $contextMenuActionCount time(s)"
                }
            },
        )
        menuPane.isContextMenuEnabled = contextMenuEnabledCheckBox.isSelected
        contextMenuEnabledCheckBox.selectedProperty().addListener { _, _, selected ->
            menuPane.isContextMenuEnabled = selected
        }

        // Trailing items: Comments/Edit/Share buttons already declared in FXML under
        // <trailingItems> - only their behaviour is wired here.
        val trailingActionHandler: (String) -> Unit = { name ->
            trailingItemActionCount++
            trailingItemActionLabel.text = "Trailing item action: \"$name\" fired $trailingItemActionCount time(s)"
        }
        commentsButton.setOnAction { trailingActionHandler("Comments") }
        editButton.setOnAction { trailingActionHandler("Edit") }
        shareButton.setOnAction { trailingActionHandler("Share") }

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
