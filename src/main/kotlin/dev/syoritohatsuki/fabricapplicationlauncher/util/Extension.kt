package dev.syoritohatsuki.fabricapplicationlauncher.util

fun execute(vararg command: String): Process = Runtime.getRuntime().exec(
    arrayOf(
        "bash",
        "-c",
        *command
    )
)