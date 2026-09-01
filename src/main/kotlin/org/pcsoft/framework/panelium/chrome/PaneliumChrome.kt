package org.pcsoft.framework.panelium.chrome

import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.internal.ChromeConfig

/**
 * Entry point that turns an existing [Stage] into an undecorated, transparent window with a
 * [ChromePane] frame around the current scene content. Must be called before [Stage.show].
 */
public object PaneliumChrome {

    public fun install(stage: Stage): ChromePane {
        val existingRoot = stage.scene?.root
        val chromePane = if (existingRoot != null) ChromePane(existingRoot) else ChromePane()

        ChromeConfig.apply(stage, chromePane)

        return chromePane
    }
}
