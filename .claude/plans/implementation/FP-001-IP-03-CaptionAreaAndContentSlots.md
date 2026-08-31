# FP-001-IP-03: CaptionAreaAndContentSlots

## Abhängigkeiten

* IP-01

## Betroffene Bereiche

* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromeCaptionBar.kt` — neu
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/ChromePane.kt` — Caption-Bar einhängen, API
* `src/main/kotlin/org/pcsoft/framework/panelium/chrome/internal/ChromeConfig.kt` — Caption-Mindesthöhe
* `docs/docs/usage.md`, `CHANGELOG.md`

## Design-Festlegungen

* `ChromeCaptionBar : Region` mit drei Slots `leftItems`, `centerItems`, `rightItems` als `ObservableList<Node>`
* Anordnung HBox-artig: links, dann Center mit `HGROW`, dann rechts, dann Caption-Button-Slot
* Caption-Button-Slot ist ein reservierter `ObjectProperty<Node>` (Befüllung in IP-05), Z-Order oben
* Default-Knoten: `titleLabel` an `stage.titleProperty`, `iconView` an erstes Bild aus `stage.icons`
* Default-Knoten liegen in `leftItems`; abschaltbar über `defaultTitleVisible` / `defaultIconVisible`
* `ChromePane`-API: `captionBar` (read-only), `captionLeftItems/centerItems/rightItems`, beide Sichtbarkeits-Properties
* Höhe variabel: `captionBar.prefHeight` aus Inhalt, Mindesthöhe aus Konstante
* FXML: `@DefaultProperty("content")` auf `ChromePane`; Slot-Listen FXML-befüllbar; `<fx:root>` nutzbar

## Aufgaben

### Aufgabe 1: ChromeCaptionBar

* `Region` mit drei `ObservableList<Node>`-Slots
* `layoutChildren()`: links/rechts feste Breite, Center dehnt sich
* Änderungen an Slots lösen `requestLayout()` aus

### Aufgabe 2: Einhängen in ChromePane

* IP-01-Caption-Platzhalter durch `ChromeCaptionBar` ersetzen
* Caption-Höhe in `ChromePane.layoutChildren()` berücksichtigen
* `captionBar` als read-only Zugriff bereitstellen

### Aufgabe 3: Default-Titel

* `titleLabel` erstellen, Text an `stage.titleProperty` binden
* in `leftItems` einsortieren
* `defaultTitleVisible`-Property schaltet Knoten ein/aus

### Aufgabe 4: Default-Icon

* `iconView` erstellen, `ListChangeListener` auf `stage.icons` setzt erstes Bild
* in `leftItems` vor dem Titel einsortieren
* `defaultIconVisible`-Property schaltet Knoten ein/aus

### Aufgabe 5: Öffentliche Slot-API und Button-Slot

* `captionLeftItems/centerItems/rightItems` an die Bar delegieren
* reservierten Caption-Button-Slot als `ObjectProperty<Node>` anlegen, Z-Order oben
* Sichtbarkeits-Properties dokumentiert freigeben

### Aufgabe 6: FXML-Unterstützung, Doku, Build

* `@DefaultProperty("content")`, Slot-Properties FXML-tauglich, `<fx:root>`-Beispiel prüfen
* `docs/docs/usage.md` „Inhalte im Fensterrahmen" inkl. FXML-Beispiel, CHANGELOG-Eintrag
* `./gradlew build`

## Fertig-Kriterien

* App kann Knoten in linken, mittleren, rechten Slot einfügen; Layout korrekt
* Titel und Icon erscheinen standardmäßig, folgen den `Stage`-Properties, sind abschaltbar
* `ChromePane` als FXML-Wurzel und via `<fx:root>` nutzbar
* `build` grün
