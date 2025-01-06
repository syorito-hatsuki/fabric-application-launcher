package dev.syoritohatsuki.fabricapplicationlauncher

import com.mojang.logging.LogUtils
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger

object FabricApplicationLauncherClientMod : ClientModInitializer {

    const val MOD_ID = "fabric-application-launcher"
    val logger: Logger = LogUtils.getLogger()

    override fun onInitializeClient() {
        logger.info("${javaClass.simpleName} initialized with mod-id $MOD_ID")
    }
}