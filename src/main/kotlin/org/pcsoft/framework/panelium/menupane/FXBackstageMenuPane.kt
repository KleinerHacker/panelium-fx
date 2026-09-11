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

package org.pcsoft.framework.panelium.menupane

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
import javafx.scene.layout.StackPane

/**
 * A self-contained, reusable backstage menu: a menu list on the left ([items], default width
 * [menuWidth] of 300px, overridable from outside), a footer of icon-only [quickActions] below it
 * (built in IP-03) and a content area that shows the [FXBackstageMenuItem.content] of the
 * [FXBackstageMenuItem] currently selected in [items] (selection wired up in IP-02). Usable from
 * FXML through the `<fx:root>` pattern, independently of [FXMenuPane].
 *
 * [FXMenuPane] sets an instance of this class as its default `backstageContent` (wired up in
 * IP-04); an application overriding `backstageContent` is unaffected.
 */
class FXBackstageMenuPane : StackPane() {

    private val viewModel: FXBackstageMenuPaneViewModel

    init {
        val tuple = FluentViewLoader.fxmlView(FXBackstageMenuPaneView::class.java)
            .root(this)
            .load()
        viewModel = tuple.viewModel

        styleClass.add("backstage-menu-pane")
    }

    /** The menu entries shown in the left-hand menu list. */
    val items: ObservableList<FXBackstageMenuItem> get() = viewModel.items

    /** The icon-only quick actions shown in the footer below the menu list. */
    val quickActions: ObservableList<FXBackstageQuickAction> get() = viewModel.quickActions

    /** The currently selected menu item, or `null` while none has been selected yet. */
    fun selectedItemProperty(): ObjectProperty<FXBackstageMenuItem?> = viewModel.selectedItem

    var selectedItem: FXBackstageMenuItem?
        get() = viewModel.selectedItem.get()
        set(value) = viewModel.selectedItem.set(value)

    /** The width of the left-hand menu area. Defaults to `300.0`, overridable from outside. */
    fun menuWidthProperty(): DoubleProperty = viewModel.menuWidth

    var menuWidth: Double
        get() = viewModel.menuWidth.get()
        set(value) = viewModel.menuWidth.set(value)

    /** The bundled default look; overridden by any stylesheet added to the hosting `Scene`. */
    override fun getUserAgentStylesheet(): String = USER_AGENT_STYLESHEET

    companion object {
        private val USER_AGENT_STYLESHEET: String =
            FXBackstageMenuPane::class.java.getResource("backstage-menu-pane.css")!!.toExternalForm()
    }
}
