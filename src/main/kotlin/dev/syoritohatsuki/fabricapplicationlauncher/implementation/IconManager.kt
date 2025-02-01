package dev.syoritohatsuki.fabricapplicationlauncher.implementation

import net.minecraft.util.Identifier

interface IconManager {
    enum class STATUS {
        LOADING,
        LOADED
    }
    fun getIconIdentifier(icon: String): Identifier
    fun getUniqueIconsCount(): Int
    fun preload(icon: String)
}