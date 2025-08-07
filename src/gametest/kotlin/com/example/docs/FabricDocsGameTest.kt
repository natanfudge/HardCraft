package com.example.docs

import io.github.natanfudge.hardcraft.utils.setBlock
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.block.Blocks
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import net.minecraft.util.math.BlockPos

//TODO: do our tests for al lteh stuff
class FabricDocsGameTest : FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun test(context: TestContext) {
        context.expectBlock(Blocks.AIR, 1, 0, 0)
        context.world.setBlock(context.getAbsolutePos(BlockPos(1,1,1)), Blocks.ANDESITE)
        context.complete()
    }
}