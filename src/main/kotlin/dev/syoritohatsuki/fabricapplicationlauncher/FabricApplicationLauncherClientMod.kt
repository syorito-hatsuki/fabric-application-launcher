package dev.syoritohatsuki.fabricapplicationlauncher

import com.mojang.logging.LogUtils
import dev.syoritohatsuki.fabricapplicationlauncher.client.gui.screen.ingame.ApplicationListScreen
import dev.syoritohatsuki.fabricapplicationlauncher.manager.linux.LinuxApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.manager.linux.LinuxIconManager
import dev.syoritohatsuki.fabricapplicationlauncher.util.ManagerRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger

object FabricApplicationLauncherClientMod : ClientModInitializer {

    const val MOD_ID = "fabric-application-launcher"
    val logger: Logger = LogUtils.getLogger()

    val openApplicationListKeyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.open.applications",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.fabricapplicationlauncher"
        )
    )

    val enableDebugApplicationListKeyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.open.applications.debug",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "category.fabricapplicationlauncher"
        )
    )

    override fun onInitializeClient() {
        logger.info("${javaClass.simpleName} initialized with mod-id $MOD_ID")

        ManagerRegistry.register("linux", LinuxApplicationManager, LinuxIconManager)

        ClientLifecycleEvents.CLIENT_STARTED.register {

            logger.info("<-----[ Manager Registry Loaded ]----->")
            logger.info("Application: " + ManagerRegistry.getApplicationManager().javaClass.simpleName)
            logger.info("Icon: " + ManagerRegistry.getIconManager().javaClass.simpleName)
            logger.info("<------------------------------------->")

            CoroutineScope(Dispatchers.IO).launch {
                ManagerRegistry.getApplicationManager().fetchApps()
                ManagerRegistry.getApplicationManager().getApps().forEach {
                    CoroutineScope(Dispatchers.IO).launch {
                        ManagerRegistry.getIconManager().preload(it.icon)
                    }
                }
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            if (openApplicationListKeyBinding.wasPressed()) {
                if (ManagerRegistry.isDummy()) {
                    client.inGameHud.setOverlayMessage(
                        Text.literal("Unsupported OS: ${ManagerRegistry.operationSystem}"), false
                    )
                    return@EndTick
                }

                client.setScreen(ApplicationListScreen())
            }
        })
    }
}