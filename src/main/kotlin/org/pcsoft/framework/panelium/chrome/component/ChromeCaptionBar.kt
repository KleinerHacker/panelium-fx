package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.internal.WindowOps

/**
 * Composable caption area of a [ChromePane]. Exposes three content slots ([leftItems],
 * [centerItems], [rightItems]) plus a default title and icon that follow the owning `Stage` and
 * can be switched off. Usable from FXML through the `<fx:root>` pattern.
 *
 * The caption background drags the window; interactive controls in the slots pass their clicks
 * through. Use [setDragRegion] to force a filled strip to drag anyway (or to opt a node out).
 *
 * The min / max-restore / close buttons are added by [installCaptionButtons] once a stage is
 * attached; their side, order and native look follow [captionOsProperty].
 *
 * Style classes: `chrome-caption-bar` on the bar itself, `chrome-caption-left` /
 * `chrome-caption-center` / `chrome-caption-right` on the three slots.
 */
class ChromeCaptionBar : StackPane() {

    private val viewModel: ChromeCaptionBarViewModel

    private var installedOps: WindowOps? = null
    private var installedStage: Stage? = null
    private var buttons: ChromeCaptionButtons? = null

    init {
        val tuple = FluentViewLoader.fxmlView(ChromeCaptionBarView::class.java)
            .root(this)
            .load()
        viewModel = tuple.viewModel

        styleClass.add("chrome-caption-bar")

        viewModel.captionOs.addListener { _, _, _ -> rebuildCaptionButtons() }
    }

    /** Nodes shown at the leading edge, after the default icon and title. */
    val leftItems: ObservableList<Node> get() = viewModel.leftItems

    /** Nodes shown in the horizontally growing center region. */
    val centerItems: ObservableList<Node> get() = viewModel.centerItems

    /** Nodes shown at the trailing edge, before the caption buttons. */
    val rightItems: ObservableList<Node> get() = viewModel.rightItems

    /** The default title text; follows `Stage.title` once the bar is attached to a stage. */
    fun titleTextProperty(): ReadOnlyStringProperty = viewModel.titleText

    fun defaultTitleVisibleProperty(): BooleanProperty = viewModel.defaultTitleVisible

    var isDefaultTitleVisible: Boolean
        get() = viewModel.defaultTitleVisible.get()
        set(value) = viewModel.defaultTitleVisible.set(value)

    fun defaultIconVisibleProperty(): BooleanProperty = viewModel.defaultIconVisible

    var isDefaultIconVisible: Boolean
        get() = viewModel.defaultIconVisible.get()
        set(value) = viewModel.defaultIconVisible.set(value)

    /** The OS whose native caption button placement and look the bar follows. */
    fun captionOsProperty(): ObjectProperty<ChromeOs> = viewModel.captionOs

    var captionOs: ChromeOs
        get() = viewModel.captionOs.get()
        set(value) = viewModel.captionOs.set(value)

    /** Minimum caption height; set by [ChromePane] from its `-panelium-caption-min-height`. */
    internal var captionMinHeight: Double
        get() = viewModel.captionMinHeight.get()
        set(value) = viewModel.captionMinHeight.set(value)

    internal fun bindStage(stage: Stage) {
        viewModel.bindStage(stage)
    }

    internal var captionButtonSlot: Node?
        get() = viewModel.captionButtonSlot.get()
        set(value) = viewModel.captionButtonSlot.set(value)

    /** Builds the OS-specific button set, wires it to [ops] / [stage] and drops it into the slot. */
    internal fun installCaptionButtons(ops: WindowOps, stage: Stage) {
        installedOps = ops
        installedStage = stage

        stage.maximizedProperty().addListener { _, _, maximized ->
            buttons?.setMaximized(maximized)
        }

        rebuildCaptionButtons()
    }

    private fun rebuildCaptionButtons() {
        val ops = installedOps ?: return
        val stage = installedStage ?: return

        val set = ChromeCaptionButtons(viewModel.captionOs.get())
        set.minimizeButton.setOnAction { ops.minimize() }
        set.maxRestoreButton.setOnAction { ops.toggleMaximize() }
        set.closeButton.setOnAction { ops.close() }
        set.maxRestoreButton.disableProperty().bind(stage.resizableProperty().not())
        set.setMaximized(stage.isMaximized)

        buttons = set
        captionButtonSlot = set
    }

    companion object {

        private const val DRAG_REGION_KEY: String = "org.pcsoft.framework.panelium.chrome.dragRegion"

        /**
         * Marks [node] as an explicit caption drag region (`true`, drags the window even when the
         * node is interactive), an explicit passthrough (`false`) or clears the mark (`null`, the
         * default - the hit test then falls back to its interactivity heuristic). The flag is
         * resolved from the picked node upwards; the first node carrying one decides.
         */
        fun setDragRegion(node: Node, value: Boolean?) {
            if (value == null) {
                node.properties.remove(DRAG_REGION_KEY)
            } else {
                node.properties[DRAG_REGION_KEY] = value
            }
        }

        /** Returns the explicit drag-region flag set via [setDragRegion], or `null` when unset. */
        fun getDragRegion(node: Node): Boolean? = node.properties[DRAG_REGION_KEY] as? Boolean
    }
}
