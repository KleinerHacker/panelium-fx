# Implementierungsplan: FP-001-IP-04-DefaultVerdrahtungUndFeinschliff

## Voraussetzung

* IP-02 (MenuelisteUndAuswahl) abgeschlossen.
* IP-03 (Schnellaktionsleiste) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/menupane/FXMenuPane.kt` (aendern)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (aendern)
* Showcase-Dateien unter `src/demo` fuer `FXMenuPane`/Backstage (aendern)

## Design-Entscheidungen

* `FXMenuPane` erzeugt eine `FXBackstageMenuPane`-Default-Instanz lazy, falls `backstageContent` nicht gesetzt ist.
* Setzt die Anwendung `backstageContent` selbst, hat dies weiterhin Vorrang vor dem Default.
* Die Default-Instanz wird nur einmal erzeugt und danach wiederverwendet.
* `project-docs`-Skill vor Doku-Aenderung laden.
* `demo-showcase-sync`-Skill vor Showcase-Aenderung laden.

## Aufgabe 1: Default-Instanz

* `FXMenuPane` erhaelt lazy erzeugte `FXBackstageMenuPane` als Default-`backstageContent`.
* Explizites Setzen von `backstageContent` durch die Anwendung ueberschreibt den Default weiterhin.

## Aufgabe 2: Dokumentation

* `project-docs`-Skill laden.
* MkDocs-Abschnitt zum Backstage-Menue in der `menu-pane`-Doku (EN und DE) ergaenzen.
* CHANGELOG-Eintrag ergaenzen.

## Aufgabe 3: Showcase

* `demo-showcase-sync`-Skill laden.
* Backstage-Menue-Beispiel im Showcase-Modul ergaenzen.

## Aufgabe 4: Tests und Build

* `testing`-Skill vor Testerstellung laden.
* Default-Backstage-Inhalt und dessen Ueberschreibbarkeit testen.
* `./gradlew build` erfolgreich ausfuehren.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Test: ohne gesetztes `backstageContent` erscheint die Default-`FXBackstageMenuPane`.
* Test: explizit gesetztes `backstageContent` ueberschreibt den Default weiterhin.
