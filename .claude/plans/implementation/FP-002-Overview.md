# Übersicht: FP-002-FXMenuTab

Feature Plan: `.claude/plans/features/FP-002-FXMenuTab.md`

## Implementierungspläne

| ID | Name | Datei |
|----|------|-------|
| IP-01 | MenuTabCore | FP-002-IP-01-MenuTabCore.md (COMPLETED) |
| IP-02 | ContextualTabs | FP-002-IP-02-ContextualTabs.md (COMPLETED) |
| IP-03 | TabStripScrolling | FP-002-IP-03-TabStripScrolling.md (COMPLETED) |
| IP-04 | FileMenuTab | FP-002-IP-04-FileMenuTab.md (COMPLETED) |
| IP-05 | BackstageOverlay | FP-002-IP-05-BackstageOverlay.md (COMPLETED) |
| IP-06 | Groups | FP-002-IP-06-Groups.md |
| IP-07 | GroupLayout | FP-002-IP-07-GroupLayout.md |
| IP-08 | GroupLauncher | FP-002-IP-08-GroupLauncher.md |
| IP-09 | GroupOverflow | FP-002-IP-09-GroupOverflow.md |
| IP-10 | DisabledState | FP-002-IP-10-DisabledState.md |
| IP-11 | ChromeDocking | FP-002-IP-11-ChromeDocking.md |
| IP-12 | ChromeOverlayHook | FP-002-IP-12-ChromeOverlayHook.md |
| IP-13 | CollapseAndExpand | FP-002-IP-13-CollapseAndExpand.md |
| IP-14 | RibbonContextMenu | FP-002-IP-14-RibbonContextMenu.md |
| IP-15 | StylingAndCssApi | FP-002-IP-15-StylingAndCssApi.md |
| IP-16 | TestHarnessAndCoverage | FP-002-IP-16-TestHarnessAndCoverage.md |

## Reihenfolge

1. IP-01 (Fundament, COMPLETED)
2. Parallel nach IP-01: IP-02, IP-03, IP-04, IP-06, IP-11
3. IP-05 nach IP-04
4. IP-07 nach IP-06; IP-08 nach IP-06 (parallel zu IP-07)
5. IP-09 nach IP-06 und IP-07
6. IP-10 nach IP-01 und IP-06
7. IP-12 nach IP-05 und IP-11
8. IP-13 nach IP-01 und IP-11
9. IP-14 nach IP-13
10. IP-15 nach IP-01 bis IP-14
11. IP-16 nach IP-15

## Abgeschlossene Implementierungspläne

* IP-01 (MenuTabCore, COMPLETED): Paket abweichend vom ursprünglichen Plan als eigenes
  Root-Paket `org.pcsoft.framework.panelium.menutab` angelegt (nicht unter `chrome`), auf
  ausdrücklichen Wunsch des Nutzers.
* IP-02 (ContextualTabs, COMPLETED): Dateien konsistent zu IP-01 direkt unter
  `org.pcsoft.framework.panelium.menutab` angelegt (nicht unter `chrome`). Zusätzlich wurde die
  Demo (`MenuTabShowcaseWindowController`) um eine schaltbare kontextuelle Gruppe "Table Tools"
  erweitert.
* IP-04 (FileMenuTab, COMPLETED): Datei-Tab als eigenes Feld `FXMenuTab.fileTab`
  (`ObjectProperty<MenuTab?>`) statt `isFileTab`-Markierung in `tabs`. Nicht Teil von
  `tabs`/`contextualTabs`/`visibleTabs`, gerendert als separater `menu-tab-strip-file-button`
  vor der scrollenden Leiste (neue `tabStripRow`-HBox im FXML); Pfeiltasten ignorieren ihn.
  `backstageContent` (`ObjectProperty<Node?>`) liegt unsichtbar/ungemanagt im Overlay-Slot
  `#backstageContentSlot`; keine Aktivierung (folgt IP-05). IP-05-Plan entsprechend angepasst
  (Auslöser: Datei-Tab-Button / `fileTabActive` statt `activeTab`-Auswahl).
* IP-05 (BackstageOverlay, COMPLETED): `fileTabActive`-Flag im ViewModel, vom Datei-Tab-Button
  geschaltet, auf `FXMenuTab` als `isFileTabActive` / `fileTabActiveProperty()`. Neues Interface
  `BackstageOverlayHost` (`showOverlay` / `hideOverlay`) als Vertrag für IP-12; `FXMenuTab.overlayHost`
  nullable, bei gesetztem Host erhält dieser das Panel statt des lokalen Slots
  (`FXMenuTabViewModel.hasOverlayHost`). Ohne Host blendet die View `#backstageContentSlot` per
  `FadeTransition` über `Duration.seconds(0.3)` ein/aus; der Slot bleibt ungemanagt und wird per
  `positionBackstageSlot` unterhalb von `tabStripRow` platziert (kein Aufziehen des Menübands,
  keine Überdeckung des Datei-Tab-Buttons). Schließen über Scene-Filter für Escape
  (`KEY_PRESSED`) und Außenklick (`MOUSE_PRESSED`), nur während aktiv installiert, sowie über
  Leisten-Tab-Auswahl (`selectStripTab`, Klick und Pfeiltasten). `activeTab` bleibt während der
  Backstage unverändert; `onBackstageClosed` als Collapse-Restore-Hook für IP-13. Demo-Showcase
  mit echtem Backstage-Panel und Status-Label.
