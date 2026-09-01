package org.pcsoft.framework.panelium.chrome

import javafx.css.PseudoClass
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.layout.HBox
import org.pcsoft.framework.panelium.chrome.internal.CaptionButtonSymbols

/**
 * The min / max-restore / close button set for the caption bar. The [os] passed in fixes both the
 * button order (`WINDOWS` / `LINUX` / `OTHER`: minimize, max-restore, close; `MAC`: close, minimize,
 * zoom) and, via the `chrome-caption-buttons` + OS style class, the native look. The look itself
 * lives in the scene-wide user-agent stylesheet `chrome.css` (see [ChromePane.getUserAgentStylesheet]);
 * actions are wired by [ChromeCaptionBar].
 */
internal class ChromeCaptionButtons(private val os: ChromeOs) : HBox() {

    val minimizeButton: Button = button("minimize", CaptionButtonSymbols.minimize(os))
    val closeButton: Button = button("close", CaptionButtonSymbols.close(os))

    private val maxGlyph: Node =
        if (os == ChromeOs.MAC) CaptionButtonSymbols.zoom(os) else CaptionButtonSymbols.maximize(os)
    private val restoreGlyph: Node = CaptionButtonSymbols.restore(os)

    val maxRestoreButton: Button = button("max-restore", maxGlyph)

    init {
        styleClass.setAll("chrome-caption-buttons", os.styleClass)
        alignment = Pos.CENTER
        isFillHeight = true
        spacing = if (os == ChromeOs.MAC) 8.0 else 0.0
        isPickOnBounds = false

        children.setAll(
            when (os) {
                ChromeOs.MAC -> listOf(closeButton, minimizeButton, maxRestoreButton)
                else -> listOf(minimizeButton, maxRestoreButton, closeButton)
            },
        )
    }

    /** Swaps the max-restore glyph and toggles the `maximized` pseudo-class on that button. */
    fun setMaximized(maximized: Boolean) {
        maxRestoreButton.graphic = if (maximized && os != ChromeOs.MAC) restoreGlyph else maxGlyph
        maxRestoreButton.pseudoClassStateChanged(MAXIMIZED, maximized)
    }

    private fun button(role: String, glyph: Node): Button = Button().apply {
        // `setAll` drops the default `button` style class on purpose (no modena pill background);
        // that also drops modena's `-fx-alignment: CENTER`, so it is set back explicitly here.
        styleClass.setAll("chrome-button", role)
        graphic = glyph
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
        alignment = Pos.CENTER
        isMnemonicParsing = false
        isFocusTraversable = false
        ChromeCaptionBar.setDragRegion(this, false)
    }

    private companion object {
        val MAXIMIZED: PseudoClass = PseudoClass.getPseudoClass("maximized")
    }
}
