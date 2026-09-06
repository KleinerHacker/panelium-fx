package org.pcsoft.framework.panelium.menupane

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.StringProperty
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.StackPane

/**
 * A titled group of action controls shown in a regular [FXMenuTab]'s group strip. [title] labels the
 * group; [content] holds the arbitrary control nodes it arranges. Register groups on a tab through
 * [FXMenuTab.groups]; the currently active regular tab's groups are the ones rendered by [FXMenuPane].
 * Usable from FXML through the `<fx:root>` pattern.
 *
 * Disable the whole group through the inherited [setDisable] / [disableProperty]: JavaFX propagates
 * the disabled state to every node in [content] and applies the `:disabled` pseudo-class to the
 * group. Individual controls can still be disabled the standard JavaFX way, independent of this
 * group-level state.
 *
 * Style classes: `menu-group` on the component itself, `menu-group-content` on the control row,
 * `menu-group-title` on the caption label.
 */
class FXMenuGroup : StackPane() {

    private val viewModel: FXMenuGroupViewModel

    init {
        val tuple = FluentViewLoader.fxmlView(FXMenuGroupView::class.java)
            .root(this)
            .load()
        viewModel = tuple.viewModel

        styleClass.add("menu-group")
    }

    /** The group caption shown below its controls. */
    fun titleProperty(): StringProperty = viewModel.title

    var title: String
        get() = viewModel.title.get()
        set(value) = viewModel.title.set(value)

    /** The ordered control nodes the group arranges. */
    val content: ObservableList<Node> get() = viewModel.content
}
