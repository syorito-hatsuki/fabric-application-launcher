package dev.syoritohatsuki.fabricapplicationlauncher

import com.mojang.logging.LogUtils
import dev.syoritohatsuki.fabricapplicationlauncher.client.gui.screen.ingame.ApplicationListScreen
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.IconManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.linux.LinuxApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.linux.LinuxIconManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.linux.LinuxSettingsScreen
import dev.syoritohatsuki.fabricapplicationlauncher.util.ManagerRegistry
import kotlinx.coroutines.*
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

    private val scope = CoroutineScope(Dispatchers.IO)

    const val MOD_ID = "fabric-application-launcher"
    val logger: Logger = LogUtils.getLogger()

    val openApplicationListKeyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.open.applications", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.fabricapplicationlauncher"
        )
    )

    val enableDebugApplicationListKeyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.open.applications.debug", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.fabricapplicationlauncher"
        )
    )

    override fun onInitializeClient() {
        logger.info("${javaClass.simpleName} initialized with mod-id $MOD_ID")

        ManagerRegistry.register(
            name = "linux",
            applicationManager = { LinuxApplicationManager },
            iconManager = { LinuxIconManager },
            settingsScreen = { LinuxSettingsScreen() })

        ClientLifecycleEvents.CLIENT_STARTED.register {

            logger.info("<-----[ Manager Registry Loaded ]----->")
            logger.info("Application: " + ManagerRegistry.getApplicationManager().javaClass.simpleName)
            logger.info("Icon: " + ManagerRegistry.getIconManager().javaClass.simpleName)
            if (ManagerRegistry.getIconManager() is LinuxIconManager) logger.info("Theme: " + LinuxIconManager.getSelectedTheme())
            logger.info("<------------------------------------->")

            scope.launch {
                ManagerRegistry.getApplicationManager().fetchApps()
                ManagerRegistry.getApplicationManager().getApps().map {
                    async {
                        ManagerRegistry.getIconManager().preload(it.icon)
                    }
                }.awaitAll()

                if (ManagerRegistry.getIconManager() is LinuxIconManager) {
                    (ManagerRegistry.getIconManager() as LinuxIconManager).status = IconManager.STATUS.LOADED
                }
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            if (openApplicationListKeyBinding.wasPressed()) {
                if (ManagerRegistry.isDummy()) {
                    client.inGameHud.setOverlayMessage(
                        Text.translatable("warning.unsupported.os", ManagerRegistry.operationSystem), false
                    )
                    return@EndTick
                }

                client.setScreen(ApplicationListScreen())
            }
        })
    }
}