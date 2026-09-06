package org.pcsoft.framework.panelium.menupane

/**
 * A named, colored group that one or more contextual [FXMenuTab]s can be assigned to via
 * [FXMenuPane.assignToGroup]. Rendered as a header above its tabs in the tab strip. [color] is
 * carried as data only for now; the actual color styling is applied in a later feature (CSS API).
 */
class FXMenuContextTabGroup(
    val name: String,
    val color: String,
)
