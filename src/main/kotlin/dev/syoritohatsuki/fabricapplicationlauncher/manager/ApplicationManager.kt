package dev.syoritohatsuki.fabricapplicationlauncher.manager

import dev.syoritohatsuki.fabricapplicationlauncher.dto.Application

interface ApplicationManager {
    fun fetchApps()
    fun getApps(): List<Application>
}