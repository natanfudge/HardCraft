package io.github.natanfudge.genericutils.client

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.OrderedText
import net.minecraft.text.Text

@JvmInline
value class RenderContext(private val matrices: MatrixStack) {
    context(Screen)
    fun drawCenteredTextWithShadow(text: Text, centerX: Int, y: Int, color: McColor) {
        drawCenteredTextWithShadow(text.asOrderedText(),centerX, y, color)
    }
    context(Screen)
    fun drawCenteredTextWithShadow(text: OrderedText, centerX: Int, y: Int, color: McColor) {
        DrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, text, centerX, y, color.value)
    }

    fun drawTextWithShadow(textRenderer: TextRenderer, text: Text, centerX: Int, y: Int, color: McColor) {
        DrawableHelper.drawTextWithShadow(matrices,textRenderer, text,centerX,y,color.value)
    }

    context (Screen)
    fun drawTooltip(text: List<Text>, x: Int, y: Int) {
        renderTooltip(matrices, text, x, y)
    }


    context (Screen)
    fun drawTooltip(text: Text, x: Int, y: Int) {
        renderTooltip(matrices, text, x, y)
    }
}