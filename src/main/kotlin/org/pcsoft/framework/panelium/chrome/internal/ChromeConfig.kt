package org.pcsoft.framework.panelium.chrome.internal

import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.ChromePane

internal object ChromeConfig {

    const val SHADOW_INSET: Double = 12.0
    const val RESIZE_BORDER: Double = 6.0
    const val CORNER_RADIUS: Double = 8.0
    const val CAPTION_MIN_HEIGHT: Double = 32.0
    const val SHADOW_RADIUS: Double = 18.0
    const val SHADOW_OFFSET_Y: Double = 3.0

    val SHADOW_COLOR: Color = Color.rgb(0, 0, 0, 0.45)
    val SURFACE_COLOR: Color = Color.WHITE

    fun apply(stage: Stage, chromePane: ChromePane) {
        check(!stage.isShowing) { "PaneliumChrome must be applied before the stage is shown." }

        stage.initStyle(StageStyle.TRANSPARENT)
        val scene = Scene(chromePane)
        scene.fill = Color.TRANSPARENT
        stage.scene = scene

        chromePane.attachStage(stage)
    }
}
