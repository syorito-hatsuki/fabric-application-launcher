package dev.syoritohatsuki.fabricapplicationlauncher.client.gui.screen.ingame

import dev.syoritohatsuki.fabricapplicationlauncher.manager.linux.LinuxApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.manager.linux.LinuxIconManager
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class ApplicationListScreen : Screen(Text.literal("Applications")) {
    override fun init() {
        if (client != null) addDrawableChild(
            ApplicationListWidget(
                client!!,
                LinuxApplicationManager,
                LinuxIconManager,
                width / 2,
                height / 2,
                48,
                36,
                "",
                this
            )
        )
    }

    override fun shouldPause(): Boolean = false

    override fun shouldCloseOnEsc(): Boolean = true

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {

    }
}