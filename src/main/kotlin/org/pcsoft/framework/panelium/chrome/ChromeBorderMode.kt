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
