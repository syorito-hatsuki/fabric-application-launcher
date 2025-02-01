package dev.syoritohatsuki.fabricapplicationlauncher.implementation.dummy

import dev.syoritohatsuki.fabricapplicationlauncher.dto.Application
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.ApplicationManager

object DummyApplicationManager : ApplicationManager {
    override fun getAppsCount(): Int = 0

    override fun fetchApps() {
    }

    override fun getApps(): List<Application> = listOf()

    override fun executeApplication(executable: Application) {
    }
}