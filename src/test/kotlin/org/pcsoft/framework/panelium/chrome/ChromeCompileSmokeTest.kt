package org.pcsoft.framework.panelium.chrome

import javafx.stage.Stage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChromeCompileSmokeTest {

    /**
     * Use case: the chrome package's public entry points must exist with the signatures the
     * rest of the library and its consumers rely on, without requiring a running JavaFX
     * toolkit to verify it.
     */
    @Test
    fun `chrome entry points are present with the expected shape`() {
        val installMethod = PaneliumChrome::class.java.getMethod("install", Stage::class.java)
        assertEquals(ChromePane::class.java, installMethod.returnType)

        assertTrue(Stage::class.java.isAssignableFrom(PaneliumStage::class.java))

        val contentGetter = ChromePane::class.java.getMethod("getContent")
        assertEquals(javafx.scene.Node::class.java, contentGetter.returnType)
    }

    /**
     * Use case: an application must be able to restyle every frame part through CSS, so
     * `ChromePane` has to declare all five `-panelium-*` styleable properties in its class
     * metadata and ship a default look through a bundled user-agent stylesheet. Both are
     * checked without a JavaFX toolkit.
     */
    @Test
    fun `ChromePane exposes the styling API and a default stylesheet`() {
        val names = ChromePane.getClassCssMetaData().map { it.property }
        assertTrue(
            names.containsAll(
                listOf(
                    "-panelium-shadow-radius",
                    "-panelium-shadow-color",
                    "-panelium-corner-radius",
                    "-panelium-resize-border",
                    "-panelium-caption-min-height",
                ),
            ),
            "expected all -panelium-* properties, got $names",
        )

        val stylesheet = ChromePane::class.java.getResource("chrome.css")
        assertTrue(stylesheet != null, "the bundled chrome.css user-agent stylesheet must be present")
    }
}
