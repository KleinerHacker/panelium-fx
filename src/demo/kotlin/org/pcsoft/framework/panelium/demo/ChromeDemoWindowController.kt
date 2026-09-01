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
