package io.github.natanfudge.genericutils.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.TooltipSupplier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

inline fun <reified T : Screen> whenCurrentScreenIs(action: T.(ClientWorld?) -> Unit) {
    val client = getClient()
    val screen = client.currentScreen
    if (screen is T) {
        action(screen, client.world)
    }
}

fun getClient(): MinecraftClient = MinecraftClient.getInstance()

/**
 * Utilities for creating a [TooltipSupplier] for screen [Widget]s
 */
object Tooltips {
    fun ofItemStack(stack: ItemStack, screen: Screen): TooltipSupplier = TooltipSupplier { _, poseStack, x, y ->
        screen.renderTooltip(poseStack, stack, x, y)
    }

    fun ofText(text: Text, screen: Screen) = TooltipSupplier { _, matrices, mouseX, mouseY: Int ->
        screen.renderTooltip(matrices, text, mouseX, mouseY)
    }
}

object Buttons {
    fun create(text: Text,x: Int, y: Int, width: Int, height: Int, onClick: (ButtonWidget) -> Unit): ButtonWidget {
        return ButtonWidget(x,y,width,height,text,onClick)
    }
}


/**
 * Performs push before [usage] and pop after.
 */
inline fun MatrixStack.use(usage: MatrixStack.() -> Unit) {
    push()
    usage()
    pop()
}