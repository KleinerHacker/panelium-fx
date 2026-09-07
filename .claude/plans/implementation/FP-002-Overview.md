# Übersicht: FP-002-FXMenuPane

Feature Plan: `.claude/plans/features/FP-002-FXMenuPane.md`

## Implementierungspläne

| ID | Name | Datei |
|----|------|-------|
| IP-01 | MenuPaneCore | FP-002-IP-01-MenuPaneCore.md (COMPLETED) |
| IP-02 | ContextualTabs | FP-002-IP-02-ContextualTabs.md (COMPLETED) |
| IP-03 | TabStripScrolling | FP-002-IP-03-TabStripScrolling.md (COMPLETED) |
| IP-04 | FileMenuTab | FP-002-IP-04-FileMenuTab.md (COMPLETED) |
| IP-05 | BackstageOverlay | FP-002-IP-05-BackstageOverlay.md (COMPLETED) |
| IP-06 | Groups | FP-002-IP-06-Groups.md (COMPLETED) |
| IP-07 | GroupLayout | FP-002-IP-07-GroupLayout.md (COMPLETED) |
| IP-08 | GroupLauncher | FP-002-IP-08-GroupLauncher.md (COMPLETED) |
| IP-09 | GroupOverflow | FP-002-IP-09-GroupOverflow.md (COMPLETED) |
| IP-10 | DisabledState | FP-002-IP-10-DisabledState.md (COMPLETED) |
| IP-11 | ChromeDocking | FP-002-IP-11-ChromeDocking.md (COMPLETED) |
| IP-12 | ChromeOverlayHook | FP-002-IP-12-ChromeOverlayHook.md (COMPLETED) |
| IP-13 | CollapseAndExpand | FP-002-IP-13-CollapseAndExpand.md |
| IP-14 | RibbonContextMenu | FP-002-IP-14-RibbonContextMenu.md |
| IP-15 | StylingAndCssApi | FP-002-IP-15-StylingAndCssApi.md |
| IP-16 | TestHarnessAndCoverage | FP-002-IP-16-TestHarnessAndCoverage.md |

## Reihenfolge

1. IP-01 (Fundament, COMPLETED)
2. Parallel nach IP-01: IP-02, IP-03, IP-04, IP-06, IP-11
3. IP-05 nach IP-04
4. IP-07 nach IP-06; IP-08 nach IP-06 (parallel zu IP-07, COMPLETED)
5. IP-09 nach IP-06 und IP-07
6. IP-10 nach IP-01 und IP-06
7. IP-12 nach IP-05 und IP-11
8. IP-13 nach IP-01 und IP-11
9. IP-14 nach IP-13
10. IP-15 nach IP-01 bis IP-14
11. IP-16 nach IP-15

## Abgeschlossene Implementierungspläne

* IP-08 (GroupLauncher, COMPLETED): `FXMenuGroup.onLauncherAction` / `onLauncherActionProperty()`
  (`ObjectProperty<EventHandler<ActionEvent>?>`, JavaFX-`onXxx`-Event-Konvention, auch aus FXML als
  `onLauncherAction="#..."` setzbar) statt `Runnable` oder eigenem Callback-Interface; `null` = kein
  Launcher. `FXMenuGroupView.fxml` setzt das `menu-group-title`-Label in eine neue `titleRow`-`HBox`
  mit nachlaufendem `launcherButton` (Style-Klasse `menu-group-launcher`, Glyph `↘`);
  `visible`/`managed` an `onLauncherAction != null` gebunden, `onAction` direkt an den Handler des
  ViewModels gebunden. Showcase verdrahtet den Launcher der "Font"-Gruppe im Controller auf ein
  `launcherLabel`. Styling folgt in IP-15. `MenuGroupStripOverflowTest`-Filter schließt jetzt auch
  `menu-group-launcher` aus; neuer Headless-Test `FXMenuGroupLauncherTest`. Doku (EN + DE) und
  CHANGELOG ergänzt.

* IP-09 (GroupOverflow, COMPLETED): Overflow je Gruppe, nur ganze Layout-Boxen
  (`FXMenuGroupLargeBox` / `FXMenuGroupSmallBox`) - lose Knoten bleiben immer sichtbar. Neuer
  interner `MenuGroupOverflowController` unter `org.pcsoft.framework.panelium.menupane` (nicht
  `chrome/menupane` wie im Stub). `FXMenuGroupView` erhielt einen Chevron-Button
  (`menu-group-overflow-button`) und ein `ContextMenu` mit den eingeklappten Boxen in
  Originalreihenfolge. Der Controller pinnt `menu-group-content` `prefWidth` auf die volle
  Wunschbreite; `FXMenuGroup` ist jetzt `HBox.hgrow=ALWAYS` + `maxWidth=USE_PREF_SIZE`, sodass die
  vom Gruppenstreifen zugeteilte Breite das Overflow-Signal ist und ein breiter werdendes Fenster
  Boxen zurückholt. Public API: `FXMenuGroup.isOverflowActive` / `overflowActiveProperty()`.
  Showcase-"Home" verbreitert, Doku (EN + DE) und CHANGELOG ergänzt, Headless-Test
  `FXMenuGroupOverflowTest`.
* IP-07 (GroupLayout, COMPLETED): kein Enum / keine Attached-Property wie im Stub-Text. Layout nach
  JavaFX-Pane-Vorbild - zwei Container-Klassen unter `org.pcsoft.framework.panelium.menupane`, die in
  `FXMenuGroup.content` gesteckt werden: `FXMenuGroupLargeBox` (`StackPane`, ein großes Control über
  volle Gruppenhöhe, Style-Klasse `menu-group-large-box`) und `FXMenuGroupSmallBox` (`VBox`, stapelt
  bis zu `MAX_CONTROLS = 3` Controls, Style-Klasse `menu-group-small-box`). Spalten entstehen von
  selbst, da `FXMenuGroup` `content` bereits horizontal anordnet; beide Box-Typen mischbar. Das
  3er-Limit ist im Vararg-Konstruktor hart (`IllegalArgumentException`); ein späteres viertes Kind
  wird als `IllegalStateException` über den Uncaught-Exception-Handler des FX-Threads gemeldet, da
  JavaFX `ListListenerHelper` Listener-Exceptions nicht weiterreicht. `FXMenuGroupView` / FXML
  unverändert. Showcase, Doku (EN + DE) und CHANGELOG ergänzt. Im selben Change-Set zwei
  vorbestehende Verhaltensfehler behoben: die im `MenuChromePane` angedockte Backstage schließt
  wieder per Escape / Außenklick, und der Gruppenstreifen wird nach dem Schließen der Backstage
  wiederhergestellt.
* IP-10 (DisabledState, COMPLETED): Tabs behalten die bestehende `FXMenuTab.disabled`-Property;
  neu sind die Aktivierungs-Sperren in `FXMenuPane.activate`, im `activeTab`-Setter und in der
  Pfeiltasten-Navigation (`FXMenuPaneView.onKeyPressed` überspringt deaktivierte Tabs). `FXMenuGroup`
  erhielt bewusst KEINE eigene API - als JavaFX-`Node` wird die geerbte `setDisable` /
  `disableProperty()` genutzt, die den Zustand auf alle `content`-Knoten überträgt. KEIN eigener
  `disabled`-PseudoClass: JavaFX setzt `:disabled` automatisch bei `disable == true`. Showcase mit
  deaktiviertem Tab und deaktivierter Gruppe; Doku-Abschnitt "Disabled-Zustand" (EN + DE);
  CHANGELOG ergänzt.
* IP-06 (Groups, COMPLETED): unter `org.pcsoft.framework.panelium.menupane` gebaut (nicht
  `.../chrome/menupane` wie im Stub). Gruppen-Komponente heißt `FXMenuGroup` (nicht `MenuGroup`),
  analog `FXMenuPane`, auf Nutzerwunsch - volles MVVM-fx-Tripel plus `FXMenuGroupView.fxml`,
  `title` + `content: ObservableList<Node>`, Style-Klassen `menu-group` / `menu-group-content` /
  `menu-group-title` (Titel unter dem Content). `FXMenuTab.groups` (`ObservableList<FXMenuGroup>`)
  als Reihenfolge-API. `FXMenuPaneView.fxml` erhielt eine `bandColumn`-VBox um `tabStripRow` und
  den neuen `groupStrip`; `backstageContentSlot` bleibt direktes `root`-Kind,
  `positionBackstageSlot()` verankert jetzt an `bandColumn`. Der Gruppenstreifen zeigt die Gruppen
  des aktiven regulären Tabs, folgt Live-Änderungen der Liste bei aktivem Tab, wechselt beim
  Tab-Wechsel und ist bei offener Backstage leer.
* IP-01 (MenuPaneCore, COMPLETED): Paket abweichend vom ursprünglichen Plan als eigenes
  Root-Paket `org.pcsoft.framework.panelium.menupane` angelegt (nicht unter `chrome`), auf
  ausdrücklichen Wunsch des Nutzers.
* IP-02 (ContextualTabs, COMPLETED): Dateien konsistent zu IP-01 direkt unter
  `org.pcsoft.framework.panelium.menupane` angelegt (nicht unter `chrome`). Zusätzlich wurde die
  Demo (`MenuPaneShowcaseWindowController`) um eine schaltbare kontextuelle Gruppe "Table Tools"
  erweitert.
* IP-04 (FileMenuTab, COMPLETED): Datei-Tab als eigenes Feld `FXMenuPane.fileTab`
  (`ObjectProperty<FXMenuTab?>`) statt `isFileTab`-Markierung in `tabs`. Nicht Teil von
  `tabs`/`contextualTabs`/`visibleTabs`, gerendert als separater `menu-pane-strip-file-button`
  vor der scrollenden Leiste (neue `tabStripRow`-HBox im FXML); Pfeiltasten ignorieren ihn.
  `backstageContent` (`ObjectProperty<Node?>`) liegt unsichtbar/ungemanagt im Overlay-Slot
  `#backstageContentSlot`; keine Aktivierung (folgt IP-05). IP-05-Plan entsprechend angepasst
  (Auslöser: Datei-Tab-Button / `fileTabActive` statt `activeTab`-Auswahl).
* IP-11 (ChromeDocking, COMPLETED): auf Nutzerwunsch auf ein Kompositions-Muster reduziert -
  `ChromePane` bekommt KEINE Ribbon-API (kein `menuPaneProperty`, kein Band-Slot). Docking =
  `ChromePane.content = BorderPane(top = FXMenuPane, center = Inhalt)`, daher kein Produktivcode in
  `src/main`. Geliefert: `MenuPaneShowcaseWindow.fxml` / `MenuPaneShowcaseApp` auf ein gerahmtes
  `ChromePane`-Fenster mit angedocktem `FXMenuPane` umgestellt; "Docking"-Abschnitte in der
  Platinum-Chrome- und MenuPane-Implementierungsdoku (EN + DE); neuer Headless-Test
  `ChromeDockingTest`. Kein CHANGELOG-Eintrag (keine endnutzersichtbare Bibliotheksänderung).
  IP-12 verdrahtet `overlayHost` künftig über einen Parent-/Scene-Lookup nach einem
  `BackstageOverlayHost` statt über eine `ChromePane`-Property.
* IP-12 (ChromeOverlayHook, COMPLETED): auf Nutzerwunsch Docking-API und Backstage-Overlay
  gemeinsam als dedizierte `ChromePane`-Subklasse `MenuChromePane`
  (`org.pcsoft.framework.panelium.chrome`, `open`, `@DefaultProperty("body")`) - NICHT als
  `BackstageOverlayHost` auf der Basis-`ChromePane` und NICHT per Parent-/Scene-Lookup.
  `MenuChromePane` hält intern ein `BorderPane` als Frame-`content`; `menuPane` in den Top-Slot
  (`overlayHost = this`), `body` in den Center innerhalb eines `StackPane` zusammen mit der
  `.chrome-backstage-overlay`-Ebene. `showOverlay`/`hideOverlay` blenden diese Ebene nur über dem
  `body` ein/aus - Caption Bar und angedocktes Ribbon (inkl. Datei-Button) bleiben sichtbar, die
  Backstage bleibt schließbar (Datei-Button, Escape, Außenklick). Basis-`ChromePane` nur `open`. `FXMenuPaneView`-Außenklick-Filter zählt jetzt
  auch einen Klick im umgehängten `backstageContent` als „innerhalb". Showcase auf `MenuChromePane`
  umgestellt; neuer Headless-Test `MenuChromePaneTest`. CHANGELOG ergänzt.
* IP-05 (BackstageOverlay, COMPLETED): `fileTabActive`-Flag im ViewModel, vom Datei-Tab-Button
  geschaltet, auf `FXMenuPane` als `isFileTabActive` / `fileTabActiveProperty()`. Neues Interface
  `BackstageOverlayHost` (`showOverlay` / `hideOverlay`) als Vertrag für IP-12; `FXMenuPane.overlayHost`
  nullable, bei gesetztem Host erhält dieser das Panel statt des lokalen Slots
  (`FXMenuPaneViewModel.hasOverlayHost`). Ohne Host blendet die View `#backstageContentSlot` per
  `FadeTransition` über `Duration.seconds(0.3)` ein/aus; der Slot bleibt ungemanagt und wird per
  `positionBackstageSlot` unterhalb von `tabStripRow` platziert (kein Aufziehen des Menübands,
  keine Überdeckung des Datei-Tab-Buttons). Schließen über Scene-Filter für Escape
  (`KEY_PRESSED`) und Außenklick (`MOUSE_PRESSED`), nur während aktiv installiert, sowie über
  Leisten-Tab-Auswahl (`selectStripTab`, Klick und Pfeiltasten). `activeTab` bleibt während der
  Backstage unverändert; `onBackstageClosed` als Collapse-Restore-Hook für IP-13. Demo-Showcase
  mit echtem Backstage-Panel und Status-Label.
