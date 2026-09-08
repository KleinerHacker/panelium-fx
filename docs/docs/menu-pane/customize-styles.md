# MenuPane - Customize Styles

`FXMenuPane` ships a complete default look as a JavaFX *user-agent stylesheet*
(`FXMenuPane.getUserAgentStylesheet()`), so a ribbon is fully styled without any application
stylesheet. Every part carries stable style classes and pseudo-classes; a stylesheet you add to the
hosting `Scene` overrides the defaults through normal CSS precedence.

The default look leans on the platform theme (Modena): its colours are taken from the standard
looked-up colours (`-fx-background`, `-fx-body-color`, `-fx-outer-border`, `-fx-accent`,
`-fx-text-base-color`, ...) instead of fixed values, so the ribbon follows the application's
`-fx-base` and blends in with the rest of a Modena-themed scene. Changing `-fx-base` on the scene
re-tints the whole ribbon.

## Attach a stylesheet

```kotlin
val menuPane = FXMenuPane()
val scene = Scene(menuPane)
scene.stylesheets.add(
    javaClass.getResource("/my-app/menu-pane.css").toExternalForm(),
)
```

Docked in a `MenuChromePane`, add the stylesheet to the `Scene` that hosts the chrome in the same
way.

## Style classes

| Style class | Node |
| --- | --- |
| `menu-pane` | the component root (`FXMenuPane`) |
| `menu-pane-strip` | the tab-strip row of tab buttons |
| `menu-pane-strip-scroll-pane` | the scrolling viewport around the tab strip |
| `menu-pane-strip-button` | a single tab button |
| `menu-pane-strip-file-button` | the file-tab button |
| `menu-pane-collapse-toggle` | the collapse/expand chevron button |
| `menu-pane-collapse-toggle-icon` | the chevron icon region inside that button (`-fx-shape`; flips with the `:collapsed` state) |
| `menu-pane-context-group-header` | the header label above a contextual-tab group |
| `menu-pane-group-strip` | the group strip below the tab row |
| `menu-pane-group-strip-scroll-pane` | the scrolling viewport around the group strip |
| `menu-pane-context-menu` | the right-click ribbon context menu |
| `menu-group` | a single group (`FXMenuGroup`) |
| `menu-group-content` | the control row inside a group |
| `menu-group-title` | the caption label below a group |
| `menu-group-launcher` | the dialog-launcher button in a group's title row |
| `menu-group-launcher-icon` | the diagonal-arrow icon region inside that button (`-fx-shape`) |
| `menu-group-overflow-button` | the chevron button holding a group's collapsed boxes |
| `menu-group-large-box` / `menu-group-small-box` | the ribbon layout boxes |

```css
.menu-pane-strip-button:hover {
    -fx-background-color: rgba(0, 0, 0, 0.08);
}
```

`menu-pane-group-strip` carries a fixed default height so the ribbon band never changes height when
the active tab's groups differ in size. Override it with `-fx-pref-height` / `-fx-min-height`:

```css
.menu-pane-group-strip {
    -fx-pref-height: 120;
    -fx-min-height: 120;
}
```

## Pseudo-classes

| Pseudo-class | Node | Active while |
| --- | --- | --- |
| `:active` | `menu-pane-strip-button` | the tab is the active tab |
| `:contextual` | `menu-pane-strip-button` | the tab is one of `contextualTabs` |
| `:disabled` | `menu-pane-strip-button`, `menu-group` | the tab / group is disabled (standard JavaFX) |
| `:collapsed` | `menu-pane` | the ribbon is collapsed |

```css
.menu-pane:collapsed .menu-pane-group-strip {
    -fx-padding: 0;
}

.menu-pane-strip-button:active {
    -fx-font-weight: bold;
}
```

## Styleable properties

Set on the `menu-pane` selector. Every colour is a paint, so a `linear-gradient` works too.

| Property | Type | Default | Effect |
| --- | --- | --- | --- |
| `-panelium-menu-pane-accent-color` | paint | `#2b579a` | accent applied to contextual tab buttons that are not in a coloured group |

The property is also available in code as `FXMenuPane.accentColor` / `accentColorProperty()`.

```css
.menu-pane {
    -panelium-menu-pane-accent-color: #b7472a;
}
```

## Context group colour

`FXMenuContextTabGroup(name, color)` carries a per-group colour. `color` is any value JavaFX can
parse as a colour (`#rrggbb`, `rgb(...)`, a named colour); it is applied to the group header label
and to the accent of that group's tab buttons, taking precedence over
`-panelium-menu-pane-accent-color`. A value JavaFX cannot parse is ignored.

```kotlin
val tools = FXMenuContextTabGroup("Table Tools", "#b7472a")
menuPane.assignToGroup(designTab, tools)
```

## Theming (light and dark)

The default stylesheet follows the platform theme through the looked-up colours listed above, so
switching the application to a dark Modena base already carries the ribbon with it. For a fully
custom dark theme redefine the `menu-pane*` and `menu-group*` rules in an application stylesheet -
the style classes and pseudo-classes above are enough to replace the whole look, not just tweak it.
