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
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.SnapshotParameters
import javafx.scene.effect.DropShadow
import javafx.scene.effect.Effect
import javafx.scene.effect.GaussianBlur
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.layout.Border
import javafx.scene.layout.BorderPane
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeLineJoin
import javafx.scene.shape.StrokeType
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.internal.CaptionDragHandler
import org.pcsoft.framework.panelium.chrome.internal.ChromeConfig
import org.pcsoft.framework.panelium.chrome.internal.ResizeOverlay
import org.pcsoft.framework.panelium.chrome.internal.WindowOps

/**
 * Undecorated window frame: surface, border, effect and a composable [ChromeCaptionBar] around the
 * actual content. When installed on a [Stage] (via `PaneliumChrome.install`, `PaneliumStage` or
 * [attachStage]) the frame also drives window move, edge/corner resize, minimize and
 * maximize/restore, and drops its effect and rounded corners while maximized or full screen.
 *
 * Instantiable from FXML; `content` is the default property, so a single child element becomes the
 * framed content.
 *
 * ## Styling
 *
 * The pane carries the `chrome-pane` style class and the `maximized`, `fullscreen`, `active` and
 * `inactive` pseudo-classes (the last two follow `Stage.focused`). Its default look ships as a
 * user-agent stylesheet ([getUserAgentStylesheet]); an application stylesheet added to the `Scene`
 * overrides it through normal CSS precedence.
 *
 * Every colour is a paint, so a `linear-gradient` works wherever a colour does. Styleable
 * properties on the `chrome-pane` selector:
 *
 * * surface: `-panelium-surface-color` (paint), `-panelium-corner-radius`
 * * border: `-panelium-border-mode` (`flat` / `raised` / `sunken`), `-panelium-border-color`,
 *   `-panelium-border-light-color`, `-panelium-border-dark-color`, `-panelium-border-width`,
 *   `-panelium-border-style` (`solid` / `dashed` / `dotted`), `-panelium-border-line-cap`,
 *   `-panelium-border-line-join`, `-panelium-border-miter-limit`, `-panelium-border-dash-offset`
 * * effect: `-panelium-shadow-radius`, `-panelium-shadow-color` (feed the built-in drop shadow),
 *   `-panelium-effect` (any `dropshadow()` / `innershadow()` - replaces the built-in one when set),
 *   `-panelium-shadow-inset` (outer gutter reserved for the effect)
 * * glass caption: `-panelium-caption-backdrop-blur` (blur radius of the frosted strip mirrored
 *   behind the caption bar; `0` disables it)
 * * layout: `-panelium-resize-border`, `-panelium-caption-min-height`
 *
 * The caption bar, its slots and the window buttons are plain JavaFX nodes styled through their
 * own `-fx-*` properties (`chrome-caption-bar`, `chrome-caption-buttons`, `chrome-button`,
 * `chrome-button-glyph-stroke` / `-fill`) - background paints, borders, `-fx-effect` and gradients
 * all apply there directly.
 *
 * Corner radius, surface, border, effect and shadow inset also follow [captionOsProperty]; an
 * explicit CSS value for the matching styleable property still wins.
 */
@Suppress("TYPE_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@DefaultProperty("content")
class ChromePane : Region {

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

    private val surfaceFillImpl: StyleableObjectProperty<Paint> =
        SimpleStyleableObjectProperty(SURFACE_COLOR_META, this, "surfaceColor", DEFAULT_SURFACE_FILL)

    private val borderModeImpl: StyleableObjectProperty<ChromeBorderMode> =
        SimpleStyleableObjectProperty(BORDER_MODE_META, this, "borderMode", DEFAULT_BORDER_MODE)

    private val borderColorImpl: StyleableObjectProperty<Paint> =
        SimpleStyleableObjectProperty(BORDER_COLOR_META, this, "borderColor", DEFAULT_BORDER_COLOR)

    private val borderWidthImpl: StyleableDoubleProperty =
        SimpleStyleableDoubleProperty(BORDER_WIDTH_META, this, "borderWidth", DEFAULT_BORDER_WIDTH)

    private val borderLightPaintImpl: StyleableObjectProperty<Paint> =
        SimpleStyleableObjectProperty(
            BORDER_LIGHT_COLOR_META, this, "borderLightColor", DEFAULT_BORDER_LIGHT_PAINT,
        )

    private val borderDarkPaintImpl: StyleableObjectProperty<Paint> =
        SimpleStyleableObjectProperty(
            BORDER_DARK_COLOR_META, this, "borderDarkColor", DEFAULT_BORDER_DARK_PAINT,
        )

    private val borderStrokeStyleImpl: StyleableObjectProperty<ChromeBorderStrokeStyle> =
        SimpleStyleableObjectProperty(
            BORDER_STYLE_META, this, "borderStyle", DEFAULT_BORDER_STROKE_STYLE,
        )

    private val borderLineCapImpl: StyleableObjectProperty<StrokeLineCap> =
        SimpleStyleableObjectProperty(BORDER_LINE_CAP_META, this, "borderLineCap", DEFAULT_LINE_CAP)

    private val borderLineJoinImpl: StyleableObjectProperty<StrokeLineJoin> =
        SimpleStyleableObjectProperty(BORDER_LINE_JOIN_META, this, "borderLineJoin", DEFAULT_LINE_JOIN)

    private val borderMiterLimitImpl: StyleableDoubleProperty =
        SimpleStyleableDoubleProperty(
            BORDER_MITER_LIMIT_META, this, "borderMiterLimit", DEFAULT_MITER_LIMIT,
        )

    private val borderDashOffsetImpl: StyleableDoubleProperty =
        SimpleStyleableDoubleProperty(
            BORDER_DASH_OFFSET_META, this, "borderDashOffset", DEFAULT_DASH_OFFSET,
        )

    private val frameEffectImpl: StyleableObjectProperty<Effect?> =
        SimpleStyleableObjectProperty(EFFECT_META, this, "frameEffect", null)

    private val shadowInsetImpl: StyleableDoubleProperty =
        SimpleStyleableDoubleProperty(SHADOW_INSET_META, this, "shadowInset", DEFAULT_SHADOW_INSET)

    private val captionBackdropBlurImpl: StyleableDoubleProperty =
        SimpleStyleableDoubleProperty(
            CAPTION_BACKDROP_BLUR_META, this, "captionBackdropBlur", DEFAULT_CAPTION_BACKDROP_BLUR,
        )

    private val viewModel: ChromePaneViewModel
    private val shadowRoot: StackPane
    private val frameBox: StackPane
    private val contentHost: BorderPane
    private val captionBackdrop: Region
    private val resizeOverlay: ResizeOverlay = ResizeOverlay()

    /** Rounded clip on the framed box so the content cannot paint over the bottom corners. */
    private val frameClip: Rectangle = Rectangle()

    /** Whether the current OS wants an effect drawn at all (macOS/Windows/Linux yes, other no). */
    private var osShadowEnabled: Boolean = true

    /** The composable caption area at the top of the frame. */
    val captionBar: ChromeCaptionBar

    /** The built-in drop shadow, used whenever `-panelium-effect` is not set. */
    private val dropShadow: DropShadow = DropShadow().apply {
        offsetY = ChromeConfig.SHADOW_OFFSET_Y
    }

    private var boundStage: Stage? = null
    private var shadowInset: Double = ChromeConfig.DEFAULT_SHADOW_INSET

    internal var windowOps: WindowOps? = null
        private set

    constructor() {
        val tuple = FluentViewLoader.fxmlView(ChromePaneView::class.java).load()
        val codeBehind = tuple.codeBehind
        viewModel = tuple.viewModel
        shadowRoot = codeBehind.shadowRoot
        frameBox = codeBehind.frameBox
        contentHost = codeBehind.contentHost
        captionBackdrop = codeBehind.captionBackdrop
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

        val relayout = javafx.beans.InvalidationListener { updateWindowState() }
        listOf<javafx.beans.Observable>(
            cornerRadius, shadowColor, surfaceFillImpl, borderModeImpl, borderColorImpl,
            borderWidthImpl, borderLightPaintImpl, borderDarkPaintImpl, borderStrokeStyleImpl,
            borderLineCapImpl, borderLineJoinImpl, borderMiterLimitImpl, borderDashOffsetImpl,
            frameEffectImpl, shadowInsetImpl, captionBackdropBlurImpl, viewModel.shadowEnabled,
        ).forEach { it.addListener(relayout) }

        // Refresh the frosted caption strip when the content under it changes.
        contentHost.boundsInLocalProperty().addListener { _, _, _ -> refreshCaptionBackdrop() }
        captionBar.heightProperty().addListener { _, _, _ -> refreshCaptionBackdrop() }
        viewModel.content.addListener { _, _, _ -> refreshCaptionBackdrop() }

        captionBar.captionOsProperty().addListener { _, _, os -> applyOsFrame(os) }
        applyOsFrame(captionBar.captionOs)

        updateWindowState()
    }

    constructor(content: Node) : this() {
        viewModel.content.set(content)
    }

    fun contentProperty(): ObjectProperty<Node?> = viewModel.content

    var content: Node?
        get() = viewModel.content.get()
        set(value) = viewModel.content.set(value)

    /**
     * Whether the frame effect and its outer insets are rendered. Defaults to `true`. The effect
     * is always suppressed while the window is maximized or full screen.
     */
    fun shadowEnabledProperty(): BooleanProperty = viewModel.shadowEnabled

    var isShadowEnabled: Boolean
        get() = viewModel.shadowEnabled.get()
        set(value) = viewModel.shadowEnabled.set(value)

    /**
     * How the frame border is composed: a flat stroke ([ChromeBorderMode.FLAT], the default) or a
     * raised / sunken bevel. Also settable from CSS via `-panelium-border-mode`; follows
     * [captionOsProperty] until an explicit value wins.
     */
    fun borderModeProperty(): ObjectProperty<ChromeBorderMode> = borderModeImpl

    var borderMode: ChromeBorderMode
        get() = borderModeImpl.get()
        set(value) = borderModeImpl.set(value)

    /**
     * Nodes in the caption's leading slot, after the default icon and title.
     *
     * This property can be populated directly from FXML as a nested element, since `FXMLLoader`
     * fills read-only `List` properties by calling `addAll` on the returned list:
     * ```xml
     * <ChromePane xmlns="http://javafx.com/javafx" xmlns:fx="http://javafx.com/fxml">
     *     <captionLeftItems>
     *         <Button text="Back"/>
     *     </captionLeftItems>
     * </ChromePane>
     * ```
     */
    val captionLeftItems: ObservableList<Node> get() = captionBar.leftItems

    /** Nodes in the caption's growing center slot. See [captionLeftItems] for FXML usage. */
    val captionCenterItems: ObservableList<Node> get() = captionBar.centerItems

    /** Nodes in the caption's trailing slot, before the caption buttons. See [captionLeftItems] for FXML usage. */
    val captionRightItems: ObservableList<Node> get() = captionBar.rightItems

    fun defaultTitleVisibleProperty(): BooleanProperty = captionBar.defaultTitleVisibleProperty()

    var isDefaultTitleVisible: Boolean
        get() = captionBar.isDefaultTitleVisible
        set(value) {
            captionBar.isDefaultTitleVisible = value
        }

    fun defaultIconVisibleProperty(): BooleanProperty = captionBar.defaultIconVisibleProperty()

    var isDefaultIconVisible: Boolean
        get() = captionBar.isDefaultIconVisible
        set(value) {
            captionBar.isDefaultIconVisible = value
        }

    /** The default caption title; follows `Stage.title` once a stage is attached. */
    fun captionTitleProperty(): ReadOnlyStringProperty = captionBar.titleTextProperty()

    /**
     * The OS whose native caption button placement and look the frame follows. Defaults to the
     * detected OS; override it to force a layout in tests, demos or cross-platform previews.
     */
    fun captionOsProperty(): ObjectProperty<ChromeOs> = captionBar.captionOsProperty()

    var captionOs: ChromeOs
        get() = captionBar.captionOs
        set(value) {
            captionBar.captionOs = value
        }

    /**
     * Binds this pane to [stage]: creates the [WindowOps] service, activates the resize zones,
     * routes caption drags to a window move, installs the OS-specific caption buttons, binds the
     * default title / icon and tracks the maximized / full-screen / focus state.
     */
    fun attachStage(stage: Stage) {
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
        val m = ChromeConfig.frameMetrics(os)
        if (!cornerRadius.isBound) cornerRadius.value = m.cornerRadius
        if (!shadowRadius.isBound) shadowRadius.value = m.shadowRadius
        if (!shadowColor.isBound) shadowColor.value = m.shadowColor
        if (!shadowInsetImpl.isBound) shadowInsetImpl.value = m.shadowInset
        if (!surfaceFillImpl.isBound) surfaceFillImpl.value = m.surfaceFill
        if (!borderModeImpl.isBound) borderModeImpl.value = m.borderMode
        if (!borderColorImpl.isBound) borderColorImpl.value = m.borderColor
        if (!borderWidthImpl.isBound) borderWidthImpl.value = m.borderWidth
        if (!borderLightPaintImpl.isBound) borderLightPaintImpl.value = m.borderLightPaint
        if (!borderDarkPaintImpl.isBound) borderDarkPaintImpl.value = m.borderDarkPaint
        if (!borderStrokeStyleImpl.isBound) borderStrokeStyleImpl.value = m.borderStrokeStyle
        if (!borderLineCapImpl.isBound) borderLineCapImpl.value = m.borderLineCap
        if (!borderLineJoinImpl.isBound) borderLineJoinImpl.value = m.borderLineJoin
        if (!borderMiterLimitImpl.isBound) borderMiterLimitImpl.value = m.borderMiterLimit
        if (!borderDashOffsetImpl.isBound) borderDashOffsetImpl.value = m.borderDashOffset
        osShadowEnabled = m.shadowEnabled
        updateWindowState()
    }

    private fun updateWindowState() {
        val stage = boundStage
        val collapsed = stage != null && (stage.isMaximized || stage.isFullScreen)
        val fullScreen = stage != null && stage.isFullScreen
        val effectOn = isShadowEnabled && osShadowEnabled && !collapsed
        val active = stage != null && stage.isFocused

        shadowInset = if (effectOn) shadowInsetImpl.get().coerceAtLeast(0.0) else 0.0
        shadowRoot.effect = if (effectOn) (frameEffectImpl.get() ?: dropShadow) else null
        resizeOverlay.frameInset = shadowInset
        applySurface(if (collapsed) 0.0 else cornerRadius.get())

        val blur = captionBackdropBlurImpl.get()
        val backdropOn = blur > 0.0 && !collapsed
        captionBackdrop.isVisible = backdropOn
        captionBackdrop.effect = if (backdropOn) GaussianBlur(blur) else null
        if (backdropOn) refreshCaptionBackdrop() else captionBackdrop.background = null

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
        frameBox.background =
            Background(BackgroundFill(surfaceFillImpl.get() ?: DEFAULT_SURFACE_FILL, corners, Insets.EMPTY))

        val widths = BorderWidths(borderWidthImpl.get().coerceAtLeast(0.0))
        val style = borderStrokeStyle()
        val light: Paint = borderLightPaintImpl.get() ?: DEFAULT_BORDER_LIGHT_PAINT
        val dark: Paint = borderDarkPaintImpl.get() ?: DEFAULT_BORDER_DARK_PAINT
        val stroke = when (borderModeImpl.get() ?: ChromeBorderMode.FLAT) {
            ChromeBorderMode.FLAT ->
                BorderStroke(borderColorImpl.get() ?: DEFAULT_BORDER_COLOR, style, corners, widths)
            ChromeBorderMode.RAISED -> bevelStroke(light, dark, style, corners, widths)
            ChromeBorderMode.SUNKEN -> bevelStroke(dark, light, style, corners, widths)
        }
        frameBox.border = Border(stroke)

        frameClip.arcWidth = radius * 2
        frameClip.arcHeight = radius * 2
    }

    /** Builds the shared [BorderStrokeStyle] from the stroke-style / cap / join / miter / dash props. */
    private fun borderStrokeStyle(): BorderStrokeStyle {
        val w = borderWidthImpl.get().coerceAtLeast(1.0)
        val dashes: List<Double> = when (borderStrokeStyleImpl.get() ?: ChromeBorderStrokeStyle.SOLID) {
            ChromeBorderStrokeStyle.SOLID -> emptyList()
            ChromeBorderStrokeStyle.DASHED -> listOf(w * 3.0, w * 2.0)
            ChromeBorderStrokeStyle.DOTTED -> listOf(0.0, w * 2.0)
        }
        return BorderStrokeStyle(
            StrokeType.INSIDE,
            borderLineJoinImpl.get() ?: StrokeLineJoin.MITER,
            borderLineCapImpl.get() ?: StrokeLineCap.BUTT,
            borderMiterLimitImpl.get().coerceAtLeast(1.0),
            borderDashOffsetImpl.get(),
            dashes,
        )
    }

    /** A four-sided [BorderStroke] with [topLeft] on the top / left edges and [bottomRight] on the rest. */
    private fun bevelStroke(
        topLeft: Paint,
        bottomRight: Paint,
        style: BorderStrokeStyle,
        corners: CornerRadii,
        widths: BorderWidths,
    ): BorderStroke = BorderStroke(
        topLeft, bottomRight, bottomRight, topLeft,
        style, style, style, style,
        corners, widths, Insets.EMPTY,
    )

    /**
     * Refreshes the frosted strip drawn behind the caption bar: a snapshot of the content directly
     * below the caption band, dropped onto [captionBackdrop] where the caption's own translucent
     * fill and the layer's [GaussianBlur] turn it into a glass surface. On-demand only - not a
     * per-frame mirror.
     */
    private fun refreshCaptionBackdrop() {
        if (!captionBackdrop.isVisible || scene == null) return
        val w = contentHost.width
        val h = captionBar.height
        if (w <= 0.0 || h <= 0.0) return

        val params = SnapshotParameters().apply {
            fill = Color.TRANSPARENT
            viewport = Rectangle2D(0.0, h, w, h)
        }
        val image = runCatching { contentHost.snapshot(params, null) }.getOrNull() ?: return
        captionBackdrop.background = Background(
            BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize(w, h, false, false, false, false),
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

    /** The bundled default look; overridden by any stylesheet added to the hosting `Scene`. */
    override fun getUserAgentStylesheet(): String = USER_AGENT_STYLESHEET

    override fun getCssMetaData(): MutableList<CssMetaData<out Styleable, *>> = CSS_META_DATA

    companion object {

        private const val DEFAULT_SHADOW_RADIUS: Double = 18.0
        private const val DEFAULT_CORNER_RADIUS: Double = 8.0
        private const val DEFAULT_RESIZE_BORDER: Double = 6.0
        private const val DEFAULT_CAPTION_MIN_HEIGHT: Double = 32.0
        private const val DEFAULT_SHADOW_INSET: Double = 12.0
        private const val DEFAULT_BORDER_WIDTH: Double = 1.0
        private const val DEFAULT_MITER_LIMIT: Double = 10.0
        private const val DEFAULT_DASH_OFFSET: Double = 0.0
        private const val DEFAULT_CAPTION_BACKDROP_BLUR: Double = 0.0
        private val DEFAULT_SHADOW_COLOR: Color = Color.rgb(0, 0, 0, 0.45)
        private val DEFAULT_SURFACE_FILL: Paint = Color.WHITE
        private val DEFAULT_BORDER_COLOR: Paint = Color.rgb(0, 0, 0, 0.25)
        private val DEFAULT_BORDER_MODE: ChromeBorderMode = ChromeBorderMode.FLAT
        private val DEFAULT_BORDER_STROKE_STYLE: ChromeBorderStrokeStyle = ChromeBorderStrokeStyle.SOLID
        private val DEFAULT_LINE_CAP: StrokeLineCap = StrokeLineCap.BUTT
        private val DEFAULT_LINE_JOIN: StrokeLineJoin = StrokeLineJoin.MITER
        private val DEFAULT_BORDER_LIGHT_PAINT: Paint = Color.rgb(255, 255, 255, 0.9)
        private val DEFAULT_BORDER_DARK_PAINT: Paint = Color.rgb(0, 0, 0, 0.35)

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

        private val SURFACE_COLOR_META: CssMetaData<ChromePane, Paint> =
            object : CssMetaData<ChromePane, Paint>(
                "-panelium-surface-color", StyleConverter.getPaintConverter(), DEFAULT_SURFACE_FILL,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.surfaceFillImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Paint> =
                    styleable.surfaceFillImpl
            }

        private val BORDER_MODE_META: CssMetaData<ChromePane, ChromeBorderMode> =
            object : CssMetaData<ChromePane, ChromeBorderMode>(
                "-panelium-border-mode",
                StyleConverter.getEnumConverter(ChromeBorderMode::class.java),
                DEFAULT_BORDER_MODE,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.borderModeImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<ChromeBorderMode> =
                    styleable.borderModeImpl
            }

        private val BORDER_COLOR_META: CssMetaData<ChromePane, Paint> =
            object : CssMetaData<ChromePane, Paint>(
                "-panelium-border-color", StyleConverter.getPaintConverter(), DEFAULT_BORDER_COLOR,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.borderColorImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Paint> =
                    styleable.borderColorImpl
            }

        private val BORDER_WIDTH_META: CssMetaData<ChromePane, Number> =
            object : CssMetaData<ChromePane, Number>(
                "-panelium-border-width", StyleConverter.getSizeConverter(), DEFAULT_BORDER_WIDTH,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.borderWidthImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Number> =
                    styleable.borderWidthImpl
            }

        private val BORDER_LIGHT_COLOR_META: CssMetaData<ChromePane, Paint> =
            object : CssMetaData<ChromePane, Paint>(
                "-panelium-border-light-color",
                StyleConverter.getPaintConverter(),
                DEFAULT_BORDER_LIGHT_PAINT,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean =
                    !styleable.borderLightPaintImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Paint> =
                    styleable.borderLightPaintImpl
            }

        private val BORDER_DARK_COLOR_META: CssMetaData<ChromePane, Paint> =
            object : CssMetaData<ChromePane, Paint>(
                "-panelium-border-dark-color",
                StyleConverter.getPaintConverter(),
                DEFAULT_BORDER_DARK_PAINT,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean =
                    !styleable.borderDarkPaintImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Paint> =
                    styleable.borderDarkPaintImpl
            }

        private val BORDER_STYLE_META: CssMetaData<ChromePane, ChromeBorderStrokeStyle> =
            object : CssMetaData<ChromePane, ChromeBorderStrokeStyle>(
                "-panelium-border-style",
                StyleConverter.getEnumConverter(ChromeBorderStrokeStyle::class.java),
                DEFAULT_BORDER_STROKE_STYLE,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean =
                    !styleable.borderStrokeStyleImpl.isBound
                override fun getStyleableProperty(
                    styleable: ChromePane,
                ): StyleableProperty<ChromeBorderStrokeStyle> = styleable.borderStrokeStyleImpl
            }

        private val BORDER_LINE_CAP_META: CssMetaData<ChromePane, StrokeLineCap> =
            object : CssMetaData<ChromePane, StrokeLineCap>(
                "-panelium-border-line-cap",
                StyleConverter.getEnumConverter(StrokeLineCap::class.java),
                DEFAULT_LINE_CAP,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean =
                    !styleable.borderLineCapImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<StrokeLineCap> =
                    styleable.borderLineCapImpl
            }

        private val BORDER_LINE_JOIN_META: CssMetaData<ChromePane, StrokeLineJoin> =
            object : CssMetaData<ChromePane, StrokeLineJoin>(
                "-panelium-border-line-join",
                StyleConverter.getEnumConverter(StrokeLineJoin::class.java),
                DEFAULT_LINE_JOIN,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean =
                    !styleable.borderLineJoinImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<StrokeLineJoin> =
                    styleable.borderLineJoinImpl
            }

        private val BORDER_MITER_LIMIT_META: CssMetaData<ChromePane, Number> =
            object : CssMetaData<ChromePane, Number>(
                "-panelium-border-miter-limit", StyleConverter.getSizeConverter(), DEFAULT_MITER_LIMIT,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean =
                    !styleable.borderMiterLimitImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Number> =
                    styleable.borderMiterLimitImpl
            }

        private val BORDER_DASH_OFFSET_META: CssMetaData<ChromePane, Number> =
            object : CssMetaData<ChromePane, Number>(
                "-panelium-border-dash-offset", StyleConverter.getSizeConverter(), DEFAULT_DASH_OFFSET,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean =
                    !styleable.borderDashOffsetImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Number> =
                    styleable.borderDashOffsetImpl
            }

        private val EFFECT_META: CssMetaData<ChromePane, Effect?> =
            object : CssMetaData<ChromePane, Effect?>(
                "-panelium-effect", StyleConverter.getEffectConverter(), null,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.frameEffectImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Effect?> =
                    styleable.frameEffectImpl
            }

        private val SHADOW_INSET_META: CssMetaData<ChromePane, Number> =
            object : CssMetaData<ChromePane, Number>(
                "-panelium-shadow-inset", StyleConverter.getSizeConverter(), DEFAULT_SHADOW_INSET,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean = !styleable.shadowInsetImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Number> =
                    styleable.shadowInsetImpl
            }

        private val CAPTION_BACKDROP_BLUR_META: CssMetaData<ChromePane, Number> =
            object : CssMetaData<ChromePane, Number>(
                "-panelium-caption-backdrop-blur",
                StyleConverter.getSizeConverter(),
                DEFAULT_CAPTION_BACKDROP_BLUR,
            ) {
                override fun isSettable(styleable: ChromePane): Boolean =
                    !styleable.captionBackdropBlurImpl.isBound
                override fun getStyleableProperty(styleable: ChromePane): StyleableProperty<Number> =
                    styleable.captionBackdropBlurImpl
            }

        private val CSS_META_DATA: MutableList<CssMetaData<out Styleable, *>> = run {
            val list = ArrayList<CssMetaData<out Styleable, *>>(Region.getClassCssMetaData())
            list.add(SHADOW_RADIUS_META)
            list.add(SHADOW_COLOR_META)
            list.add(CORNER_RADIUS_META)
            list.add(RESIZE_BORDER_META)
            list.add(CAPTION_MIN_HEIGHT_META)
            list.add(SURFACE_COLOR_META)
            list.add(BORDER_MODE_META)
            list.add(BORDER_COLOR_META)
            list.add(BORDER_WIDTH_META)
            list.add(BORDER_LIGHT_COLOR_META)
            list.add(BORDER_DARK_COLOR_META)
            list.add(BORDER_STYLE_META)
            list.add(BORDER_LINE_CAP_META)
            list.add(BORDER_LINE_JOIN_META)
            list.add(BORDER_MITER_LIMIT_META)
            list.add(BORDER_DASH_OFFSET_META)
            list.add(EFFECT_META)
            list.add(SHADOW_INSET_META)
            list.add(CAPTION_BACKDROP_BLUR_META)
            java.util.Collections.unmodifiableList(list)
        }

        private val MAXIMIZED_CLASS: PseudoClass = PseudoClass.getPseudoClass("maximized")
        private val FULLSCREEN_CLASS: PseudoClass = PseudoClass.getPseudoClass("fullscreen")
        private val ACTIVE_CLASS: PseudoClass = PseudoClass.getPseudoClass("active")
        private val INACTIVE_CLASS: PseudoClass = PseudoClass.getPseudoClass("inactive")

        /** The styleable properties of [ChromePane], following the JavaFX `Control` convention. */
        fun getClassCssMetaData(): MutableList<CssMetaData<out Styleable, *>> = CSS_META_DATA
    }
}
