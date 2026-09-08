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
 * A named, colored group that one or more contextual [FXMenuTab]s can be assigned to via
 * [FXMenuPane.assignToGroup]. Rendered as a header above its tabs in the tab strip. [color] is any
 * value JavaFX can parse as a colour (`#rrggbb`, `rgb(...)`, a named colour); it is applied to the
 * group header and to the accent of its tab-strip buttons. A value JavaFX cannot parse is ignored,
 * leaving the default look.
 */
class FXMenuContextTabGroup(
    val name: String,
    val color: String,
)
