package io.github.natanfudge.hardcraft.gametest

import io.github.natanfudge.hardcraft.utils.minus
import io.github.natanfudge.hardcraft.utils.plusDoubles
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.block.Blocks
import net.minecraft.block.Blocks.AIR
import net.minecraft.block.Blocks.GLASS
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class HardCraftGameTest : FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun zombieClimbBridgeBreak(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.ZOMBIE)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun skeletonClimbBridgeBreak(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.SKELETON)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun spiderClimbBridgeBreak(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.SPIDER)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun creeperClimbBridgeBreak(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.CREEPER)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun escapeBox(context: TestContext) {
        gameTest(context) {
            val villagerPos = BlockPos(5, 5, 5)
            addCagedEntity(BlockPos(5, 2, 5), EntityType.ZOMBIE)
            addCagedEntity(BlockPos(5, 5, 5), EntityType.VILLAGER)

            wait(15.seconds)

            // Villager should be dead
            dontExpectEntityAt(EntityType.VILLAGER, villagerPos)
        }
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun digDown(context: TestContext) = gameTest(context) {
        val zombiePos = Vec3d(5.5, 5.0, 5.5)
        val villagerPos = BlockPos(5, 2, 5)
        addCagedEntity(villagerPos, EntityType.VILLAGER)
        EntityType.ZOMBIE.spawn(zombiePos)

        wait(15.seconds)

        // Villager should be dead
        dontExpectEntityAt(EntityType.VILLAGER, villagerPos)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun noWaterResistance(context: TestContext) = waterResistanceTest(context, resistanceEnabled = false)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun yesWaterResistance(context: TestContext) = waterResistanceTest(context, resistanceEnabled = true)
}

private fun waterResistanceTest(context: TestContext, resistanceEnabled: Boolean) = gameTest(context) {
    val villagerPos = BlockPos(2, 2, 1)
    addCagedEntity(villagerPos, EntityType.VILLAGER)
    val zombie = EntityType.ZOMBIE.spawn(Vec3d(6.0, 2.0, 5.0))
    zombie.hardcraft_setIsPushedByFluids(!resistanceEnabled)
    villagerPos.up(3).place(Blocks.WATER)
    villagerPos.up(3).south() .place(Blocks.WATER)
    villagerPos.up(3).east().place(Blocks.WATER)
    villagerPos.up(3).south().east().place(Blocks.WATER)

    wait(15.seconds)
    if (resistanceEnabled) {
        // Zombie has resistance - can kill villager
        dontExpectEntityAt(EntityType.VILLAGER, villagerPos)
    } else {
        // No resistance - villager is safe
        expectEntityAt(EntityType.VILLAGER, villagerPos)
    }
}


fun climbBridgeBreak(context: TestContext, enemyType: EntityType<out HostileEntity>, time: Duration = 15.seconds) = gameTest(context) {
    buildGlassCage(BlockPos(4, 4, 0))

    val villagerPos = BlockPos(5, 5, 1)
    addCagedEntity(villagerPos, EntityType.VILLAGER)

    enemyType.spawn(Vec3d(2.0, 2.0, 5.0))

    wait(time)

    // Villager should be dead
    dontExpectEntityAt(EntityType.VILLAGER, villagerPos)
}

context(ctx: TestContext)
private fun addCagedEntity(pos: BlockPos, entity: EntityType<*>) {
    buildGlassCage(pos.minus(x = 1, y = 1, z = 1))
    entity.spawn(pos.plusDoubles(x = 0.5, z = 0.5))
}

context(ctx: TestContext)
fun buildGlassCage(pos: BlockPos) = ctx.build(pos) {
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