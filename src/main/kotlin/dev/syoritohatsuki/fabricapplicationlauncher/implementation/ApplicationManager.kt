package dev.syoritohatsuki.fabricapplicationlauncher.implementation

import dev.syoritohatsuki.fabricapplicationlauncher.dto.Application

interface ApplicationManager {
    fun getAppsCount(): Int
    fun fetchApps()
    fun getApps(): List<Application>
    fun executeApplication(application: Application)
}