/*
 * Copyright (c) KleinerHacker alias Pfeiffer C Soft 2026.
 * This work is licensed under the Apache License, Version 2.0.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations.
 */

package org.pcsoft.framework.panelium.menupane

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.StringProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.layout.StackPane

/**
 * A titled group of action controls shown in a regular [FXMenuTab]'s group strip. [title] labels the
 * group; [content] holds the arbitrary control nodes it arranges. Register groups on a tab through
 * [FXMenuTab.groups]; the currently active regular tab's groups are the ones rendered by [FXMenuPane].
 * Usable from FXML through the `<fx:root>` pattern.
 *
 * Disable the whole group through the inherited [setDisable] / [disableProperty]: JavaFX propagates
 * the disabled state to every node in [content] and applies the `:disabled` pseudo-class to the
 * group.
 *
 * [content] accepts arbitrary nodes and arranges them horizontally. For a ribbon-style layout wrap
 * the controls in the layout boxes [FXMenuGroupLargeBox] (one prominent control spanning the full
 * group height) and [FXMenuGroupSmallBox] (a stack of up to three small controls).
 *
 * **Anchor.** A group that contains any layout box must designate exactly one of them as its
 * [anchor]: the box that always stays visible. It is a normal member of [content] - its position in
 * the rendered row is simply its index in [content], so order [content] the way you want the row to
 * read and point [anchor] at one of its boxes. The mandatory constructor takes the full ordered
 * content plus the anchor; from FXML fill the `<content>` property element and set `<anchor>` as a
 * property tag referencing a box already declared in it:
 * ```
 * <FXMenuGroup title="Clipboard">
 *     <content>
 *         <FXMenuGroupSmallBox fx:id="edit">...</FXMenuGroupSmallBox>
 *         <FXMenuGroupLargeBox fx:id="paste">...</FXMenuGroupLargeBox>
 *     </content>
 *     <anchor><fx:reference source="paste"/></anchor>
 * </FXMenuGroup>
 * ```
 * Removing the anchor box from [content] is rejected; move it by replacing the whole list
 * (`content.setAll(...)`), which never leaves the list without it.
 *
 * **Overflow.** When the group strip cannot fit every group, the groups organise themselves: each
 * group keeps its preferred width (untouched groups do not change at all) and the strip-wide
 * coordinator collapses whole non-anchor boxes into a per-group chevron popup, lowest
 * [FXMenuGroupBoxPriority] first; the [anchor] and loose (non-box) nodes always stay visible. If
 * nothing more can be collapsed the strip overflows and scrolls horizontally with the mouse wheel.
 * [isOverflowActive] / [overflowActiveProperty] report whether this group currently has boxes in its
 * popup.
 *
 * **Launcher.** Set [onLauncherAction] to attach a small launcher button to the group's title row
 * (bottom-right, following the ribbon convention). The button is shown only while [onLauncherAction]
 * is non-`null`; clicking it fires an [ActionEvent] to that handler, following the JavaFX `onXxx`
 * event convention. Settable from FXML as `onLauncherAction="#methodName"`.
 *
 * Style classes: `menu-group` on the component itself, `menu-group-content` on the control row,
 * `menu-group-title` on the caption label, `menu-group-overflow-button` on the chevron button,
 * `menu-group-launcher` on the launcher button.
 */
class FXMenuGroup() : StackPane() {

    private val viewModel: FXMenuGroupViewModel

    init {
        val tuple = FluentViewLoader.fxmlView(FXMenuGroupView::class.java)
            .root(this)
            .load()
        viewModel = tuple.viewModel

        styleClass.add("menu-group")
        // Sit at the preferred width in the group strip - the overflow coordinator resizes a group
        // only by collapsing whole non-anchor boxes, never by squeezing it.
        minWidth = USE_PREF_SIZE
        maxWidth = USE_PREF_SIZE

        viewModel.content.addListener(
            ListChangeListener {
                val current = viewModel.anchor.get()
                check(current == null || current in viewModel.content) {
                    "FXMenuGroup anchor must stay in content; replace the whole list to reorder it"
                }
            },
        )
    }

    /** Builds the group from the full ordered [content] and its always-visible [anchor] box. */
    constructor(vararg content: Node, anchor: FXMenuGroupBox) : this() {
        require((anchor as Node) in content) {
            "FXMenuGroup anchor must be one of the content nodes; place it in the content varargs"
        }
        viewModel.content.setAll(*content)
        viewModel.anchor.set(anchor)
    }

    /** The group caption shown below its controls. */
    fun titleProperty(): StringProperty = viewModel.title

    var title: String
        get() = viewModel.title.get()
        set(value) = viewModel.title.set(value)

    /** The ordered control nodes the group arranges; contains the [anchor] box. */
    val content: ObservableList<Node> get() = viewModel.content

    /**
     * The layout box that always stays visible. Must be an [FXMenuGroupLargeBox] /
     * [FXMenuGroupSmallBox] already contained in [content]. Required for any group that holds layout
     * boxes; the overflow computation throws if it is missing.
     */
    fun anchorProperty(): ObjectProperty<Node?> = viewModel.anchor

    var anchor: Node?
        get() = viewModel.anchor.get()
        set(value) {
            require(value is FXMenuGroupBox) {
                "FXMenuGroup anchor must be a FXMenuGroupLargeBox or FXMenuGroupSmallBox"
            }
            require(value in viewModel.content) {
                "FXMenuGroup anchor must be one of the content nodes"
            }
            viewModel.anchor.set(value)
        }

    /**
     * The handler behind the group's launcher button. While non-`null` a `menu-group-launcher`
     * button is shown in the title row and a click fires an [ActionEvent] to this handler; `null`
     * hides the button. Follows the JavaFX `onXxx` event convention.
     */
    fun onLauncherActionProperty(): ObjectProperty<EventHandler<ActionEvent>?> = viewModel.onLauncherAction

    var onLauncherAction: EventHandler<ActionEvent>?
        get() = viewModel.onLauncherAction.get()
        set(value) = viewModel.onLauncherAction.set(value)

    /** Whether one or more non-anchor boxes are currently collapsed into the chevron overflow popup. */
    fun overflowActiveProperty(): ReadOnlyBooleanProperty = viewModel.overflowActive

    val isOverflowActive: Boolean
        get() = viewModel.overflowActive.get()

    /** The renderer/measurer used by [MenuGroupStripOverflowCoordinator]. */
    internal val overflowController: MenuGroupOverflowController
        get() = viewModel.overflowController ?: error("FXMenuGroupView is not initialised yet")
}
