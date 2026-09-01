package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.DefaultProperty
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.effect.DropShadow
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.Border
import javafx.scene.layout.BorderPane
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.internal.CaptionDragHandler
import org.pcsoft.framework.panelium.chrome.internal.ChromeConfig
import org.pcsoft.framework.panelium.chrome.internal.ResizeOverlay
import org.pcsoft.framework.panelium.chrome.internal.WindowOps

/**
 * Undecorated window frame: shadow, border and a composable [ChromeCaptionBar] around the actual
 * content. When installed on a [Stage] (via `PaneliumChrome.install`, `PaneliumStage` or
 * [attachStage]) the frame also drives window move, edge/corner resize, minimize and
 * maximize/restore, and drops its shadow and rounded corners while maximized or full screen.
 *
 * Instantiable from FXML; `content` is the default property, so a single child element becomes the
 * framed content.
 */
@DefaultProperty("content")
public class ChromePane : Region {

    private val viewModel: ChromePaneViewModel
    private val shadowRoot: StackPane
    private val frameBox: BorderPane
    private val resizeOverlay: ResizeOverlay = ResizeOverlay()

    /** The composable caption area at the top of the frame. */
    public val captionBar: ChromeCaptionBar

    private val dropShadow: DropShadow = DropShadow().apply {
        radius = ChromeConfig.SHADOW_RADIUS
        offsetY = ChromeConfig.SHADOW_OFFSET_Y
        color = ChromeConfig.SHADOW_COLOR
    }

    private var boundStage: Stage? = null
    private var shadowInset: Double = ChromeConfig.SHADOW_INSET

    internal var windowOps: WindowOps? = null
        private set

    public constructor() {
        val tuple = FluentViewLoader.fxmlView(ChromePaneView::class.java).load()
        val codeBehind = tuple.codeBehind
        viewModel = tuple.viewModel
        shadowRoot = codeBehind.shadowRoot
        frameBox = codeBehind.frameBox
        captionBar = codeBehind.captionBar

        shadowRoot.effect = dropShadow
        children.addAll(shadowRoot, resizeOverlay)

        viewModel.shadowEnabled.addListener { _, _, _ -> updateWindowState() }

        applySurface(ChromeConfig.CORNER_RADIUS)
    }

    public constructor(content: Node) : this() {
        viewModel.content.set(content)
    }

    public fun contentProperty(): ObjectProperty<Node?> = viewModel.content

    public var content: Node?
        get() = viewModel.content.get()
        set(value) = viewModel.content.set(value)

    /**
     * Whether the drop shadow and its outer insets are rendered. Defaults to `true`. The shadow
     * is always suppressed while the window is maximized or full screen.
     */
    public fun shadowEnabledProperty(): BooleanProperty = viewModel.shadowEnabled

    public var isShadowEnabled: Boolean
        get() = viewModel.shadowEnabled.get()
        set(value) = viewModel.shadowEnabled.set(value)

    /** Nodes in the caption's leading slot, after the default icon and title. */
    public val captionLeftItems: ObservableList<Node> get() = captionBar.leftItems

    /** Nodes in the caption's growing center slot. */
    public val captionCenterItems: ObservableList<Node> get() = captionBar.centerItems

    /** Nodes in the caption's trailing slot, before the caption buttons. */
    public val captionRightItems: ObservableList<Node> get() = captionBar.rightItems

    public fun defaultTitleVisibleProperty(): BooleanProperty = captionBar.defaultTitleVisibleProperty()

    public var isDefaultTitleVisible: Boolean
        get() = captionBar.isDefaultTitleVisible
        set(value) {
            captionBar.isDefaultTitleVisible = value
        }

    public fun defaultIconVisibleProperty(): BooleanProperty = captionBar.defaultIconVisibleProperty()

    public var isDefaultIconVisible: Boolean
        get() = captionBar.isDefaultIconVisible
        set(value) {
            captionBar.isDefaultIconVisible = value
        }

    /** The default caption title; follows `Stage.title` once a stage is attached. */
    public fun captionTitleProperty(): ReadOnlyStringProperty = captionBar.titleTextProperty()

    /**
     * Binds this pane to [stage]: creates the [WindowOps] service, activates the resize zones,
     * routes caption drags to a window move, binds the default title / icon and tracks the
     * maximized / full-screen state. Called by the internal entry points; safe to call once for a
     * manually built [Stage].
     */
    public fun attachStage(stage: Stage) {
        boundStage = stage
        val ops = WindowOps(stage)
        windowOps = ops

        resizeOverlay.attach(stage, ops)

        CaptionDragHandler(captionBar, ops, stage).install()
        captionBar.bindStage(stage)

        stage.maximizedProperty().addListener { _, _, _ -> updateWindowState() }
        stage.fullScreenProperty().addListener { _, _, _ -> updateWindowState() }

        updateWindowState()
    }

    private fun updateWindowState() {
        val stage = boundStage
        val collapsed = stage != null && (stage.isMaximized || stage.isFullScreen)
        val fullScreen = stage != null && stage.isFullScreen
        val shadowOn = isShadowEnabled && !collapsed

        shadowInset = if (shadowOn) ChromeConfig.SHADOW_INSET else 0.0
        shadowRoot.effect = if (shadowOn) dropShadow else null
        applySurface(if (collapsed) 0.0 else ChromeConfig.CORNER_RADIUS)

        captionBar.isVisible = !fullScreen
        captionBar.isManaged = !fullScreen

        requestLayout()
    }

    private fun applySurface(radius: Double) {
        val corners = CornerRadii(radius)
        frameBox.background = Background(BackgroundFill(ChromeConfig.SURFACE_COLOR, corners, Insets.EMPTY))
        frameBox.border = Border(
            BorderStroke(
                BORDER_COLOR,
                BorderStrokeStyle.SOLID,
                corners,
                BorderWidths(1.0),
            ),
        )
    }

    override fun layoutChildren() {
        val inset = shadowInset
        val innerWidth = (width - 2 * inset).coerceAtLeast(0.0)
        val innerHeight = (height - 2 * inset).coerceAtLeast(0.0)

        shadowRoot.resizeRelocate(inset, inset, innerWidth, innerHeight)
        resizeOverlay.resizeRelocate(0.0, 0.0, width, height)
    }

    private companion object {
        val BORDER_COLOR: Color = Color.rgb(0, 0, 0, 0.25)
    }
}
