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
    fun noWaterResistance(context: TestContext) = resistanceTest(context, resistanceEnabled = false, water = true)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun yesWaterResistance(context: TestContext) = resistanceTest(context, resistanceEnabled = true, water = true)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun noLavaResistance(context: TestContext) = resistanceTest(context, resistanceEnabled = false, water = false)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun yesLavaResistance(context: TestContext) = resistanceTest(context, resistanceEnabled = true, water = false)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun noPistonResistance(context: TestContext) = pistonTest(context, resistanceEnabled = false)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun yesPistonResistance(context: TestContext) = pistonTest(context, resistanceEnabled = true)
}

private fun pistonTest(context: TestContext, resistanceEnabled: Boolean) = gameTest(context) {
    val villagerPos = BlockPos(2, 2, 1)
    addCagedEntity(villagerPos, EntityType.VILLAGER)
    val zombie = EntityType.ZOMBIE.spawn(Vec3d(6.0, 2.0, 5.0))
    zombie.hardcraft_setIsPistonImmune(resistanceEnabled)
    // TODO: proper automatic piston setup for test
}

private fun resistanceTest(context: TestContext, resistanceEnabled: Boolean, water: Boolean) = gameTest(context) {
    val villagerPos = BlockPos(2, 2, 1)
    addCagedEntity(villagerPos, EntityType.VILLAGER)


    val fluid = if (water) Blocks.WATER else Blocks.LAVA
    villagerPos.up(3).place(fluid)
    villagerPos.up(3).south().place(fluid)
    villagerPos.up(3).east().place(fluid)
    villagerPos.up(3).south().east().place(fluid)

    if (!water) {
        // Lava is slow
        wait(7.seconds)
    }

    val zombie = EntityType.ZOMBIE.spawn(Vec3d(6.0, 2.0, 5.0))
    if (water) {
        zombie.hardcraft_setIsPushedByFluids(!resistanceEnabled)
    } else {
        // For lava tests, give push resistance for the non-resistant case as well, so we know the fire damage is what stops the mob and not the push of fluid
        zombie.hardcraft_setIsPushedByFluids(false)
        zombie.hardcraft_setIsFireImmune(resistanceEnabled)
    }


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