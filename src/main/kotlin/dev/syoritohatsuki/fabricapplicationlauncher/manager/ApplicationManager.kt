package dev.syoritohatsuki.fabricapplicationlauncher.manager

import dev.syoritohatsuki.fabricapplicationlauncher.dto.Application

interface ApplicationManager {
    fun getAppsCount(): Int
    fun fetchApps()
    fun getApps(): List<Application>
}