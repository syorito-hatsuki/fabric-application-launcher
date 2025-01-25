package dev.syoritohatsuki.fabricapplicationlauncher.manager

import net.minecraft.util.Identifier

interface IconManager {
    fun getIconIdentifier(icon: String): Identifier
    fun getUniqueIconsCount(): Int
}