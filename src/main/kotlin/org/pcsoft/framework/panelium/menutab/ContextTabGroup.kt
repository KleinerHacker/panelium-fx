package org.pcsoft.framework.panelium.menutab

/**
 * A named, colored group that one or more contextual [MenuTab]s can be assigned to via
 * [FXMenuTab.assignToGroup]. Rendered as a header above its tabs in the tab strip. [color] is
 * carried as data only for now; the actual color styling is applied in a later feature (CSS API).
 */
class ContextTabGroup(
    val name: String,
    val color: String,
)
