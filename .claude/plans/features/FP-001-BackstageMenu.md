# Feature Plan: BackstageMenu

## 1. Objective

Die Backstage-Flaeche von `FXMenuPane` (`backstageContent`) ist aktuell leer, solange die Anwendung
keinen eigenen Inhalt setzt. Es soll eine eigenstaendige, wiederverwendbare Komponente entstehen,
die standardmaessig ein Backstage-Menue mit linksseitiger Menueliste, Fusszeile mit
Icon-Schnellaktionen und einem Inhaltsbereich bereitstellt, der pro Menuepunkt eine eigene Pane
zeigt.

## 2. Current State

* `FXMenuPane.backstageContent` ist ein reiner `Node?`-Slot, den die Anwendung selbst befuellt.
* `BackstageOverlayHost` und `FXMenuPaneView` regeln nur Anzeige, Positionierung und Ein-/Ausblenden
  der Backstage-Flaeche - nicht deren Inhalt.
* Es existiert keine vorgefertigte Menue-Komponente fuer die Backstage-Flaeche.
* Komponenten im Projekt folgen durchgehend dem MVVM-Pattern (View/ViewModel getrennt, FXML-faehig
  ueber `<fx:root>`), eigenem Stylesheet und liegen thematisch gruppiert in einem Package.

## 3. Target State

* Eine neue Komponente `FXBackstageMenuPane` existiert im Package
  `org.pcsoft.framework.panelium.menupane`, eigenstaendig nutzbar und unabhaengig von `FXMenuPane`.
* Links zeigt sie eine Menueliste (Standardbreite 300px, von aussen ueberschreibbar) mit Eintraegen
  aus optionalem Icon und Text.
* Am Fuss der Menueliste liegt eine horizontale Leiste mit reinen Icon-Buttons fuer Schnellaktionen,
  deren Klick die Aktion direkt ausloest.
* Jeder Menuepunkt traegt seine eigene Inhalts-Node; im Rest der Flaeche wird ausschliesslich die
  Node des aktuell ausgewaehlten Menuepunkts angezeigt, und erst nachdem ein Menuepunkt angeklickt
  wurde.
* `FXMenuPane` setzt standardmaessig eine Instanz von `FXBackstageMenuPane` als
  `backstageContent`, so dass die Backstage-Flaeche nicht mehr leer ist, sofern die Anwendung sie
  nicht explizit ueberschreibt.

## 4. Requirements

### Functional Requirements

* Menueliste links, Standardbreite 300px, Breite von aussen setzbar.
* Menuepunkt = optionales Icon + Text.
* Fusszeile mit reinen Icon-Buttons fuer Schnellaktionen, horizontal angeordnet.
* Klick auf einen Menuepunkt waehlt ihn aus und zeigt dessen Inhalts-Node im Restbereich.
* Kein Menuepunkt ausgewaehlt => Restbereich zeigt keinen Menuepunkt-Inhalt.
* Klick auf eine Schnellaktion loest die zugehoerige Aktion sofort aus (keine Auswahl, kein
  Inhaltswechsel).
* `FXMenuPane` zeigt dieses Menue standardmaessig als Backstage-Inhalt.

### Technical Requirements

* Kotlin, MVVM-Pattern wie bei bestehenden Komponenten (`FXMenuPane`, `FXMenuGroupView`, ...).
* Menueliste wird ueber eine `ListView` realisiert.
* Instanziierbar und nutzbar per FXML, wie andere Komponenten des Projekts.
* Eigenes Stylesheet analog `menu-pane.css`.
* Keine neue Drittanbieter-Abhaengigkeit ohne Rueckfrage.

## 5. Architecture

* Neue Klassen im Package `org.pcsoft.framework.panelium.menupane`:
    * `FXBackstageMenuPane` (Root-Komponente, View + ViewModel), mit `ObservableList` an
      Menuepunkten, einer `menuWidth`-Property (Default 300px) und einer `ObservableList` an
      Schnellaktionen.
    * `FXBackstageMenuItem` (Datenmodell: `icon` optional, `text`, `content: Node`).
    * `FXBackstageQuickAction` (Datenmodell: `icon`, `onAction`-Callback).
* Layout: `BorderPane`-artige Struktur - links die Menueliste (`ListView<FXBackstageMenuItem>` mit
  custom Cell fuer Icon+Text) plus Fusszeile mit Icon-Buttons, im Zentrum ein `StackPane` als
  Inhaltsbereich, der die `content`-Node des ausgewaehlten `FXBackstageMenuItem` anzeigt.
* Integration: `FXMenuPane` erzeugt/verwendet standardmaessig eine `FXBackstageMenuPane`-Instanz
  als `backstageContent`, ohne die bestehende Ueberschreibbarkeit durch die Anwendung einzuschraenken.
* Keine Aenderung an `BackstageOverlayHost` oder der Anzeigelogik in `FXMenuPaneView` noetig - die
  neue Komponente ist nur ein Inhalt fuer den bestehenden Slot.

## 6. Implementation Plan Overview

| ID    | Implementation Plan               | Objective                                                                 | Dependencies | Status |
| ----- | ---------------------------------- | -------------------------------------------------------------------------- | ------------ | ------ |
| IP-01 | Grundgeruest & Datenmodell         | Neue Komponente `FXBackstageMenuPane` mit Layout-Skelett und Datenmodellen | -            | COMPLETED |
| IP-02 | Menueliste & Auswahl               | Menue-`ListView` mit Icon+Text-Zellen, Auswahl steuert Inhaltsbereich       | IP-01        | COMPLETED |
| IP-03 | Schnellaktionsleiste               | Icon-Button-Fusszeile mit direkter Aktionsausloesung                       | IP-01        | COMPLETED |
| IP-04 | Default-Verdrahtung & Feinschliff  | Standardverdrahtung in `FXMenuPane`, Stylesheet, Doku/Showcase             | IP-02, IP-03 | NOT_STARTED |

## 7. Implementation Plans

### IP-01: Grundgeruest & Datenmodell (COMPLETED)

**Objective**

Legt die neue Komponente `FXBackstageMenuPane` mit MVVM-Grundgeruest, Layout-Skelett (Menuebereich
links, Inhaltsbereich rechts) und den Datenmodellen `FXBackstageMenuItem` sowie
`FXBackstageQuickAction` an.

**Scope**

* Neue Klassen: `FXBackstageMenuPane`, `FXBackstageMenuPaneView`, `FXBackstageMenuPaneViewModel`,
  `FXBackstageMenuItem`, `FXBackstageQuickAction`.
* `menuWidth`-Property (Default 300px, von aussen setzbar).
* Grundlegendes Layout ohne Menueliste/Fusszeile-Feinschliff (folgt in IP-02/IP-03).
* Kein Wiring in `FXMenuPane` (folgt in IP-04).

**Dependencies**

Keine - unabhaengig.

**Interfaces to Other Plans**

Stellt Klassen, Properties und Layout-Slots bereit, auf denen IP-02 (Menueliste) und IP-03
(Fusszeile) direkt aufbauen.

**Umsetzung**

Wie geplant umgesetzt, ohne Abweichungen: `FXBackstageMenuPane`, `FXBackstageMenuPaneView`,
`FXBackstageMenuPaneViewModel`, `FXBackstageMenuItem`, `FXBackstageQuickAction` im Package
`menupane` angelegt; Layout als `StackPane`-Root mit `BorderPane` (links `menuArea`-`VBox`,
zentral `contentArea`-`StackPane`); `menuWidth` als `DoubleProperty` im ViewModel, Default
`300.0`, von aussen ueberschreibbar. Eigenes Stylesheet `backstage-menu-pane.css` angelegt, aber
noch nicht an `getUserAgentStylesheet()` gebunden (folgt bei Bedarf in IP-04).

### IP-02: Menueliste & Auswahl (COMPLETED)

**Objective**

Baut die linke Menueliste als `ListView<FXBackstageMenuItem>` mit Icon+Text-Zellen und verbindet die
Auswahl eines Eintrags mit der Anzeige seiner Inhalts-Node im Restbereich.

**Scope**

* Custom `ListCell` fuer Icon (optional) + Text.
* Auswahl-Handling: Klick waehlt Eintrag, Inhaltsbereich zeigt dessen `content`-Node.
* Kein Eintrag ausgewaehlt => Inhaltsbereich bleibt leer.
* Keine Fusszeile (siehe IP-03).

**Dependencies**

IP-01.

**Interfaces to Other Plans**

Nutzt die von IP-01 bereitgestellten Layout-Slots und Datenmodelle; liefert keine eigene
Schnittstelle an andere Plaene.

**Umsetzung**

Wie geplant umgesetzt, ohne Abweichungen: `ListView<FXBackstageMenuItem>` in `menuArea` ergaenzt,
custom `ListCell` mit Icon+Text-`HBox`, Auswahl steuert `selectedItem` im ViewModel und den Inhalt
des `contentArea`-`StackPane`. Zusaetzlich zum Plan wurde die neue Auswahlfunktion direkt im
`MenuPaneShowcaseWindowController`/`MenuPaneShowcaseWindow.fxml` demonstriert (`FXBackstageMenuPane`
mit drei Menuepunkten und Statuszeile fuer die aktuelle Auswahl), obwohl die Standardverdrahtung in
`FXMenuPane` erst in IP-04 erfolgt.

### IP-03: Schnellaktionsleiste (COMPLETED)

**Objective**

Baut die horizontale Fusszeile mit reinen Icon-Buttons fuer Schnellaktionen, deren Klick die
zugehoerige Aktion direkt und ohne Auswahlwechsel ausloest.

**Scope**

* Rendering der `FXBackstageQuickAction`-Liste als Icon-Buttons.
* Direkte Ausloesung des jeweiligen Callbacks bei Klick.
* Keine Beeinflussung der Menue-Auswahl oder des Inhaltsbereichs.

**Dependencies**

IP-01.

**Interfaces to Other Plans**

Nutzt die von IP-01 bereitgestellten Layout-Slots und Datenmodelle; liefert keine eigene
Schnittstelle an andere Plaene.

**Umsetzung**

Wie geplant umgesetzt, ohne Abweichungen: `quickActionArea`-`HBox` am Fuss von `menuArea` in
`FXBackstageMenuPaneView.fxml` ergaenzt; `FXBackstageMenuPaneView.kt` baut daraus bei jeder
Aenderung von `viewModel.quickActions` reine Icon-`Button`s neu auf (`ListChangeListener`), deren
Klick ausschliesslich `FXBackstageQuickAction.onAction` ausloest und weder `selectedItem` noch
`contentArea` beruehrt. Styling in `backstage-menu-pane.css` ergaenzt
(`backstage-menu-pane-quick-action-area`/`-button`). Showcase (`MenuPaneShowcaseWindow.fxml`,
`MenuPaneShowcaseWindowController`) demonstriert zwei Schnellaktionen (Refresh-Zaehler, Backstage
schliessen) mit eigenem Statuslabel.

### IP-04: Default-Verdrahtung & Feinschliff

**Objective**

Verdrahtet `FXBackstageMenuPane` als Standard-Backstage-Inhalt von `FXMenuPane`, ergaenzt das
Stylesheet und aktualisiert Dokumentation/Showcase.

**Scope**

* `FXMenuPane` erzeugt standardmaessig eine `FXBackstageMenuPane`-Instanz als `backstageContent`.
* Bestehende Ueberschreibbarkeit von `backstageContent` bleibt erhalten.
* Eigenes Stylesheet fuer die neue Komponente.
* Anpassung von Doku (KDoc/MkDocs/Changelog) und Showcase gemaess Projektregeln.

**Dependencies**

IP-02, IP-03.

**Interfaces to Other Plans**

Konsumiert die fertige Komponente aus IP-02/IP-03; liefert keine Schnittstelle an weitere Plaene.

## 8. Dependency Graph

```text
IP-01 (COMPLETED)
├── IP-02 (COMPLETED)
│   └── IP-04
└── IP-03 (COMPLETED)
    └── IP-04
```

## 9. Risks and Open Questions

* Offen: Genaues visuelles Erscheinungsbild (Farben, Abstaende) der neuen Komponente - wird in IP-01
  im Detail entschieden, orientiert an bestehendem `menu-pane.css`.
* Offen: Ob `FXBackstageMenuPane` zusaetzlich ueber FXML-Property-Elemente (`<items>`,
  `<quickActions>`) befuellbar sein soll, analog zu `FXMenuTab.groups` - wird in IP-01 entschieden.
* Risiko: Default-Instanziierung in `FXMenuPane` (IP-04) darf bestehende Anwendungen, die
  `backstageContent` bereits selbst setzen, nicht beeinflussen - muss beim Wiring beruecksichtigt
  werden.

## 10. Feature Completion Criteria

* `FXBackstageMenuPane` existiert als eigenstaendige, per FXML nutzbare Komponente mit Menueliste,
  Fusszeile und Inhaltsbereich gemaess Abschnitt 3 und 4.
* `FXMenuPane` zeigt ohne weiteres Zutun der Anwendung ein funktionierendes Backstage-Menue.
* Auswahl eines Menuepunkts zeigt dessen Inhalt; Klick auf eine Schnellaktion loest sie direkt aus.
* Alle vier Implementation Plans sind gemaess Statusdatei `COMPLETED`.
