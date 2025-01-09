package dev.syoritohatsuki.fabricapplicationlauncher.dto

data class Application(
    var name: String,
    var description: String = "",
    var icon: String = "",
    var categories: List<String> = emptyList(),
    var executable: String = "",
    var path: String = ""
)