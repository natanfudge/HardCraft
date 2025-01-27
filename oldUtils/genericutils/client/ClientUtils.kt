package io.github.natanfudge.genericutils.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.TooltipSupplier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.item.ItemStack
import net.minecraft.text.Text



fun getClient(): MinecraftClient = MinecraftClient.getInstance()

