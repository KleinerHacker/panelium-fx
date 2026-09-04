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
- `activate(tab)`: activates a tab that is already in `tabs` or `contextualTabs`.
- `MenuTab.disabled`: disables the matching tab-strip button.
- Left/right arrow keys switch tabs (with wrap-around) while the strip is focused.

### Contextual tabs

`contextualTabs` is a second, ordered list of `MenuTab` entries that are only relevant to a
particular context (e.g. a selected table). They render after the permanent `tabs`, in
insertion order:

```kotlin
val design = MenuTab(id = "design", title = "Design")
menuTab.contextualTabs.add(design)
menuTab.activate(design)
```

- Removing the active contextual tab activates the permanent tab that was active before the
  contextual tab was activated (or `null`, if none was).
- `ContextTabGroup(name, color)` groups contextual tabs under a shared header rendered in the
  tab strip. `color` is data only for now; the visual color styling lands with the CSS API.
- `FXMenuTab.assignToGroup(tab, group)` / `groupOf(tab)`: assign a contextual tab to a group,
  or read its current group assignment.

Planned topics for this page:

- Add the dependency.
- Create a MenuPane and attach it to a scene.
- Define tabs, groups and action controls.
- React to actions.
