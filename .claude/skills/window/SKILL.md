---
Name: window
Description: Basic architecture for a Java FX window component; required to create or edit Java FX windows.
---

# Java FX Windows

## Template

* The template is located here: `src/main/kotlin/org/pcsoft/framework/panelium/chrome/window`
  * Their design and architecture MUST be adopted (Component, View, ViewModel)

## Structure

* The FXML always contains the window content
  * The content is fully defined there

## Location

* Window classes MUST be placed under the root package in `ui.window`
  * Every required class MUST be placed in its own file
* Window FXML MUST be placed in the same directory within the resource directory