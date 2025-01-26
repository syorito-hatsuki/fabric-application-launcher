package dev.syoritohatsuki.fabricapplicationlauncher.client.gui.screen.ingame

import dev.syoritohatsuki.fabricapplicationlauncher.FabricApplicationLauncherClientMod
import dev.syoritohatsuki.fabricapplicationlauncher.manager.linux.LinuxApplicationManager
import dev.syoritohatsuki.fabricapplicationlauncher.manager.linux.LinuxIconManager
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.DirectionalLayoutWidget
import net.minecraft.client.gui.widget.LockButtonWidget
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
    private var settingsButton: LockButtonWidget? = null
    private lateinit var applicationListWidget: ApplicationListWidget

    override fun init() {
        val column = positioningWidget.add(DirectionalLayoutWidget.vertical().spacing(8))
        column.mainPositioner.alignHorizontalCenter()

        val row = column.add(DirectionalLayoutWidget.horizontal().spacing(8))
        searchBox = row?.add(
            TextFieldWidget(textRenderer, 190, 20, Text.literal("Search..."))
        )

        settingsButton = row?.add(
            LockButtonWidget(20, 20) {
                // TODO Open settings
            }.apply {
                isLocked = true
            }
        )

        applicationListWidget = column.add(
            ApplicationListWidget(
                client = client ?: return,
                applicationManager = LinuxApplicationManager,
                iconManager = LinuxIconManager,
                width = if (width > 220) width else 220,
                height = height / 2,
                y = 48,
                itemHeight = 36,
                this
            )
        )

        positioningWidget.forEachChild(::addDrawableChild)

        searchBox?.setChangedListener(applicationListWidget::setSearch)

        this.refreshWidgetPositions()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (settingsButton?.isHovered == true) {
            setTooltip(Text.literal("Settings (WIP)"))
        }
        if (debugMode) {
            context.drawTooltip(
                textRenderer, listOf(
                    Text.literal("Apps count: ${LinuxApplicationManager.getApps().count()}"),
                    Text.literal("Unique icons: ${LinuxIconManager.getUniqueIconsCount()}")
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
                    if (app.icon.isNotBlank()) add(
                        Text.literal("Icon path: ").formatted(Formatting.GREEN).append(
                            Text.literal(LinuxIconManager.iconPaths[app.icon]).formatted(Formatting.YELLOW)
                        )
                    )
                }, -PADDING, textRenderer.fontHeight * 6
            )
        }
    }

    override fun refreshWidgetPositions() {
        positioningWidget.refreshPositions()
        SimplePositioningWidget.setPos(this.positioningWidget, this.navigationFocus)
        applicationListWidget.setDimensions(if (width > 220) width else 220, height / 2)
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