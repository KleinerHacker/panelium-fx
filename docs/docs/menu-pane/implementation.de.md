# MenuPane - Implementierung

!!! note
    Das vollständige MenuPane-Steuerelement ist noch nicht implementiert. Diese Seite
    beschreibt den aktuellen Baustein `FXMenuTab` und wächst, sobald weitere Teile
    (Gruppen, kontextuelle Tabs, Docking) dazukommen.

MenuPane wird eine Menüleiste bereitstellen, die ihren Inhalt in Tabs anordnet; jeder Tab
enthält Gruppen; jede Gruppe enthält die eigentlichen Aktions-Steuerelemente.

## FXMenuTab

`FXMenuTab` (Paket `org.pcsoft.framework.panelium.menutab`) rendert die Tableiste: eine
Reihe von `MenuTab`-Einträgen, von denen einer aktiv sein kann.

```kotlin
val menuTab = FXMenuTab()
val home = MenuTab(id = "home", title = "Home")
val edit = MenuTab(id = "edit", title = "Edit")
menuTab.tabs.addAll(home, edit)
menuTab.activate(home)
```

- `tabs`: die permanente, geordnete Liste der registrierten `MenuTab`-Einträge.
- `activeTab` / `activeTabProperty()`: der aktuell ausgewählte Tab oder `null`.
- `activate(tab)`: aktiviert einen bereits in `tabs` oder `contextualTabs` enthaltenen Tab.
- `MenuTab.disabled`: deaktiviert den zugehörigen Tab-Button.
- Pfeiltasten links/rechts wechseln den Tab (mit Umlauf), wenn die Tableiste fokussiert ist.

### Kontextuelle Tabs

`contextualTabs` ist eine zweite, geordnete Liste von `MenuTab`-Einträgen, die nur in einem
bestimmten Kontext relevant sind (z. B. eine ausgewählte Tabelle). Sie werden nach den
permanenten `tabs` gerendert, in Einfügereihenfolge:

```kotlin
val design = MenuTab(id = "design", title = "Design")
menuTab.contextualTabs.add(design)
menuTab.activate(design)
```

- Wird der aktive kontextuelle Tab entfernt, wird der permanente Tab aktiviert, der aktiv war,
  bevor der kontextuelle Tab aktiviert wurde (oder `null`, falls keiner aktiv war).
- `ContextTabGroup(name, color)` fasst kontextuelle Tabs unter einem gemeinsamen Header in der
  Tableiste zusammen. `color` ist vorerst nur Daten; die visuelle Farbgebung folgt mit der
  CSS-API.
- `FXMenuTab.assignToGroup(tab, group)` / `groupOf(tab)`: ordnet einen kontextuellen Tab einer
  Gruppe zu bzw. liest seine aktuelle Gruppenzuordnung.

### Scrollen der Tableiste

Die Tableiste ist in einen horizontal scrollenden Viewport eingebettet und bleibt so auch dann
vollständig nutzbar, wenn mehr Tabs vorhanden sind, als in die verfügbare Breite passen:

- Es wird nie eine horizontale Scrollbar angezeigt, auch nicht bei Überlauf; das Mausrad ist die
  einzige sichtbare Interaktion und scrollt die Tableiste horizontal.
- Wird ein Tab aktiviert - per Klick, Code oder Pfeiltasten -, wird automatisch zu ihm gescrollt.
- Die Tab-Buttons schrumpfen nie, und es gibt kein Überlauf-Menü; Scrollen ist der einzige Weg,
  um Tabs außerhalb des sichtbaren Bereichs zu erreichen.

Geplante Themen für diese Seite:

- Die Abhängigkeit hinzufügen.
- Ein MenuPane erstellen und an eine Scene anhängen.
- Tabs, Gruppen und Aktions-Steuerelemente definieren.
- Auf Aktionen reagieren.
