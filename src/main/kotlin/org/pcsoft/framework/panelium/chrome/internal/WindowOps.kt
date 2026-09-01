package org.pcsoft.framework.panelium.chrome.internal

import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage

/**
 * The eight resize handles of the frame. Each flag marks which window edges the handle moves.
 */
internal enum class ResizeEdge(
    val north: Boolean,
    val south: Boolean,
    val east: Boolean,
    val west: Boolean,
) {
    N(north = true, south = false, east = false, west = false),
    S(north = false, south = true, east = false, west = false),
    E(north = false, south = false, east = true, west = false),
    W(north = false, south = false, east = false, west = true),
    NE(north = true, south = false, east = true, west = false),
    NW(north = true, south = false, east = false, west = true),
    SE(north = false, south = true, east = true, west = false),
    SW(north = false, south = true, east = false, west = true),
}

/**
 * Window operation service for an undecorated [Stage]: move, edge/corner resize, minimize and
 * maximize/restore. All coordinates are screen coordinates. UI triggers (buttons, double-click)
 * are wired by later plans; this class only performs the operations.
 */
internal class WindowOps(private val stage: Stage) {

    private var moveAnchorX = 0.0
    private var moveAnchorY = 0.0
    private var moveOriginX = 0.0
    private var moveOriginY = 0.0

    private var resizeAnchorX = 0.0
    private var resizeAnchorY = 0.0
    private var resizeOrigin = Rectangle2D(0.0, 0.0, 0.0, 0.0)

    private var restoreBounds: Rectangle2D? = null

    val isMaximized: Boolean
        get() = restoreBounds != null

    fun startMove(screenX: Double, screenY: Double) {
        moveAnchorX = screenX
        moveAnchorY = screenY
        moveOriginX = stage.x
        moveOriginY = stage.y
    }

    fun moveTo(screenX: Double, screenY: Double) {
        if (isMaximized || stage.isFullScreen) return
        stage.x = moveOriginX + (screenX - moveAnchorX)
        stage.y = moveOriginY + (screenY - moveAnchorY)
    }

    fun startResize(screenX: Double, screenY: Double) {
        resizeAnchorX = screenX
        resizeAnchorY = screenY
        resizeOrigin = Rectangle2D(stage.x, stage.y, stage.width, stage.height)
    }

    fun resize(edge: ResizeEdge, screenX: Double, screenY: Double) {
        if (!stage.isResizable || isMaximized || stage.isFullScreen) return

        val dx = screenX - resizeAnchorX
        val dy = screenY - resizeAnchorY

        val minW = stage.minWidth.coerceAtLeast(1.0)
        val minH = stage.minHeight.coerceAtLeast(1.0)
        val maxW = if (stage.maxWidth > 0.0) stage.maxWidth else Double.MAX_VALUE
        val maxH = if (stage.maxHeight > 0.0) stage.maxHeight else Double.MAX_VALUE

        var x = resizeOrigin.minX
        var y = resizeOrigin.minY
        var w = resizeOrigin.width
        var h = resizeOrigin.height

        if (edge.east) {
            w = (resizeOrigin.width + dx).coerceIn(minW, maxW)
        }
        if (edge.south) {
            h = (resizeOrigin.height + dy).coerceIn(minH, maxH)
        }
        if (edge.west) {
            val right = resizeOrigin.minX + resizeOrigin.width
            w = (resizeOrigin.width - dx).coerceIn(minW, maxW)
            x = right - w
        }
        if (edge.north) {
            val bottom = resizeOrigin.minY + resizeOrigin.height
            h = (resizeOrigin.height - dy).coerceIn(minH, maxH)
            y = bottom - h
        }

        stage.x = x
        stage.y = y
        stage.width = w
        stage.height = h
    }

    fun minimize() {
        stage.isIconified = true
    }

    fun toggleMaximize() {
        if (stage.isFullScreen) return
        if (isMaximized) restore() else maximize()
    }

    fun maximize() {
        if (isMaximized || stage.isFullScreen) return
        restoreBounds = Rectangle2D(stage.x, stage.y, stage.width, stage.height)
        val bounds = screenForStage().visualBounds
        stage.x = bounds.minX
        stage.y = bounds.minY
        stage.width = bounds.width
        stage.height = bounds.height
        stage.isMaximized = true
    }

    fun restore() {
        val previous = restoreBounds ?: return
        restoreBounds = null
        stage.isMaximized = false
        stage.x = previous.minX
        stage.y = previous.minY
        stage.width = previous.width
        stage.height = previous.height
    }

    fun close() {
        stage.close()
    }

    private fun screenForStage(): Screen {
        val screens = Screen.getScreensForRectangle(
            stage.x,
            stage.y,
            stage.width.coerceAtLeast(1.0),
            stage.height.coerceAtLeast(1.0),
        )
        return screens.firstOrNull() ?: Screen.getPrimary()
    }
}
