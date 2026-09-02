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
public enum class ChromeBorderStrokeStyle {
    SOLID,
    DASHED,
    DOTTED,
}
