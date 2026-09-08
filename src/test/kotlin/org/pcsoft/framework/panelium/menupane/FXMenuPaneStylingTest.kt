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

import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.menupane.support.AbstractMenuPaneUiTest

/**
 * Covers the CSS styling API of [FXMenuPane]: the bundled `menu-pane.css` user-agent stylesheet and
 * an application stylesheet overriding it, the `contextual` and `disabled` pseudo-classes on the
 * tab-strip buttons, the `-panelium-menu-pane-accent-color` styleable property and its effect on
 * contextual tabs, and the colour styling of an [FXMenuContextTabGroup].
 */
class FXMenuPaneStylingTest : AbstractMenuPaneUiTest() {

    /**
     * Use case: [FXMenuPane] ships a default look as a user-agent stylesheet (`menu-pane.css`), so a
     * ribbon is fully styled without any application stylesheet.
     */
    @Test
    fun `the bundled user-agent stylesheet is menu-pane_css`() {
        val menuPane = showMenuPaneStage()

        val uaStylesheet = onFx { menuPane.userAgentStylesheet }
        assertTrue(uaStylesheet.endsWith("menu-pane.css"), "got $uaStylesheet")
    }

    /**
     * Use case: a stylesheet added to the hosting scene overrides the bundled defaults by normal CSS
     * precedence - here it re-sets `-panelium-menu-pane-accent-color` - while the user-agent
     * stylesheet stays in place.
     */
    @Test
    fun `an application stylesheet overrides the user-agent default`() {
        val menuPane = showMenuPaneStage()
        val appSheet = javaClass.getResource("/org/pcsoft/framework/panelium/menupane/app-override.css")

        onFx {
            menuPane.scene.stylesheets.add(appSheet!!.toExternalForm())
            menuPane.applyCss()
        }
        pumpFx()

        assertTrue(onFx { menuPane.scene.stylesheets.any { it.endsWith("app-override.css") } })
        assertTrue(onFx { menuPane.userAgentStylesheet.endsWith("menu-pane.css") })
        assertEquals(Color.web("#123456"), onFx { accentColorOf(menuPane) })
    }

    /**
     * Use case: a tab-strip button carries the `contextual` pseudo-class exactly while its tab is a
     * contextual tab, so a stylesheet can set contextual tabs apart from permanent ones.
     */
    @Test
    fun `the contextual pseudo-class is set on contextual tab buttons only`() {
        val menuPane = showMenuPaneStage()
        onFx {
            menuPane.tabs.add(FXMenuTab("home", "Home"))
            menuPane.contextualTabs.add(FXMenuTab("design", "Design"))
        }
        pumpFx()

        val buttons = onFx { stripButtons(menuPane) }
        assertFalse(onFx { hasPseudo(buttons[0], "contextual") })
        assertTrue(onFx { hasPseudo(buttons[1], "contextual") })
    }

    /**
     * Use case: a disabled tab's button reports the standard JavaFX `disabled` pseudo-class, so a
     * stylesheet can dim it.
     */
    @Test
    fun `a disabled tab carries the disabled pseudo-class on its button`() {
        val menuPane = showMenuPaneStage()
        onFx { menuPane.tabs.add(FXMenuTab("locked", "Locked").apply { isDisabled = true }) }
        pumpFx()

        val button = onFx { stripButtons(menuPane).first() }
        assertTrue(onFx { button.isDisabled })
        assertTrue(onFx { hasPseudo(button, "disabled") })
    }

    /**
     * Use case: an application sets `-panelium-menu-pane-accent-color` via an inline style; the
     * styleable property must pick up the CSS paint value.
     */
    @Test
    fun `the accent colour styleable property takes its value from CSS`() {
        val menuPane = showMenuPaneStage()

        onFx {
            menuPane.style = "-panelium-menu-pane-accent-color: #ff0000;"
            menuPane.applyCss()
        }
        pumpFx()

        assertEquals(Color.web("#ff0000"), onFx { accentColorOf(menuPane) })
    }

    /**
     * Use case: the accent colour reaches the tab-strip buttons of contextual tabs that are not in a
     * coloured group, so restyling one property recolours every plain contextual tab.
     */
    @Test
    fun `the accent colour reaches contextual tab buttons`() {
        val menuPane = showMenuPaneStage()

        onFx {
            menuPane.contextualTabs.add(FXMenuTab("design", "Design"))
            menuPane.style = "-panelium-menu-pane-accent-color: #ff0000;"
            menuPane.applyCss()
        }
        pumpFx()

        val style = onFx { stripButtons(menuPane).first().style }
        assertTrue(style.contains("#FF0000", ignoreCase = true), "got $style")
    }

    /**
     * Use case: a coloured [FXMenuContextTabGroup] applies its colour to both its header label and
     * the accent of its contextual tab button.
     */
    @Test
    fun `a coloured context group styles its header and its tab button`() {
        val menuPane = showMenuPaneStage()
        val design = FXMenuTab("design", "Design")
        onFx {
            menuPane.contextualTabs.add(design)
            menuPane.assignToGroup(design, FXMenuContextTabGroup("Table Tools", "#00ff00"))
        }
        pumpFx()

        val header = onFx { menuPane.lookup(".menu-pane-context-group-header") as Label }
        assertTrue(onFx { header.style.contains("#00ff00") }, "header style ${onFx { header.style }}")
        assertTrue(onFx { stripButtons(menuPane).first().style.contains("#00ff00") })
    }

    /**
     * Use case: an [FXMenuContextTabGroup] colour that JavaFX cannot parse is ignored, leaving the
     * header at its default look instead of an invalid inline style.
     */
    @Test
    fun `an unparseable context group colour is ignored`() {
        val menuPane = showMenuPaneStage()
        val design = FXMenuTab("design", "Design")
        onFx {
            menuPane.contextualTabs.add(design)
            menuPane.assignToGroup(design, FXMenuContextTabGroup("Broken", "not-a-colour"))
        }
        pumpFx()

        val header = onFx { menuPane.lookup(".menu-pane-context-group-header") as Label }
        assertEquals("", onFx { header.style })
    }

    /**
     * Use case: the styleable properties of [FXMenuPane] are published through
     * `getClassCssMetaData()`, following the JavaFX control convention.
     */
    @Test
    fun `the accent colour metadata is published`() {
        assertTrue(FXMenuPane.getClassCssMetaData().any { it.property == "-panelium-menu-pane-accent-color" })
    }

    private fun hasPseudo(node: Node, name: String): Boolean =
        node.pseudoClassStates.any { it.pseudoClassName == name }

    private fun stripButtons(menuPane: FXMenuPane): List<ToggleButton> {
        val strip = menuPane.lookup(".menu-pane-strip") as HBox
        return strip.children.filterIsInstance<ToggleButton>()
    }

    @Suppress("UNCHECKED_CAST")
    private fun accentColorOf(menuPane: FXMenuPane): Paint {
        val meta = FXMenuPane.getClassCssMetaData()
            .first { it.property == "-panelium-menu-pane-accent-color" } as CssMetaData<Styleable, Any>
        return meta.getStyleableProperty(menuPane).value as Paint
    }
}
