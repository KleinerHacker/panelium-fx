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
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.MenuChromePane

/**
 * Standalone showcase window for [org.pcsoft.framework.panelium.menupane.FXMenuPane]. The whole
 * window content is declared in `MenuPaneShowcaseWindow.fxml` and wired by
 * [MenuPaneShowcaseWindowController]; this class only bootstraps the [Stage]. The FXML root is a
 * [MenuChromePane], the `ChromePane` subclass that docks the `FXMenuPane` below the caption bar and
 * hosts the file-tab backstage overlay, mirroring [ChromeDemoApp].
 */
class MenuPaneShowcaseApp : Application() {

    override fun start(primaryStage: Stage) {
        val stage = Stage()
        stage.title = "PaneliumFX FXMenuPane Showcase"
        stage.width = 720.0
        stage.height = 420.0
        stage.minWidth = 480.0
        stage.minHeight = 320.0

        val pane = FXMLLoader.load<MenuChromePane>(
            MenuPaneShowcaseApp::class.java.getResource("MenuPaneShowcaseWindow.fxml"),
        )
        stage.scene = Scene(pane).apply { fill = Color.TRANSPARENT }
        stage.initStyle(StageStyle.TRANSPARENT)
        pane.attachStage(stage)

        stage.show()
    }
}

fun main() {
    Application.launch(MenuPaneShowcaseApp::class.java)
}
