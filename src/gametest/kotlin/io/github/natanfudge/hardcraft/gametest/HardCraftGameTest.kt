package io.github.natanfudge.hardcraft.gametest

import io.github.natanfudge.hardcraft.utils.facing
import io.github.natanfudge.hardcraft.utils.minus
import io.github.natanfudge.hardcraft.utils.plusDoubles
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.block.Blocks.*
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class HardCraftGameTest : FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun zombieClimbBridgeBreak(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.ZOMBIE)
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun dumbZombie(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.ZOMBIE, smartEnemy = false)

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

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun noLavaResistance(context: TestContext) = lavaResistanceTest(context, resistanceEnabled = false)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun yesLavaResistance(context: TestContext) = lavaResistanceTest(context, resistanceEnabled = true)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun noPistonResistance(context: TestContext) = pistonTest(context, resistanceEnabled = false)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE, requiredSuccesses = 3)
    fun yesPistonResistance(context: TestContext) = pistonTest(context, resistanceEnabled = true)
}

private fun pistonTest(context: TestContext, resistanceEnabled: Boolean) = gameTest(context) {
    val villagerPos = BlockPos(3, 2, 1)
    addCagedEntity(villagerPos, EntityType.VILLAGER)

    var torchPosition: BlockPos? = null
    var observerPosition: BlockPos? = null
    build(BlockPos(0, 2, 3)) {
        layer {
            val facingPiston = PISTON.defaultState.facing(Direction.SOUTH)
            row(AIR, REDSTONE_WIRE, REDSTONE_WIRE, REDSTONE_WIRE)
            row(REDSTONE_WIRE, REDSTONE_WIRE, REPEATER, REPEATER)
            torchPosition = row(REDSTONE_WIRE.defaultState, REDSTONE_TORCH.defaultState, facingPiston, facingPiston)[1]
            observerPosition = row(REDSTONE_WIRE.defaultState, OBSERVER.defaultState.facing(Direction.EAST))[1]
        }
    }

    wait(1)
    // Destroy torch to start loop
    torchPosition!!.place(AIR)

    val zombie = EntityType.ZOMBIE.spawn(Vec3d(3.0, 2.0, 6.5))
    zombie.hardcraft_setIsPistonImmune(resistanceEnabled)

    wait(15.seconds)
    // Stop infinite loop after test is done
    observerPosition!!.place(AIR)
    if (resistanceEnabled) {
        // Zombie has resistance - can kill villager
        dontExpectEntityAt(EntityType.VILLAGER, villagerPos)
    } else {
        // No resistance - villager is safe
        expectEntityAt(EntityType.VILLAGER, villagerPos)
    }
}

private fun waterResistanceTest(context: TestContext, resistanceEnabled: Boolean) = gameTest(context) {
    val villagerPos = BlockPos(2, 2, 1)
    addCagedEntity(villagerPos, EntityType.VILLAGER)


    villagerPos.up(3).place(WATER)
    villagerPos.up(3).south().place(WATER)
    villagerPos.up(3).east().place(WATER)
    villagerPos.up(3).south().east().place(WATER)


    val zombie = EntityType.ZOMBIE.spawn(Vec3d(6.0, 2.0, 5.0))
    zombie.hardcraft_setIsPushedByFluids(!resistanceEnabled)

    wait(15.seconds)
    if (resistanceEnabled) {
        // Zombie has resistance - can kill villager
        dontExpectEntityAt(EntityType.VILLAGER, villagerPos)
    } else {
        // No resistance - villager is safe
        expectEntityAt(EntityType.VILLAGER, villagerPos)
    }
}

private fun lavaResistanceTest(context: TestContext, resistanceEnabled: Boolean) = gameTest(context) {
    val villagerPos = BlockPos(2, 2, 1)
    addCagedEntity(villagerPos, EntityType.VILLAGER)


    villagerPos.south().south().south().place(LAVA)
    villagerPos.south().south().south().east().place(LAVA)
    villagerPos.south().south().south().east().east().place(LAVA)


    val zombie = EntityType.ZOMBIE.spawn(Vec3d(6.0, 2.0, 5.0))

    // For lava tests, give push resistance for the non-resistant case as well, so we know the fire damage is what stops the mob and not the push of fluid
    zombie.hardcraft_setIsPushedByFluids(false)
    zombie.hardcraft_setIsFireImmune(resistanceEnabled)


    wait(15.seconds)
    if (resistanceEnabled) {
        // Zombie has resistance - can kill villager
        dontExpectEntityAt(EntityType.VILLAGER, villagerPos)
    } else {
        // No resistance - villager is safe
        expectEntityAt(EntityType.VILLAGER, villagerPos)
    }
}


fun climbBridgeBreak(context: TestContext, enemyType: EntityType<out HostileEntity>, time: Duration = 15.seconds, smartEnemy: Boolean = true) = gameTest(context) {
    buildGlassCage(BlockPos(4, 4, 0))

    val villagerPos = BlockPos(5, 5, 1)
    addCagedEntity(villagerPos, EntityType.VILLAGER)

    enemyType.spawn(Vec3d(2.0, 2.0, 5.0), smartEnemy = smartEnemy)

    wait(time)

    if(smartEnemy) {
        // Villager should be dead
        dontExpectEntityAt(EntityType.VILLAGER, villagerPos)
    } else {
        expectEntityAt(EntityType.VILLAGER, villagerPos)
    }

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