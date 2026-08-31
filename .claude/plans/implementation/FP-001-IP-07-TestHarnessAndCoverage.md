# FP-001-IP-07: TestHarnessAndCoverage

## Abhängigkeiten

* IP-01
* IP-02
* IP-03
* IP-04
* IP-05
* IP-06

## Betroffene Bereiche

* `build.gradle.kts` — Test-Abhängigkeiten, Headless-JVM-Args
* `src/test/kotlin/org/pcsoft/framework/panelium/chrome/*` — neue Testklassen
* `.github/workflows/ci.yml` — Test-Job für Headless-JavaFX
* `CHANGELOG.md`

## Design-Festlegungen

* Test-Abhängigkeiten (Testscope): `org.testfx:testfx-junit5`, `org.testfx:openjfx-monocle` — vom Betreiber freigegeben
* Headless-JVM-Args: `-Dtestfx.robot=glass`, `-Dtestfx.headless=true`, `-Dglass.platform=Monocle`, `-Dprism.order=sw`
* Testpaket spiegelt `org.pcsoft.framework.panelium.chrome`
* nur Entwicklertests (kein `IT`-Suffix, kein Integrationssystem)
* `ChromePane.captionOsProperty` wird in Tests gesetzt, um OS-Verhalten zu prüfen
* Lizenzprüfung: neue Abhängigkeiten müssen den `licensee`-/License-Report bestehen
* vor Testklassen `testing`-Skill laden, vor `ci.yml` `ci-pipeline`-Skill laden

## Aufgaben

### Aufgabe 1: Test-Abhängigkeiten und Gradle

* TestFX und Monocle als `testImplementation` ergänzen
* Headless-JVM-Args an `tasks.test` setzen
* License-Report/`licensee` prüfen, bei fehlender Lizenz Betreiber fragen

### Aufgabe 2: Test-Basis

* `testing`-Skill laden
* TestFX-Basisklasse mit Monocle-Setup, Paketspiegelung anlegen
* Hilfsroutine zum Erzeugen einer Test-Stage mit `ChromePane`

### Aufgabe 3: Tests Fensteroperationen

* Move, Kanten-/Ecken-Resize innerhalb der Constraints, `resizable=false`
* Minimieren, Maximieren auf Arbeitsfläche, Restaurieren
* FullScreen blendet Caption aus und wieder ein

### Aufgabe 4: Tests Caption und FXML

* Einfügen in linke/mittlere/rechte Slots, Layoutprüfung
* Default-Titel/Icon-Bindung und Abschaltung
* Laden von `ChromePane` als FXML-Wurzel

### Aufgabe 5: Tests Drag und Hit-Test

* Ziehzone verschiebt Fenster, Control nicht
* `dragRegion=true`/`false`-Override
* Doppelklick maximiert, Rechtsklick öffnet Fenstermenü

### Aufgabe 6: Tests Buttons

* Platzierung je `ChromeOs` (Property injiziert)
* Aktionen Minimieren/Maximieren/Schließen
* `max-restore`-Zustand folgt Maximierung

### Aufgabe 7: Tests Styling

* Pseudo-Klassen `maximized`/`fullscreen`/`active` schalten korrekt
* styleable Properties wirken
* `getUserAgentStylesheet()` gesetzt, App-Stylesheet überschreibt

### Aufgabe 8: Test Einstiegspunkt-Äquivalenz

* `ChromePane`, `install(stage)`, `PaneliumStage` erzeugen gleiche Struktur und Properties
* eine Testmethode je Einstiegspunkt plus Vergleich

### Aufgabe 9: CI und Abschluss

* `ci-pipeline`-Skill laden, `ci.yml` Test-Job auf Headless-JavaFX prüfen/anpassen
* Kover-Bericht erzeugen und prüfen
* CHANGELOG-Eintrag; `./gradlew build`

## Fertig-Kriterien

* Headless-Testsuite läuft lokal und in CI grün
* Kernverhalten aus IP-01 bis IP-06 ist abgedeckt
* Kover-Bericht wird erzeugt
* `build` grün
