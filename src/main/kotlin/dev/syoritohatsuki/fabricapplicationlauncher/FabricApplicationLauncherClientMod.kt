package dev.syoritohatsuki.fabricapplicationlauncher

import com.mojang.logging.LogUtils
import dev.syoritohatsuki.fabricapplicationlauncher.client.gui.screen.ingame.ApplicationListScreen
import dev.syoritohatsuki.fabricapplicationlauncher.manager.linux.LinuxApplicationManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger


object FabricApplicationLauncherClientMod : ClientModInitializer {

    const val MOD_ID = "fabric-application-launcher"
    val logger: Logger = LogUtils.getLogger()

    private val openApplicationListKeyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.open.applications",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.fabricapplicationlauncher"
        )
    )

    override fun onInitializeClient() {
        logger.info("${javaClass.simpleName} initialized with mod-id $MOD_ID")

        LinuxApplicationManager.fetchApps()

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            if (openApplicationListKeyBinding.wasPressed()) client.setScreen(ApplicationListScreen())
        })
    }
}