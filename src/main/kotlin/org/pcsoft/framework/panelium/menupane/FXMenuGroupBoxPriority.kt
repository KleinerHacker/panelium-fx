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

package org.pcsoft.framework.panelium.menupane

/**
 * Retention priority of a non-anchor group layout box ([FXMenuGroupLargeBox] / [FXMenuGroupSmallBox])
 * when the group strip runs out of width. The overflow coordinator collapses boxes into the chevron
 * popup in ascending priority order - [LOW] boxes give way first, [HIGH] boxes last. Ordering across
 * boxes of equal priority: the rightmost group first, then the rightmost box within that group.
 *
 * The always-visible element is not expressed here but structurally, through the mandatory
 * [FXMenuGroup] anchor box, which is never collapsed regardless of its priority.
 */
enum class FXMenuGroupBoxPriority {

    /** Collapses first. */
    LOW,

    /** The default - collapses after [LOW] and before [HIGH]. */
    MEDIUM,

    /** Collapses last, only once every [LOW] and [MEDIUM] box in the strip is already collapsed. */
    HIGH,
}
