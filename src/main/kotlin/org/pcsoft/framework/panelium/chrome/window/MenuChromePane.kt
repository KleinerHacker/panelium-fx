package org.pcsoft.framework.panelium.chrome

import javafx.animation.FadeTransition
import javafx.beans.DefaultProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.util.Duration
import org.pcsoft.framework.panelium.menupane.BackstageOverlayHost
import org.pcsoft.framework.panelium.menupane.FXMenuPane

/**
 * A [ChromePane] specialised for a docked [FXMenuPane] window.
 *
 * The frame content is an internal `BorderPane`: the [menuPane] sits in its `top`, directly below the
 * caption bar, and [body] fills the `center` with the rest of the window. Assigning a [menuPane] also
 * wires an internal overlay host into it, so the file-tab backstage is painted as an overlay over the
 * [body] only - the caption bar and the docked [menuPane] (with its File button) stay visible and
 * interactive, so the backstage can always be dismissed by clicking the File button again, pressing
 * Escape, or clicking outside it. Clearing [menuPane] releases the hook again. That overlay wiring is
 * a module-internal detail; applications do not plug into it.
 *
 * Instantiable from FXML; `body` is the default property, so a single child element becomes the
 * window body. Do not set the inherited `content` property on this subclass - it holds the internal
 * layout.
 */
@DefaultProperty("body")
open class MenuChromePane : ChromePane() {

    private val backstageOverlay: StackPane = StackPane().apply {
        styleClass.add("chrome-backstage-overlay")
        isVisible = false
    }

    /** Stacks the [body] node under the [backstageOverlay]; the overlay is always the last child. */
    private val bodyStack: StackPane = StackPane(backstageOverlay)

    private val innerLayout: BorderPane = BorderPane().apply { center = bodyStack }

    private var backstageFade: FadeTransition? = null

    /**
     * Handed to the docked [FXMenuPane] as its overlay host. Kept private: the backstage wiring is a
     * module-internal detail of this subclass, not an extension point for applications.
     */
    private val backstageOverlayHost: BackstageOverlayHost = object : BackstageOverlayHost {

        /** Shows [node] as an overlay over the [body], fading it in over 0.3 seconds. */
        override fun showOverlay(node: Node) {
            backstageFade?.stop()
            backstageOverlay.children.setAll(node)
            backstageOverlay.opacity = 0.0
            backstageOverlay.isVisible = true
            backstageFade = FadeTransition(BACKSTAGE_FADE_DURATION, backstageOverlay).apply {
                fromValue = 0.0
                toValue = 1.0
                play()
            }
        }

        /** Fades the overlay shown through [showOverlay] back out and clears it. */
        override fun hideOverlay() {
            backstageFade?.stop()
            if (!backstageOverlay.isVisible) {
                backstageOverlay.children.clear()
                return
            }
            backstageFade = FadeTransition(BACKSTAGE_FADE_DURATION, backstageOverlay).apply {
                fromValue = backstageOverlay.opacity
                toValue = 0.0
                setOnFinished {
                    backstageOverlay.isVisible = false
                    backstageOverlay.opacity = 1.0
                    backstageOverlay.children.clear()
                }
                play()
            }
        }
    }

    private val menuPaneProp: ObjectProperty<FXMenuPane?> =
        SimpleObjectProperty(this, "menuPane", null)

    private val bodyProp: ObjectProperty<Node?> =
        SimpleObjectProperty(this, "body", null)

    init {
        content = innerLayout

        menuPaneProp.addListener { _, old, new ->
            if (old != null && old.overlayHost === backstageOverlayHost) {
                old.overlayHost = null
            }
            innerLayout.top = new
            new?.overlayHost = backstageOverlayHost
        }
        bodyProp.addListener { _, old, new ->
            if (old != null) {
                bodyStack.children.remove(old)
            }
            if (new != null) {
                bodyStack.children.add(0, new)
            }
        }
    }

    /** The docked menu pane, or `null` when the window has no ribbon. */
    fun menuPaneProperty(): ObjectProperty<FXMenuPane?> = menuPaneProp

    /** The docked menu pane, or `null` when the window has no ribbon. */
    var menuPane: FXMenuPane?
        get() = menuPaneProp.get()
        set(value) = menuPaneProp.set(value)

    /** The window body shown below the [menuPane]. */
    fun bodyProperty(): ObjectProperty<Node?> = bodyProp

    /** The window body shown below the [menuPane]. */
    var body: Node?
        get() = bodyProp.get()
        set(value) = bodyProp.set(value)

    private companion object {
        val BACKSTAGE_FADE_DURATION: Duration = Duration.seconds(0.3)
    }
}
