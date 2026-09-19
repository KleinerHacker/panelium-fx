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
 * Where the default caption title sits relative to the [ChromePane.captionLeftItems]. The default
 * icon always stays at the very leading edge, unaffected by this setting.
 *
 * [NEXT_TO_LOGO] (the default) keeps the title directly next to the icon, with `captionLeftItems`
 * following. [AFTER_LEFT_ITEMS] moves the title behind `captionLeftItems` instead, while the icon
 * stays put. On macOS, "leading edge" still means after the caption buttons - only the relative
 * order of the title vs. `captionLeftItems` changes.
 *
 * Selectable via [ChromePane.captionTitlePositionProperty] / [ChromeCaptionBar.captionTitlePositionProperty].
 */
enum class ChromeCaptionTitlePosition {
    NEXT_TO_LOGO,
    AFTER_LEFT_ITEMS,
}
