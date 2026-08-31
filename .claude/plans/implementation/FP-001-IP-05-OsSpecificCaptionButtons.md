# FP-001-IP-05: OsSpecificCaptionButtons

## Abhängigkeiten

* IP-02
* IP-03

## Betroffene Bereiche

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromeOs.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromeCaptionButtons.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromeCaptionBar.kt` — Button-Slot füllen
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromePane.kt` — Platzierung nach OS
* `docs/docs/usage.md`, `CHANGELOG.md`

## Design-Festlegungen

* `enum ChromeOs { WINDOWS, MAC, LINUX, OTHER }`, Erkennung aus `System.getProperty("os.name")`
* für Tests überschreibbar: `ChromePane.captionOsProperty` mit Default aus Erkennung
* `ChromeCaptionButtons : HBox` mit Buttons `minimizeButton`, `maxRestoreButton`, `closeButton`
* Platzierung: WINDOWS/LINUX/OTHER rechts, Reihenfolge min, max, close; MAC links, Reihenfolge close, min, max
* rechts wird der reservierte Button-Slot rechts in der Bar gesetzt; bei MAC links vor `leftItems`
* ein generischer Default-Look über Style-Klassen; keine OS-Versionsvarianten
* Symbol-Formen vorerst als einfache `Region`-Shapes; finale CSS-Gestaltung in IP-06
* `maxRestoreButton` schaltet Pseudo-Klasse/Icon nach `stage.maximizedProperty`
* `maxRestoreButton` deaktiviert bei `stage.isResizable == false`
* Aktionen → `WindowOps.minimize`, `toggleMaximize`, `close`

## Aufgaben

### Aufgabe 1: ChromeOs

* Enum plus Erkennung aus `os.name` (case-insensitive)
* `ChromePane.captionOsProperty` mit Default aus Erkennung, überschreibbar

### Aufgabe 2: ChromeCaptionButtons

* `HBox` mit drei Buttons, Style-Klassen `chrome-button` plus `minimize`/`max-restore`/`close`
* generische Symbol-Shapes
* Buttons als Felder für Verdrahtung erreichbar

### Aufgabe 3: Platzierung nach OS

* WINDOWS/LINUX/OTHER: Button-Box in den rechten Button-Slot, Reihenfolge min, max, close
* MAC: Button-Box links, Reihenfolge close, min, max
* Neuplatzierung bei Änderung von `captionOsProperty`

### Aufgabe 4: Verdrahtung mit WindowOps

* `minimizeButton` → `minimize`, `maxRestoreButton` → `toggleMaximize`, `closeButton` → `close`
* `maxRestoreButton`-Zustand/Icon an `stage.maximizedProperty`
* `maxRestoreButton.disable` an `!stage.isResizable`

### Aufgabe 5: Koexistenz mit Slot-Inhalten

* Button-Box immer außen, App-`rightItems` innen daneben
* Z-Order der Button-Box oben

### Aufgabe 6: Doku und Build

* `docs/docs/usage.md` Abschnitt „Fensterknöpfe" mit OS-Verhalten
* CHANGELOG-Eintrag
* `./gradlew build`

## Fertig-Kriterien

* auf Windows/Linux Buttons rechts, auf macOS links, jeweils korrekte Reihenfolge
* Buttons führen Minimieren, Maximieren/Restaurieren, Schließen aus
* `maxRestoreButton` spiegelt den Maximierungszustand und ist bei `resizable=false` deaktiviert
* `build` grün
