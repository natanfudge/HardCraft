package com.example.docs

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.block.Blocks
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext

class FabricDocsGameTest : FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun test(context: TestContext) {
        context.expectBlock(Blocks.AIR, 0, 0, 0)
        context.complete()
    }
}