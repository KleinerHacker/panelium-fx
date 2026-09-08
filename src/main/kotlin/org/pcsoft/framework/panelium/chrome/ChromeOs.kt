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

/**
 * The host operating system as far as the window chrome cares: it decides the caption button side,
 * their order and which native look (`chrome-caption-buttons.css`) is applied. Detected once from
 * `os.name`; overridable per [ChromePane] (see [ChromePane.captionOsProperty]) for tests and demos.
 */
enum class ChromeOs {
    WINDOWS,
    MAC,
    LINUX,
    OTHER,
    ;

    /** Lower-case style-class suffix used both on the button box and inside the stylesheet. */
    internal val styleClass: String
        get() = name.lowercase()

    companion object {

        /** Resolves the current OS from the `os.name` system property, [OTHER] when unrecognised. */
        fun detect(): ChromeOs = fromOsName(System.getProperty("os.name").orEmpty())

        internal fun fromOsName(osName: String): ChromeOs {
            val name = osName.lowercase()
            return when {
                name.contains("win") -> WINDOWS
                name.contains("mac") || name.contains("darwin") -> MAC
                name.contains("nux") || name.contains("nix") || name.contains("aix") -> LINUX
                else -> OTHER
            }
        }
    }
}
