package dev.syoritohatsuki.fabricapplicationlauncher.manager.dummy

import dev.syoritohatsuki.fabricapplicationlauncher.dto.Application
import dev.syoritohatsuki.fabricapplicationlauncher.manager.ApplicationManager

object DummyApplicationManager : ApplicationManager {
    override fun getAppsCount(): Int = 0

    override fun fetchApps() {
    }

    override fun getApps(): List<Application> = listOf()
}