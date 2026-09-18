# Feature Status: BackstageMenu

Status: COMPLETED

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | Grundgeruest & Datenmodell | COMPLETED |
| IP-02 | Menueliste & Auswahl | COMPLETED |
| IP-03 | Schnellaktionsleiste | COMPLETED |
| IP-04 | Default-Verdrahtung & Feinschliff | COMPLETED |
| IP-05 | Menue-Optik & CSS-API | COMPLETED |
| IP-06 | Hover-Textfarbe auf selektiertem Eintrag | COMPLETED |

## Overall Progress

100%

## Notes

IP-01 abgeschlossen: `FXBackstageMenuPane`, View/ViewModel, `FXBackstageMenuItem`,
`FXBackstageQuickAction` und Layout-Skelett angelegt, wie im Feature Plan vorgesehen. Keine
Abweichungen vom Plan.

IP-02 abgeschlossen: Menue-`ListView` mit Icon+Text-Zellen ergaenzt, Auswahl steuert den
Inhaltsbereich, Styling fuer Liste/Auswahl ergaenzt. Zusaetzlich zum Plan wurde die neue
Auswahlfunktion im `MenuPaneShowcaseWindowController`/`MenuPaneShowcaseWindow.fxml` demonstriert
(Menuepunkte mit Inhalt, Statuszeile fuer die aktuelle Auswahl).

IP-03 abgeschlossen: Icon-Button-Fusszeile fuer Schnellaktionen ergaenzt (`quickActionArea` in
`FXBackstageMenuPaneView.fxml`/`.kt`), Klick loest ausschliesslich `onAction` aus, ohne Auswahl
oder Inhaltsbereich zu beruehren. Styling in `backstage-menu-pane.css` ergaenzt. Showcase um zwei
Schnellaktionen (Refresh-Zaehler, Backstage schliessen) erweitert.

IP-04 abgeschlossen: `FXMenuPane` verdrahtet beim ersten Oeffnen der Backstage lazy eine
Standard-`FXBackstageMenuPane` als `backstageContent`, sofern die Anwendung noch keinen eigenen
Inhalt gesetzt hat; explizites Setzen bleibt weiterhin vorrangig. Abweichung vom Plan: Die
Default-Instanz wird nicht am Konstruktor, sondern erst beim ersten Oeffnen der Backstage erzeugt
(echte Lazy-Erzeugung). Dokumentation (`menu-pane/implementation.md`/`.de.md`) um einen neuen
Abschnitt zu `FXBackstageMenuPane` ergaenzt, CHANGELOG aktualisiert, Showcase um eine Checkbox zum
Umschalten zwischen Default- und custom Backstage-Inhalt erweitert, Tests fuer Default-Inhalt und
Ueberschreibbarkeit ergaenzt.

IP-05 abgeschlossen: Menueliste von `FXBackstageMenuPane` von Listbox- auf Menue-Optik umgestellt
(kein Fokus-Rahmen, Hover-Highlight, groesseres Padding); Hover- und Auswahlfarbe als CSS-Custom-
Properties `-backstage-menu-pane-item-hover-color` / `-backstage-menu-pane-item-selected-color`
freigegeben. Keine Abweichungen vom Plan. Dokumentation (`customize-styles.md`/`.de.md`) um einen
neuen `FXBackstageMenuPane`-Abschnitt ergaenzt, CHANGELOG aktualisiert.

IP-06 abgeschlossen: Textfarbe eines bereits selektierten Menuepunkts bleibt beim Hover nun lesbar
(dunkel statt weiss auf dem helleren Hover-Hintergrund). Keine Abweichungen vom Plan. Das UNRELEASED-
Changelog wurde bei dieser Gelegenheit von einzelnen IP-Eintraegen zu zwei zusammenfassenden
Eintraegen (Komponente inkl. Default-Verdrahtung; Menue-Optik inkl. Hover-Textfarben-Korrektur)
konsolidiert.
