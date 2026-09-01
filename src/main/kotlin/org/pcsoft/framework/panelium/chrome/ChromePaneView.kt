package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [ChromePaneViewModel]: the shadow root holding the framed box, whose top is the
 * [ChromeCaptionBar] and whose center follows the content property. The surrounding
 * [ChromePane] positions this tree and drives the shadow / surface.
 */
internal class ChromePaneView : FxmlView<ChromePaneViewModel>, Initializable {

    @FXML
    internal lateinit var shadowRoot: StackPane

    @FXML
    internal lateinit var frameBox: BorderPane

    @FXML
    internal lateinit var captionBar: ChromeCaptionBar

    @InjectViewModel
    private lateinit var viewModel: ChromePaneViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        frameBox.centerProperty().bind(viewModel.content)
    }
}
