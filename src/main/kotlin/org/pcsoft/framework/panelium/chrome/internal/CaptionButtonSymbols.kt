package org.pcsoft.framework.panelium.chrome.internal

import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineCap
import org.pcsoft.framework.panelium.chrome.ChromeOs

/**
 * Builds the small glyph node placed inside each caption button. The geometry mirrors the native
 * window buttons of the target OS as closely as a vector drawing can:
 *
 * * [ChromeOs.WINDOWS] / [ChromeOs.OTHER] - thin 1px strokes on a 10x10 box (Segoe Fluent style):
 *   a baseline for minimize, a hollow square for maximize, two offset squares for restore, an X.
 * * [ChromeOs.MAC] - the tiny dark glyph revealed on hover of the traffic lights, kept well inside
 *   the 12px dot ([MAC_BOX]): a minus, an X and the two corner triangles of the zoom button.
 *   Hidden by default via the stylesheet. Nudged by [MAC_NUDGE_X] px so it sits optically centred
 *   in the circle.
 * * [ChromeOs.LINUX] - the GNOME/Adwaita symbolic shapes: a rounded bottom bar, a rounded square,
 *   offset rounded squares and a round-capped X.
 *
 * Each glyph is returned inside a fixed square [StackPane] so the button centres a symmetric box
 * regardless of the drawing's own stroke overhang. The wrapper carries the `chrome-button-glyph`
 * style class; the inner shapes carry `chrome-button-glyph-stroke` / `-fill`, so
 * `chrome-caption-buttons.css` (and, later, IP-06) controls colour, opacity and stroke width.
 */
internal object CaptionButtonSymbols {

    private const val BOX: Double = 10.0

    /** Extent of the macOS glyphs; ~6px keeps them a few pixels clear of the 12px circle edge. */
    private const val MAC_BOX: Double = 6.0

    /** Optical-centre correction for the macOS glyphs inside their circle. */
    private const val MAC_NUDGE_X: Double = 0.5

    fun minimize(os: ChromeOs): Node = when (os) {
        ChromeOs.MAC -> macGlyph(
            line(0.0, MAC_BOX / 2.0, MAC_BOX, MAC_BOX / 2.0, cap = StrokeLineCap.ROUND, width = 1.25),
        )
        ChromeOs.LINUX -> framed(BOX, bar(0.0, 7.0, BOX, 1.6))
        else -> framed(BOX, line(0.0, 5.5, BOX, 5.5))
    }

    fun maximize(os: ChromeOs): Node = when (os) {
        ChromeOs.MAC -> {
            val leg = MAC_BOX * 0.55
            macGlyph(
                triangle(0.0, 0.0, leg, 0.0, 0.0, leg),
                triangle(MAC_BOX, MAC_BOX, MAC_BOX - leg, MAC_BOX, MAC_BOX, MAC_BOX - leg),
            )
        }
        ChromeOs.LINUX -> framed(BOX, square(0.5, 0.5, BOX - 1.0, arc = 2.4, width = 1.4))
        else -> framed(BOX, square(0.5, 0.5, BOX - 1.0))
    }

    fun restore(os: ChromeOs): Node = when (os) {
        ChromeOs.MAC -> maximize(os)
        ChromeOs.LINUX -> framed(
            BOX,
            square(0.5, 2.5, BOX - 3.0, arc = 2.0, width = 1.4),
            square(2.5, 0.5, BOX - 3.0, arc = 2.0, width = 1.4),
        )
        else -> framed(
            BOX,
            square(0.5, 2.5, BOX - 3.0),
            square(2.5, 0.5, BOX - 3.0),
        )
    }

    fun close(os: ChromeOs): Node = when (os) {
        ChromeOs.MAC -> macGlyph(
            line(0.0, 0.0, MAC_BOX, MAC_BOX, cap = StrokeLineCap.ROUND, width = 1.25),
            line(0.0, MAC_BOX, MAC_BOX, 0.0, cap = StrokeLineCap.ROUND, width = 1.25),
        )
        ChromeOs.LINUX -> framed(
            BOX,
            line(1.0, 1.0, BOX - 1.0, BOX - 1.0, cap = StrokeLineCap.ROUND, width = 1.4),
            line(1.0, BOX - 1.0, BOX - 1.0, 1.0, cap = StrokeLineCap.ROUND, width = 1.4),
        )
        else -> framed(
            BOX,
            line(0.0, 0.0, BOX, BOX),
            line(0.0, BOX, BOX, 0.0),
        )
    }

    /** The green macOS button reveals the same corner triangles; elsewhere it is unused. */
    fun zoom(os: ChromeOs): Node = maximize(os)

    private fun macGlyph(vararg parts: Shape): Node = framed(MAC_BOX, *parts).apply {
        translateX = MAC_NUDGE_X
    }

    private fun framed(box: Double, vararg parts: Shape): Node = StackPane(Group(*parts)).apply {
        styleClass.add("chrome-button-glyph")
        isMouseTransparent = true
        setMinSize(box, box)
        setPrefSize(box, box)
        setMaxSize(box, box)
    }

    private fun line(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        cap: StrokeLineCap = StrokeLineCap.BUTT,
        width: Double = 1.0,
    ): Line = Line(x1, y1, x2, y2).apply {
        stroke = DEFAULT_INK
        strokeWidth = width
        strokeLineCap = cap
        styleClass.add("chrome-button-glyph-stroke")
    }

    private fun square(
        x: Double,
        y: Double,
        size: Double,
        arc: Double = 0.0,
        width: Double = 1.0,
    ): Rectangle = Rectangle(x, y, size, size).apply {
        fill = Color.TRANSPARENT
        stroke = DEFAULT_INK
        strokeWidth = width
        arcWidth = arc
        arcHeight = arc
        styleClass.add("chrome-button-glyph-stroke")
    }

    private fun bar(x: Double, y: Double, w: Double, h: Double): Rectangle =
        Rectangle(x, y, w, h).apply {
            fill = DEFAULT_INK
            arcWidth = h
            arcHeight = h
            styleClass.add("chrome-button-glyph-fill")
        }

    private fun triangle(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        x3: Double,
        y3: Double,
    ): Polygon = Polygon(x1, y1, x2, y2, x3, y3).apply {
        fill = DEFAULT_INK
        styleClass.add("chrome-button-glyph-fill")
    }

    private val DEFAULT_INK: Color = Color.web("#1a1a1a")
}
