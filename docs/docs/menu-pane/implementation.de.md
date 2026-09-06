# MenuPane - Implementierung

!!! note
    Das vollständige MenuPane-Steuerelement ist noch nicht implementiert. Diese Seite
    beschreibt den aktuellen Baustein `FXMenuPane` und wächst, sobald weitere Teile
    (Gruppen, Gruppen-Layouts, Einklappen) dazukommen.

MenuPane wird eine Menüleiste bereitstellen, die ihren Inhalt in Tabs anordnet; jeder Tab
enthält Gruppen; jede Gruppe enthält die eigentlichen Aktions-Steuerelemente.

## FXMenuPane

`FXMenuPane` (Paket `org.pcsoft.framework.panelium.menupane`) rendert die Tableiste: eine
Reihe von `FXMenuTab`-Einträgen, von denen einer aktiv sein kann.

```kotlin
val menuPane = FXMenuPane()
val home = FXMenuTab(id = "home", title = "Home")
val edit = FXMenuTab(id = "edit", title = "Edit")
menuPane.tabs.addAll(home, edit)
menuPane.activate(home)
```

- `tabs`: die permanente, geordnete Liste der registrierten `FXMenuTab`-Einträge.
- `activeTab` / `activeTabProperty()`: der aktuell ausgewählte Tab oder `null`.
- `activate(tab)`: aktiviert einen bereits in `tabs` oder `contextualTabs` enthaltenen Tab.
- `FXMenuTab.disabled`: deaktiviert den zugehörigen Tab-Button.
- Pfeiltasten links/rechts wechseln den Tab (mit Umlauf), wenn die Tableiste fokussiert ist.

### Datei-Tab

`fileTab` ist der ausgezeichnete erste Tab (das "Datei"-Menü). Er liegt in einem eigenen Slot,
wird aus `tabs` und der zusammengeführten Liste sichtbarer Tabs herausgehalten und als separater
Button vor der Leiste fixiert gerendert - er scrollt also nie mit und wird von der
Pfeiltasten-Navigation nie erreicht:

```kotlin
menuPane.fileTab = FXMenuTab(id = "file", title = "File")
menuPane.backstageContent = buildBackstagePanel()
```

- `fileTab` / `fileTabProperty()`: der Datei-Tab oder `null`, wenn keiner gesetzt ist.
- `backstageContent` / `backstageContentProperty()`: das von der Anwendung bereitgestellte Panel,
  das die Backstage des Datei-Tabs zeigt.
- `FXMenuTab.disabled` am Datei-Tab deaktiviert dessen Button, genau wie bei einem Leisten-Tab.

### Backstage-Overlay

Ein Klick auf den Datei-Tab-Button öffnet die Backstage: `isFileTabActive` /
`fileTabActiveProperty()` wechseln auf `true` und `backstageContent` wird angezeigt.

```kotlin
menuPane.onBackstageClosed = { restoreRibbonCollapseState() }
menuPane.isFileTabActive = true              // wie ein Klick auf den Datei-Button
```

- Ein eigenständiges `FXMenuPane` blendet das Backstage-Panel über 0,3 Sekunden als ungemanagte
  Ebene ein, die direkt unterhalb der Tableisten-Zeile beginnt und bis zum unteren Szenenrand
  reicht - sie vergrößert also das Menüband nicht und verdeckt den gedrückten Datei-Tab-Button
  nicht. Damit das Panel diese Ebene füllt, `maxWidth` / `maxHeight` auf `Double.MAX_VALUE` setzen.
- In einem `MenuChromePane` angedockt (siehe *Andocken an Platinum Chrome*) wird die Backstage
  stattdessen als Overlay über den gesamten Fensterinhalt gezeichnet; das Andocken verdrahtet das
  automatisch, es gibt nichts zu konfigurieren.
- Die Backstage schließt bei Escape, bei einem Klick außerhalb ihres Inhalts oder wenn ein
  Leisten-Tab ausgewählt wird (per Klick oder Pfeiltaste). Der Leisten-Tab, der beim Öffnen aktiv
  war, wird wiederhergestellt.
- `onBackstageClosed` läuft, nachdem die Backstage geschlossen und der vorherige Tab
  wiederhergestellt wurde, sodass ein Host den vorherigen Collapse-Zustand des Ribbons
  wiederherstellen kann.

### Kontextuelle Tabs

`contextualTabs` ist eine zweite, geordnete Liste von `FXMenuTab`-Einträgen, die nur in einem
bestimmten Kontext relevant sind (z. B. eine ausgewählte Tabelle). Sie werden nach den
permanenten `tabs` gerendert, in Einfügereihenfolge:

```kotlin
val design = FXMenuTab(id = "design", title = "Design")
menuPane.contextualTabs.add(design)
menuPane.activate(design)
```

- Wird der aktive kontextuelle Tab entfernt, wird der permanente Tab aktiviert, der aktiv war,
  bevor der kontextuelle Tab aktiviert wurde (oder `null`, falls keiner aktiv war).
- `FXMenuContextTabGroup(name, color)` fasst kontextuelle Tabs unter einem gemeinsamen Header in der
  Tableiste zusammen. `color` ist vorerst nur Daten; die visuelle Farbgebung folgt mit der
  CSS-API.
- `FXMenuPane.assignToGroup(tab, group)` / `groupOf(tab)`: ordnet einen kontextuellen Tab einer
  Gruppe zu bzw. liest seine aktuelle Gruppenzuordnung.

### Gruppen

Jeder reguläre `FXMenuTab` besitzt in `FXMenuTab.groups` eine geordnete Liste von `FXMenuGroup`s. Die
Gruppen des aktiven regulären Tabs werden im Gruppenstreifen direkt unter der Tab-Streifenzeile
dargestellt:

```kotlin
val clipboard = FXMenuGroup().apply {
    title = "Clipboard"
    content.addAll(Button("Paste"), Button("Cut"), Button("Copy"))
}
home.groups.add(clipboard)
```

- `FXMenuGroup.title` / `titleProperty()`: die Beschriftung unter den Steuerelementen der Gruppe.
- `FXMenuGroup.content`: die geordneten Steuerelement-Knoten der Gruppe; Änderungen erscheinen
  live, solange der besitzende Tab aktiv ist.
- `FXMenuTab.groups`: Gruppen direkt über die Liste hinzufügen, entfernen oder umsortieren; der
  Gruppenstreifen folgt.
- Ein Wechsel des aktiven Tabs tauscht den Gruppenstreifen gegen die Gruppen des neuen Tabs. Der
  Streifen ist leer, während die Datei-Tab-Backstage offen ist, und wird beim Schließen
  wiederhergestellt.

### Deaktivierter Zustand

Tabs und Gruppen lassen sich als Ganzes deaktivieren, unabhängig vom standardmäßigen Deaktivieren
einzelner Steuerelemente, das JavaFX bereits bietet:

- `FXMenuTab.disabled` / `isDisabled`: deaktiviert den Tab-Button (bzw. den Datei-Tab-Button). Ein
  deaktivierter Tab kann nie aktiv werden - `activate(tab)`, das Setzen von `activeTab` und die
  Pfeiltasten-Navigation überspringen ihn.
- `FXMenuGroup`: die gesamte Gruppe wird über den geerbten JavaFX-`disable`-Zustand deaktiviert
  (`setDisable(true)` / `disableProperty()`). JavaFX überträgt das auf jedes Steuerelement in
  `content` und setzt die `:disabled`-Pseudoklasse auf der Gruppe für das Styling.
- Einzelne Steuerelemente innerhalb einer Gruppe nutzen weiterhin das standardmäßige
  JavaFX-Deaktivieren, unabhängig vom Gruppen-Flag.

### Scrollen der Tableiste

Die Tableiste ist in einen horizontal scrollenden Viewport eingebettet und bleibt so auch dann
vollständig nutzbar, wenn mehr Tabs vorhanden sind, als in die verfügbare Breite passen:

- Es wird nie eine horizontale Scrollbar angezeigt, auch nicht bei Überlauf; das Mausrad ist die
  einzige sichtbare Interaktion und scrollt die Tableiste horizontal.
- Wird ein Tab aktiviert - per Klick, Code oder Pfeiltasten -, wird automatisch zu ihm gescrollt.
- Die Tab-Buttons schrumpfen nie, und es gibt kein Überlauf-Menü; Scrollen ist der einzige Weg,
  um Tabs außerhalb des sichtbaren Bereichs zu erreichen.

### Andocken an Platinum Chrome

`MenuChromePane` (Paket `org.pcsoft.framework.panelium.chrome`) ist die `ChromePane`-Subklasse für
MenuPane-Fenster. Das `FXMenuPane` kommt in den `menuPane`-Slot - direkt unter der Titelleiste
angedockt - und der Rest des Fensters in `body`:

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
            <!-- Fensterinhalt -->
        </body>
    </MenuChromePane>
    ```

`MenuChromePane` zeichnet die Datei-Tab-Backstage des angedockten `FXMenuPane` als Overlay über dem
`body`. Die Titelleiste und das angedockte Tab (mit seinem Datei-Button) bleiben sichtbar, sodass
sich die Backstage per erneutem Klick auf den Datei-Button, per Escape oder per Klick außerhalb
schließen lässt. Die geerbte `content`-Property von `MenuChromePane` NICHT setzen - sie hält das
interne Layout.

Ohne `MenuChromePane` lässt sich ein `FXMenuPane` weiterhin per Komposition in ein einfaches
`ChromePane` andocken - als `top` eines `BorderPane`, das als `ChromePane`-Inhalt dient -, dann
nutzt die Datei-Tab-Backstage aber das lokale Einblenden.

Geplante Themen für diese Seite:

- Die Abhängigkeit hinzufügen.
- Ein MenuPane erstellen und an eine Scene anhängen.
- Tabs, Gruppen und Aktions-Steuerelemente definieren.
- Auf Aktionen reagieren.
