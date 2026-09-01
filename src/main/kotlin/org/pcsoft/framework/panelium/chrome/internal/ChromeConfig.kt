package org.pcsoft.framework.panelium.chrome.internal

import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.ChromePane

internal object ChromeConfig {

    const val SHADOW_INSET: Double = 12.0

    fun apply(stage: Stage, chromePane: ChromePane) {
        check(!stage.isShowing) { "PaneliumChrome must be applied before the stage is shown." }

        stage.initStyle(StageStyle.TRANSPARENT)
        val scene = Scene(chromePane)
        scene.fill = Color.TRANSPARENT
        stage.scene = scene
    }
}
