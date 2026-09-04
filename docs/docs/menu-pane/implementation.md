# MenuPane - Implementation

!!! note
    The full MenuPane control is not implemented yet. This page describes the current
    building block, `FXMenuTab`, and will grow as further pieces (groups, contextual
    tabs, docking) land.

MenuPane will provide a menu pane that arranges its content into tabs; each tab
contains groups; each group contains the actual action controls.

## FXMenuTab

`FXMenuTab` (package `org.pcsoft.framework.panelium.menutab`) renders the tab strip: a
row of `MenuTab` entries, one of which can be active at a time.

```kotlin
val menuTab = FXMenuTab()
val home = MenuTab(id = "home", title = "Home")
val edit = MenuTab(id = "edit", title = "Edit")
menuTab.tabs.addAll(home, edit)
menuTab.activate(home)
```

- `tabs`: the permanent, ordered list of registered `MenuTab` entries.
- `activeTab` / `activeTabProperty()`: the currently selected tab, or `null`.
- `activate(tab)`: activates a tab that is already in `tabs`.
- `MenuTab.disabled`: disables the matching tab-strip button.
- Left/right arrow keys switch tabs (with wrap-around) while the strip is focused.

Planned topics for this page:

- Add the dependency.
- Create a MenuPane and attach it to a scene.
- Define tabs, groups and action controls.
- React to actions.
