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

import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

/**
 * Central lookup for the library's user-visible strings. Backed by the
 * `panelium-strings` resource bundle next to this class; the shipped translations cover the same
 * locales JavaFX itself localises its built-in controls for (English base plus `de`, `es`, `fr`,
 * `it`, `ja`, `ko`, `pt_BR`, `sv`, `zh_CN`, `zh_TW`).
 *
 * The bundle for the current [Locale.getDefault] is resolved on every call, so a locale switch at
 * runtime takes effect the next time a menu or label is rebuilt. Every lookup carries an English
 * [fallback] that is returned unchanged when the key or the whole bundle is missing, so a broken or
 * stripped resource never leaves the UI blank.
 */
internal object PaneliumI18n {

    private const val BUNDLE_NAME: String = "org.pcsoft.framework.panelium.internal.panelium-strings"

    /**
     * Returns the localised text for [key] in the current default locale, or [fallback] when the
     * key (or the bundle) cannot be resolved.
     */
    fun string(key: String, fallback: String): String =
        try {
            ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString(key)
        } catch (_: MissingResourceException) {
            fallback
        }
}
