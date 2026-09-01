# Platinum Chrome - Implementierung

`PaneliumFX` kann ein JavaFX-Fenster in ein undekoriertes, transparentes Fenster mit
eigenem Rahmen (`ChromePane`) um den eigentlichen Inhalt verwandeln: Schlagschatten,
Rahmenlinie, eine zusammensetzbare Titelleiste und den Inhaltsbereich.

## Abhängigkeit hinzufügen

Die Artefakte werden auf Maven Central unter der Gruppe `org.pcsoft.framework` veröffentlicht.

=== "Gradle (Kotlin DSL)"

    ```kotlin
    dependencies {
        implementation("org.pcsoft.framework:panelium:<version>")
    }
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>org.pcsoft.framework</groupId>
        <artifactId>panelium</artifactId>
        <version>VERSION</version>
    </dependency>
    ```

JavaFX (`javafx.controls`, Version 25 oder neuer, Java 25 oder neuer) wird transitiv
mitgeliefert, sodass für die öffentlichen API-Typen keine zusätzliche JavaFX-Abhängigkeit
nötig ist. Das Laden von `ChromePane` oder `ChromeCaptionBar` aus FXML erfordert
zusätzlich das Modul `javafx.fxml` auf dem Modulpfad.

## Einstiegspunkte

Es gibt drei Einstiegspunkte, die über eine gemeinsame Konfigurationsroutine alle
dieselbe Rahmenstruktur erzeugen:

- `PaneliumChrome.install(stage)` - wandelt eine bestehende `Stage` um, bevor sie
  angezeigt wird; die aktuelle Scene-Root wird zum Inhalt des Rahmens. Gibt den erzeugten
  `ChromePane` zurück.
- `PaneliumStage` - eine `Stage`-Unterklasse, die bereits mit einem `ChromePane`
  vorkonfiguriert ist; die `content`-Eigenschaft setzt die eigene Oberfläche. Unterstützt
  `initOwner`/`initModality` wie jede andere `Stage`.
- `ChromePane` - der Rahmen selbst, für Fälle, in denen `Scene` und `Stage` manuell
  aufgebaut werden, oder als Wurzelelement einer FXML-Datei.

!!! warning
    `PaneliumChrome.install(stage)` muss vor `Stage.show()` aufgerufen werden. Es
    schaltet die Stage auf `StageStyle.TRANSPARENT` und installiert eine neue
    transparente `Scene`.

Jedes Beispiel unten zeigt die Kotlin-Variante und die entsprechende FXML-Variante
nebeneinander. In der FXML-Variante wird `ChromePane` (`@DefaultProperty("content")`)
bzw. `ChromeCaptionBar` direkt in die FXML-Datei eingebettet - entweder als
Wurzelelement der Datei oder über das `<fx:root>`-Muster - und mit `FXMLLoader` geladen.

## PaneliumStage

`PaneliumStage` baut die `Stage` selbst auf und bleibt daher Kotlin-Code; der darauf
platzierte Inhalt kann trotzdem aus FXML stammen.

```kotlin
val stage = PaneliumStage()
stage.content = Label("Hallo, PaneliumFX!")
stage.show()
```

## PaneliumChrome.install

`PaneliumChrome.install(stage)` baut ebenfalls die `Stage` auf, die umschlossene
Scene-Root kann aber genauso gut in Kotlin gebaut oder aus FXML geladen werden.

```kotlin
val stage = Stage()
stage.scene = Scene(buildRoot())
val chrome = PaneliumChrome.install(stage)
stage.show()
```

## ChromePane

=== "Kotlin"

    ```kotlin
    val chrome = ChromePane(Label("Hallo, PaneliumFX!"))
    val scene = Scene(chrome)
    scene.fill = Color.TRANSPARENT

    val stage = Stage()
    stage.initStyle(StageStyle.TRANSPARENT)
    stage.scene = scene
    stage.show()
    ```

=== "FXML"

    ```xml
    <?import javafx.scene.control.Label?>
    <?import org.pcsoft.framework.panelium.chrome.ChromePane?>

    <ChromePane xmlns:fx="http://javafx.com/fxml">
        <Label text="Hallo, PaneliumFX!"/>
    </ChromePane>
    ```

    ```kotlin
    val chrome = FXMLLoader.load<ChromePane>(resource)
    val stage = Stage()
    stage.initStyle(StageStyle.TRANSPARENT)
    stage.scene = Scene(chrome).apply { fill = Color.TRANSPARENT }
    chrome.attachStage(stage)
    stage.show()
    ```

Der Inhalt kann zur Laufzeit über die `content`-Eigenschaft ausgetauscht werden (bzw.
`contentProperty()` für Bindings).

Sobald der Rahmen an eine `Stage` angehängt ist, übernimmt der Rahmen selbst die
üblichen Fensteroperationen (Verschieben, Größenänderung, Maximieren, Vollbild) - keine
zusätzliche Verdrahtung ist nötig. Die Größenänderung berücksichtigt `Stage.isResizable`
sowie die `minWidth`/`minHeight`/`maxWidth`/`maxHeight`-Einschränkungen der `Stage`.

## Inhalts- und Titelleisten-Slots

Die Titelleiste (`ChromePane.captionBar`, ein `ChromeCaptionBar`) besitzt drei
Inhalts-Slots, jeweils eine `ObservableList<Node>`:

- `captionLeftItems` - Vorderkante, nach dem Standardsymbol und -titel
- `captionCenterItems` - horizontal wachsender mittlerer Bereich
- `captionRightItems` - Hinterkante, vor den Fensterschaltflächen

=== "Kotlin"

    ```kotlin
    val chrome = ChromePane(buildRoot())
    chrome.captionCenterItems.add(Label("Unbenannt"))
    chrome.captionRightItems.add(Button("Anmelden"))
    ```

=== "FXML"

    ```xml
    <?import javafx.scene.control.Button?>
    <?import javafx.scene.control.Label?>
    <?import org.pcsoft.framework.panelium.chrome.ChromePane?>

    <ChromePane xmlns:fx="http://javafx.com/fxml">
        <captionCenterItems>
            <Label text="Unbenannt"/>
        </captionCenterItems>
        <captionRightItems>
            <Button text="Anmelden"/>
        </captionRightItems>
    </ChromePane>
    ```

Ein Standard-Titelknoten (gebunden an `Stage.title`) und ein Standard-Symbolknoten
(gebunden an das erste Bild in `Stage.icons`) werden im vorderen Slot angezeigt.
Beides lässt sich über `isDefaultTitleVisible` / `isDefaultIconVisible` (bzw. deren
`*Property()`-Zugriffe) abschalten:

=== "Kotlin"

    ```kotlin
    chrome.isDefaultTitleVisible = false
    ```

=== "FXML"

    ```xml
    <ChromePane xmlns:fx="http://javafx.com/fxml" defaultTitleVisible="false">
        ...
    </ChromePane>
    ```

### Drag-Bereiche und Passthrough

Der Hintergrund der Titelleiste ist die Ziehzone des Fensters; interaktive Steuerelemente,
die einem Slot hinzugefügt werden, behalten ihre eigenen Klicks. Bei Knoten, für die die
Heuristik falsch liegt, lässt sich das über die angehängte Eigenschaft
`ChromeCaptionBar.setDragRegion(node, value)` überschreiben:

- `true` - der Knoten zieht das Fenster, obwohl er interaktiv ist (z. B. eine gefüllte
  Symbolleiste)
- `false` - der Knoten zieht nie, auch wenn er inaktiv wirkt (ein eigenes Hit-Target)
- `null` - Überschreibung aufheben und zur Heuristik zurückkehren (Standard)

=== "Kotlin"

    ```kotlin
    val strip = HBox(Label("Projekt"), Separator(), Label("main"))
    ChromeCaptionBar.setDragRegion(strip, true)
    chrome.captionCenterItems.add(strip)
    ```

=== "FXML"

    ```xml
    <?import javafx.scene.control.Label?>
    <?import javafx.scene.control.Separator?>
    <?import javafx.scene.layout.HBox?>
    <?import org.pcsoft.framework.panelium.chrome.ChromeCaptionBar?>

    <captionCenterItems>
        <HBox ChromeCaptionBar.dragRegion="true">
            <Label text="Projekt"/>
            <Separator/>
            <Label text="main"/>
        </HBox>
    </captionCenterItems>
    ```

Die Markierung wird ausgehend vom Knoten unter dem Zeiger nach oben aufgelöst; der erste
Knoten mit einer Markierung entscheidet.

### Fensterschaltflächen

Die Schaltflächen zum Minimieren, Maximieren/Wiederherstellen und Schließen werden
automatisch hinzugefügt, sobald der Rahmen (über einen der drei Einstiegspunkte) an eine
`Stage` angehängt wird. Sie belegen den reservierten Schaltflächen-Slot der Titelleiste
und bleiben daher stets außerhalb der selbst hinzugefügten `captionRightItems` /
`captionLeftItems`.

Anordnung und natives Erscheinungsbild folgen dem Host-Betriebssystem: Unter Windows,
Linux und jeder anderen Plattform sitzen die Schaltflächen an der Hinterkante in der
Reihenfolge Minimieren, Maximieren, Schließen, mit Standardsymbol und -titel an der
Vorderkante; unter macOS sitzen sie an der Vorderkante in der Reihenfolge Schließen,
Minimieren, Zoomen, und Standardsymbol sowie -titel wandern an die Hinterkante.

Das erkannte Betriebssystem lässt sich über `captionOsProperty()` (bzw. die
`captionOs`-Eigenschaft) überschreiben - nützlich für Tests, Demos und
plattformübergreifende Vorschauen:

=== "Kotlin"

    ```kotlin
    val chrome = PaneliumStage().apply { content = buildRoot() }
    chrome.chromePane.captionOs = ChromeOs.MAC
    ```

=== "FXML"

    ```xml
    <?import org.pcsoft.framework.panelium.chrome.ChromeOs?>
    <?import org.pcsoft.framework.panelium.chrome.ChromePane?>

    <ChromePane xmlns:fx="http://javafx.com/fxml" captionOs="MAC">
        ...
    </ChromePane>
    ```
