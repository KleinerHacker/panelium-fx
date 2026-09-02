package org.pcsoft.framework.panelium.chrome.internal

import javafx.beans.InvalidationListener
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.stage.Stage

/**
 * Transparent layer above the frame that carries eight invisible resize zones (four edges, four
 * corners) just inside the shadow insets. Zones forward their drags to [WindowOps] and are
 * disabled while the stage is not resizable, maximized or full screen.
 */
internal class ResizeOverlay : Pane() {

    private var stage: Stage? = null
    private var windowOps: WindowOps? = null

    /** Width of the invisible edge/corner grab zones; driven by `-panelium-resize-border`. */
    internal var resizeBorder: Double = 6.0
        set(value) {
            field = value
            requestLayout()
        }

    /** Transparent gutter around the frame; driven by `-panelium-shadow-inset` via [ChromePane]. */
    internal var frameInset: Double = ChromeConfig.DEFAULT_SHADOW_INSET
        set(value) {
            field = value
            requestLayout()
        }

    private val zones: Map<ResizeEdge, Region> = ResizeEdge.entries.associateWith { edge ->
        Region().apply {
            cursor = cursorFor(edge)
            isPickOnBounds = true
            isManaged = false
            addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
                windowOps?.startResize(event.screenX, event.screenY)
                event.consume()
            }
            addEventHandler(MouseEvent.MOUSE_DRAGGED) { event ->
                windowOps?.resize(edge, event.screenX, event.screenY)
                event.consume()
            }
        }
    }

    init {
        isPickOnBounds = false
        children.addAll(zones.values)
    }

    fun attach(stage: Stage, windowOps: WindowOps) {
        this.stage = stage
        this.windowOps = windowOps
        val refresh = InvalidationListener { updateActive() }
        stage.resizableProperty().addListener(refresh)
        stage.maximizedProperty().addListener(refresh)
        stage.fullScreenProperty().addListener(refresh)
        updateActive()
    }

    private fun updateActive() {
        val current = stage
        val active = current != null &&
            current.isResizable &&
            !current.isMaximized &&
            !current.isFullScreen
        isMouseTransparent = !active
        zones.values.forEach { it.isVisible = active }
    }

    override fun layoutChildren() {
        val border = resizeBorder
        val inset = frameInset
        val left = inset
        val top = inset
        val right = (width - inset).coerceAtLeast(left)
        val bottom = (height - inset).coerceAtLeast(top)
        val innerWidth = (right - left - 2 * border).coerceAtLeast(0.0)
        val innerHeight = (bottom - top - 2 * border).coerceAtLeast(0.0)

        place(ResizeEdge.NW, left, top, border, border)
        place(ResizeEdge.NE, right - border, top, border, border)
        place(ResizeEdge.SW, left, bottom - border, border, border)
        place(ResizeEdge.SE, right - border, bottom - border, border, border)
        place(ResizeEdge.N, left + border, top, innerWidth, border)
        place(ResizeEdge.S, left + border, bottom - border, innerWidth, border)
        place(ResizeEdge.W, left, top + border, border, innerHeight)
        place(ResizeEdge.E, right - border, top + border, border, innerHeight)
    }

    private fun place(edge: ResizeEdge, x: Double, y: Double, w: Double, h: Double) {
        zones.getValue(edge).resizeRelocate(x, y, w, h)
    }

    private companion object {
        fun cursorFor(edge: ResizeEdge): Cursor = when (edge) {
            ResizeEdge.N -> Cursor.N_RESIZE
            ResizeEdge.S -> Cursor.S_RESIZE
            ResizeEdge.E -> Cursor.E_RESIZE
            ResizeEdge.W -> Cursor.W_RESIZE
            ResizeEdge.NE -> Cursor.NE_RESIZE
            ResizeEdge.NW -> Cursor.NW_RESIZE
            ResizeEdge.SE -> Cursor.SE_RESIZE
            ResizeEdge.SW -> Cursor.SW_RESIZE
        }
    }
}
