# MenuPane - Implementierung

!!! note
    Das vollständige MenuPane-Steuerelement ist noch nicht implementiert. Diese Seite
    beschreibt den aktuellen Baustein `FXMenuPane` und wächst, sobald weitere Teile
    dazukommen.

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
  Tableiste zusammen. `color` wird auf den Header und den Tab-Button-Akzent der Gruppe angewendet
  (siehe *Styling und CSS-API*); ein nicht interpretierbarer Wert wird ignoriert.
- `FXMenuPane.assignToGroup(tab, group)` / `groupOf(tab)`: ordnet einen kontextuellen Tab einer
  Gruppe zu bzw. liest seine aktuelle Gruppenzuordnung.

### Gruppen

Jeder reguläre `FXMenuTab` besitzt in `FXMenuTab.groups` eine geordnete Liste von `FXMenuGroup`s. Die
Gruppen des aktiven regulären Tabs werden im Gruppenstreifen direkt unter der Tab-Streifenzeile
dargestellt:

```kotlin
val paste = FXMenuGroupLargeBox(Button("Paste"))
val clipboard = FXMenuGroup(
    paste,
    FXMenuGroupSmallBox(Button("Cut"), Button("Copy")),
    anchor = paste,
).apply { title = "Clipboard" }
home.groups.add(clipboard)
```

- `FXMenuGroup.title` / `titleProperty()`: die Beschriftung unter den Steuerelementen der Gruppe.
- `FXMenuGroup.content`: die geordneten Layout-Boxen der Gruppe; Änderungen erscheinen live,
  solange der besitzende Tab aktiv ist.
- `FXMenuTab.groups`: Gruppen direkt über die Liste hinzufügen, entfernen oder umsortieren; der
  Gruppenstreifen folgt.
- Ein Wechsel des aktiven Tabs tauscht den Gruppenstreifen gegen die Gruppen des neuen Tabs. Der
  Streifen ist leer, während die Datei-Tab-Backstage offen ist, und wird beim Schließen
  wiederhergestellt.

### Gruppen-Layout-Boxen

`FXMenuGroup.content` nimmt ausschließlich die zwei Ribbon-Layout-Boxen auf - lose Steuerelemente
sind nicht erlaubt, jedes Control wird also in eine davon gepackt. Beide erweitern `FXMenuGroupBox`
(eine JavaFX-`Pane`) und werden in einer Reihe angeordnet. Eine nicht leere Gruppe muss genau eine
ihrer Boxen als **Anchor** benennen - die Box, die vom Overflow (siehe unten) nie eingeklappt wird.
Der Pflicht-Konstruktor nimmt den vollständigen, geordneten Inhalt plus den Anchor:

```kotlin
val paste = FXMenuGroupLargeBox(Button("Paste"))
val clipboard = FXMenuGroup(
    paste,
    FXMenuGroupSmallBox(FXMenuGroupBoxPriority.HIGH, Button("Cut"), Button("Copy")),
    FXMenuGroupSmallBox(FXMenuGroupBoxPriority.LOW, Button("Format Painter")),
    anchor = paste,
).apply { title = "Clipboard" }
```

Aus FXML ist der Anchor eine `<fx:reference>` auf eine bereits in `<content>` deklarierte Box:

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

- `FXMenuGroupLargeBox`: hält ein hervorgehobenes Steuerelement und streckt es in beide Richtungen
  auf die volle Boxgröße (die Box selbst spannt die volle Höhe der Content-Zeile der Gruppe), sodass
  ein einfacher `Button` den ganzen Slot ausfüllt. Style-Klasse `menu-group-large-box`.
- `FXMenuGroupSmallBox`: stapelt bis zu `FXMenuGroupSmallBox.MAX_CONTROLS` (drei) kleine
  Steuerelemente vertikal; jede Zeile wird auf die volle Boxbreite gestreckt und auf ein Drittel der
  Boxhöhe festgelegt, sodass eine Box mit einem oder zwei Steuerelementen Ribbon-große Zeilen behält
  und die ungenutzten Zeilen unten leer lässt. Der Konstruktor lehnt mehr als drei mit
  `IllegalArgumentException` ab; ein nachträglich hinzugefügtes viertes Kind wird als
  `IllegalStateException` über den
  Uncaught-Exception-Handler des FX-Threads gemeldet. Style-Klasse `menu-group-small-box`.
- Beide Boxen erweitern `FXMenuGroupBox` und tragen eine `priority` (`FXMenuGroupBoxPriority`,
  Default `MEDIUM`), gesetzt über den Konstruktor oder die `priority`-Property / das FXML-Attribut.
- Alle Boxen einer Gruppe tragen gleiches `HBox`-Gewicht und eine unbeschränkte Maximalbreite,
  sodass die Content-Zeile der Gruppe ihre Breite gleichmäßig auf die Boxen aufteilt, sobald sie
  breiter ist als die Boxen benötigen (etwa wenn die Gruppenbeschriftung länger ist als die Boxen).
- `FXMenuGroup.anchor` / `anchorProperty()`: die Anchor-Box. Sie ist ein normales Element von
  `content` (ihre Position in der Zeile ist ihr Index in `content`); der Konstruktor lehnt einen
  nicht in `content` enthaltenen `anchor` ab, ebenso das nachträgliche Entfernen der Anchor-Box aus
  `content` (zum Umsortieren die ganze Liste ersetzen).
- Da die Gruppe ihren Inhalt horizontal anordnet, bilden mehrere `FXMenuGroupSmallBox`-Instanzen
  nebeneinander die Spalten einer Gruppe; große und kleine Boxen lassen sich in einer Gruppe
  mischen.
- Beide Boxen erweitern `FXMenuGroupBox` (eine JavaFX-`Pane`) und können aus FXML mit ihren
  verschachtelten Kind-Steuerelementen genutzt werden.

### Gruppen-Overflow

Wenn der Gruppenstreifen nicht alle Gruppen fassen kann, organisieren sich die Gruppen als Ganzes,
statt sich gleichmäßig zu stauchen:

- Jede Gruppe sitzt auf ihrer Pref-Breite (sie ist nicht `HBox.hgrow`). Eine Gruppe, die der
  Koordinator nicht anfasst, behält Breite und Control-Größen exakt - das Einklappen anderer Gruppen
  verändert sie nicht.
- Ein streifenweiter Koordinator klappt komplette `FXMenuGroupLargeBox` / `FXMenuGroupSmallBox`-Spalten
  in das Chevron-Popup ihrer Gruppe ein, nach einer Erhaltungsmatrix: aufsteigende
  `FXMenuGroupBoxPriority` (`LOW` zuerst, dann `MEDIUM`, dann `HIGH`), dann Gruppe von rechts, dann
  Box in der Gruppe von rechts.
- Die `anchor`-Box der Gruppe ist nie ein Kandidat - in jeder Gruppe bleibt immer mindestens eine
  Komponente sichtbar, unabhängig von Priorität und verfügbarer Breite.
- Die eingeklappten Boxen wandern in einen Chevron-Button am rechten Rand der Gruppe (Style-Klasse
  `menu-group-overflow-button`), sichtbar nur solange diese Gruppe eine eingeklappte Box hat. Ein
  Klick öffnet die Boxen in einem Popup in ihrer ursprünglichen Reihenfolge.
- Das Verbreitern des Fensters stellt die Boxen in umgekehrter Reihenfolge wieder her, solange sie
  passen.
- Ist bereits jede einklappbare Box eingeklappt und der Streifen passt trotzdem nicht, läuft er über
  und ist per Mausrad horizontal scrollbar (der Gruppenstreifen liegt in einer
  `menu-pane-group-strip-scroll-pane`-`ScrollPane` mit ausgeblendeten Scrollbalken, analog zur
  Tableiste).
- `FXMenuGroup.isOverflowActive` / `overflowActiveProperty()` geben an, ob diese Gruppe gerade Boxen
  im Popup hat.

### Gruppen-Launcher

Eine Gruppe kann in ihrer Titelzeile einen Launcher-Button tragen - nach Ribbon-Konvention ein
kleiner Dialog-Starter in der unteren rechten Ecke der Gruppe.

- `FXMenuGroup.onLauncherAction` / `onLauncherActionProperty()` hält einen
  `EventHandler<ActionEvent>` nach der JavaFX-`onXxx`-Event-Konvention. Solange er nicht `null` ist,
  wird der Launcher-Button (Style-Klasse `menu-group-launcher`) am hinteren Ende der Titelzeile
  angezeigt; ein Zurücksetzen auf `null` blendet ihn wieder aus.
- Ein Klick auf den Button feuert ein `ActionEvent` an den Handler. Der Button ist per Tastatur
  fokussierbar.
- Auch aus FXML setzbar: `onLauncherAction="#handlerMethode"` mit Verweis auf eine Controller-Methode.

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

### Ribbon ein-/ausklappen

Das Ribbon lässt sich auf die Tab-Streifenzeile einklappen und wieder ausklappen:

```kotlin
menuPane.isCollapsed = true                  // Gruppenstreifen ausblenden
menuPane.collapsedProperty().addListener { _, _, collapsed -> /* reagieren */ }
```

- `isCollapsed` / `collapsedProperty()`: der Collapse-Zustand. Der Nutzer schaltet ihn per
  Doppelklick auf den aktiven Tab oder mit dem Chevron-Button am rechten Rand der Tab-Streifenzeile
  um (Style-Klasse `menu-pane-collapse-toggle`).
- Im eingeklappten Zustand aktiviert ein einfacher Klick auf einen Tab diesen und blendet seine
  Gruppen vorübergehend ein (ein "Peek"), ohne `isCollapsed` zu ändern. Der Peek schließt bei einem
  Klick außerhalb des Ribbons oder bei erneutem Klick auf den Peek-Tab; auch das Ausklappen beendet
  ihn.
- Der Collapse-Zustand wird beim Öffnen der Datei-Tab-Backstage gesichert und beim Schließen
  wiederhergestellt, sodass die Backstage das Ribbon nie in einem anderen Zustand hinterlässt, als
  der Nutzer es verlassen hat.
- Im eingeklappten Zustand wird die `collapsed`-Pseudoklasse auf der Komponente gesetzt (für das
  Styling).
- `isCollapsible` / `collapsibleProperty()`: schaltet die Funktion komplett ab. Solange `false`,
  ist das Ribbon fest ausgeklappt (`isCollapsed = true` wird ignoriert, ein bereits eingeklapptes
  Ribbon klappt sofort aus), der Chevron-Button ist ausgeblendet, der Doppelklick wirkungslos und
  das Ribbon-Kontextmenü öffnet nicht. Standard ist `true`.

### Ribbon-Kontextmenü

Ein Rechtsklick auf die Tab-Leiste oder den Gruppenstreifen öffnet an der Cursor-Position ein
Kontextmenü. Es enthält einen einzigen Eintrag, der `isCollapsed` umschaltet - dieselbe Aktion wie
die Chevron-Schaltfläche - und dessen Beschriftung dem aktuellen Zustand folgt (`Collapse Ribbon` im
ausgeklappten, `Expand Ribbon` im eingeklappten Zustand). Das Menü benötigt keine Einrichtung; das
`ContextMenu` trägt die Style-Klasse `menu-pane-context-menu`.

### Styling und CSS-API

`FXMenuPane.getUserAgentStylesheet()` liefert ein gebündeltes Standard-Stylesheet (`menu-pane.css`),
sodass das Ribbon ohne Anwendungs-Stylesheet einen vollständigen Look hat; ein an die Host-`Scene`
angehängtes Stylesheet überschreibt es über die normale CSS-Priorität.

- Style-Klassen an jedem Teil (`menu-pane`, `menu-pane-strip`, `menu-pane-strip-button`,
  `menu-pane-strip-file-button`, `menu-pane-collapse-toggle`, `menu-pane-context-group-header`,
  `menu-pane-group-strip`, `menu-group`, `menu-group-title`, `menu-group-launcher`,
  `menu-group-overflow-button`, `menu-group-large-box` / `menu-group-small-box`, `menu-pane-context-menu`).
- Pseudoklassen: `active` und `contextual` an einem Tab-Button, das Standard-JavaFX-`disabled` an
  einem deaktivierten Tab oder einer deaktivierten Gruppe, `collapsed` an der Komponente, solange das
  Ribbon eingeklappt ist.
- `FXMenuPane.accentColor` / `accentColorProperty()`, styleable als
  `-panelium-menu-pane-accent-color` auf dem `menu-pane`-Selektor: der Akzent für kontextuelle
  Tab-Buttons, die nicht in einer gefärbten `FXMenuContextTabGroup` liegen.
- `FXMenuContextTabGroup.color` wird auf das Gruppen-Header-Label und den Tab-Button-Akzent dieser
  Gruppe angewendet und hat Vorrang vor `-panelium-menu-pane-accent-color`; ein nicht
  interpretierbarer Wert wird ignoriert.

Die vollständige Referenz steht unter [Styles anpassen](customize-styles.de.md).

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
