package dev.syoritohatsuki.fabricapplicationlauncher.manager.linux

import dev.syoritohatsuki.fabricapplicationlauncher.FabricApplicationLauncherClientMod
import dev.syoritohatsuki.fabricapplicationlauncher.dto.Application
import dev.syoritohatsuki.fabricapplicationlauncher.manager.ApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.util.HOME
import dev.syoritohatsuki.fabricapplicationlauncher.util.XDG_DATA_DIRS
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readLines

object LinuxApplicationManager : ApplicationManager {

    private val LINUX_APPLICATIONS_LOCATIONS = arrayOf(
        "$HOME/.local/share/applications", *XDG_DATA_DIRS.split(":").map {
            "$it/applications"
        }.toTypedArray()
    )

    private val apps: MutableMap<String, Application> = mutableMapOf()

    override fun fetchApps() {
        LINUX_APPLICATIONS_LOCATIONS.forEach { location ->
            try {
                Path(location).listDirectoryEntries().forEach dirs@{ filePath ->
                    val application = Application("")
                    filePath.readLines().forEach line@{ line ->
                        if (line.startsWith("Name=")) {
                            application.name.ifBlank { application.name = line.split("=")[1] }
                        }

                        if (line.startsWith("Icon=")) application.icon.ifBlank {
                            application.icon = line.split("=")[1]
                        }

                        if (line.startsWith("Categories=")) application.categories.ifEmpty {
                            application.categories = line.split("=")[1].split(";")
                        }

                        if (line.startsWith("GenericName=")) application.description.ifBlank {
                            application.description = line.split("=")[1]
                        }

                        if (line.startsWith("Exec=")) application.executable.ifBlank {
                            application.executable = line.split("=")[1]
                        }
                        if (line == "NoDisplay=true") return@dirs
                    }
                    if (application.name.isNotBlank()) {
                        application.path = filePath.toString()
                        apps[application.name] = application
                    }
                }
            } catch (e: Exception) {
                FabricApplicationLauncherClientMod.logger.warn("Failed to fetch applications: ${e.message}")
            }
        }
    }

    override fun getApps(): List<Application> = apps.values.toList()

    override fun getAppsCount(): Int = apps.count()
}