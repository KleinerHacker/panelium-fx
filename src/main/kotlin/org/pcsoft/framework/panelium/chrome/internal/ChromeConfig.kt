package org.pcsoft.framework.panelium.chrome.internal

import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.ChromeBorderMode
import org.pcsoft.framework.panelium.chrome.ChromeBorderStrokeStyle
import org.pcsoft.framework.panelium.chrome.ChromeOs
import org.pcsoft.framework.panelium.chrome.ChromePane

internal object ChromeConfig {

    /** Default transparent gutter reserved around the frame for the drop shadow / effect. */
    const val DEFAULT_SHADOW_INSET: Double = 12.0

    /** Vertical offset of the built-in default drop shadow (styleable effect aside). */
    const val SHADOW_OFFSET_Y: Double = 3.0

    /**
     * Per-OS frame geometry. Everything here is a *default* that the matching `-panelium-*`
     * styleable property on [ChromePane] overrides; [ChromePane] re-applies these whenever
     * `captionOs` changes so the window form follows the selected platform.
     */
    data class FrameMetrics(
        val cornerRadius: Double,
        val shadowRadius: Double,
        val shadowColor: Color,
        val shadowEnabled: Boolean,
        val shadowInset: Double,
        val surfaceFill: Paint,
        val borderMode: ChromeBorderMode,
        val borderWidth: Double,
        val borderColor: Paint,
        val borderLightPaint: Paint,
        val borderDarkPaint: Paint,
        val borderStrokeStyle: ChromeBorderStrokeStyle,
        val borderLineCap: StrokeLineCap,
        val borderLineJoin: StrokeLineJoin,
        val borderMiterLimit: Double,
        val borderDashOffset: Double,
    )

    private val BEVEL_LIGHT: Color = Color.rgb(255, 255, 255, 0.90)
    private val BEVEL_DARK: Color = Color.rgb(0, 0, 0, 0.40)

    private fun base(
        cornerRadius: Double,
        shadowRadius: Double,
        shadowColor: Color,
        shadowEnabled: Boolean,
        borderColor: Color,
        borderDarkPaint: Paint = BEVEL_DARK,
    ): FrameMetrics = FrameMetrics(
        cornerRadius = cornerRadius,
        shadowRadius = shadowRadius,
        shadowColor = shadowColor,
        shadowEnabled = shadowEnabled,
        shadowInset = DEFAULT_SHADOW_INSET,
        surfaceFill = Color.WHITE,
        borderMode = ChromeBorderMode.FLAT,
        borderWidth = 1.0,
        borderColor = borderColor,
        borderLightPaint = BEVEL_LIGHT,
        borderDarkPaint = borderDarkPaint,
        borderStrokeStyle = ChromeBorderStrokeStyle.SOLID,
        borderLineCap = StrokeLineCap.BUTT,
        borderLineJoin = StrokeLineJoin.MITER,
        borderMiterLimit = 10.0,
        borderDashOffset = 0.0,
    )

    fun frameMetrics(os: ChromeOs): FrameMetrics = when (os) {
        ChromeOs.WINDOWS -> base(
            cornerRadius = 8.0,
            shadowRadius = 18.0,
            shadowColor = Color.rgb(0, 0, 0, 0.45),
            shadowEnabled = true,
            borderColor = Color.rgb(0, 0, 0, 0.25),
        )
        ChromeOs.MAC -> base(
            cornerRadius = 10.0,
            shadowRadius = 34.0,
            shadowColor = Color.rgb(0, 0, 0, 0.35),
            shadowEnabled = true,
            borderColor = Color.rgb(0, 0, 0, 0.16),
            borderDarkPaint = Color.rgb(0, 0, 0, 0.32),
        )
        ChromeOs.LINUX -> base(
            cornerRadius = 12.0,
            shadowRadius = 14.0,
            shadowColor = Color.rgb(0, 0, 0, 0.40),
            shadowEnabled = true,
            borderColor = Color.rgb(0, 0, 0, 0.22),
        )
        ChromeOs.OTHER -> base(
            cornerRadius = 0.0,
            shadowRadius = 0.0,
            shadowColor = Color.TRANSPARENT,
            shadowEnabled = false,
            borderColor = Color.rgb(0, 0, 0, 0.35),
            borderDarkPaint = Color.rgb(0, 0, 0, 0.45),
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
