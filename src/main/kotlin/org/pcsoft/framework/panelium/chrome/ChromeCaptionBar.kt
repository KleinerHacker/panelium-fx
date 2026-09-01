package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.stage.Stage

/**
 * Composable caption area of a [ChromePane]. Exposes three content slots ([leftItems],
 * [centerItems], [rightItems]) plus a default title and icon that follow the owning `Stage` and
 * can be switched off. Usable from FXML through the `<fx:root>` pattern.
 *
 * The caption background drags the window; interactive controls in the slots pass their clicks
 * through. Use [setDragRegion] to force a filled strip to drag anyway (or to opt a node out).
 */
public class ChromeCaptionBar : StackPane() {

    private val viewModel: ChromeCaptionBarViewModel

    init {
        val tuple = FluentViewLoader.fxmlView(ChromeCaptionBarView::class.java)
            .root(this)
            .load()
        viewModel = tuple.viewModel
    }

    /** Nodes shown at the leading edge, after the default icon and title. */
    public val leftItems: ObservableList<Node> get() = viewModel.leftItems

    /** Nodes shown in the horizontally growing center region. */
    public val centerItems: ObservableList<Node> get() = viewModel.centerItems

    /** Nodes shown at the trailing edge, before the caption buttons. */
    public val rightItems: ObservableList<Node> get() = viewModel.rightItems

    /** The default title text; follows `Stage.title` once the bar is attached to a stage. */
    public fun titleTextProperty(): ReadOnlyStringProperty = viewModel.titleText

    public fun defaultTitleVisibleProperty(): BooleanProperty = viewModel.defaultTitleVisible

    public var isDefaultTitleVisible: Boolean
        get() = viewModel.defaultTitleVisible.get()
        set(value) = viewModel.defaultTitleVisible.set(value)

    public fun defaultIconVisibleProperty(): BooleanProperty = viewModel.defaultIconVisible

    public var isDefaultIconVisible: Boolean
        get() = viewModel.defaultIconVisible.get()
        set(value) = viewModel.defaultIconVisible.set(value)

    internal fun bindStage(stage: Stage) {
        viewModel.bindStage(stage)
    }

    internal var captionButtonSlot: Node?
        get() = viewModel.captionButtonSlot.get()
        set(value) = viewModel.captionButtonSlot.set(value)

    public companion object {

        private const val DRAG_REGION_KEY: String = "org.pcsoft.framework.panelium.chrome.dragRegion"

        /**
         * Marks [node] as an explicit caption drag region (`true`, drags the window even when the
         * node is interactive), an explicit passthrough (`false`) or clears the mark (`null`, the
         * default - the hit test then falls back to its interactivity heuristic). The flag is
         * resolved from the picked node upwards; the first node carrying one decides.
         */
        public fun setDragRegion(node: Node, value: Boolean?) {
            if (value == null) {
                node.properties.remove(DRAG_REGION_KEY)
            } else {
                node.properties[DRAG_REGION_KEY] = value
            }
        }

        /** Returns the explicit drag-region flag set via [setDragRegion], or `null` when unset. */
        public fun getDragRegion(node: Node): Boolean? = node.properties[DRAG_REGION_KEY] as? Boolean
    }
}
