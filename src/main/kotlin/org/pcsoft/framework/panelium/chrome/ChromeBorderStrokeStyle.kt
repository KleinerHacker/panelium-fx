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
 * The dash pattern of the [ChromePane] frame border, mirroring CSS `border-style`.
 *
 * [SOLID] draws a continuous line. [DASHED] and [DOTTED] derive their dash array from the current
 * `-panelium-border-width` so the rhythm scales with the border thickness; [DOTTED] combined with
 * `-panelium-border-line-cap: round` produces round dots.
 *
 * Selectable from CSS via `-panelium-border-style` (`solid` / `dashed` / `dotted`). The dash
 * offset is set separately through `-panelium-border-dash-offset`.
 */
enum class ChromeBorderStrokeStyle {
    SOLID,
    DASHED,
    DOTTED,
}
