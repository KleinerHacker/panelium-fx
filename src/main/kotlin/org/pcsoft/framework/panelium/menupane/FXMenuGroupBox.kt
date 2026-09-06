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

import javafx.beans.property.ObjectProperty

/**
 * Common type of the ribbon layout boxes that make up an [FXMenuGroup]'s content row -
 * [FXMenuGroupLargeBox] and [FXMenuGroupSmallBox]. Each box carries a retention [priority] that the
 * strip-wide overflow coordinator uses to decide which non-anchor boxes collapse into the chevron
 * popup first. An [FXMenuGroup]'s mandatory anchor element must be one of these.
 */
sealed interface FXMenuGroupBox {

    /** Retention priority when the group strip runs out of width. Ignored for a group's anchor box. */
    var priority: FXMenuGroupBoxPriority

    fun priorityProperty(): ObjectProperty<FXMenuGroupBoxPriority>
}
