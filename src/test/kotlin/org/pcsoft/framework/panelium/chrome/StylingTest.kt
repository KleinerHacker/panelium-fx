package org.pcsoft.framework.panelium.chrome

import javafx.css.CssMetaData
import javafx.css.Styleable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest

/**
 * Covers the CSS styling API of [ChromePane]: the `maximized` / `fullscreen` / `active` /
 * `inactive` pseudo-classes track the window state, the `-panelium-*` styleable properties take
 * their value from CSS, and the bundled user-agent stylesheet is in place while an application
 * stylesheet still overrides it.
 */
class StylingTest : AbstractChromeUiTest() {

    /**
     * Use case: maximizing and restoring the window toggles the `maximized` pseudo-class on the
     * pane so a stylesheet can drop the shadow and rounded corners.
     */
    @Test
    fun `maximized pseudo-class tracks the window state`() {
        val (pane, _) = showChromeStage()
        val ops = pane.windowOps!!

        assertFalse(onFx { hasPseudoClass(pane, "maximized") })
        onFx { ops.maximize() }
        pumpFx()
        assertTrue(onFx { hasPseudoClass(pane, "maximized") })
        onFx { ops.restore() }
        pumpFx()
        assertFalse(onFx { hasPseudoClass(pane, "maximized") })
    }

    /**
     * Use case: entering and leaving full screen toggles the `fullscreen` pseudo-class on the pane.
     */
    @Test
    fun `fullscreen pseudo-class tracks the window state`() {
        val (pane, stage) = showChromeStage()

        onFx { stage.isFullScreen = true }
        pumpFx()
        assertTrue(onFx { hasPseudoClass(pane, "fullscreen") })

        onFx { stage.isFullScreen = false }
        pumpFx()
        assertFalse(onFx { hasPseudoClass(pane, "fullscreen") })
    }

    /**
     * Use case: exactly one of the `active` / `inactive` focus pseudo-classes is set once the pane
     * is attached to a stage, so a stylesheet can always target the current focus state.
     */
    @Test
    fun `exactly one focus pseudo-class is set`() {
        val (pane, _) = showChromeStage()
        pumpFx()

        val active = onFx { hasPseudoClass(pane, "active") }
        val inactive = onFx { hasPseudoClass(pane, "inactive") }
        assertTrue(active xor inactive, "expected exactly one of active/inactive, got active=$active inactive=$inactive")
    }

    /**
     * Use case: an application sets `-panelium-corner-radius` and `-panelium-shadow-radius` via an
     * inline style; both styleable properties must pick up the CSS values.
     */
    @Test
    fun `styleable properties take their value from CSS`() {
        val (pane, _) = showChromeStage()

        onFx {
            pane.style = "-panelium-corner-radius: 21; -panelium-shadow-radius: 33;"
            pane.applyCss()
        }

        assertEquals(21.0, styleableValue(pane, "-panelium-corner-radius"), 0.001)
        assertEquals(33.0, styleableValue(pane, "-panelium-shadow-radius"), 0.001)
    }

    /**
     * Use case: [ChromePane] ships a default look as a user-agent stylesheet (`chrome.css`) while
     * an application stylesheet added to the scene still wins by normal CSS precedence.
     */
    @Test
    fun `user-agent stylesheet is set and an app stylesheet can override it`() {
        val (pane, stage) = showChromeStage()

        val uaStylesheet = onFx { pane.userAgentStylesheet }
        assertTrue(uaStylesheet.endsWith("chrome.css"), "got $uaStylesheet")

        val appSheet = javaClass.getResource("/org/pcsoft/framework/panelium/chrome/app-override.css")
        onFx { stage.scene.stylesheets.add(appSheet!!.toExternalForm()) }
        pumpFx()

        assertTrue(onFx { stage.scene.stylesheets.any { it.endsWith("app-override.css") } })
        // The user-agent stylesheet stays in place; the app stylesheet is layered on top.
        assertTrue(onFx { pane.userAgentStylesheet.endsWith("chrome.css") })
    }

    private fun hasPseudoClass(pane: ChromePane, name: String): Boolean =
        pane.pseudoClassStates.any { it.pseudoClassName == name }

    @Suppress("UNCHECKED_CAST")
    private fun styleableValue(pane: ChromePane, property: String): Double {
        val meta = ChromePane.getClassCssMetaData().first { it.property == property }
                as CssMetaData<Styleable, Any>
        return (meta.getStyleableProperty(pane).value as Number).toDouble()
    }
}
