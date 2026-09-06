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
import javafx.fxml.Initializable
import javafx.scene.control.ComboBox
import org.pcsoft.framework.panelium.chrome.ChromeOs
import org.pcsoft.framework.panelium.chrome.ChromePane
import java.net.URL
import java.util.ResourceBundle

/** Controller for `ChromeDemoWindow.fxml`; wires the caption OS selector and the included pages. */
class ChromeDemoWindowController : Initializable {

    @FXML
    private lateinit var chromePane: ChromePane

    @FXML
    private lateinit var osSelector: ComboBox<ChromeOs>

    @FXML
    private lateinit var chromeOptionsPageController: ChromeOptionsPageController

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        osSelector.value = chromePane.captionOs
        chromePane.captionOsProperty().bindBidirectional(osSelector.valueProperty())

        chromeOptionsPageController.chromePane = chromePane
    }
}
