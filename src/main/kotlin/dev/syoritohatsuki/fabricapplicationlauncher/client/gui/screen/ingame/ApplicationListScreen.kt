package dev.syoritohatsuki.fabricapplicationlauncher.client.gui.screen.ingame

import dev.syoritohatsuki.fabricapplicationlauncher.FabricApplicationLauncherClientMod
import dev.syoritohatsuki.fabricapplicationlauncher.manager.linux.LinuxApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.manager.linux.LinuxIconManager
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.Text

class ApplicationListScreen : Screen(Text.literal("Applications")) {

    companion object {
        const val PADDING = 4
        const val TEXT_SIZE = 12
    }

    private var debugMode = false

    override fun init() {
        addDrawableChild(
            ApplicationListWidget(
                client = client ?: return,
                applicationManager = LinuxApplicationManager,
                iconManager = LinuxIconManager,
                width = width / 2,
                height = height / 2,
                y = 48,
                itemHeight = 36,
                search = ""
            )
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (debugMode) {
            TextWidget(
                Text.literal("Apps count: ${LinuxApplicationManager.getApps().count()}"), textRenderer
            ).apply {
                setPosition(PADDING, PADDING)
                renderWidget(context, mouseX, mouseY, delta)
            }

            TextWidget(
                Text.literal("Unique icons: ${LinuxIconManager.getUniqueIconsCount()}"), textRenderer
            ).apply {
                setPosition(PADDING, TEXT_SIZE + PADDING)
                renderWidget(context, mouseX, mouseY, delta)
            }
        }

        super.render(context, mouseX, mouseY, delta)
    }

    override fun shouldPause(): Boolean = false

    override fun shouldCloseOnEsc(): Boolean = true

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (FabricApplicationLauncherClientMod.openApplicationListKeyBinding.matchesKey(keyCode, -1)) {
            close()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
    }
}