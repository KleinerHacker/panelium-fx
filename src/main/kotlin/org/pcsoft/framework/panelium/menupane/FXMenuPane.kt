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
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.PseudoClass
import javafx.css.SimpleStyleableObjectProperty
import javafx.css.StyleConverter
import javafx.css.Styleable
import javafx.css.StyleableObjectProperty
import javafx.css.StyleableProperty
import javafx.scene.Node
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint

/**
 * A row of tabs rendered as a tab strip. Permanent tabs are registered through [tabs]; contextual
 * tabs - shown after the permanent ones, and only relevant to a particular context - through
 * [contextualTabs]. [activeTab] tracks which one is currently selected and can be changed either
 * from code or by the user (click, or left/right arrow while the strip is focused). Removing the
 * active contextual tab falls back to the previously active permanent tab. Usable from FXML
 * through the `<fx:root>` pattern.
 *
 * A disabled tab ([FXMenuTab.disabledProperty]) can never become the [activeTab]: [activate], the
 * [activeTab] setter and arrow-key navigation all skip it, and its tab-strip button is disabled.
 *
 * [fileTab] is the distinguished first tab (the "File" menu). It is NOT part of [tabs] or
 * [visibleTabs]: it lives in its own slot and is drawn as a separate button pinned before the
 * strip, so it never scrolls and is never reached by arrow-key navigation. Activating it opens the
 * backstage: [fileTabActive] flips to `true`, [backstageContent] is shown - through the chrome
 * integration's overlay host when the component is docked, otherwise in the component's own overlay
 * slot with a short fade - and the
 * previously active strip tab is remembered. The backstage closes on Escape, on a click outside its
 * content, or when a strip tab is selected again, restoring that remembered tab and invoking
 * [onBackstageClosed] for a host to restore its ribbon collapse state.
 *
 * [isCollapsed] collapses the ribbon down to just the tab strip and expands it again. The user
 * toggles it by double-clicking the active tab or with the chevron button at the trailing edge of
 * the tab-strip row. While collapsed, a single click on a tab reveals that tab's groups temporarily
 * (a "peek") without expanding; the peek closes on an outside click or on clicking the tab again.
 * The collapse state is preserved across opening and closing the file-tab backstage. While collapsed
 * the `collapsed` pseudo-class is set on the component.
 *
 * **Styling.** [getUserAgentStylesheet] returns a bundled default stylesheet (`menu-pane.css`), so
 * the ribbon has a complete look without an application stylesheet; a stylesheet added to the
 * hosting `Scene` overrides it by normal CSS precedence. Style classes: `menu-pane` on the component
 * itself, `menu-pane-strip` / `menu-pane-strip-scroll-pane` on the tab strip and its viewport,
 * `menu-pane-strip-button` on each tab button, `menu-pane-strip-file-button` on the file-tab button,
 * `menu-pane-collapse-toggle` on the collapse/expand button, `menu-pane-context-group-header` on
 * each context-group header, `menu-pane-group-strip` / `menu-pane-group-strip-scroll-pane` on the
 * group strip and its viewport. A tab button carries the `active` pseudo-class while its tab is the
 * [activeTab] and the `contextual` pseudo-class while its tab is one of the [contextualTabs].
 * [accentColor] / [accentColorProperty] (`-panelium-menu-pane-accent-color`) is the accent applied
 * to contextual tab buttons that do not belong to a coloured [FXMenuContextTabGroup]; a group's
 * [FXMenuContextTabGroup.color] takes precedence for its own header and tabs.
 *
 * From FXML the permanent [tabs] are set as a `<tabs>` property element, as are `fileTab`,
 * `backstageContent`, `activeTab` and `contextualTabs`.
 */
class FXMenuPane : StackPane() {

    private val viewModel: FXMenuPaneViewModel

    /** Backing styleable property for [accentColor] (`-panelium-menu-pane-accent-color`). */
    internal val accentColorImpl: StyleableObjectProperty<Paint> =
        SimpleStyleableObjectProperty(ACCENT_COLOR_META, this, "accentColor", DEFAULT_ACCENT_COLOR)

    init {
        val tuple = FluentViewLoader.fxmlView(FXMenuPaneView::class.java)
            .root(this)
            .load()
        viewModel = tuple.viewModel

        styleClass.add("menu-pane")

        viewModel.tabs.addListener(ListChangeListener { onTabsChanged(it) })
        viewModel.contextualTabs.addListener(ListChangeListener { onContextualTabsChanged(it) })
        viewModel.activeTab.addListener { _, _, active ->
            if (active != null && viewModel.tabs.contains(active)) {
                viewModel.previousPermanentTab = active
            }
        }
        viewModel.fileTabActive.addListener { _, _, active -> onFileTabActiveChanged(active) }
        viewModel.collapsed.addListener { _, _, collapsed ->
            pseudoClassStateChanged(COLLAPSED_PSEUDO_CLASS, collapsed)
        }
    }

    /** The permanent tabs shown in the strip. */
    val tabs: ObservableList<FXMenuTab> get() = viewModel.tabs

    /** The contextual tabs shown after the permanent tabs, in insertion order. */
    val contextualTabs: ObservableList<FXMenuTab> get() = viewModel.contextualTabs

    /** The currently active tab, or `null` when none is active. */
    fun activeTabProperty(): ObjectProperty<FXMenuTab?> = viewModel.activeTab

    var activeTab: FXMenuTab?
        get() = viewModel.activeTab.get()
        set(value) {
            if (value != null && value.isDisabled) {
                return
            }
            viewModel.activeTab.set(value)
        }

    /**
     * The distinguished first tab, drawn as a separate button before the strip, or `null` when the
     * component has no file tab. It is kept out of [tabs] and [visibleTabs] on purpose.
     */
    fun fileTabProperty(): ObjectProperty<FXMenuTab?> = viewModel.fileTab

    var fileTab: FXMenuTab?
        get() = viewModel.fileTab.get()
        set(value) = viewModel.fileTab.set(value)

    /** The application-supplied panel the file tab's backstage shows. */
    fun backstageContentProperty(): ObjectProperty<Node?> = viewModel.backstageContent

    var backstageContent: Node?
        get() = viewModel.backstageContent.get()
        set(value) = viewModel.backstageContent.set(value)

    /** Whether the file tab's backstage is currently open. */
    fun fileTabActiveProperty(): BooleanProperty = viewModel.fileTabActive

    var isFileTabActive: Boolean
        get() = viewModel.fileTabActive.get()
        set(value) = viewModel.fileTabActive.set(value)

    /**
     * Whether the ribbon is collapsed to just the tab strip. Setting it from code is equivalent to
     * double-clicking the active tab or using the collapse/expand chevron. Expanding also ends any
     * active peek.
     */
    fun collapsedProperty(): BooleanProperty = viewModel.collapsed

    var isCollapsed: Boolean
        get() = viewModel.collapsed.get()
        set(value) = viewModel.collapsed.set(value)

    /**
     * The accent paint applied to contextual tab buttons that are not assigned to a coloured
     * [FXMenuContextTabGroup]. Styleable from CSS as `-panelium-menu-pane-accent-color` on the
     * `menu-pane` selector; every colour is a paint, so a gradient works too.
     */
    fun accentColorProperty(): ObjectProperty<Paint> = accentColorImpl

    var accentColor: Paint
        get() = accentColorImpl.get()
        set(value) = accentColorImpl.set(value)

    /**
     * The host that paints [backstageContent] above the whole window while the backstage is open.
     * When `null`, [FXMenuPane] shows the backstage in its own overlay slot instead. Set only by
     * the chrome integration in this module (`MenuChromePane`); not part of the public API.
     */
    internal var overlayHost: BackstageOverlayHost? = null
        set(value) {
            field = value
            viewModel.hasOverlayHost = value != null
        }

    /**
     * Invoked after the backstage has closed and the previously active strip tab has been restored.
     * A host uses this to restore the ribbon's prior collapse state (wired up by a later plan).
     */
    var onBackstageClosed: (() -> Unit)? = null

    /**
     * Activates [tab]. [tab] MUST already be registered in [tabs] or [contextualTabs]. A disabled
     * [tab] is ignored, leaving the current [activeTab] unchanged.
     */
    fun activate(tab: FXMenuTab) {
        require(viewModel.tabs.contains(tab) || viewModel.contextualTabs.contains(tab)) {
            "Tab is not registered: ${tab.id}"
        }
        if (tab.isDisabled) {
            return
        }
        viewModel.activeTab.set(tab)
    }

    /** Assigns [tab] to [group], rendering a group header above it in the tab strip. */
    fun assignToGroup(tab: FXMenuTab, group: FXMenuContextTabGroup) {
        viewModel.groupByTab[tab] = group
        viewModel.rebuildVisibleTabs()
    }

    /** The group [tab] is assigned to, or `null` when it is not assigned to any group. */
    fun groupOf(tab: FXMenuTab): FXMenuContextTabGroup? = viewModel.groupByTab[tab]

    /** The bundled default look; overridden by any stylesheet added to the hosting `Scene`. */
    override fun getUserAgentStylesheet(): String = USER_AGENT_STYLESHEET

    override fun getCssMetaData(): MutableList<CssMetaData<out Styleable, *>> = CSS_META_DATA

    private fun onFileTabActiveChanged(active: Boolean) {
        if (active) {
            viewModel.previousActiveTab = viewModel.activeTab.get()
            viewModel.backstageContent.get()?.let { overlayHost?.showOverlay(it) }
        } else {
            overlayHost?.hideOverlay()
            viewModel.previousActiveTab?.let { restored ->
                if (viewModel.activeTab.get() == null) {
                    viewModel.activeTab.set(restored)
                }
            }
            viewModel.previousActiveTab = null
            onBackstageClosed?.invoke()
        }
    }

    private fun onTabsChanged(change: ListChangeListener.Change<out FXMenuTab>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.removed.forEach { viewModel.groupByTab.remove(it) }
                if (viewModel.previousPermanentTab in change.removed) {
                    viewModel.previousPermanentTab = null
                }
                if (viewModel.previousActiveTab in change.removed) {
                    viewModel.previousActiveTab = null
                }
            }
        }
    }

    private fun onContextualTabsChanged(change: ListChangeListener.Change<out FXMenuTab>) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.removed.forEach { viewModel.groupByTab.remove(it) }
                if (viewModel.activeTab.get() in change.removed) {
                    viewModel.activeTab.set(viewModel.previousPermanentTab)
                }
                if (viewModel.previousActiveTab in change.removed) {
                    viewModel.previousActiveTab = null
                }
            }
        }
    }

    companion object {

        val COLLAPSED_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("collapsed")

        private val DEFAULT_ACCENT_COLOR: Paint = Color.web("#2B579A")

        private val USER_AGENT_STYLESHEET: String =
            FXMenuPane::class.java.getResource("menu-pane.css")!!.toExternalForm()

        private val ACCENT_COLOR_META: CssMetaData<FXMenuPane, Paint> =
            object : CssMetaData<FXMenuPane, Paint>(
                "-panelium-menu-pane-accent-color",
                StyleConverter.getPaintConverter(),
                DEFAULT_ACCENT_COLOR,
            ) {
                override fun isSettable(styleable: FXMenuPane): Boolean = !styleable.accentColorImpl.isBound
                override fun getStyleableProperty(styleable: FXMenuPane): StyleableProperty<Paint> =
                    styleable.accentColorImpl
            }

        private val CSS_META_DATA: MutableList<CssMetaData<out Styleable, *>> = run {
            val list = ArrayList<CssMetaData<out Styleable, *>>(Region.getClassCssMetaData())
            list.add(ACCENT_COLOR_META)
            java.util.Collections.unmodifiableList(list)
        }

        /** The styleable properties of [FXMenuPane], following the JavaFX `Control` convention. */
        fun getClassCssMetaData(): MutableList<CssMetaData<out Styleable, *>> = CSS_META_DATA
    }
}
