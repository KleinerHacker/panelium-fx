package org.pcsoft.framework.panelium.chrome.support

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.pcsoft.framework.panelium.chrome.ChromePane
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable

/**
 * Base class for the headless JavaFX UI tests of the chrome package. Boots the Monocle toolkit
 * once, offers [onFx] to run code on the FX application thread and [showChromeStage] to bring up a
 * transparent [ChromePane] stage that is torn down after every test.
 */
abstract class AbstractChromeUiTest {

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

    /**
     * Creates a transparent stage hosting a fresh [ChromePane] (built through [factory]), shows it
     * and returns the pane together with its stage. The stage is closed in [tearDown].
     */
    protected fun showChromeStage(
        title: String = "Chrome Test Window",
        width: Double = 480.0,
        height: Double = 320.0,
        content: () -> Region = { Region() },
        factory: (Region) -> ChromePane = { ChromePane(it) },
    ): ChromeStage = onFx {
        val pane = factory(content())
        val stage = Stage()
        stage.title = title
        val scene = Scene(pane, width, height)
        stage.scene = scene
        stage.x = 120.0
        stage.y = 90.0
        stage.width = width
        stage.height = height
        pane.attachStage(stage)
        stage.show()
        openStages += stage
        ChromeStage(pane, stage)
    }.also { pumpFx() }

    /** Registers [stage] for automatic teardown after the current test. */
    protected fun trackStage(stage: Stage) {
        onFx { openStages += stage }
    }

    @AfterEach
    fun tearDown() {
        onFx {
            openStages.forEach(Stage::close)
            openStages.clear()
        }
        pumpFx()
    }

    /** A shown [ChromePane] and the [Stage] it is attached to. */
    data class ChromeStage(val pane: ChromePane, val stage: Stage)

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
