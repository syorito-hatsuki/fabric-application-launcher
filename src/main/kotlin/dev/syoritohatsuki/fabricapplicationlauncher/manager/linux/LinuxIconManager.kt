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
import kotlin.io.path.listDirectoryEntries

object LinuxIconManager : IconManager {
    private val loadedIcons: MutableMap<String, Identifier> = mutableMapOf()
    private val loadedNativeImageBackedTexture: MutableMap<String, NativeImageBackedTexture> = mutableMapOf()

    private val ICON_DIRECTORIES: Array<Path> = arrayOf(
        Paths.get("/", "usr", "share", "icons"),
        Paths.get("/", "usr", "share", "pixmaps"),
        Paths.get(HOME, ".local", "share", "icons"),
        Paths.get(HOME, ".icons")
    )

    private const val PREFERRED_ICON_SIZE: Int = 64

    private val ICON_EXTENSIONS = listOf(".png", ".jpg", ".jpeg", ".svg")

    override fun getIconPath(icon: String): Path {
        ICON_DIRECTORIES.forEach { baseDir ->
            if (Files.exists(baseDir) && Files.isDirectory(baseDir)) {
                val iconPath = searchInTheme(baseDir, icon)
                if (iconPath != null) {
                    if (icon.contains("yaak")) {
                        FabricApplicationLauncherClientMod.logger.error("1 Path not null $iconPath")
                    }
                    return iconPath
                }
            }
        }
        return Paths.get("/usr/share/icons/default-icon.png")
    }

    private fun searchInTheme(themeDir: Path, iconName: String): Path? {
        val sizeToPathMap: MutableSet<Pair<Int, Path>> = mutableSetOf()

        try {
            Files.walk(themeDir, 2).use { paths ->
                paths.forEach { path ->
                    if (Files.isDirectory(path)) {
                        val dirName = path.fileName.toString()
                        when {
                            dirName == "scalable" -> sizeToPathMap.add(Pair(Int.MAX_VALUE, path))
                            dirName.matches("\\d+x\\d+".toRegex()) -> {
                                val size = dirName.split("x")[0].toIntOrNull()
                                if (size != null) {
                                    sizeToPathMap.add(Pair(size, path))
                                }
                            }
                        }
                    }
                }
            }

            ICON_EXTENSIONS.forEach { ext ->
                sizeToPathMap.forEach { (_, closestSizePath) ->
                    closestSizePath.listDirectoryEntries().map { closestSizePath.resolve(it).resolve(iconName + ext) }
                        .firstOrNull { Files.exists(it) }?.let { return it }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

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
        val id = Identifier.of(FabricApplicationLauncherClientMod.MOD_ID, icon.lowercase())

        loadedIcons[icon] = id

        MinecraftClient.getInstance().textureManager.registerTexture(id, loadedNativeImageBackedTexture[icon])

        return id
    }

    fun getNative(icon: String): NativeImageBackedTexture? {
        val path = getIconPath(icon)

        if (icon.isNotBlank() && Files.isRegularFile(
                path, *arrayOfNulls<LinkOption>(0)
            )
        ) {
            Files.newInputStream(path).use { inputStream ->
                try {
                    loadedNativeImageBackedTexture[icon] = NativeImageBackedTexture(
                        if (path.toString().endsWith(".svg")) {
                            NativeImage.read(svgToPngInputStream(inputStream))
                        } else {
                            NativeImage.read(inputStream)
                        }
                    )
                } catch (e: Exception) {
                    FabricApplicationLauncherClientMod.logger.error(e.message + ": $path")
                }
            }
        }
        return null
    }
}