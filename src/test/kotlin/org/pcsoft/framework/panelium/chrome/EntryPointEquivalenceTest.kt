package org.pcsoft.framework.panelium.chrome

import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.framework.panelium.chrome.support.AbstractChromeUiTest

/**
 * Covers that the three public entry points into the chrome - a bare [ChromePane] attached to a
 * stage, [PaneliumChrome.install] on an existing stage and a [PaneliumStage] - all produce the
 * same frame structure and the same window configuration.
 */
class EntryPointEquivalenceTest : AbstractChromeUiTest() {

    private data class FrameShape(
        val hasChromePaneClass: Boolean,
        val cssPropertyNames: List<String>,
        val hasCaptionBar: Boolean,
        val userAgentStylesheet: String?,
        val windowOpsPresent: Boolean,
        val stageStyle: StageStyle,
        val sceneRootIsPane: Boolean,
        val sceneFill: Color?,
    )

    private fun shapeOf(pane: ChromePane, stage: Stage): FrameShape = onFx {
        FrameShape(
            hasChromePaneClass = pane.styleClass.contains("chrome-pane"),
            cssPropertyNames = ChromePane.getClassCssMetaData().map { it.property }.filter { it.startsWith("-panelium-") }.sorted(),
            hasCaptionBar = true.and(pane.captionBar.styleClass.contains("chrome-caption-bar")),
            userAgentStylesheet = pane.userAgentStylesheet.substringAfterLast('/'),
            windowOpsPresent = pane.windowOps != null,
            stageStyle = stage.style,
            sceneRootIsPane = stage.scene.root === pane,
            sceneFill = stage.scene.fill as? Color,
        )
    }

    /**
     * Use case: a bare [ChromePane] and a [PaneliumStage] must be indistinguishable in frame
     * structure and window configuration once both are shown.
     */
    @Test
    fun `bare ChromePane matches PaneliumStage`() {
        assertEquals(paneliumStageShape(), bareChromePaneShape())
    }

    /**
     * Use case: turning an existing stage into a chrome window with [PaneliumChrome.install] must
     * yield the same frame structure and window configuration as a [PaneliumStage].
     */
    @Test
    fun `install on an existing stage matches PaneliumStage`() {
        assertEquals(paneliumStageShape(), installShape())
    }

    private fun bareChromePaneShape(): FrameShape {
        val result = onFx {
            val pane = ChromePane(Region())
            val stage = Stage()
            stage.initStyle(StageStyle.TRANSPARENT)
            val scene = Scene(pane, 400.0, 300.0)
            scene.fill = Color.TRANSPARENT
            stage.scene = scene
            pane.attachStage(stage)
            stage.show()
            trackStage(stage)
            pane to stage
        }
        pumpFx()
        return shapeOf(result.first, result.second)
    }

    private fun installShape(): FrameShape {
        val result = onFx {
            val stage = Stage()
            stage.scene = Scene(Region(), 400.0, 300.0)
            val pane = PaneliumChrome.install(stage)
            stage.show()
            trackStage(stage)
            pane to stage
        }
        pumpFx()
        return shapeOf(result.first, result.second)
    }

    private fun paneliumStageShape(): FrameShape {
        val result = onFx {
            val stage = PaneliumStage()
            stage.content = Region()
            stage.show()
            trackStage(stage)
            stage.chromePane to (stage as Stage)
        }
        pumpFx()
        return shapeOf(result.first, result.second)
    }
}
