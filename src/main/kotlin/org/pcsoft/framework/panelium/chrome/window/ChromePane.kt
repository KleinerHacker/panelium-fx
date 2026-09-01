package org.pcsoft.framework.panelium.chrome

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.DefaultProperty
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.collections.ObservableList
import javafx.css.CssMetaData
import javafx.css.PseudoClass
import javafx.css.SimpleStyleableDoubleProperty
import javafx.css.SimpleStyleableObjectProperty
import javafx.css.StyleConverter
import javafx.css.Styleable
import javafx.css.StyleableDoubleProperty
import javafx.css.StyleableObjectProperty
import javafx.css.StyleableProperty
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
import javafx.scene.shape.Rectangle
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
 *
 * ## Styling
 *
 * The pane carries the `chrome-pane` style class and the `maximized`, `fullscreen`, `active` and
 * `inactive` pseudo-classes (the last two follow `Stage.focused`). Its default look ships as a
 * user-agent stylesheet ([getUserAgentStylesheet]); an application stylesheet added to the `Scene`
 * overrides it through normal CSS precedence. Five styleable properties tune the frame:
 * `-panelium-shadow-radius`, `-panelium-shadow-color`, `-panelium-corner-radius`,
 * `-panelium-resize-border` and `-panelium-caption-min-height`.
 *
 * Corner radius, shadow radius / colour, border colour and shadow visibility also follow
 * [captionOsProperty]; an explicit CSS value for the matching styleable property still wins.
 */
@Suppress("TYPE_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@DefaultProperty("content")
public class ChromePane : Region {

    private val shadowRadius: StyleableDoubleProperty =
        SimpleStyleableDoubleProperty(SHADOW_RADIUS_META, this, "shadowRadius", DEFAULT_SHADOW_RADIUS)

    private val shadowColor: StyleableObjectProperty<Color> =
        SimpleStyleableObjectProperty(SHADOW_COLOR_META, this, "shadowColor", DEFAULT_SHADOW_COLOR)

    private val cornerRadius: StyleableDoubleProperty =
        SimpleStyleableDoubleProperty(CORNER_RADIUS_META, this, "cornerRadius", DEFAULT_CORNER_RADIUS)

    private val resizeBorder: StyleableDoubleProperty =
        SimpleStyleableDoubleProperty(RESIZE_BORDER_META, this, "resizeBorder", DEFAULT_RESIZE_BORDER)

    private val captionMinHeight: StyleableDoubleProperty =
        SimpleStyleableDoubleProperty(
            CAPTION_MIN_HEIGHT_META, this, "captionMinHeight", DEFAULT_CAPTION_MIN_HEIGHT,
        )

    private val viewModel: ChromePaneViewModel
    private val shadowRoot: StackPane
    private val frameBox: BorderPane
    private val resizeOverlay: ResizeOverlay = ResizeOverlay()

    /** Rounded clip on the framed box so the content cannot paint over the bottom corners. */
    private val frameClip: Rectangle = Rectangle()

    /** OS-derived frame geometry; refreshed from [ChromeConfig.frameMetrics] on `captionOs` change. */
    private var osBorderColor: Color = BORDER_COLOR
    private var osShadowEnabled: Boolean = true

    /** The composable caption area at the top of the frame. */
    public val captionBar: ChromeCaptionBar

    private val dropShadow: DropShadow = DropShadow().apply {
        offsetY = ChromeConfig.SHADOW_OFFSET_Y
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

        styleClass.add("chrome-pane")

        shadowRoot.effect = dropShadow
        children.addAll(shadowRoot, resizeOverlay)

        frameBox.clip = frameClip
        frameClip.widthProperty().bind(frameBox.widthProperty())
        frameClip.heightProperty().bind(frameBox.heightProperty())

        dropShadow.radiusProperty().bind(shadowRadius)
        dropShadow.colorProperty().bind(shadowColor)

        resizeOverlay.resizeBorder = resizeBorder.get()
        resizeBorder.addListener { _, _, value -> resizeOverlay.resizeBorder = value.toDouble() }

        captionBar.captionMinHeight = captionMinHeight.get()
        captionMinHeight.addListener { _, _, value -> captionBar.captionMinHeight = value.toDouble() }

        cornerRadius.addListener { _, _, _ -> updateWindowState() }
        shadowColor.addListener { _, _, _ -> updateWindowState() }
        viewModel.shadowEnabled.addListener { _, _, _ -> updateWindowState() }

        captionBar.captionOsProperty().addListener { _, _, os -> applyOsFrame(os) }
        applyOsFrame(captionBar.captionOs)

        updateWindowState()
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
     * The OS whose native caption button placement and look the frame follows. Defaults to the
     * detected OS; override it to force a layout in tests, demos or cross-platform previews.
     */
    public fun captionOsProperty(): ObjectProperty<ChromeOs> = captionBar.captionOsProperty()

    public var captionOs: ChromeOs
        get() = captionBar.captionOs
        set(value) {
            captionBar.captionOs = value
        }

    /**
     * Binds this pane to [stage]: creates the [WindowOps] service, activates the resize zones,
     * routes caption drags to a window move, installs the OS-specific caption buttons, binds the
     * default title / icon and tracks the maximized / full-screen / focus state.
     */
    public fun attachStage(stage: Stage) {
        boundStage = stage
        val ops = WindowOps(stage)
        windowOps = ops

        resizeOverlay.attach(stage, ops)

        CaptionDragHandler(captionBar, ops, stage).install()
        captionBar.bindStage(stage)
        captionBar.installCaptionButtons(ops, stage)

        stage.maximizedProperty().addListener { _, _, _ -> updateWindowState() }
        stage.fullScreenProperty().addListener { _, _, _ -> updateWindowState() }
        stage.focusedProperty().addListener { _, _, _ -> updateWindowState() }

        updateWindowState()
    }

    /** Pushes the [ChromeConfig.frameMetrics] for [os] into the styleable / derived frame geometry. */
    private fun applyOsFrame(os: ChromeOs) {
        val metrics = ChromeConfig.frameMetrics(os)
        if (!cornerRadius.isBound) cornerRadius.value = metrics.cornerRadius
        if (!shadowRadius.isBound) shadowRadius.value = metrics.shadowRadius
        if (!shadowColor.isBound) shadowColor.value = metrics.shadowColor
        osBorderColor = metrics.borderColor
        osShadowEnabled = metrics.shadowEnabled
        updateWindowState()
    }

    private fun updateWindowState() {
        val stage = boundStage
        val collapsed = stage != null && (stage.isMaximized || stage.isFullScreen)
        val fullScreen = stage != null && stage.isFullScreen
        val shadowOn = isShadowEnabled && osShadowEnabled && !collapsed
        val active = stage != null && stage.isFocused

        shadowInset = if (shadowOn) ChromeConfig.SHADOW_INSET else 0.0
        shadowRoot.effect = if (shadowOn) dropShadow else null
        applySurface(if (collapsed) 0.0 else cornerRadius.get())

        captionBar.isVisible = !fullScreen
        captionBar.isManaged = !fullScreen

        pseudoClassStateChanged(MAXIMIZED_CLASS, stage != null && stage.isMaximized)
        pseudoClassStateChanged(FULLSCREEN_CLASS, fullScreen)
        pseudoClassStateChanged(ACTIVE_CLASS, active)
        pseudoClassStateChanged(INACTIVE_CLASS, stage != null && !active)

        requestLayout()
    }

    private fun applySurface(radius: Double) {
        val corners = CornerRadii(radius)
        frameBox.background = Background(BackgroundFill(ChromeConfig.SURFACE_COLOR, corners, Insets.EMPTY))
        frameBox.border = Border(
            BorderStroke(
                osBorderColor,
                BorderStrokeStyle.SOLID,
                corners,
                BorderWidths(1.0),
            ),
        )
        frameClip.arcWidth = radius * 2
        frameClip.arcHeight = radius * 2
    }

    override fun layoutChildren() {
        val inset = shadowInset
        val innerWidth = (width - 2 * inset).coerceAtLeast(0.0)
        val innerHeight = (height - 2 * inset).coerceAtLeast(0.0)

        shadowRoot.resizeRelocate(inset, inset, innerWidth, innerHeight)
        resizeOverlay.resizeRelocate(0.0, 0.0, width, height)
    }

    /** The bundled default look; overridden by any stylesheet added to the hosting `Scene`. */
    override fun getUserAgentStylesheet(): String = USER_AGENT_STYLESHEET

    override fun getCssMetaData(): MutableList<CssMetaData<out Styleable, *>> = CSS_META_DATA

    public companion object {

        private val BORDER_COLOR: Color = Color.rgb(0, 0, 0, 0.25)

        private const val DEFAULT_SHADOW_RADIUS: Double = 18.0
        private const val DEFAULT_CORNER_RADIUS: Double = 8.0
        private const val DEFAULT_RESIZE_BORDER: Double = 6.0
        private const val DEFAULT_CAPTION_MIN_HEIGHT: Double = 32.0
        private val DEFAULT_SHADOW_COLOR: Color = Color.rgb(0, 0, 0, 0.45)

        private val USER_AGENT_STYLESHEET: String =
            ChromePane::class.java.getResource("chrome.css")!!.toExternalForm()

        private val SHADOW_RADIUS_META: CssMetaData<ChromePane, Number> =
            object : CssMetaData<ChromePane, Number>(
                "-panelium-shadow-radius", StyleConverter.getSizeConverter(), DEFAULT_SHADOW_RADIUS,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.shadowRadius.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Number> =
                    styleable.shadowRadius
            }

        private val SHADOW_COLOR_META: CssMetaData<ChromePane, Color> =
            object : CssMetaData<ChromePane, Color>(
                "-panelium-shadow-color", StyleConverter.getColorConverter(), DEFAULT_SHADOW_COLOR,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.shadowColor.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Color> =
                    styleable.shadowColor
            }

        private val CORNER_RADIUS_META: CssMetaData<ChromePane, Number> =
            object : CssMetaData<ChromePane, Number>(
                "-panelium-corner-radius", StyleConverter.getSizeConverter(), DEFAULT_CORNER_RADIUS,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.cornerRadius.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Number> =
                    styleable.cornerRadius
            }

        private val RESIZE_BORDER_META: CssMetaData<ChromePane, Number> =
            object : CssMetaData<ChromePane, Number>(
                "-panelium-resize-border", StyleConverter.getSizeConverter(), DEFAULT_RESIZE_BORDER,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.resizeBorder.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Number> =
                    styleable.resizeBorder
            }

        private val CAPTION_MIN_HEIGHT_META: CssMetaData<ChromePane, Number> =
            object : CssMetaData<ChromePane, Number>(
                "-panelium-caption-min-height",
                StyleConverter.getSizeConverter(),
                DEFAULT_CAPTION_MIN_HEIGHT,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean =
                    !styleable.captionMinHeight.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Number> =
                    styleable.captionMinHeight
            }

        private val CSS_META_DATA: MutableList<CssMetaData<out Styleable, *>> = run {
            val list = ArrayList<CssMetaData<out Styleable, *>>(Region.getClassCssMetaData())
            list.add(SHADOW_RADIUS_META)
            list.add(SHADOW_COLOR_META)
            list.add(CORNER_RADIUS_META)
            list.add(RESIZE_BORDER_META)
            list.add(CAPTION_MIN_HEIGHT_META)
            java.util.Collections.unmodifiableList(list)
        }

        private val MAXIMIZED_CLASS: PseudoClass = PseudoClass.getPseudoClass("maximized")
        private val FULLSCREEN_CLASS: PseudoClass = PseudoClass.getPseudoClass("fullscreen")
        private val ACTIVE_CLASS: PseudoClass = PseudoClass.getPseudoClass("active")
        private val INACTIVE_CLASS: PseudoClass = PseudoClass.getPseudoClass("inactive")

        /** The styleable properties of [ChromePane], following the JavaFX `Control` convention. */
        public fun getClassCssMetaData(): MutableList<CssMetaData<out Styleable, *>> = CSS_META_DATA
    }
}
