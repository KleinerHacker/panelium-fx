package org.pcsoft.framework.panelium.chrome

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseEvent
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
import org.pcsoft.framework.panelium.chrome.internal.ChromeConfig
import org.pcsoft.framework.panelium.chrome.internal.ResizeOverlay
import org.pcsoft.framework.panelium.chrome.internal.WindowOps

/**
 * Undecorated window frame: shadow, border and a caption placeholder around the actual content.
 * When installed on a [Stage] (via `PaneliumChrome.install`, `PaneliumStage` or
 * [attachStage]) the frame also drives window move, edge/corner resize, minimize and
 * maximize/restore, and drops its shadow and rounded corners while maximized or full screen.
 */
public class ChromePane : Region {

    private val shadowRoot: StackPane = StackPane()
    private val frameBox: BorderPane = BorderPane()
    private val resizeOverlay: ResizeOverlay = ResizeOverlay()
    private val captionPlaceholder: Region = Region()

    private val contentProperty: ObjectProperty<Node?> = SimpleObjectProperty(this, "content")

    private val shadowEnabledProperty: BooleanProperty =
        object : SimpleBooleanProperty(this, "shadowEnabled", true) {
            override fun invalidated() {
                updateWindowState()
            }
        }

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
        captionPlaceholder.minHeight = CAPTION_PLACEHOLDER_HEIGHT
        captionPlaceholder.prefHeight = CAPTION_PLACEHOLDER_HEIGHT
        captionPlaceholder.isPickOnBounds = true

        frameBox.top = captionPlaceholder
        frameBox.centerProperty().bind(contentProperty)

        shadowRoot.children.add(frameBox)
        shadowRoot.effect = dropShadow

        children.addAll(shadowRoot, resizeOverlay)

        applySurface(ChromeConfig.CORNER_RADIUS)
    }

    public constructor(content: Node) : this() {
        contentProperty.set(content)
    }

    public fun contentProperty(): ObjectProperty<Node?> = contentProperty

    public var content: Node?
        get() = contentProperty.get()
        set(value) = contentProperty.set(value)

    /**
     * Whether the drop shadow and its outer insets are rendered. Defaults to `true`. The shadow
     * is always suppressed while the window is maximized or full screen.
     */
    public fun shadowEnabledProperty(): BooleanProperty = shadowEnabledProperty

    public var isShadowEnabled: Boolean
        get() = shadowEnabledProperty.get()
        set(value) = shadowEnabledProperty.set(value)

    /**
     * Binds this pane to [stage]: creates the [WindowOps] service, activates the resize zones and
     * tracks the maximized / full-screen state. Called by the internal entry points; safe to call
     * once for a manually built [Stage].
     */
    public fun attachStage(stage: Stage) {
        boundStage = stage
        val ops = WindowOps(stage)
        windowOps = ops

        resizeOverlay.attach(stage, ops)

        captionPlaceholder.addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
            ops.startMove(event.screenX, event.screenY)
        }
        captionPlaceholder.addEventHandler(MouseEvent.MOUSE_DRAGGED) { event ->
            ops.moveTo(event.screenX, event.screenY)
        }

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

        captionPlaceholder.isVisible = !fullScreen
        captionPlaceholder.isManaged = !fullScreen

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
        const val CAPTION_PLACEHOLDER_HEIGHT: Double = 32.0
        val BORDER_COLOR: Color = Color.rgb(0, 0, 0, 0.25)
    }
}
