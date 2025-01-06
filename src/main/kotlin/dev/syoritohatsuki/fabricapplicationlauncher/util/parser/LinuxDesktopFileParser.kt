package dev.syoritohatsuki.fabricapplicationlauncher.util.parser

import dev.syoritohatsuki.fabricapplicationlauncher.util.XDG_DATA_DIRS
import dev.syoritohatsuki.fabricapplicationlauncher.util.XDG_DATA_HOME
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readLines

object LinuxDesktopFileParser {

    private val LINUX_APPLICATIONS_LOCATIONS = arrayOf(
        "$XDG_DATA_HOME/applications", *XDG_DATA_DIRS.split(":").map {
            "$it/applications"
        }.toTypedArray()
    )

    fun getApps() {
        LINUX_APPLICATIONS_LOCATIONS.forEach { dirPath ->
            try {
                Path(dirPath).listDirectoryEntries().forEachIndexed { index, filePath ->
                    var name: String? = null
                    var categories: List<String>? = null
                    var icon: String? = null
                    var summary: String? = null
                    filePath.readLines().forEach line@{ line ->
                        if (line.startsWith("Name")) name = line.split("=")[1]
                        if (line.startsWith("Icon")) icon = line.split("=")[1]
                        if (line.startsWith("Categories")) categories = line.split("=")[1].split(";")
                        if (line.startsWith("GenericName")) summary = line.split("=")[1]
                        if (name != null && icon != null && categories != null && summary != null) return@line
                    }
                    println("$index. (${icon}) | $name | $summary | [${categories?.joinToString(", ")}]")
                }
            } catch (ignore: Exception) {
            }
        }
    }
}