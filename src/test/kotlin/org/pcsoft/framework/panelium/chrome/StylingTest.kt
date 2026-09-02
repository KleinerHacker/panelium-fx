package org.pcsoft.framework.panelium.chrome

import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.scene.Parent
import javafx.scene.effect.DropShadow
import javafx.scene.effect.GaussianBlur
import javafx.scene.effect.InnerShadow
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.LinearGradient
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineCap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest

/**
 * Covers the CSS styling API of [ChromePane]: the `maximized` / `fullscreen` / `active` /
 * `inactive` pseudo-classes track the window state, every `-panelium-*` styleable property (surface
 * paint, border stroke, effect, caption backdrop blur) takes its value from CSS, and the bundled
 * user-agent stylesheet is in place while an application stylesheet still overrides it.
 */
class StylingTest : AbstractChromeUiTest() {

    /**
     * Use case: maximizing and restoring the window toggles the `maximized` pseudo-class on the
     * pane so a stylesheet can drop the effect and rounded corners.
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
        assertTrue(onFx { pane.userAgentStylesheet.endsWith("chrome.css") })
    }

    /**
     * Use case: without any styling the frame border is a flat hairline - all four edges of the
     * rendered [BorderStroke] share one paint and [ChromePane.borderMode] is [ChromeBorderMode.FLAT].
     */
    @Test
    fun `default border mode is a flat hairline`() {
        val (pane, _) = showChromeStage()
        pumpFx()

        assertEquals(ChromeBorderMode.FLAT, onFx { pane.borderMode })
        val stroke = onFx { frameBorderStroke(pane) }
        assertEquals(stroke.topStroke, stroke.bottomStroke)
        assertEquals(stroke.leftStroke, stroke.rightStroke)
        assertEquals(stroke.topStroke, stroke.leftStroke)
    }

    /**
     * Use case: `-panelium-border-mode: raised` (and the [ChromePane.borderMode] API) draws a bevel -
     * the top / left edges take one paint, the bottom / right edges the opposite one.
     */
    @Test
    fun `raised border mode renders a bevel`() {
        val (pane, _) = showChromeStage()

        onFx { pane.borderMode = ChromeBorderMode.RAISED }
        pumpFx()

        val stroke = onFx { frameBorderStroke(pane) }
        assertEquals(stroke.topStroke, stroke.leftStroke)
        assertEquals(stroke.bottomStroke, stroke.rightStroke)
        assertNotEquals(stroke.topStroke, stroke.bottomStroke)
    }

    /**
     * Use case: `-panelium-surface-color` accepts a full paint; a `linear-gradient` reaches the
     * frame surface as a [LinearGradient] background fill.
     */
    @Test
    fun `surface color accepts a linear-gradient paint`() {
        val (pane, _) = showChromeStage()

        onFx {
            pane.style = "-panelium-surface-color: linear-gradient(to bottom, #ffffff, #f2f6ff);"
            pane.applyCss()
        }
        pumpFx()

        val fill = onFx { frameBox(pane).background.fills.first().fill }
        assertInstanceOf(LinearGradient::class.java, fill)
    }

    /**
     * Use case: `-panelium-border-color` styles the flat-mode stroke and also accepts a gradient.
     */
    @Test
    fun `flat border color takes its value from CSS`() {
        val (pane, _) = showChromeStage()

        onFx {
            pane.style = "-panelium-border-mode: flat; -panelium-border-color: #ff0000;"
            pane.applyCss()
        }
        pumpFx()

        assertEquals(Color.web("#ff0000"), onFx { frameBorderStroke(pane).topStroke })
    }

    /**
     * Use case: `-panelium-border-style` and `-panelium-border-line-cap` shape the shared
     * [javafx.scene.layout.BorderStrokeStyle] - a dashed style yields a non-empty dash array and a
     * round cap reaches the stroke.
     */
    @Test
    fun `border stroke style and line cap take their value from CSS`() {
        val (pane, _) = showChromeStage()

        onFx {
            pane.style = "-panelium-border-width: 4; -panelium-border-style: dashed; " +
                "-panelium-border-line-cap: round;"
            pane.applyCss()
        }
        pumpFx()

        val style = onFx { frameBorderStroke(pane).topStyle }
        assertTrue(style.dashArray.isNotEmpty(), "expected a dash array for dashed style")
        assertEquals(StrokeLineCap.ROUND, style.lineCap)
    }

    /**
     * Use case: the bevel edges accept any paint; a `linear-gradient` on
     * `-panelium-border-light-color` reaches the rendered [BorderStroke].
     */
    @Test
    fun `bevel edge accepts a linear-gradient paint`() {
        val (pane, _) = showChromeStage()

        onFx {
            pane.style = "-panelium-border-mode: raised; -panelium-border-width: 5; " +
                "-panelium-border-light-color: linear-gradient(to bottom right, #ffffff, #7cc4ff);"
            pane.applyCss()
        }
        pumpFx()

        assertInstanceOf(LinearGradient::class.java, onFx { frameBorderStroke(pane).topStroke })
    }

    /**
     * Use case: `-panelium-effect` replaces the built-in drop shadow with any CSS effect; without
     * it the frame keeps a [DropShadow] fed by `-panelium-shadow-*`.
     */
    @Test
    fun `frame effect follows -panelium-effect and falls back to a drop shadow`() {
        val (pane, _) = showChromeStage()
        pumpFx()

        assertInstanceOf(DropShadow::class.java, onFx { shadowRoot(pane).effect })

        onFx {
            pane.style = "-panelium-effect: innershadow(gaussian, red, 12, 0.2, 0, 0);"
            pane.applyCss()
        }
        pumpFx()
        assertInstanceOf(InnerShadow::class.java, onFx { shadowRoot(pane).effect })

        onFx {
            pane.style = ""
            pane.applyCss()
        }
        pumpFx()
        assertInstanceOf(DropShadow::class.java, onFx { shadowRoot(pane).effect })
    }

    /**
     * Use case: `-panelium-caption-backdrop-blur` arms the frosted strip behind the caption -
     * a positive value shows the backdrop layer with a [GaussianBlur], `0` hides it.
     */
    @Test
    fun `caption backdrop blur toggles the frosted layer`() {
        val (pane, _) = showChromeStage()

        onFx {
            pane.style = "-panelium-caption-backdrop-blur: 20;"
            pane.applyCss()
        }
        pumpFx()
        assertTrue(onFx { backdrop(pane).isVisible })
        assertInstanceOf(GaussianBlur::class.java, onFx { backdrop(pane).effect })

        onFx {
            pane.style = "-panelium-caption-backdrop-blur: 0;"
            pane.applyCss()
        }
        pumpFx()
        assertFalse(onFx { backdrop(pane).isVisible })
    }

    /**
     * Use case: the caption-button glyphs carry no paint set in code, so an application stylesheet
     * can recolour every glyph through `.chrome-button-glyph-stroke` / `-fill`.
     */
    @Test
    fun `application stylesheet recolours the caption-button glyphs`() {
        val (pane, stage) = showChromeStage()

        val themeSheet = javaClass.getResource("/org/pcsoft/framework/panelium/chrome/glyph-theme.css")
        onFx {
            stage.scene.stylesheets.add(themeSheet!!.toExternalForm())
            pane.applyCss()
        }
        pumpFx()

        val strokes = onFx {
            pane.lookupAll(".chrome-button-glyph-stroke").filterIsInstance<Shape>().map { it.stroke }
        }
        assertTrue(strokes.isNotEmpty(), "no glyph stroke shapes found")
        assertTrue(strokes.all { it == Color.web("#00ff00") }, "expected every glyph stroke green, got $strokes")
    }

    private fun hasPseudoClass(pane: ChromePane, name: String): Boolean =
        pane.pseudoClassStates.any { it.pseudoClassName == name }

    /** The shadow root (outermost stacked layer, carrier of the frame effect). */
    private fun shadowRoot(pane: ChromePane): Parent =
        pane.childrenUnmodifiable.filterIsInstance<Parent>().first()

    /** The framed box carrying the surface fill and border. */
    private fun frameBox(pane: ChromePane): Region =
        shadowRoot(pane).childrenUnmodifiable.filterIsInstance<StackPane>().first()

    /** The single [BorderStroke] currently rendered on the framed box. */
    private fun frameBorderStroke(pane: ChromePane): BorderStroke =
        frameBox(pane).border.strokes.first()

    /** The frosted caption backdrop layer. */
    private fun backdrop(pane: ChromePane): Region =
        pane.lookup(".chrome-caption-backdrop") as Region

    @Suppress("UNCHECKED_CAST")
    private fun styleableValue(pane: ChromePane, property: String): Double {
        val meta = ChromePane.getClassCssMetaData().first { it.property == property }
                as CssMetaData<Styleable, Any>
        return (meta.getStyleableProperty(pane).value as Number).toDouble()
    }
}
