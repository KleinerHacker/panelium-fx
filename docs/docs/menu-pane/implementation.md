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

### File tab

`fileTab` is the distinguished first tab (the "File" menu). It is held in its own slot, kept out
of `tabs` and the merged visible-tabs list, and rendered as a separate button pinned before the
strip, so it never scrolls and is never reached by arrow-key navigation:

```kotlin
menuTab.fileTab = MenuTab(id = "file", title = "File")
menuTab.backstageContent = buildBackstagePanel()
```

- `fileTab` / `fileTabProperty()`: the file tab, or `null` for none.
- `backstageContent` / `backstageContentProperty()`: the application-supplied panel the file tab's
  backstage will show. It is only stored for now; activation and the full-window overlay land in a
  later step.
- `MenuTab.disabled` on the file tab disables its button, exactly like a strip tab.

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

### Tab-strip scrolling

The tab strip is embedded in a horizontally scrolling viewport, so it stays fully usable even
when there are more tabs than fit the available width:

- No horizontal scrollbar is ever shown, not even while overflowing; the mouse wheel is the only
  visible interaction, scrolling the tab strip horizontally.
- Activating a tab, whether by click, code or arrow keys, scrolls it into view automatically.
- Tab buttons never shrink and there is no overflow menu; scrolling is the only way to reach
  tabs outside the visible area.

Planned topics for this page:

- Add the dependency.
- Create a MenuPane and attach it to a scene.
- Define tabs, groups and action controls.
- React to actions.
