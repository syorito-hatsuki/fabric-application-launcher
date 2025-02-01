package dev.syoritohatsuki.fabricapplicationlauncher.implementation.linux

import dev.syoritohatsuki.fabricapplicationlauncher.implementation.AbstractSettingsScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.DirectionalLayoutWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

class LinuxSettingsScreen : AbstractSettingsScreen(Text.literal("Linux Settings")) {

    private var languageSelectionList: ThemeSelectionListWidget? = null
    private var buttonWidget: ButtonWidget? = null

    override fun init() {
        val column = positioningWidget.add(DirectionalLayoutWidget.vertical().spacing(8))
        column.mainPositioner.alignHorizontalCenter()

        this.languageSelectionList = column.add(ThemeSelectionListWidget())

        buttonWidget = column.add(
            ButtonWidget.builder(Text.of("Use")) {
                if (languageSelectionList?.selectedOrNull?.themeName.equals(
                        LinuxIconManager.getSelectedTheme(),
                        true
                    )
                ) {
                    close()
                } else if (!LinuxIconManager.isLoading()) {
                    LinuxIconManager.reload(languageSelectionList?.selectedOrNull?.themeName ?: "Theme is Null :(")
                    it.active = false
                }
            }.build()
        )

        super.init()
    }

    override fun tick() {
        if (!LinuxIconManager.isLoading()) {
            buttonWidget?.active = true
            if (languageSelectionList?.selectedOrNull?.themeName.equals(LinuxIconManager.getSelectedTheme(), true)) {
                buttonWidget?.message = Text.literal("Done. Close")
                return
            }
            buttonWidget?.message = Text.literal("Use")
        } else {
            buttonWidget?.message = Text.literal("Loading...")
        }
    }

    override fun close() {
        if (!LinuxIconManager.isLoading()) super.close()
    }

    inner class ThemeSelectionListWidget : AlwaysSelectedEntryListWidget<ThemeSelectionListWidget.ThemeEntry>(
        client, this@LinuxSettingsScreen.width, this@LinuxSettingsScreen.height - 33 - 53, 33, 18
    ) {

        init {
            LinuxIconManager.THEMES.toSortedMap().forEach {
                addEntry(ThemeEntry(it.key))
            }

            children().onEach {
                if (it.themeName.equals(LinuxIconManager.getSelectedTheme(), true)) {
                    setSelected(it)
                    return@onEach
                }
            }
        }

        inner class ThemeEntry(val themeName: String) : Entry<ThemeEntry>() {

            override fun render(
                context: DrawContext,
                index: Int,
                y: Int,
                x: Int,
                entryWidth: Int,
                entryHeight: Int,
                mouseX: Int,
                mouseY: Int,
                hovered: Boolean,
                tickDelta: Float
            ) {
                context.drawCenteredTextWithShadow(
                    this@LinuxSettingsScreen.textRenderer,
                    this.themeName,
                    this@ThemeSelectionListWidget.width / 2,
                    y + entryHeight / 2 - 9 / 2,
                    Colors.WHITE
                )
            }

            override fun getNarration(): Text = Text.empty()

            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
                if (LinuxIconManager.isLoading()) return false

                this.onPressed()
                return super.mouseClicked(mouseX, mouseY, button)
            }

            private fun onPressed() {
                this@ThemeSelectionListWidget.setSelected(this)
            }
        }
    }
}