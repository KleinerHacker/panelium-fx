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

package org.pcsoft.framework.panelium.demo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.ChromePane

/**
 * Showcase window for [org.pcsoft.framework.panelium.chrome.PaneliumStage]. The whole window
 * content — caption slots, tool bar, navigation tabs, pages and status bar — is declared in
 * `ChromeDemoWindow.fxml` and the FXML files under `pages`; this class only bootstraps the [Stage].
 */
class ChromeDemoApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = Stage()
        stage.title = "PaneliumFX Chrome Demo"
        stage.icons.setAll(
            appIcon(16), appIcon(32), appIcon(48), appIcon(64), appIcon(128),
        )
        stage.width = 960.0
        stage.height = 640.0
        stage.minWidth = 640.0
        stage.minHeight = 440.0

        val pane = FXMLLoader.load<ChromePane>(
            ChromeDemoApp::class.java.getResource("ChromeDemoWindow.fxml"),
        )
        stage.scene = Scene(pane).apply { fill = Color.TRANSPARENT }
        stage.initStyle(StageStyle.TRANSPARENT)
        pane.attachStage(stage)

        stage.show()
    }

    private fun appIcon(size: Int): Image =
        Image(
            ChromeDemoApp::class.java.getResourceAsStream("icons/app-$size.png")
                ?: error("missing demo resource: icons/app-$size.png"),
        )
}

fun main() {
    Application.launch(ChromeDemoApp::class.java)
}
