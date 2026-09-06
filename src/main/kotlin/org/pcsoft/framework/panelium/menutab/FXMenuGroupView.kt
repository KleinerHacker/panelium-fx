package org.pcsoft.framework.panelium.menutab

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import java.net.URL
import java.util.ResourceBundle

/**
 * Renders [FXMenuGroupViewModel]: an HBox of the group's [FXMenuGroupViewModel.content] nodes with
 * the group [FXMenuGroupViewModel.title] shown as a label below them, following the ribbon
 * convention of a caption under the group body.
 */
internal class FXMenuGroupView : FxmlView<FXMenuGroupViewModel>, Initializable {

    @FXML
    private lateinit var root: StackPane

    @FXML
    private lateinit var groupContent: HBox

    @FXML
    private lateinit var groupTitle: Label

    @InjectViewModel
    private lateinit var viewModel: FXMenuGroupViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        groupTitle.textProperty().bind(viewModel.title)

        groupContent.children.setAll(viewModel.content)
        viewModel.content.addListener(ListChangeListener { groupContent.children.setAll(viewModel.content) })
    }
}
