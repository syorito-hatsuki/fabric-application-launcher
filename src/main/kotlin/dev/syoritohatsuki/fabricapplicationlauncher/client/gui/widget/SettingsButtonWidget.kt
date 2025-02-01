package dev.syoritohatsuki.fabricapplicationlauncher.client.gui.widget

import dev.syoritohatsuki.fabricapplicationlauncher.FabricApplicationLauncherClientMod
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class SettingsButtonWidget(x: Int, y: Int, action: PressAction) : ButtonWidget(
    x, y, 20, 20, Text.empty(), action, DEFAULT_NARRATION_SUPPLIER
) {
    public override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawGuiTexture(
            { texture: Identifier? -> RenderLayer.getGuiTextured(texture) }, when {
                !this.active -> Icon.SETTINGS_DISABLED
                this.isSelected -> Icon.SETTINGS_HOVER
                else -> Icon.SETTINGS
            }.texture, this.x, this.y, this.width, this.height
        )
    }

    companion object {
        enum class Icon(val texture: Identifier) {
            SETTINGS(
                Identifier.of(
                    FabricApplicationLauncherClientMod.MOD_ID,
                    "widget/settings_button"
                )
            ),
            SETTINGS_HOVER(
                Identifier.of(
                    FabricApplicationLauncherClientMod.MOD_ID, "widget/settings_button_highlighted"
                )
            ),
            SETTINGS_DISABLED(
                Identifier.of(
                    FabricApplicationLauncherClientMod.MOD_ID, "widget/settings_button_disabled"
                )
            ),
        }
    }
}