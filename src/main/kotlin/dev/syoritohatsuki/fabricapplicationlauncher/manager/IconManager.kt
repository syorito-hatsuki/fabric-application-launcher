package dev.syoritohatsuki.fabricapplicationlauncher.manager

import net.minecraft.util.Identifier
import java.nio.file.Path

interface IconManager {
    fun getIconPath(icon: String): Path?
    fun getIconIdentifier(icon: String): Identifier
    fun getLoadedIconsCount(): Int
    fun getIconsCount(): Int
}