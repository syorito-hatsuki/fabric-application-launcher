package dev.syoritohatsuki.fabricapplicationlauncher.implementation.dummy

import dev.syoritohatsuki.fabricapplicationlauncher.FabricApplicationLauncherClientMod
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.IconManager
import net.minecraft.util.Identifier

object DummyIconManager : IconManager {
    override fun getIconIdentifier(icon: String): Identifier =
        Identifier.of(FabricApplicationLauncherClientMod.MOD_ID, "dummy")

    override fun getUniqueIconsCount(): Int = 0

    override fun preload(icon: String) {
    }
}