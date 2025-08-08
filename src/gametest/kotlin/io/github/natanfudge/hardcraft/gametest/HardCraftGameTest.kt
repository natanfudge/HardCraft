package io.github.natanfudge.hardcraft.gametest

import io.github.natanfudge.hardcraft.mixinhandler.floorToBlockPos
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.block.Blocks.AIR
import net.minecraft.block.Blocks.GLASS
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.SkeletonEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

//TODO: do our tests for al lteh stuff
class HardCraftGameTest : FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE)
    fun zombieClimbBridgeBreak(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.ZOMBIE)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE)
    fun skeletonClimbBridgeBreak(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.SKELETON)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE)
    fun spiderClimbBridgeBreak(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.SPIDER)

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = Int.MAX_VALUE)
    fun creeperClimbBridgeBreak(context: TestContext) = climbBridgeBreak(context, enemyType = EntityType.CREEPER)

}

fun climbBridgeBreak(context: TestContext, enemyType: EntityType<out HostileEntity>, time: Duration = 35.seconds) = gameTest(context) {
    buildGlassCage(BlockPos(4, 4, 0))

    val villagerPos = Vec3d(5.5, 5.0, 1.5)

    val villager = EntityType.VILLAGER.spawn(villagerPos)
    // Don't move. Just let it happen
    villager.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.0;
    val enemy = enemyType.spawn(Vec3d(2.0, 2.0, 5.0))
    if(enemy is SkeletonEntity) {
        enemy.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.BOW))
    }

    wait(time)

    // Villager should be dead
    dontExpectEntityAt(EntityType.VILLAGER, villagerPos.floorToBlockPos())
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