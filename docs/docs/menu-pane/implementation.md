# MenuPane - Implementation

!!! note
    The full MenuPane control is not implemented yet. This page describes the current
    building block, `FXMenuTab`, and will grow as further pieces (groups, group
    layouts, collapsing) land.

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
  backstage shows.
- `MenuTab.disabled` on the file tab disables its button, exactly like a strip tab.

### Backstage overlay

Clicking the file-tab button opens the backstage: `isFileTabActive` / `fileTabActiveProperty()`
flip to `true` and `backstageContent` is shown.

```kotlin
menuTab.overlayHost = chromeBackstageHost   // optional; see below
menuTab.onBackstageClosed = { restoreRibbonCollapseState() }
menuTab.isFileTabActive = true              // same as clicking the File button
```

- Without an `overlayHost`, the backstage panel is faded in over 0.3 seconds as an unmanaged layer
  that starts just below the tab-strip row and reaches to the bottom of the scene, so it never
  enlarges the ribbon band and never covers the pressed file-tab button. With one set, `FXMenuTab`
  instead calls `BackstageOverlayHost.showOverlay(...)` / `hideOverlay()` so the host chrome can
  paint it above the whole window. `MenuChromePane` (see *Docking into Platinum Chrome*) sets this
  `overlayHost` for you, so a docked `FXMenuTab` paints its backstage across the window body
  automatically. Give the backstage panel `maxWidth` / `maxHeight` of `Double.MAX_VALUE` to make it
  fill that layer.
- The backstage closes on Escape, on a click outside its content, or when a strip tab is selected
  (by click or arrow key). The strip tab that was active when it opened is restored.
- `onBackstageClosed` runs after the backstage has closed and the previous tab is restored, so a
  host can restore the ribbon's prior collapse state.

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

### Groups

Every regular `MenuTab` owns an ordered list of `FXMenuGroup`s in `MenuTab.groups`. The groups of
the active regular tab are rendered in the group strip directly below the tab-strip row:

```kotlin
val clipboard = FXMenuGroup().apply {
    title = "Clipboard"
    content.addAll(Button("Paste"), Button("Cut"), Button("Copy"))
}
home.groups.add(clipboard)
```

- `FXMenuGroup.title` / `titleProperty()`: the caption shown below the group's controls.
- `FXMenuGroup.content`: the ordered control nodes the group arranges; edits show up live while the
  owning tab is active.
- `MenuTab.groups`: add, remove or reorder groups through the list directly; the group strip
  follows.
- Switching the active tab swaps the group strip to the new tab's groups. The strip is empty while
  the file-tab backstage is open and is restored when it closes.

### Tab-strip scrolling

The tab strip is embedded in a horizontally scrolling viewport, so it stays fully usable even
when there are more tabs than fit the available width:

- No horizontal scrollbar is ever shown, not even while overflowing; the mouse wheel is the only
  visible interaction, scrolling the tab strip horizontally.
- Activating a tab, whether by click, code or arrow keys, scrolls it into view automatically.
- Tab buttons never shrink and there is no overflow menu; scrolling is the only way to reach
  tabs outside the visible area.

### Docking into Platinum Chrome

`MenuChromePane` (package `org.pcsoft.framework.panelium.chrome`) is the `ChromePane` subclass for
MenuPane windows. Put the `FXMenuTab` in its `menuTab` slot - it is docked directly below the
caption bar - and the rest of the window in `body`:

=== "Kotlin"

    ```kotlin
    val menuTab = FXMenuTab().apply {
        tabs.addAll(MenuTab("home", "Home"), MenuTab("view", "View"))
        activate(tabs.first())
    }
    val chrome = MenuChromePane().apply {
        this.menuTab = menuTab
        body = buildContent()
    }
    ```

=== "FXML"

    ```xml
    <?import org.pcsoft.framework.panelium.chrome.MenuChromePane?>
    <?import org.pcsoft.framework.panelium.menutab.FXMenuTab?>

    <MenuChromePane xmlns:fx="http://javafx.com/fxml">
        <menuTab>
            <FXMenuTab fx:id="menuTab"/>
        </menuTab>
        <body>
            <!-- window body -->
        </body>
    </MenuChromePane>
    ```

`MenuChromePane` also wires the docked `FXMenuTab`'s `overlayHost` to itself, so the file-tab
backstage is painted as an overlay over the `body`. The caption bar and the docked tab (with its
File button) stay visible, so the backstage can be closed by clicking the File button again,
pressing Escape, or clicking outside it. Do not set the inherited `content` property on
`MenuChromePane` - it holds the internal layout.

Without `MenuChromePane` an `FXMenuTab` can still be docked into a plain `ChromePane` by
composition - as the `top` of a `BorderPane` used as the `ChromePane` content - but then the
file-tab backstage uses the local fade unless an `overlayHost` is set explicitly.

Planned topics for this page:

- Add the dependency.
- Create a MenuPane and attach it to a scene.
- Define tabs, groups and action controls.
- React to actions.
