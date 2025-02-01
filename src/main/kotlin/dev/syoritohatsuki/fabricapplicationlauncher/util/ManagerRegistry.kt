package dev.syoritohatsuki.fabricapplicationlauncher.util

import dev.syoritohatsuki.fabricapplicationlauncher.implementation.AbstractSettingsScreen
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.ApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.IconManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.dummy.DummyApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.dummy.DummyIconManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.dummy.DummySettingsScreen

object ManagerRegistry {

    private val registry: MutableMap<String, Triple<ApplicationManager, IconManager, AbstractSettingsScreen>> =
        mutableMapOf()

    val operationSystem: String = System.getProperty("os.name")

    fun register(
        name: String,
        applicationManager: ApplicationManager,
        iconManager: IconManager,
        settingsScreen: AbstractSettingsScreen
    ) {
        registry[name] = Triple(applicationManager, iconManager, settingsScreen)
    }

    fun getApplicationManager(): ApplicationManager = registry.entries.firstOrNull {
        operationSystem.contains(it.key, true)
    }?.value?.first ?: DummyApplicationManager

    fun getIconManager(): IconManager = registry.entries.firstOrNull {
        operationSystem.contains(it.key, true)
    }?.value?.second ?: DummyIconManager

    fun getSettingsScreen(): AbstractSettingsScreen = registry.entries.firstOrNull {
        operationSystem.contains(it.key, true)
    }?.value?.third ?: DummySettingsScreen()

    fun isSettingsDummy() = getSettingsScreen() is DummySettingsScreen

    fun isDummy(): Boolean = getApplicationManager() is DummyApplicationManager || getIconManager() is DummyIconManager
}