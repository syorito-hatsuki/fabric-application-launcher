package dev.syoritohatsuki.fabricapplicationlauncher.util

import dev.syoritohatsuki.fabricapplicationlauncher.implementation.AbstractSettingsScreen
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.ApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.IconManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.dummy.DummyApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.dummy.DummyIconManager
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.dummy.DummySettingsScreen

object ManagerRegistry {

    private val registry: MutableMap<String, () -> Triple<ApplicationManager, IconManager, AbstractSettingsScreen>> =
        mutableMapOf()

    val operationSystem: String = System.getProperty("os.name").lowercase()

    fun register(
        name: String,
        applicationManager: () -> ApplicationManager,
        iconManager: () -> IconManager,
        settingsScreen: () -> AbstractSettingsScreen
    ) {
        registry[name.lowercase()] = { Triple(applicationManager(), iconManager(), settingsScreen()) }
    }

    private fun getManagers(): Triple<ApplicationManager, IconManager, AbstractSettingsScreen> =
        registry.entries.firstOrNull { operationSystem.startsWith(it.key) }?.value?.invoke()
            ?: Triple(DummyApplicationManager, DummyIconManager, DummySettingsScreen())

    fun getApplicationManager(): ApplicationManager = getManagers().first
    fun getIconManager(): IconManager = getManagers().second
    fun getSettingsScreen(): AbstractSettingsScreen = getManagers().third

    fun isSettingsDummy() = getSettingsScreen() is DummySettingsScreen
    fun isDummy(): Boolean = getApplicationManager() is DummyApplicationManager || getIconManager() is DummyIconManager
}