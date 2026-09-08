# MenuPane - Implementation

!!! note
    The full MenuPane control is not implemented yet. This page describes the current
    building block, `FXMenuPane`, and will grow as further pieces land.

MenuPane will provide a menu pane that arranges its content into tabs; each tab
contains groups; each group contains the actual action controls.

## FXMenuPane

`FXMenuPane` (package `org.pcsoft.framework.panelium.menupane`) renders the tab strip: a
row of `FXMenuTab` entries, one of which can be active at a time.

```kotlin
val menuPane = FXMenuPane()
val home = FXMenuTab(id = "home", title = "Home")
val edit = FXMenuTab(id = "edit", title = "Edit")
menuPane.tabs.addAll(home, edit)
menuPane.activate(home)
```

- `tabs`: the permanent, ordered list of registered `FXMenuTab` entries.
- `activeTab` / `activeTabProperty()`: the currently selected tab, or `null`.
- `activate(tab)`: activates a tab that is already in `tabs` or `contextualTabs`.
- `FXMenuTab.disabled`: disables the matching tab-strip button.
- Left/right arrow keys switch tabs (with wrap-around) while the strip is focused.

### File tab

`fileTab` is the distinguished first tab (the "File" menu). It is held in its own slot, kept out
of `tabs` and the merged visible-tabs list, and rendered as a separate button pinned before the
strip, so it never scrolls and is never reached by arrow-key navigation:

```kotlin
menuPane.fileTab = FXMenuTab(id = "file", title = "File")
menuPane.backstageContent = buildBackstagePanel()
```

- `fileTab` / `fileTabProperty()`: the file tab, or `null` for none.
- `backstageContent` / `backstageContentProperty()`: the application-supplied panel the file tab's
  backstage shows.
- `FXMenuTab.disabled` on the file tab disables its button, exactly like a strip tab.

### Backstage overlay

Clicking the file-tab button opens the backstage: `isFileTabActive` / `fileTabActiveProperty()`
flip to `true` and `backstageContent` is shown.

```kotlin
menuPane.onBackstageClosed = { restoreRibbonCollapseState() }
menuPane.isFileTabActive = true              // same as clicking the File button
```

- A standalone `FXMenuPane` fades the backstage panel in over 0.3 seconds as an unmanaged layer
  that starts just below the tab-strip row and reaches to the bottom of the scene, so it never
  enlarges the ribbon band and never covers the pressed file-tab button. Give the backstage panel
  `maxWidth` / `maxHeight` of `Double.MAX_VALUE` to make it fill that layer.
- Docked in a `MenuChromePane` (see *Docking into Platinum Chrome*) the backstage is instead
  painted as an overlay across the whole window body; the docking wires this up automatically and
  there is nothing to configure.
- The backstage closes on Escape, on a click outside its content, or when a strip tab is selected
  (by click or arrow key). The strip tab that was active when it opened is restored.
- `onBackstageClosed` runs after the backstage has closed and the previous tab is restored, so a
  host can restore the ribbon's prior collapse state.

### Contextual tabs

`contextualTabs` is a second, ordered list of `FXMenuTab` entries that are only relevant to a
particular context (e.g. a selected table). They render after the permanent `tabs`, in
insertion order:

```kotlin
val design = FXMenuTab(id = "design", title = "Design")
menuPane.contextualTabs.add(design)
menuPane.activate(design)
```

- Removing the active contextual tab activates the permanent tab that was active before the
  contextual tab was activated (or `null`, if none was).
- `FXMenuContextTabGroup(name, color)` groups contextual tabs under a shared header rendered in the
  tab strip. `color` is applied to the header and the group's tab-button accent (see *Styling and
  CSS API*); an unparseable value is ignored.
- `FXMenuPane.assignToGroup(tab, group)` / `groupOf(tab)`: assign a contextual tab to a group,
  or read its current group assignment.

### Groups

Every regular `FXMenuTab` owns an ordered list of `FXMenuGroup`s in `FXMenuTab.groups`. The groups of
the active regular tab are rendered in the group strip directly below the tab-strip row:

```kotlin
val paste = FXMenuGroupLargeBox(Button("Paste"))
val clipboard = FXMenuGroup(
    paste,
    FXMenuGroupSmallBox(Button("Cut"), Button("Copy")),
    anchor = paste,
).apply { title = "Clipboard" }
home.groups.add(clipboard)
```

- `FXMenuGroup.title` / `titleProperty()`: the caption shown below the group's controls.
- `FXMenuGroup.content`: the ordered layout boxes the group arranges; edits show up live while the
  owning tab is active.
- `FXMenuTab.groups`: add, remove or reorder groups through the list directly; the group strip
  follows.
- Switching the active tab swaps the group strip to the new tab's groups. The strip is empty while
  the file-tab backstage is open and is restored when it closes.
- The group strip has a fixed height, so the ribbon band keeps the same height no matter which tab's
  groups it currently shows. Change it from a scene stylesheet through `-fx-pref-height` /
  `-fx-min-height` on `.menu-pane-group-strip`.

### Group layout boxes

`FXMenuGroup.content` hosts only the two ribbon layout boxes - it never takes loose controls, so
every control is wrapped in one of them. Both extend `FXMenuGroupBox` (a JavaFX `Pane`) and are
laid out in a row. A non-empty group must designate exactly one of its boxes as its **anchor** -
the box that is never collapsed by the overflow (see below). The mandatory constructor takes the
full ordered content plus the anchor:

```kotlin
val paste = FXMenuGroupLargeBox(Button("Paste"))
val clipboard = FXMenuGroup(
    paste,
    FXMenuGroupSmallBox(FXMenuGroupBoxPriority.HIGH, Button("Cut"), Button("Copy")),
    FXMenuGroupSmallBox(FXMenuGroupBoxPriority.LOW, Button("Format Painter")),
    anchor = paste,
).apply { title = "Clipboard" }
```

From FXML the anchor is an `<fx:reference>` to a box already declared in `<content>`:

```xml
<FXMenuTab id="home" title="Home">
    <groups>
        <FXMenuGroup title="Clipboard">
            <content>
                <FXMenuGroupLargeBox fx:id="paste"><Button text="Paste"/></FXMenuGroupLargeBox>
                <FXMenuGroupSmallBox priority="HIGH">
                    <Button text="Cut"/><Button text="Copy"/>
                </FXMenuGroupSmallBox>
            </content>
            <anchor><fx:reference source="paste"/></anchor>
        </FXMenuGroup>
    </groups>
</FXMenuTab>
```

- `FXMenuGroupLargeBox`: holds one prominent control and stretches it to fill the box in both
  directions (the box itself spans the full height of the group's content row), so a plain `Button`
  fills the whole slot. Style class `menu-group-large-box`.
- `FXMenuGroupSmallBox`: stacks up to `FXMenuGroupSmallBox.MAX_CONTROLS` (three) small controls
  vertically; each row is stretched to the box's full width and pinned to one third of the box
  height, so a box holding one or two controls keeps ribbon-sized rows and leaves the unused rows
  empty at the bottom. The constructor rejects more than three with `IllegalArgumentException`; a fourth
  child added afterwards is reported as an `IllegalStateException` on the FX thread's
  uncaught-exception handler. Style class `menu-group-small-box`.
- Both boxes extend `FXMenuGroupBox` and carry a `priority` (`FXMenuGroupBoxPriority`, default
  `MEDIUM`), set through the constructor or the `priority` property / FXML attribute.
- All boxes in a group carry equal `HBox` grow weight and an unbounded max width, so the group's
  content row divides its width evenly across the boxes whenever it is wider than they need (for
  example when the group caption is longer than the boxes).
- `FXMenuGroup.anchor` / `anchorProperty()`: the anchor box. It is a normal member of `content`
  (its position in the row is its index in `content`); the constructor rejects an `anchor` not
  contained in `content`, and removing the anchor box from `content` afterwards is rejected too
  (reorder by replacing the whole list).
- Because the group arranges its content horizontally, several `FXMenuGroupSmallBox` instances side
  by side form the columns of a group; large and small boxes can be mixed in one group.
- Both boxes extend `FXMenuGroupBox` (a JavaFX `Pane`) and can be used from FXML with their child
  controls nested inside.

### Group overflow

When the group strip cannot fit every group, the groups organise themselves as a whole instead of
shrinking evenly:

- Every group sits at its preferred width (it is not `HBox.hgrow`). A group the coordinator does not
  touch keeps its exact width and control sizes - other groups collapsing never resizes it.
- A strip-wide coordinator collapses whole `FXMenuGroupLargeBox` / `FXMenuGroupSmallBox` columns into
  their group's chevron popup, following a retention matrix: ascending `FXMenuGroupBoxPriority`
  (`LOW` first, then `MEDIUM`, then `HIGH`), then the rightmost group, then the rightmost box within
  that group.
- The group's `anchor` box is never a candidate, so at least one component always stays visible in
  every group, regardless of priority or available width.
- The hidden boxes move into a chevron button at the group's trailing edge (style class
  `menu-group-overflow-button`), shown only while that group has a collapsed box. Clicking it opens
  the boxes in a popup in their original order.
- Widening the window restores boxes in the reverse order while they still fit.
- If every collapsible box is already collapsed and the strip still does not fit, it is left
  overflowing and scrolls horizontally with the mouse wheel (the group strip is wrapped in a
  `menu-pane-group-strip-scroll-pane` `ScrollPane` with hidden scrollbars, mirroring the tab strip).
- `FXMenuGroup.isOverflowActive` / `overflowActiveProperty()` report whether that group currently has
  boxes in its popup.

### Group launcher

A group can carry a launcher button in its title row, following the ribbon convention of a small
dialog launcher at the bottom-right of the group.

- `FXMenuGroup.onLauncherAction` / `onLauncherActionProperty()` holds an `EventHandler<ActionEvent>`,
  following the JavaFX `onXxx` event convention. While it is non-`null` the launcher button (style
  class `menu-group-launcher`) is shown at the trailing edge of the title row; setting it back to
  `null` hides the button again.
- Clicking the button fires an `ActionEvent` to the handler. The button is keyboard focusable.
- Settable from FXML too: `onLauncherAction="#handlerMethod"` referencing a controller method.

### Disabled state

Tabs and groups can be disabled as a whole, independent of the standard per-control disabling
JavaFX already provides:

- `FXMenuTab.disabled` / `isDisabled`: disables the tab-strip button (or the file-tab button). A
  disabled tab can never become active - `activate(tab)`, setting `activeTab` and arrow-key
  navigation all skip it.
- `FXMenuGroup`: disable the whole group through the inherited JavaFX `disable` state
  (`setDisable(true)` / `disableProperty()`). JavaFX propagates this to every control in `content`
  and sets the `:disabled` pseudo-class on the group for styling.
- Individual controls inside a group keep using standard JavaFX disabling, unaffected by the
  group-level flag.

### Tab-strip scrolling

The tab strip is embedded in a horizontally scrolling viewport, so it stays fully usable even
when there are more tabs than fit the available width:

- No horizontal scrollbar is ever shown, not even while overflowing; the mouse wheel is the only
  visible interaction, scrolling the tab strip horizontally.
- Activating a tab, whether by click, code or arrow keys, scrolls it into view automatically.
- Tab buttons never shrink and there is no overflow menu; scrolling is the only way to reach
  tabs outside the visible area.

### Ribbon collapse/expand

The ribbon can be collapsed to just the tab-strip row and expanded again:

```kotlin
menuPane.isCollapsed = true                  // hide the group strip
menuPane.collapsedProperty().addListener { _, _, collapsed -> /* react */ }
```

- `isCollapsed` / `collapsedProperty()`: the collapse state. The user toggles it by double-clicking
  the active tab or with the chevron button at the trailing edge of the tab-strip row (style class
  `menu-pane-collapse-toggle`).
- While collapsed, a single click on a tab activates it and reveals its groups temporarily (a
  "peek") without changing `isCollapsed`. The peek closes on a click outside the ribbon or by
  clicking the peeking tab again; expanding also ends it.
- The collapse state is saved when the file-tab backstage opens and restored when it closes, so the
  backstage never leaves the ribbon in a different state than the user left it in.
- While collapsed the `collapsed` pseudo-class is set on the component for styling.
- `isCollapsible` / `collapsibleProperty()`: switch the whole feature off. While `false` the ribbon
  is forced expanded (setting `isCollapsed = true` is ignored and an already collapsed ribbon
  expands at once), the chevron button is hidden, the double-click gesture is inert and the ribbon
  context menu does not open. Defaults to `true`.

### Ribbon context menu

A right-click on the tab-strip row or the group strip opens a context menu at the cursor. It holds a
single entry that toggles `isCollapsed` - the same action as the chevron button - and whose label
follows the current state (`Collapse` while expanded, `Expand` while collapsed). The
menu needs no setup; the `ContextMenu` carries the style class `menu-pane-context-menu`.

### Styling and CSS API

`FXMenuPane.getUserAgentStylesheet()` returns a bundled default stylesheet (`menu-pane.css`), so the
ribbon has a complete look without an application stylesheet; a stylesheet added to the hosting
`Scene` overrides it by normal CSS precedence.

- Style classes on every part (`menu-pane`, `menu-pane-strip`, `menu-pane-strip-button`,
  `menu-pane-strip-file-button`, `menu-pane-collapse-toggle`, `menu-pane-context-group-header`,
  `menu-pane-group-strip`, `menu-group`, `menu-group-title`, `menu-group-launcher`,
  `menu-group-overflow-button`, `menu-group-large-box` / `menu-group-small-box`, `menu-pane-context-menu`).
- Pseudo-classes: `active` and `contextual` on a tab button, the standard JavaFX `disabled` on a
  disabled tab or group, `collapsed` on the component while the ribbon is collapsed.
- `FXMenuPane.accentColor` / `accentColorProperty()`, styleable as `-panelium-menu-pane-accent-color`
  on the `menu-pane` selector: the accent applied to contextual tab buttons that are not in a
  coloured `FXMenuContextTabGroup`.
- `FXMenuContextTabGroup.color` is applied to the group header and to that group's tab-button accent,
  taking precedence over `-panelium-menu-pane-accent-color`; an unparseable value is ignored.

See [Customize styles](customize-styles.md) for the full reference.

### Docking into Platinum Chrome

`MenuChromePane` (package `org.pcsoft.framework.panelium.chrome`) is the `ChromePane` subclass for
MenuPane windows. Put the `FXMenuPane` in its `menuPane` slot - it is docked directly below the
caption bar - and the rest of the window in `body`:

=== "Kotlin"

    ```kotlin
    val menuPane = FXMenuPane().apply {
        tabs.addAll(FXMenuTab("home", "Home"), FXMenuTab("view", "View"))
        activate(tabs.first())
    }
    val chrome = MenuChromePane().apply {
        this.menuPane = menuPane
        body = buildContent()
    }
    ```

=== "FXML"

    ```xml
    <?import org.pcsoft.framework.panelium.chrome.MenuChromePane?>
    <?import org.pcsoft.framework.panelium.menupane.FXMenuPane?>

    <MenuChromePane xmlns:fx="http://javafx.com/fxml">
        <menuPane>
            <FXMenuPane fx:id="menuPane"/>
        </menuPane>
        <body>
            <!-- window body -->
        </body>
    </MenuChromePane>
    ```

`MenuChromePane` paints the docked `FXMenuPane`'s file-tab backstage as an overlay over the `body`.
The caption bar and the docked tab (with its File button) stay visible, so the backstage can be
closed by clicking the File button again, pressing Escape, or clicking outside it. Do not set the
inherited `content` property on `MenuChromePane` - it holds the internal layout.

Without `MenuChromePane` an `FXMenuPane` can still be docked into a plain `ChromePane` by
composition - as the `top` of a `BorderPane` used as the `ChromePane` content - but then the
file-tab backstage uses the local fade.

Planned topics for this page:

- Add the dependency.
- Create a MenuPane and attach it to a scene.
- Define tabs, groups and action controls.
- React to actions.
