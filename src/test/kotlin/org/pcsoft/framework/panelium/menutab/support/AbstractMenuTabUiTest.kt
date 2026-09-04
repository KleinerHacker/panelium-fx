package org.pcsoft.framework.panelium.menutab.support

import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.pcsoft.framework.panelium.menutab.FXMenuTab
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable

/**
 * Base class for the headless JavaFX UI tests of the menutab package. Boots the Monocle toolkit
 * once, offers [onFx] to run code on the FX application thread and [showMenuTabStage] to bring up
 * a stage hosting a fresh [FXMenuTab] that is torn down after every test.
 */
abstract class AbstractMenuTabUiTest {

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

    /** Creates a stage hosting a fresh [FXMenuTab], shows it and returns it. Closed in [tearDown]. */
    protected fun showMenuTabStage(): FXMenuTab = onFx {
        val menuTab = FXMenuTab()
        val stage = Stage()
        stage.scene = Scene(menuTab, 400.0, 60.0)
        stage.show()
        openStages += stage
        menuTab
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
