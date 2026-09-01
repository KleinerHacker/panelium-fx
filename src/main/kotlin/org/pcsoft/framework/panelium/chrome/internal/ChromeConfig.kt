package org.pcsoft.framework.panelium.chrome.internal

import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.ChromeOs
import org.pcsoft.framework.panelium.chrome.ChromePane

internal object ChromeConfig {

    /** Transparent gutter reserved around the frame for the drop shadow. */
    const val SHADOW_INSET: Double = 12.0

    /** Vertical offset of the drop shadow; radius and colour are styleable on [ChromePane]. */
    const val SHADOW_OFFSET_Y: Double = 3.0

    val SURFACE_COLOR: Color = Color.WHITE

    /**
     * Per-OS frame geometry: corner radius, drop-shadow radius / colour, border colour and whether
     * a shadow is drawn at all. Picked up by [ChromePane] whenever `captionOs` changes so the
     * window form follows the selected platform. Explicit CSS on the pane still wins.
     */
    data class FrameMetrics(
        val cornerRadius: Double,
        val shadowRadius: Double,
        val shadowColor: Color,
        val borderColor: Color,
        val shadowEnabled: Boolean,
    )

    fun frameMetrics(os: ChromeOs): FrameMetrics = when (os) {
        ChromeOs.WINDOWS -> FrameMetrics(
            cornerRadius = 8.0,
            shadowRadius = 18.0,
            shadowColor = Color.rgb(0, 0, 0, 0.45),
            borderColor = Color.rgb(0, 0, 0, 0.25),
            shadowEnabled = true,
        )
        ChromeOs.MAC -> FrameMetrics(
            cornerRadius = 10.0,
            shadowRadius = 34.0,
            shadowColor = Color.rgb(0, 0, 0, 0.35),
            borderColor = Color.rgb(0, 0, 0, 0.16),
            shadowEnabled = true,
        )
        ChromeOs.LINUX -> FrameMetrics(
            cornerRadius = 12.0,
            shadowRadius = 14.0,
            shadowColor = Color.rgb(0, 0, 0, 0.40),
            borderColor = Color.rgb(0, 0, 0, 0.22),
            shadowEnabled = true,
        )
        ChromeOs.OTHER -> FrameMetrics(
            cornerRadius = 0.0,
            shadowRadius = 0.0,
            shadowColor = Color.TRANSPARENT,
            borderColor = Color.rgb(0, 0, 0, 0.35),
            shadowEnabled = false,
        )
    }

    fun apply(stage: Stage, chromePane: ChromePane) {
        check(!stage.isShowing) { "PaneliumChrome must be applied before the stage is shown." }

        stage.initStyle(StageStyle.TRANSPARENT)
        val scene = Scene(chromePane)
        scene.fill = Color.TRANSPARENT
        stage.scene = scene

        chromePane.attachStage(stage)
    }
}
