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

package org.pcsoft.framework.panelium.internal

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.PropertyResourceBundle
import java.util.ResourceBundle

/**
 * Covers [PaneliumI18n]: locale-aware lookup of the library's user-visible strings, the English
 * fallback for unknown keys, and the completeness of every shipped translation bundle.
 */
class PaneliumI18nTest {

    private var savedLocale: Locale = Locale.getDefault()

    /** Records the machine locale and clears the bundle cache so each case starts clean. */
    @BeforeEach
    fun setUp() {
        savedLocale = Locale.getDefault()
        ResourceBundle.clearCache()
    }

    /** Restores the machine locale and clears the bundle cache again. */
    @AfterEach
    fun tearDown() {
        Locale.setDefault(savedLocale)
        ResourceBundle.clearCache()
    }

    /**
     * Use case: with the default locale set to English, a known key resolves to the value from the
     * base bundle.
     */
    @Test
    fun `known key resolves against the English base bundle`() {
        Locale.setDefault(Locale.ENGLISH)

        assertEquals("Close", PaneliumI18n.string("window.menu.close", "fallback"))
        assertEquals("Collapse", PaneliumI18n.string("menupane.contextmenu.collapse", "fallback"))
    }

    /**
     * Use case: switching the default locale to German makes the same keys resolve to the German
     * translation, proving the lookup follows the current locale.
     */
    @Test
    fun `known key resolves against the German bundle when the locale is German`() {
        Locale.setDefault(Locale.GERMAN)

        assertEquals("Schließen", PaneliumI18n.string("window.menu.close", "fallback"))
        assertEquals("Reduzieren", PaneliumI18n.string("menupane.contextmenu.collapse", "fallback"))
    }

    /**
     * Use case: a key that exists in no bundle returns the supplied English fallback unchanged
     * instead of raising, so a stripped or broken resource never blanks the UI.
     */
    @Test
    fun `unknown key returns the supplied fallback`() {
        Locale.setDefault(Locale.ENGLISH)

        assertEquals("My Fallback", PaneliumI18n.string("does.not.exist", "My Fallback"))
    }

    /**
     * Use case: an unresolvable locale falls back to the English base bundle rather than the
     * fallback argument, because the base bundle is always present.
     */
    @Test
    fun `unsupported locale falls back to the English base bundle`() {
        Locale.setDefault(Locale.forLanguageTag("xx"))

        assertEquals("Close", PaneliumI18n.string("window.menu.close", "fallback"))
    }

    /**
     * Use case: every shipped translation bundle defines exactly the same key set as the English
     * base bundle - no missing key (which would silently fall back to English) and no stray key.
     */
    @Test
    fun `every translation bundle carries the full key set`() {
        val baseKeys = keysOf("")
        assertTrue(baseKeys.isNotEmpty(), "the base bundle must define keys")

        for (suffix in TRANSLATION_SUFFIXES) {
            assertEquals(
                baseKeys,
                keysOf("_$suffix"),
                "bundle panelium-strings_$suffix.properties must define the same keys as the base bundle",
            )
        }
    }

    private companion object {
        val TRANSLATION_SUFFIXES = listOf(
            "af", "am", "ar", "az", "be", "bg", "bn", "ca", "cs", "da", "de", "el", "es", "et", "fa",
            "fi", "fil", "fr", "ga", "gu", "ha", "he", "hi", "hr", "hu", "hy", "id", "ig", "is", "it",
            "ja", "ka", "kk", "km", "kn", "ko", "lo", "lt", "lv", "mk", "ml", "mn", "mr", "ms", "my",
            "nb", "ne", "nl", "pa", "pl", "pt", "pt_BR", "ro", "ru", "si", "sk", "sl", "sq", "sr",
            "sv", "sw", "ta", "te", "th", "tr", "uk", "ur", "uz", "vi", "yo", "zh_CN", "zh_TW", "zu",
        )
    }

    private fun keysOf(suffix: String): Set<String> {
        val path = "org/pcsoft/framework/panelium/internal/panelium-strings$suffix.properties"
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: error("missing resource bundle: $path")
        stream.use {
            val bundle = PropertyResourceBundle(InputStreamReader(it, StandardCharsets.UTF_8))
            return bundle.keySet()
        }
    }
}
