package dev.syoritohatsuki.fabricapplicationlauncher.manager.linux

import dev.syoritohatsuki.fabricapplicationlauncher.FabricApplicationLauncherClientMod
import dev.syoritohatsuki.fabricapplicationlauncher.manager.IconManager
import dev.syoritohatsuki.fabricapplicationlauncher.util.HOME
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.math.abs

object LinuxIconManager : IconManager {
    private val loadedIcons: MutableMap<String, Identifier> = mutableMapOf()

    private val ICON_DIRECTORIES: Array<String> = arrayOf(
        "/usr/share/icons/", "$HOME/.local/share/icons/", "$HOME/.icons/"
    )

    private const val PREFERRED_ICON_SIZE: Int = 64

    override fun getIconPath(icon: String): Path {
        ICON_DIRECTORIES.forEach { baseDir ->
            val themeDir = Paths.get(baseDir)
            if (Files.exists(themeDir) && Files.isDirectory(themeDir)) {
                val iconPath: String? = searchInTheme(themeDir, icon)
                if (iconPath != null) {
                    return Path.of(iconPath)
                }
            }
        }

        return Path.of("TODO Fallback icon")
    }

    private fun searchInTheme(themeDir: Path, iconName: String): String? {
        try {
            val sizeToPathMap: MutableMap<Int, Path> = TreeMap()

            Files.walk(themeDir).filter { path: Path -> Files.isDirectory(path) }
                .filter { path: Path -> path.fileName.toString().matches("\\d+x\\d+".toRegex()) }
                .forEach { path: Path ->
                    try {
                        sizeToPathMap[path.fileName.toString().split("x".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0].toInt()] = path
                    } catch (ignored: NumberFormatException) {
                    }
                }

            arrayOf(".jpg", ".jpeg", ".png", ".svg").forEach { ext ->
                val iconFile = (findClosestSize(sizeToPathMap) ?: return null).resolve("apps")
                    .resolve(iconName + ext)
                if (Files.exists(iconFile)) {
                    return iconFile.toString()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun findClosestSize(sizeToPathMap: Map<Int, Path>): Path? =
        sizeToPathMap.entries.minByOrNull { abs(it.key - PREFERRED_ICON_SIZE) }?.value

    private fun svgToPngInputStream(inputStream: InputStream): InputStream {

        val svgContent = inputStream.bufferedReader().use { it.readText() }
        val fixedSvgContent = svgContent.replace("""version="1"""", """version="1.1"""")

        val transcoderInput = TranscoderInput(ByteArrayInputStream(fixedSvgContent.toByteArray(Charsets.UTF_8)))

        val outputStream = ByteArrayOutputStream()
        val transcoderOutput = TranscoderOutput(outputStream)

        val pngTranscoder = PNGTranscoder()
        pngTranscoder.transcode(transcoderInput, transcoderOutput)

        return ByteArrayInputStream(outputStream.toByteArray())
    }

    override fun getIconIdentifier(icon: String): Identifier = loadedIcons[icon] ?: loadIcon(icon)

    private fun loadIcon(icon: String): Identifier {
        val path = getIconPath(icon)
        val id = Identifier.of(FabricApplicationLauncherClientMod.MOD_ID, icon.lowercase())

        loadedIcons[icon] = id

        if (icon.isNotBlank() && Files.isRegularFile(
                path, *arrayOfNulls<LinkOption>(0)
            )
        ) {
            Files.newInputStream(path).use { inputStream ->
                try {
                    val image: NativeImage? = if (path.toString().endsWith(".svg")) {
                        FabricApplicationLauncherClientMod.logger.error("Icon is SVG")
                        NativeImage.read(svgToPngInputStream(inputStream))
                    } else {
                        FabricApplicationLauncherClientMod.logger.error("Icon is not SVG")
                        NativeImage.read(inputStream)
                    }
                    FabricApplicationLauncherClientMod.logger.error("${icon}: [${image?.width}x${image?.height}]")
                    MinecraftClient.getInstance().textureManager.registerTexture(id, NativeImageBackedTexture(image))
                    return id
                } catch (e: Exception) {
                    FabricApplicationLauncherClientMod.logger.error(e.message + ": $path")
                }
            }
        }
        return id
    }
}