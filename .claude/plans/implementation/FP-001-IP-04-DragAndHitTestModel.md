# FP-001-IP-04: DragAndHitTestModel

## Abhängigkeiten

* IP-02
* IP-03

## Betroffene Bereiche

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromeCaptionBar.kt` — Attached Property, Filter
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/internal/CaptionDragHandler.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/internal/WindowMenu.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/internal/WindowOps.kt` — Anbindung
* `docs/docs/usage.md`, `CHANGELOG.md`

## Design-Festlegungen

* Attached Property über `ChromeCaptionBar.setDragRegion(node, Boolean?)` / `getDragRegion(node)` via `node.properties`
* Standardregel: Caption-Hintergrund ist Ziehzone
* ein Knoten ist Passthrough, wenn interaktiv (Control, fokustraversierbar, eigene Maus-Handler) oder `dragRegion == false`
* `dragRegion == true` erzwingt Ziehen trotz Interaktivität
* Auflösung: vom `pickResult`-Knoten aufwärts; erstes explizites Flag gewinnt, sonst Heuristik, sonst Default-Ziehen
* Doppelklick auf Ziehzone → `WindowOps.toggleMaximize()`, nur bei `stage.isResizable`
* Rechtsklick auf Ziehzone → `ContextMenu` mit Wiederherstellen, Verschieben, Größe ändern, Minimieren, Maximieren, Schließen
* vorläufige Move-Bindung aus IP-02 wird durch `CaptionDragHandler` ersetzt

## Aufgaben

### Aufgabe 1: Attached Property dragRegion

* statische `setDragRegion` / `getDragRegion` auf Basis von `Node.getProperties()`
* Wert `null` (unbestimmt), `true`, `false`
* öffentlich, dokumentiert

### Aufgabe 2: Hit-Test-Routine

* Startknoten aus `MouseEvent.pickResult`
* aufwärts bis `ChromeCaptionBar`: erstes explizites Flag entscheidet
* ohne Flag: Interaktiv-Heuristik, sonst Ziehen

### Aufgabe 3: Interaktiv-Heuristik

* `Node` ist Passthrough bei `is Control`, `isFocusTraversable`, oder gesetzten Maus-Handlern
* Ergebnis testbar kapseln

### Aufgabe 4: CaptionDragHandler

* Maus-Filter `PRESSED`/`DRAGGED` auf `ChromeCaptionBar`
* bei Ziehzone `WindowOps.startMove/moveTo`
* vorläufige Bindung aus IP-02 entfernen

### Aufgabe 5: Doppelklick-Maximieren

* `PRESSED` mit `clickCount == 2` auf Ziehzone → `toggleMaximize`
* nur wenn `stage.isResizable`

### Aufgabe 6: Fenster-Kontextmenü

* `WindowMenu` baut `ContextMenu` mit Fensteraktionen
* Einträge rufen `WindowOps`; nicht mögliche Aktionen deaktivieren (z. B. Maximieren)
* an Rechtsklick auf Ziehzone anzeigen

### Aufgabe 7: Doku und Build

* `docs/docs/usage.md` „Ziehbereiche und Passthrough" inkl. `dragRegion`-Beispiel
* CHANGELOG-Eintrag
* `./gradlew build`

## Fertig-Kriterien

* Ziehen am leeren Caption-Bereich verschiebt das Fenster, Klick auf Controls nicht
* als Ziehgriff markierte Knoten verschieben trotz Interaktivität
* Doppelklick maximiert/restauriert; Rechtsklick zeigt funktionierendes Fenstermenü
* `build` grün
