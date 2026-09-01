# Implementierungsplan: FP-002-IP-09-GroupOverflow

## Voraussetzung

* IP-06 (Groups) und IP-07 (GroupLayout) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/MenuGroupView.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/GroupOverflowController.kt` (neu)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/menutab/MenuGroupView.fxml` (ändern)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `GroupOverflowController` misst die benötigte Breite der Layout-Einheiten aus IP-07.
* Bei Platzmangel wandern die zuletzt platzierten Einheiten (von rechts) in ein Overflow-Menü.
* Chevron-Button am Gruppenende, nur sichtbar wenn Overflow aktiv ist.
* Neuberechnung bei Breitenänderung der Gruppe (Width-Property-Listener).

## Aufgabe 1: Breitenmessung und Overflow-Erkennung

* `GroupOverflowController` mit Methode zur Ermittlung sichtbarer vs. überlaufender Einheiten.
* Listener auf die verfügbare Breite der Gruppe (`widthProperty`).
* Berücksichtigung der Layout-Varianten aus IP-07 bei der Breitenberechnung.

## Aufgabe 2: Overflow-UI

* Chevron-Button in `MenuGroupView.fxml` ergänzen, Sichtbarkeit an Overflow-Zustand gebunden.
* `ContextMenu` mit den überlaufenden Einheiten beim Klick auf den Chevron öffnen.
* Reihenfolge der Overflow-Einträge entspricht der ursprünglichen Anordnung.

## Aufgabe 3: Dokumentation und Build

* Doku-Abschnitt "Group-Overflow" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Overflow-Auslösung bei schmaler Breite, Chevron-Menü-Inhalt headless testen.
