/*
 * Copyright (c) KleinerHacker alias Pfeiffer C Soft 2026.
 * This work is licensed under the Apache License, Version 2.0.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations.
 */

package org.pcsoft.framework.panelium.chrome

import javafx.stage.Stage
import org.pcsoft.framework.panelium.chrome.internal.ChromeConfig

/**
 * Entry point that turns an existing [Stage] into an undecorated, transparent window with a
 * [ChromePane] frame around the current scene content. Must be called before [Stage.show].
 */
object PaneliumChrome {

    fun install(stage: Stage): ChromePane {
        val existingRoot = stage.scene?.root
        val chromePane = if (existingRoot != null) ChromePane(existingRoot) else ChromePane()

        ChromeConfig.apply(stage, chromePane)

        return chromePane
    }
}
