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

package org.pcsoft.framework.panelium.menupane.support

import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.pcsoft.framework.panelium.menupane.FXBackstageMenuPane
import org.pcsoft.framework.panelium.menupane.FXMenuPane
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable

/**
 * Base class for the headless JavaFX UI tests of the menupane package. Boots the Monocle toolkit
 * once, offers [onFx] to run code on the FX application thread, [showMenuPaneStage] to bring up a
 * stage hosting a fresh [FXMenuPane] and [showBackstageMenuPaneStage] to bring up a stage hosting a
 * fresh [FXBackstageMenuPane]; both are torn down after every test.
 */
abstract class AbstractMenuPaneUiTest {

    private val openStages: MutableList<Stage> = mutableListOf()

    /** Runs [block] on the FX application thread and returns its result, propagating failures. */
    protected fun <T> onFx(block: () -> T): T {
        if (Platform.isFxApplicationThread()) {
            return block()
        }
        val result = WaitForAsyncUtils.asyncFx(Callable { block() })
        WaitForAsyncUtils.waitForFxEvents()
        return result.get()
    }

    /** Pumps the FX event queue until all pending events are processed. */
    protected fun pumpFx() {
        WaitForAsyncUtils.waitForFxEvents()
    }

    /** Creates a stage hosting a fresh [FXMenuPane], shows it and returns it. Closed in [tearDown]. */
    protected fun showMenuPaneStage(): FXMenuPane = onFx {
        val menuPane = FXMenuPane()
        val stage = Stage()
        stage.scene = Scene(menuPane, 400.0, 60.0)
        stage.show()
        openStages += stage
        menuPane
    }.also { pumpFx() }

    /**
     * Creates a stage hosting a fresh [FXBackstageMenuPane], shows it and returns it. Closed in
     * [tearDown].
     */
    protected fun showBackstageMenuPaneStage(): FXBackstageMenuPane = onFx {
        val backstageMenuPane = FXBackstageMenuPane()
        val stage = Stage()
        stage.scene = Scene(backstageMenuPane, 500.0, 300.0)
        stage.show()
        openStages += stage
        backstageMenuPane
    }.also { pumpFx() }

    @AfterEach
    fun tearDown() {
        onFx {
            openStages.forEach(Stage::close)
            openStages.clear()
        }
        pumpFx()
    }

    companion object {

        @BeforeAll
        @JvmStatic
        fun bootToolkit() {
            // Mirror the Gradle system properties so the suite also runs headless from an IDE.
            System.setProperty("testfx.robot", "glass")
            System.setProperty("testfx.headless", "true")
            System.setProperty("glass.platform", "Monocle")
            System.setProperty("monocle.platform", "Headless")
            System.setProperty("prism.order", "sw")
            System.setProperty("java.awt.headless", "true")

            FxToolkit.registerPrimaryStage()
        }
    }
}
