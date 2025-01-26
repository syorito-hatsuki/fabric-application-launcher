package dev.syoritohatsuki.fabricapplicationlauncher.client.gui.screen.ingame

import dev.syoritohatsuki.fabricapplicationlauncher.dto.Application
import dev.syoritohatsuki.fabricapplicationlauncher.manager.ApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.manager.IconManager
import dev.syoritohatsuki.fabricapplicationlauncher.util.execute
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.MultilineTextWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util

class ApplicationListWidget(
    client: MinecraftClient,
    val applicationManager: ApplicationManager,
    val iconManager: IconManager,
    width: Int,
    height: Int,
    y: Int,
    itemHeight: Int,
    private val screen: ApplicationListScreen
) : AlwaysSelectedEntryListWidget<ApplicationListWidget.ApplicationEntry>(client, width, height, y, itemHeight) {

    private var search = ""

    init {
        setPosition(this.width / 2, this.height / 2)
        applicationManager.getApps().sortedBy { it.name }.forEach { app ->
            addEntry(ApplicationEntry(client, iconManager, app))
        }
    }

    fun setSearch(search: String) {
        if (this.search != search) {
            this.clearEntries()
            refreshScroll()
            applicationManager.getApps().sortedBy { it.name }.filter {
                search.isBlank() || (it.name.contains(search, true) || it.description.contains(search, true))
            }.forEach { app ->
                addEntry(ApplicationEntry(client, iconManager, app))
            }
            this.search = search
        }
    }

    override fun drawScrollbar(context: DrawContext) {
    }

    override fun drawMenuListBackground(context: DrawContext) {
    }

    override fun drawHeaderAndFooterSeparators(context: DrawContext) {
        mapOf(
            Screen.INWORLD_HEADER_SEPARATOR_TEXTURE to this.y - 2,
            Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE to this.bottom
        ).map {
            context.drawTexture(
                RenderLayer::getGuiTextured,
                it.key,
                (screen.searchBox?.x ?: 0) - 2,
                it.value,
                0.0f,
                0.0f,
                rowWidth + 2,
                2,
                32,
                2
            )
        }
    }

    inner class ApplicationEntry(
        private val client: MinecraftClient, private val iconManager: IconManager, val application: Application
    ) : Entry<ApplicationEntry>(), AutoCloseable {

        private var clickTime: Long = 0

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
            MultilineTextWidget(
                x + 32 + 3, y + 1, Text.literal(application.name).formatted(Formatting.WHITE), client.textRenderer
            ).apply {
                setMaxWidth(entryWidth - 36)
                setMaxRows(1)
            }.renderWidget(context, mouseX, mouseY, tickDelta)

            MultilineTextWidget(
                x + 32 + 3,
                y + 9 + 3,
                Text.literal(application.description).formatted(Formatting.GRAY),
                client.textRenderer
            ).apply {
                setMaxWidth(entryWidth - 36)
                setMaxRows(1)
            }.renderWidget(context, mouseX, mouseY, tickDelta)

            MultilineTextWidget(
                x + 32 + 3,
                y + 9 + 9 + 3,
                Text.literal(application.executable).formatted(Formatting.GRAY),
                client.textRenderer,
            ).apply {
                setMaxWidth(entryWidth - 36)
                setMaxRows(1)
            }.renderWidget(context, mouseX, mouseY, tickDelta)

            context.drawTexture(
                RenderLayer::getGuiTextured,
                iconManager.getIconIdentifier(application.icon),
                x,
                y,
                0.0f,
                0.0f,
                32,
                32,
                32,
                32
            )
        }

        override fun getNarration(): Text = Text.empty()

        override fun close() {
            client.setScreen(null)
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            this.onPressed()

            if (Util.getMeasuringTimeMs() - this.clickTime < 250L) {
                execute(application.executable)
            }

            this.clickTime = Util.getMeasuringTimeMs()
            return super.mouseClicked(mouseX, mouseY, button)
        }

        private fun onPressed() {
            this@ApplicationListWidget.setSelected(this)
        }
    }
}