package dev.syoritohatsuki.fabricapplicationlauncher.client.gui.screen.ingame

import dev.syoritohatsuki.fabricapplicationlauncher.dto.Application
import dev.syoritohatsuki.fabricapplicationlauncher.manager.ApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.manager.IconManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Colors

class ApplicationListWidget(
    client: MinecraftClient,
    applicationManager: ApplicationManager,
    iconManager: IconManager,
    width: Int,
    height: Int,
    y: Int,
    itemHeight: Int,
    search: String
) : AlwaysSelectedEntryListWidget<ApplicationListWidget.ApplicationEntry>(client, width, height, y, itemHeight) {

    init {
        setPosition(this.width / 2, this.height / 2)
        applicationManager.getApps().sortedBy { it.name }.forEach { app ->
            addEntry(ApplicationEntry(client, iconManager, app))
        }
    }

    override fun clearEntries() {
        children().forEach(ApplicationEntry::close)
        super.clearEntries()
    }

    class ApplicationEntry(
        private val client: MinecraftClient,
        private val iconManager: IconManager,
        private val application: Application
    ) :
        Entry<ApplicationEntry>(), AutoCloseable {

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
            val icon = iconManager.getIconIdentifier(application.icon)

            context.drawTextWithShadow(client.textRenderer, application.name, x + 32 + 3, y + 1, Colors.WHITE)
            context.drawTextWithShadow(
                client.textRenderer, application.description, x + 32 + 3, y + 9 + 3, Colors.LIGHT_GRAY
            )
            context.drawTextWithShadow(
                client.textRenderer, application.executable, x + 32 + 3, y + 9 + 9 + 3, Colors.GRAY
            )
            icon.let {
                context.drawTexture(
                    RenderLayer::getGuiTextured,
                    it,
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
        }

        override fun getNarration(): Text = Text.empty()

        override fun close() {
        }

    }
}