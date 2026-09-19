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

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ToggleButton
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.pcsoft.framework.panelium.chrome.ChromeBorderMode
import org.pcsoft.framework.panelium.chrome.ChromeCaptionTitlePosition
import org.pcsoft.framework.panelium.chrome.ChromePane
import org.pcsoft.framework.panelium.chrome.PaneliumChrome

/** Controller for the "Chrome options" tab; wires the toggles to the [chromePane] of the demo window. */
class ChromeOptionsPageController {

    @FXML
    private lateinit var titleToggle: ToggleButton

    @FXML
    private lateinit var iconToggle: ToggleButton

    @FXML
    private lateinit var overrideToggle: ToggleButton

    @FXML
    private lateinit var signatureToggle: ToggleButton

    @FXML
    private lateinit var borderToggle: ToggleButton

    @FXML
    private lateinit var titlePositionToggle: ToggleButton

    @FXML
    private lateinit var fxmlButton: Button

    @FXML
    private lateinit var installButton: Button

    var chromePane: ChromePane? = null
        set(value) {
            field = value
            value?.let(::bindChromePane)
        }

    private fun bindChromePane(chromePane: ChromePane) {
        titleToggle.selectedProperty().bindBidirectional(chromePane.defaultTitleVisibleProperty())
        iconToggle.selectedProperty().bindBidirectional(chromePane.defaultIconVisibleProperty())

        borderToggle.selectedProperty().addListener { _, _, on ->
            chromePane.borderMode = if (on) ChromeBorderMode.RAISED else ChromeBorderMode.FLAT
        }

        titlePositionToggle.selectedProperty().addListener { _, _, on ->
            chromePane.captionTitlePosition =
                if (on) ChromeCaptionTitlePosition.AFTER_LEFT_ITEMS else ChromeCaptionTitlePosition.NEXT_TO_LOGO
        }

        val overrideSheet = ChromeOptionsPageController::class.java.getResource("chrome-override.css")!!.toExternalForm()
        overrideToggle.selectedProperty().addListener { _, _, on ->
            val sheets = overrideToggle.scene?.stylesheets ?: return@addListener
            if (on) sheets.add(overrideSheet) else sheets.remove(overrideSheet)
        }

        val signatureSheet = ChromeOptionsPageController::class.java.getResource("chrome-signature.css")!!.toExternalForm()
        signatureToggle.selectedProperty().addListener { _, _, on ->
            val sheets = signatureToggle.scene?.stylesheets ?: return@addListener
            if (on) sheets.add(signatureSheet) else sheets.remove(signatureSheet)
        }
    }

    @FXML
    private fun openFxmlWindow() {
        val stage = Stage()
        stage.title = "FXML Chrome Window"
        stage.icons.setAll(appIcon(32), appIcon(64))
        stage.width = 460.0
        stage.height = 260.0

        val pane = FXMLLoader.load<ChromePane>(
            ChromeOptionsPageController::class.java.getResource("FxmlChromeWindow.fxml"),
        )
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.scene = Scene(pane).apply { fill = Color.TRANSPARENT }
        pane.attachStage(stage)
        stage.show()
    }

    @FXML
    private fun openInstalledWindow() {
        val stage = Stage()
        stage.icons.setAll(appIcon(32), appIcon(64))
        stage.width = 420.0
        stage.height = 240.0

        val root = FXMLLoader.load<javafx.scene.Parent>(
            ChromeOptionsPageController::class.java.getResource("pages/InstalledWindowContent.fxml"),
        )
        stage.scene = Scene(root)

        PaneliumChrome.install(stage)
        stage.show()
    }

    private fun appIcon(size: Int) =
        javafx.scene.image.Image(
            ChromeOptionsPageController::class.java.getResourceAsStream("icons/app-$size.png")
                ?: error("missing demo resource: icons/app-$size.png"),
        )
}
