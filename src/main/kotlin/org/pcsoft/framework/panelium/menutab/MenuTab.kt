package org.pcsoft.framework.panelium.menutab

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty

/**
 * A single tab registered on an [FXMenuTab]. Identified by [id], labelled by [title]; [disabled]
 * controls whether its tab-strip button can be activated.
 */
public class MenuTab(
    public val id: String,
    public val title: String,
) {

    public val disabled: BooleanProperty = SimpleBooleanProperty(this, "disabled", false)

    public var isDisabled: Boolean
        get() = disabled.get()
        set(value) = disabled.set(value)
}
