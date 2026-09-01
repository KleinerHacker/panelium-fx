package org.pcsoft.framework.panelium.chrome

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import org.pcsoft.framework.panelium.chrome.internal.ChromeConfig

/**
 * Undecorated window frame: shadow, border and a caption placeholder around the actual content.
 */
public class ChromePane : Region {

    private val shadowRoot: StackPane = StackPane()
    private val frameBox: BorderPane = BorderPane()
    private val resizeOverlay: StackPane = StackPane()
    private val captionPlaceholder: Region = Region()

    private val contentProperty: ObjectProperty<Node?> = SimpleObjectProperty(this, "content")

    public constructor() {
        captionPlaceholder.minHeight = CAPTION_PLACEHOLDER_HEIGHT
        captionPlaceholder.prefHeight = CAPTION_PLACEHOLDER_HEIGHT

        frameBox.top = captionPlaceholder
        frameBox.centerProperty().bind(contentProperty)

        shadowRoot.children.add(frameBox)

        resizeOverlay.isPickOnBounds = false
        resizeOverlay.isMouseTransparent = true

        children.addAll(shadowRoot, resizeOverlay)
    }

    public constructor(content: Node) : this() {
        contentProperty.set(content)
    }

    public fun contentProperty(): ObjectProperty<Node?> = contentProperty

    public var content: Node?
        get() = contentProperty.get()
        set(value) = contentProperty.set(value)

    override fun layoutChildren() {
        val inset = ChromeConfig.SHADOW_INSET
        val innerWidth = (width - 2 * inset).coerceAtLeast(0.0)
        val innerHeight = (height - 2 * inset).coerceAtLeast(0.0)

        shadowRoot.resizeRelocate(inset, inset, innerWidth, innerHeight)
        resizeOverlay.resizeRelocate(0.0, 0.0, width, height)
    }

    private companion object {
        const val CAPTION_PLACEHOLDER_HEIGHT: Double = 32.0
    }
}
