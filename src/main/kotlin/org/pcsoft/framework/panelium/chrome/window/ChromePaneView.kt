package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [ChromePaneViewModel]: the shadow root holding the framed box. The box stacks the content
 * host (whose center follows the content property and whose top inset matches the caption height),
 * the caption backdrop layer (a blurred mirror used for the glass caption look) and the
 * [ChromeCaptionBar] itself. The surrounding [ChromePane] positions this tree and drives the
 * surface, border, effect and backdrop.
 */
internal class ChromePaneView : FxmlView<ChromePaneViewModel>, Initializable {

    @FXML
    internal lateinit var shadowRoot: StackPane

    @FXML
    internal lateinit var frameBox: StackPane

    @FXML
    internal lateinit var contentHost: BorderPane

    @FXML
    internal lateinit var captionBackdrop: Region

    @FXML
    internal lateinit var captionBar: ChromeCaptionBar

    @InjectViewModel
    private lateinit var viewModel: ChromePaneViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        contentHost.centerProperty().bind(viewModel.content)

        // Both overlays span the full width but keep their own (caption) height.
        for (overlay in listOf(captionBar, captionBackdrop)) {
            StackPane.setAlignment(overlay, Pos.TOP_LEFT)
            overlay.maxWidth = Double.MAX_VALUE
            overlay.maxHeight = Region.USE_PREF_SIZE
        }

        captionBackdrop.isMouseTransparent = true
        captionBackdrop.isVisible = false
        captionBackdrop.prefHeightProperty().bind(captionBar.heightProperty())

        // Keep the content clear of the caption band; with an opaque caption this reproduces the
        // former BorderPane(top = caption) layout exactly.
        val syncInset = { contentHost.padding = Insets(captionBar.height, 0.0, 0.0, 0.0) }
        syncInset()
        captionBar.heightProperty().addListener { _, _, _ -> syncInset() }
    }
}
