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
}
