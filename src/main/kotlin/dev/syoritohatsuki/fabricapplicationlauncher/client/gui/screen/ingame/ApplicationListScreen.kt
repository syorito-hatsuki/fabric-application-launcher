package dev.syoritohatsuki.fabricapplicationlauncher.client.gui.screen.ingame

import dev.syoritohatsuki.fabricapplicationlauncher.FabricApplicationLauncherClientMod
import dev.syoritohatsuki.fabricapplicationlauncher.client.gui.widget.SettingsButtonWidget
import dev.syoritohatsuki.fabricapplicationlauncher.implementation.linux.LinuxIconManager
import dev.syoritohatsuki.fabricapplicationlauncher.util.ManagerRegistry
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.DirectionalLayoutWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ApplicationListScreen : Screen(Text.literal("Applications")) {

    companion object {
        const val PADDING = 4
    }

    private var debugMode = false
    private var positioningWidget: SimplePositioningWidget = SimplePositioningWidget(0, 0, this.width, this.height)

    var searchBox: TextFieldWidget? = null
    private var settingsButton: SettingsButtonWidget? = null
    private lateinit var applicationListWidget: ApplicationListWidget

    override fun init() {
        val column = positioningWidget.add(DirectionalLayoutWidget.vertical().spacing(8))
        column.mainPositioner.alignHorizontalCenter()

        val row = column.add(DirectionalLayoutWidget.horizontal().spacing(8))
        searchBox = row.add(
            TextFieldWidget(textRenderer, 190, 20, Text.literal("Search..."))
        )

        settingsButton = row.add(
            SettingsButtonWidget(20, 20) {
                client?.setScreen(ManagerRegistry.getSettingsScreen().apply {
                    setParent(this@ApplicationListScreen)
                })
            }.apply {
                active = !ManagerRegistry.isSettingsDummy()
            }
        )

        applicationListWidget = column.add(
            ApplicationListWidget(
                client = client ?: return,
                applicationManager = ManagerRegistry.getApplicationManager(),
                iconManager = ManagerRegistry.getIconManager(),
                width = if (width > 220) width else 220,
                height = height / 2,
                y = 48,
                itemHeight = 36,
                this
            )
        )

        positioningWidget.forEachChild(::addDrawableChild)

        searchBox?.setChangedListener(applicationListWidget::setSearch)

        refreshWidgetPositions()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (settingsButton?.isHovered == true) {
            setTooltip(Text.literal("Settings (WIP)"))
        }
        if (debugMode) {
            context.drawTooltip(
                textRenderer, listOf(
                    Text.literal("Apps count: ${ManagerRegistry.getApplicationManager().getApps().count()}"),
                    Text.literal("Unique icons: ${ManagerRegistry.getIconManager().getUniqueIconsCount()}")
                ), -PADDING, PADDING - 1 + textRenderer.fontHeight * 2
            )

            val app = (applicationListWidget.selectedOrNull ?: return).application
            context.drawTooltip(
                textRenderer, mutableListOf<Text>().apply {
                    if (app.name.isNotBlank()) add(
                        Text.literal("Name: ").formatted(Formatting.GREEN)
                            .append(Text.literal(app.name).formatted(Formatting.YELLOW))
                    )
                    if (app.description.isNotBlank()) add(
                        Text.literal("Description: ").formatted(Formatting.GREEN)
                            .append(Text.literal(app.description).formatted(Formatting.YELLOW))
                    )
                    if (app.categories.isNotEmpty()) add(
                        Text.literal("Categories: ").formatted(Formatting.GREEN)
                            .append(Text.literal(app.categories.toString()).formatted(Formatting.YELLOW))
                    )
                    if (app.path.isNotBlank()) add(
                        Text.literal("Path: ").formatted(Formatting.GREEN)
                            .append(Text.literal(app.path).formatted(Formatting.YELLOW))
                    )
                    if (app.executable.isNotBlank()) add(
                        Text.literal("Exec: ").formatted(Formatting.GREEN)
                            .append(Text.literal(app.executable).formatted(Formatting.YELLOW))
                    )
                    try {
                        if (app.icon.isNotBlank() && !LinuxIconManager.iconPaths[app.icon].isNullOrBlank()) add(
                            Text.literal("Icon path: ").formatted(Formatting.GREEN).append(
                                Text.literal(LinuxIconManager.iconPaths[app.icon] ?: "Impossible path!")
                                    .formatted(Formatting.YELLOW)
                            )
                        )
                    } catch (ignore: Exception) {
                    }
                }, -PADDING, textRenderer.fontHeight * 6
            )
        }
    }

    override fun refreshWidgetPositions() {
        applicationListWidget.height = height / 2
        positioningWidget.refreshPositions()
        SimplePositioningWidget.setPos(this.positioningWidget, this.navigationFocus)
    }

    override fun shouldPause(): Boolean = false

    override fun shouldCloseOnEsc(): Boolean = true

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (searchBox?.isSelected == false) {
            if (FabricApplicationLauncherClientMod.openApplicationListKeyBinding.matchesKey(keyCode, -1)) {
                close()
                return true
            }
            if (FabricApplicationLauncherClientMod.enableDebugApplicationListKeyBinding.matchesKey(keyCode, -1)) {
                debugMode = !debugMode
                return true
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderDarkening(context)
    }
}