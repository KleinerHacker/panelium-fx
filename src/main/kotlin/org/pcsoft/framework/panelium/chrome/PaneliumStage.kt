package org.pcsoft.framework.panelium.chrome

import javafx.scene.Node
import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.internal.ChromeConfig

/**
 * [Stage] subclass that is preconfigured as an undecorated, transparent window with a
 * [ChromePane] frame. Supports [initOwner]/[initModality] like any other [Stage].
 */
public open class PaneliumStage : Stage {

    public val chromePane: ChromePane = ChromePane()

    public constructor() : super() {
        ChromeConfig.apply(this, chromePane)
    }

    public var content: Node?
        get() = chromePane.content
        set(value) {
            chromePane.content = value
        }
}
