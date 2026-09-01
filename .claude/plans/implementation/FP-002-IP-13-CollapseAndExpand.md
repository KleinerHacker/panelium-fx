# Implementierungsplan: FP-002-IP-13-CollapseAndExpand

## Voraussetzung

* IP-01 (MenuTabCore) und IP-11 (ChromeDocking) abgeschlossen.

## Betroffene Dateien

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTab.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabView.kt` (ändern)
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/menutab/CollapseController.kt` (neu)
* `src/main/resources/org/pcsoft/framework/panelium/chrome/menutab/FXMenuTabView.fxml` (ändern)
* `docs/docs/menu-pane/implementation.md`, `.de.md` (aktualisieren)
* `CHANGELOG.md` (Eintrag ergänzen)

## Design-Entscheidungen

* `collapsedProperty` (`BooleanProperty`) auf `FXMenuTab`, verwaltet durch `CollapseController`.
* Doppelklick auf aktiven Tab-Button togglet `collapsed`.
* Expliziter Toggle-Button am Ende des Tab-Strips.
* Einzelklick auf Tab im eingeklappten Zustand zeigt Gruppen temporär (Peek), ohne `collapsed`
  zu ändern; Peek schließt bei Fokusverlust oder erneutem Klick.
* Backstage-Aktivierung (IP-05) merkt sich `collapsed` vor Öffnen und stellt ihn beim Schließen
  wieder her.

## Aufgabe 1: Collapse-Zustand

* `CollapseController` mit `collapsedProperty` und Toggle-Methode anlegen.
* Group-Strip-Sichtbarkeit an `collapsed` binden (ausgeblendet wenn `true`, außer Peek aktiv).

## Aufgabe 2: Trigger

* Doppelklick-Handler auf aktivem Tab-Button ergänzen.
* Toggle-Button in `FXMenuTabView.fxml` ergänzen, verdrahtet mit `CollapseController`.
* Einzelklick-Peek-Logik: temporäres Einblenden, Schließen bei Fokusverlust/erneutem Klick.

## Aufgabe 3: Zusammenspiel mit Backstage

* Hook-Property/Callback für IP-05: Collapse-Zustand vor Backstage-Öffnung sichern.
* Wiederherstellung des gesicherten Zustands beim Schließen der Backstage.

## Aufgabe 4: Dokumentation und Build

* Doku-Abschnitt "Ribbon Ein-/Ausklappen" ergänzen.
* CHANGELOG-Eintrag ergänzen.
* `./gradlew build` grün.

## Tests

* `testing`-Skill vor Testerstellung laden.
* Doppelklick, Toggle-Button, Peek-Verhalten, Restore nach Backstage headless testen.
