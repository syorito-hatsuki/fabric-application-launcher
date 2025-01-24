package dev.syoritohatsuki.fabricapplicationlauncher.manager.linux

import dev.syoritohatsuki.fabricapplicationlauncher.FabricApplicationLauncherClientMod
import dev.syoritohatsuki.fabricapplicationlauncher.manager.IconManager
import dev.syoritohatsuki.fabricapplicationlauncher.util.HOME
import dev.syoritohatsuki.fabricapplicationlauncher.util.XDG_DATA_DIRS
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.util.XMLResourceDescriptor
import org.w3c.dom.svg.SVGDocument
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO

object LinuxIconManager : IconManager {
    private val loadedIcons: MutableMap<String, Identifier> = mutableMapOf()
    private val loadedNativeImageBackedTexture: MutableMap<String, NativeImageBackedTexture> = mutableMapOf()

    private val RESOLUTIONS = Runtime.getRuntime().exec(
        arrayOf(
            "bash",
            "-c",
            "find /usr/share/icons/ -type d -regextype posix-extended -regex '.*/[0-9]+x[0-9]+$' | awk -F/ '{print \$NF}' | sort -u"
        )
    ).inputStream.bufferedReader().readLines().filter { it.matches(Regex("\\d+x\\d+")) }
        .map { it to it.split("x")[1].toInt() }.sortedByDescending { it.second }.map { it.first }.toMutableList()
        .apply {
            add("scalable")
            add("")
        }

    private val FORMATS = arrayOf(".png", ".svg", ".xpm")

    private val ICON_DIRECTORIES: Array<Path> = arrayOf(
        *XDG_DATA_DIRS.split(":").map(Paths::get).toTypedArray(), Paths.get(HOME, ".local", "share")
    )

    private fun createEmptyPng(): InputStream = ByteArrayInputStream(ByteArrayOutputStream().apply {
        ImageIO.write(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", this)
    }.toByteArray())

    private fun svgToPngInputStream(inputStream: InputStream): InputStream {
        try {
            val svgDocument: SVGDocument =
                SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName()).createSVGDocument(
                    null, inputStream.bufferedReader().use {
                        it.readText()
                    }.replace("""version="1"""", """version="1.1"""").byteInputStream()
                )

            val svgRoot = svgDocument.documentElement

            val currentWidth = svgRoot.getAttribute("width").toIntOrNull()
            val currentHeight = svgRoot.getAttribute("height").toIntOrNull()

            if (currentWidth == null || currentWidth < 64) svgRoot.setAttribute("width", "64")
            if (currentHeight == null || currentHeight < 64) svgRoot.setAttribute("height", "64")

            if (svgRoot.getAttribute("viewBox").isNullOrBlank()) {
                svgRoot.setAttribute("viewBox", "0 0 ${currentWidth ?: 16} ${currentHeight ?: 16}")
            }

            val outputStream = ByteArrayOutputStream()

            PNGTranscoder().transcode(TranscoderInput(svgDocument), TranscoderOutput(outputStream))

            return ByteArrayInputStream(outputStream.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun loadIcon(icon: String): Identifier {
        val id = Identifier.of(FabricApplicationLauncherClientMod.MOD_ID, icon.lowercase())
        loadedIcons[icon] = id
        MinecraftClient.getInstance().textureManager.registerTexture(id, loadedNativeImageBackedTexture[icon])
        return id
    }

    fun getNative(icon: String) {
        val path = getIconPath(icon) ?: run {
            loadedNativeImageBackedTexture[icon] = NativeImageBackedTexture(NativeImage.read(createEmptyPng()))
            return
        }

        if (icon.isNotBlank() && Files.isRegularFile(path, *arrayOfNulls<LinkOption>(0))) {
            Files.newInputStream(path).use { inputStream ->
                try {
                    loadedNativeImageBackedTexture[icon] = NativeImageBackedTexture(
                        when {
                            path.toString().endsWith(".svg") -> NativeImage.read(svgToPngInputStream(inputStream))
                            else -> NativeImage.read(inputStream)
                        }
                    )
                } catch (e: Exception) {
                    FabricApplicationLauncherClientMod.logger.error(e.message + ": $path")
                }
            }
        }
    }

    override fun getIconPath(icon: String): Path? {
        var themeName = "Papirus"
        themeName = Objects.requireNonNullElse(themeName, "hicolor")

        ICON_DIRECTORIES.forEach { basePath ->
            val iconPath = searchIcon(basePath.resolve("icons").resolve(themeName), icon)
            if (iconPath != null) return iconPath
        }

        val hicolorPath = ICON_DIRECTORIES.map { it.resolve("icons/hicolor") }.map { searchIcon(it, icon) }
            .firstOrNull(Objects::nonNull)

        if (hicolorPath != null) return hicolorPath

        val pixmapsPath = searchIcon(Paths.get("/", "usr", "share", "pixmaps"), icon)
        if (pixmapsPath != null) return pixmapsPath

        ICON_DIRECTORIES.forEach { basePath ->
            val fallbackPath = searchIcon(basePath.resolve("icons"), icon)
            if (fallbackPath != null) return fallbackPath
        }

        return null
    }

    private fun searchIcon(themePath: Path, iconName: String): Path? {
        RESOLUTIONS.forEach { resolution ->
            val resolutionPath = themePath.resolve(resolution)
            if (!Files.isDirectory(resolutionPath)) return@forEach

            try {
                Files.walk(resolutionPath).use { files ->
                    return files.filter {
                        Files.isRegularFile(it)
                    }.filter {
                        isMatchingIcon(it, iconName)
                    }.findFirst().orElse(null) ?: return@forEach
                }
            } catch (e: IOException) {
                System.err.println("Error searching in: " + resolutionPath + " - " + e.message)
            }
        }
        return null
    }

    private fun isMatchingIcon(filePath: Path, iconName: String): Boolean {
        val fileName = filePath.fileName.toString()
        return fileName.startsWith("$iconName.") && FORMATS.any(fileName::endsWith)
    }

    override fun getIconIdentifier(icon: String): Identifier = loadedIcons[icon] ?: loadIcon(icon)

    override fun getIconsCount() = loadedIcons.size

    override fun getLoadedIconsCount(): Int = loadedNativeImageBackedTexture.count {
        (it.value.image?.width ?: 0) > 1 && (it.value.image?.height ?: 0) > 1
    }
}