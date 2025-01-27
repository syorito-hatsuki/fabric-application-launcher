package dev.syoritohatsuki.fabricapplicationlauncher.util

import dev.syoritohatsuki.fabricapplicationlauncher.manager.ApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.manager.IconManager
import dev.syoritohatsuki.fabricapplicationlauncher.manager.dummy.DummyApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.manager.dummy.DummyIconManager

object ManagerRegistry {

    private val registry: MutableMap<String, Pair<ApplicationManager, IconManager>> = mutableMapOf()

    val operationSystem: String = System.getProperty("os.name")

    fun register(name: String, applicationManager: ApplicationManager, iconManager: IconManager) {
        registry[name] = Pair(applicationManager, iconManager)
    }

    fun getApplicationManager(): ApplicationManager = registry.entries.firstOrNull {
        operationSystem.contains(it.key, true)
    }?.value?.first ?: DummyApplicationManager

    fun getIconManager(): IconManager = registry.entries.firstOrNull {
        operationSystem.contains(it.key, true)
    }?.value?.second ?: DummyIconManager


    fun isDummy(): Boolean = getApplicationManager() is DummyApplicationManager || getIconManager() is DummyIconManager
}