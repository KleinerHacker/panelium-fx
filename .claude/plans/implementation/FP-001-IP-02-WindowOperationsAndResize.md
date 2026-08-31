# FP-001-IP-02: WindowOperationsAndResize

## Abhängigkeiten

* IP-01

## Betroffene Bereiche

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/internal/WindowOps.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/internal/ResizeOverlay.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromePane.kt` — Overlay, Zustand-Listener
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/internal/ChromeConfig.kt` — Schatten-/Radius-Konstanten
* `docs/docs/usage.md`, `CHANGELOG.md`

## Design-Festlegungen

* `WindowOps(stage)` kapselt `startMove/moveTo`, `resize(edge, delta)`, `toggleMaximize`, `minimize`, `close`
* `ResizeOverlay`: acht unsichtbare Zonen (vier Kanten, vier Ecken), Breite `RESIZE_BORDER`, außerhalb der Schatten-Insets
* Resize respektiert `stage.minWidth/minHeight/maxWidth/maxHeight`; Zonen inaktiv bei `stage.isResizable == false`
* Maximieren manuell über `Screen.getScreensForRectangle(...).visualBounds`, nicht `setMaximized(true)` allein
* vorherige Bounds vor Maximieren merken, bei Restore zurücksetzen
* Zustand maximiert oder `fullScreen`: Schatten-Insets 0, Ecken eckig; danach zurück
* `fullScreen`: Caption-Platzhalter `visible=false`, `managed=false`
* Schatten via `DropShadow` auf `shadowRoot`; `shadowEnabled`-Property (default true), bei false Insets 0
* Doppelklick-/Button-Auslöser folgen in IP-04 bzw. IP-05; hier nur `WindowOps`-API plus vorläufige Bindung an Caption-Hintergrund

## Aufgaben

### Aufgabe 1: WindowOps

* Klasse mit Referenz auf `Stage`, Methoden Move, Resize, ToggleMaximize, Minimize, Close
* Move: Mausanker merken, `stage.x/y` relativ verschieben
* Resize: Kante/Ecke → neue Bounds unter Beachtung der Größen-Constraints

### Aufgabe 2: ResizeOverlay

* acht Zonen als Nodes über dem Frame, je passender `Cursor`
* Maus-Handler `PRESSED`/`DRAGGED` → `WindowOps.resize`
* Overlay deaktivieren, wenn `stage.isResizable == false`

### Aufgabe 3: Maximieren und Restaurieren

* Screen unter dem Fenster ermitteln, `visualBounds` als Zielbounds setzen
* vorherige Bounds speichern und bei Restore wiederherstellen
* auf zweitem Monitor prüfen

### Aufgabe 4: Zustandsumschaltung Schatten und Ecken

* Listener auf `stage.maximizedProperty` und `stage.fullScreenProperty`
* bei aktiv: Schatten-Insets 0, Eckenradius 0; bei inaktiv: Konstantenwerte
* `resizeOverlay` bei maximiert/fullScreen deaktivieren

### Aufgabe 5: FullScreen-Caption

* bei `fullScreen == true` Caption-Platzhalter aus Layout nehmen
* bei Verlassen wiederherstellen

### Aufgabe 6: Schatten und runde Ecken

* `DropShadow` mit Konstanten auf `shadowRoot`, `frameBox` mit Hintergrund-/Rahmenradius
* `shadowEnabled`-Property, Insets folgen dem Wert

### Aufgabe 7: Vorläufige Move-Bindung, Doku, Build

* Maus-Drag auf Caption-Hintergrund → `WindowOps.startMove/moveTo`
* `docs/docs/usage.md` Abschnitt „Fensteroperationen", CHANGELOG-Eintrag
* `./gradlew build`

## Fertig-Kriterien

* Fenster an allen Kanten/Ecken größenveränderbar innerhalb der Constraints; inaktiv bei `resizable=false`
* Minimieren, Maximieren (Arbeitsfläche, auch zweiter Monitor), Restaurieren funktionieren
* Schatten und Insets im maximierten und FullScreen-Zustand entfernt, danach zurück
* `build` grün
