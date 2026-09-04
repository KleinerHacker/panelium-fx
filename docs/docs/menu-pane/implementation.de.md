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
- `activate(tab)`: aktiviert einen bereits in `tabs` enthaltenen Tab.
- `MenuTab.disabled`: deaktiviert den zugehörigen Tab-Button.
- Pfeiltasten links/rechts wechseln den Tab (mit Umlauf), wenn die Tableiste fokussiert ist.

Geplante Themen für diese Seite:

- Die Abhängigkeit hinzufügen.
- Ein MenuPane erstellen und an eine Scene anhängen.
- Tabs, Gruppen und Aktions-Steuerelemente definieren.
- Auf Aktionen reagieren.
