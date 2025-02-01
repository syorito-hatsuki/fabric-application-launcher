package dev.syoritohatsuki.fabricapplicationlauncher.implementation

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.text.Text

abstract class AbstractSettingsScreen(title: Text) : Screen(title) {
    private var parent: Screen? = null

    var positioningWidget: SimplePositioningWidget = SimplePositioningWidget(0, 0, this.width, this.height)

    override fun init() {
        positioningWidget.forEachChild(::addDrawableChild)

        refreshWidgetPositions()
    }

    override fun refreshWidgetPositions() {
        positioningWidget.refreshPositions()
        SimplePositioningWidget.setPos(this.positioningWidget, this.navigationFocus)
    }

    override fun shouldPause(): Boolean = false

    override fun shouldCloseOnEsc(): Boolean = true

    fun setParent(parent: Screen) {
        this.parent = parent
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderDarkening(context)
    }
}