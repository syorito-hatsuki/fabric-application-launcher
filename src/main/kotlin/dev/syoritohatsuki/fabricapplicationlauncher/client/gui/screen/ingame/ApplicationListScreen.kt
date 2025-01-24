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

    override fun init() {
        if (client != null) {
            // Basic debug
            addDrawableChild(
                TextWidget(
                    Text.literal("Apps count: ${LinuxApplicationManager.getApps().count()}"),
                    textRenderer
                ).apply { setPosition(PADDING, PADDING) })
            addDrawableChild(
                TextWidget(
                    Text.literal("Loaded icons: ${LinuxIconManager.getLoadedIconsCount()}"),
                    textRenderer
                ).apply { setPosition(PADDING, TEXT_SIZE + PADDING) })
            addDrawableChild(
                TextWidget(
                    Text.literal("Rendered icons: ${LinuxIconManager.getIconsCount()}"),
                    textRenderer
                ).apply { setPosition(PADDING, TEXT_SIZE * 2 + PADDING) })

            //Selected debug
            // TODO Selected debug

            // Actual view
            addDrawableChild(
                ApplicationListWidget(
                    client!!,
                    LinuxApplicationManager,
                    LinuxIconManager,
                    width / 2,
                    height / 2,
                    48,
                    36,
                    ""
                )
            )
        }
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