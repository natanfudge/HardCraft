package io.github.natanfudge.hardcraft.gametest

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.block.Blocks.AIR
import net.minecraft.block.Blocks.GLASS
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

//TODO: do our tests for al lteh stuff
class HardCraftGameTest : FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun test(context: TestContext) = with(context) {
        buildEnclosure()
        buildGlassCage(BlockPos(4, 4, 0))

        val villager = EntityType.VILLAGER.spawn(Vec3d(5.5, 5.0, 1.5))
        // Don't move. Just let it happen
        villager.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.0;
        EntityType.ZOMBIE.spawn(Vec3d(1.5, 2.0, 5.0))

        complete()
    }
}


fun TestContext.buildGlassCage(pos: BlockPos) = build(pos) {
    layer {
        row(GLASS, GLASS, GLASS)
        row(GLASS, GLASS, GLASS)
        row(GLASS, GLASS, GLASS)
    }
    layer {
        row(GLASS, GLASS, GLASS)
        row(GLASS, AIR, GLASS)
        row(GLASS, GLASS, GLASS)
    }
    layer {
        row(GLASS, GLASS, GLASS)
        row(GLASS, AIR, GLASS)
        row(GLASS, GLASS, GLASS)
    }
    layer {
        row(GLASS, GLASS, GLASS)
        row(GLASS, GLASS, GLASS)
        row(GLASS, GLASS, GLASS)
    }
}