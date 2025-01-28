package dev.syoritohatsuki.fabricapplicationlauncher.manager.linux

import dev.syoritohatsuki.fabricapplicationlauncher.FabricApplicationLauncherClientMod
import dev.syoritohatsuki.fabricapplicationlauncher.manager.IconManager
import dev.syoritohatsuki.fabricapplicationlauncher.util.HOME
import dev.syoritohatsuki.fabricapplicationlauncher.util.XDG_DATA_DIRS
import dev.syoritohatsuki.fabricapplicationlauncher.util.execute
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
import java.io.*
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.zip.GZIPInputStream
import javax.imageio.ImageIO

object LinuxIconManager : IconManager {
    private val loadedIcons: MutableMap<String, Identifier> = mutableMapOf()
    private val loadedNativeImageBackedTexture: MutableMap<String, NativeImageBackedTexture> = mutableMapOf()
    val iconPaths: MutableMap<String, String> = mutableMapOf()

    enum class FORMATS {
        SVG, SVGZ, PNG
    }

    val RESOLUTIONS = execute(
        "find /usr/share/icons/ -type d -regextype posix-extended -regex '.*/[0-9]+x[0-9]+$' | awk -F/ '{print \$NF}' | sort -u"
    ).inputStream.bufferedReader().readLines().asSequence().filter { it.matches(Regex("\\d+x\\d+")) }
        .map { it to it.split("x")[1].toInt() }.sortedByDescending { it.second }.map { it.first }.toMutableList()
        .apply {
            addFirst("scalable")
            add("")
        }

    private val ICON_DIRECTORIES: Array<Path> = arrayOf(
        *XDG_DATA_DIRS.split(":").map(Paths::get).toTypedArray(), Paths.get(HOME, ".local", "share")
    )

    private fun createEmptyPng(): InputStream = ByteArrayInputStream(ByteArrayOutputStream().apply {
        ImageIO.write(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", this)
    }.toByteArray())

    private fun svgToPngInputStream(inputStream: InputStream): InputStream {
        try {

            val bufferedInputStream = BufferedInputStream(inputStream)

            val decompressedInputStream = when {
                isGzipStream(bufferedInputStream) -> GZIPInputStream(bufferedInputStream)
                else -> bufferedInputStream
            }

            val svgDocument: SVGDocument =
                SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName()).createSVGDocument(
                    null, decompressedInputStream.bufferedReader().use {
                        it.readText()
                    }.replace("""version="1"""", """version="1.1"""").replace(
                        Regex("""<stop((?!offset=)[^>])*?>"""), "<stop offset=\"0%\"$1>"
                    ).byteInputStream()
                )

            val svgRoot = svgDocument.documentElement

            val currentWidth = svgRoot.getAttribute("width").toIntOrNull()
            val currentHeight = svgRoot.getAttribute("height").toIntOrNull()

            if (currentWidth == null || currentWidth < 64) svgRoot.setAttribute("width", "64")
            if (currentHeight == null || currentHeight < 64) svgRoot.setAttribute("height", "64")

            if (svgRoot.getAttribute("viewBox").isNullOrBlank()) {
                svgRoot.setAttribute("viewBox", "0 0 ${currentWidth ?: 64} ${currentHeight ?: 64}")
            }

            val outputStream = ByteArrayOutputStream()

            PNGTranscoder().transcode(TranscoderInput(svgDocument), TranscoderOutput(outputStream))

            return ByteArrayInputStream(outputStream.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun loadIcon(icon: String): Identifier =
        Identifier.of(FabricApplicationLauncherClientMod.MOD_ID, icon.lowercase()).apply {
            loadedIcons[icon] = this
            MinecraftClient.getInstance().textureManager.registerTexture(this, loadedNativeImageBackedTexture[icon])
        }

    override fun preload(icon: String) {
        val path = getIconPath(icon) ?: run {
            loadedNativeImageBackedTexture[icon] = NativeImageBackedTexture(NativeImage.read(createEmptyPng()))
            return
        }

        if (icon.isNotBlank() && Files.isRegularFile(path, *arrayOfNulls<LinkOption>(0))) {
            Files.newInputStream(path).use { inputStream ->
                try {
                    loadedNativeImageBackedTexture[icon] = NativeImageBackedTexture(
                        NativeImage.read(
                            when {
                                path.toString().endsWith(".${FORMATS.SVG.name}", true) -> svgToPngInputStream(
                                    inputStream
                                )

                                path.toString().endsWith(".${FORMATS.SVGZ.name}", true) -> svgToPngInputStream(
                                    inputStream
                                )
                                else -> inputStream
                            }
                        )
                    )
                    iconPaths[icon] = path.toString()
                } catch (e: Exception) {
                    FabricApplicationLauncherClientMod.logger.error(e.message + ": $path")
                }
            }
        }
    }

    private fun getIconPath(icon: String, theme: String = ""): Path? {

        FORMATS.entries.forEach {
            if (icon.endsWith(it.name, true)) return Path.of(icon)
        }

        ICON_DIRECTORIES.forEach { basePath ->
            return searchIcon(icon, basePath.resolve("icons").resolve(theme)) ?: return@forEach
        }

        return ICON_DIRECTORIES.map { it.resolve("icons/hicolor") }.map { searchIcon(icon, it) }
            .firstOrNull(Objects::nonNull) ?: searchIcon(icon, Paths.get("/", "usr", "share", "pixmaps")) ?: run {

            ICON_DIRECTORIES.forEach { basePath ->
                return searchIcon(icon, basePath.resolve("icons"))
            }

            return null
        }
    }

    private fun searchIcon(iconName: String, themePath: Path): Path? {
        RESOLUTIONS.forEach { resolution ->
            val resolutionPath = themePath.resolve(resolution)
            if (!Files.isDirectory(resolutionPath)) return@forEach

            try {
                Files.walk(resolutionPath).use { files ->
                    return files.filter(Files::isRegularFile).filter { path ->
                        /* TODO Add SVG priority */
                        path.fileName.toString().startsWith("$iconName.") && FORMATS.entries.any {
                            path.fileName.toString().endsWith(it.name, true)
                        }
                    }.findFirst().orElse(null) ?: return@forEach
                }
            } catch (e: IOException) {
                System.err.println("Error searching in: " + resolutionPath + " - " + e.message)
            }
        }
        return null
    }

    private fun isGzipStream(inputStream: InputStream): Boolean {
        inputStream.mark(2)
        val magicNumber = ByteArray(2)
        val bytesRead = inputStream.read(magicNumber)
        inputStream.reset()

        return bytesRead == 2 && magicNumber[0] == 0x1F.toByte() && magicNumber[1] == 0x8B.toByte() // GZIP magic number
    }

    override fun getIconIdentifier(icon: String): Identifier = loadedIcons[icon] ?: loadIcon(icon)

    override fun getUniqueIconsCount(): Int = loadedNativeImageBackedTexture.count {
        (it.value.image?.width ?: 0) > 1 && (it.value.image?.height ?: 0) > 1
    }
}