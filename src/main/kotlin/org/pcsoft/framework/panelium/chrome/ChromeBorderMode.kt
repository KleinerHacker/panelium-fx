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
 * How the frame border of a [ChromePane] is composed across its four edges.
 *
 * [FLAT] paints all edges in one paint (`-panelium-border-color`). [RAISED] and [SUNKEN] render a
 * bevel: two edges take `-panelium-border-light-color`, the opposite two
 * `-panelium-border-dark-color`, so the window looks lifted off ([RAISED]) or pressed into
 * ([SUNKEN]) the desktop. A bevel reads best with a small or zero `-panelium-corner-radius`.
 *
 * Independent of the mode, the stroke's dash pattern, line cap, line join, miter limit and dash
 * offset follow `-panelium-border-style`, `-panelium-border-line-cap`,
 * `-panelium-border-line-join`, `-panelium-border-miter-limit` and `-panelium-border-dash-offset`.
 *
 * Selectable from CSS via `-panelium-border-mode` (`flat` / `raised` / `sunken`) or the
 * [ChromePane.borderModeProperty] API.
 */
enum class ChromeBorderMode {
    FLAT,
    RAISED,
    SUNKEN,
}
