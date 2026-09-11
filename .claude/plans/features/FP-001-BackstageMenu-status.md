# Feature Status: BackstageMenu

Status: IN_PROGRESS

## Implementation Plans

| ID | Implementation Plan | Status |
|----|---------------------|--------|
| IP-01 | Grundgeruest & Datenmodell | COMPLETED |
| IP-02 | Menueliste & Auswahl | COMPLETED |
| IP-03 | Schnellaktionsleiste | NOT_STARTED |
| IP-04 | Default-Verdrahtung & Feinschliff | NOT_STARTED |

## Overall Progress

50%

## Notes

IP-01 abgeschlossen: `FXBackstageMenuPane`, View/ViewModel, `FXBackstageMenuItem`,
`FXBackstageQuickAction` und Layout-Skelett angelegt, wie im Feature Plan vorgesehen. Keine
Abweichungen vom Plan.

IP-02 abgeschlossen: Menue-`ListView` mit Icon+Text-Zellen ergaenzt, Auswahl steuert den
Inhaltsbereich, Styling fuer Liste/Auswahl ergaenzt. Zusaetzlich zum Plan wurde die neue
Auswahlfunktion im `MenuPaneShowcaseWindowController`/`MenuPaneShowcaseWindow.fxml` demonstriert
(Menuepunkte mit Inhalt, Statuszeile fuer die aktuelle Auswahl).
