package io.github.natanfudge.hardcraft.gametest

import io.github.natanfudge.genericutils.place
import io.github.natanfudge.hardcraft.ai.makeSmart
import io.github.natanfudge.hardcraft.block.EnclosureBlock
import io.github.natanfudge.hardcraft.mixinhandler.MobHooks
import io.github.natanfudge.hardcraft.utils.plus
import io.github.natanfudge.hardcraft.utils.plusY
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks.AIR
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.SkeletonEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.test.TestContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

fun gameTest(context: TestContext, code: suspend TestContext.() -> Unit) {
    MobHooks.hateVillagers = true

    val server = context.world.server
    val dispatcher = ServerDispatcher(server) // MinecraftServer implements Executor via #execute

    // Scope tied to this single test; no GlobalScope
    val scope = CoroutineScope(dispatcher)

    scope.launch {
        try {
            context.buildEnclosure()
            context.code()
            context.complete()
        } catch (e: Throwable) {
            context.test.fail(e)
        }
    }
}

suspend fun TestContext.wait(ticks: Long) = suspendCoroutine { cont ->
    waitAndRun(ticks) {
        cont.resume(Unit)
    }
}

class ServerDispatcher(private val executor: Executor) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        executor.execute(block)
    }
}


const val TPS = 20

suspend fun TestContext.wait(time: Duration) = wait(time.inWholeMilliseconds / 1000 * TPS)


context(ctx: TestContext)
fun <T : Entity> EntityType<T>.spawn(pos: Vec3d, smartEnemy: Boolean = true): T {
    val entity = create(ctx.world) ?: error("Could not create entity $this")
    entity.setPosition(ctx.getAbsolute(pos))
    ctx.world.spawnEntity(entity)
    if (entity is SkeletonEntity) {
        val bow = ItemStack(Items.BOW).apply {
            addEnchantment(Enchantments.POWER, 50)  // Give them a lot of damage so they won't take forever to kill their target in the test
        }
        // Skeletons are supposed to have bows
        entity.equipStack(EquipmentSlot.MAINHAND, bow)
    } else if (entity is VillagerEntity) {
        // Don't move. Just let it happen
        entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.0;
    } else if (entity is HostileEntity) {
        entity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)?.baseValue = 40.0; // Increase enemy damage to make tests go faster
    }

    if (smartEnemy && entity is HostileEntity) {
        entity.makeSmart()
        entity.hardcraft_setDemolition(100) // Destroy blocks fast
    }

    return entity
}

fun TestContext.build(origin: BlockPos, builder: BuildContext.() -> Unit) {
    BuildContext(origin, this).builder()
}

fun TestContext.buildEnclosure(origin: BlockPos = BlockPos(-1, 1, -1), size: Int = 10) {
    build(origin) {
        // Floor
        layer {
            repeat(size) {
                row(List(size) { EnclosureBlock })
            }
        }
        // Walls
        repeat(size - 2) { h ->
            layer {
                repeat(size) { r ->
                    if (r == 0 || r == size - 1) {
                        // Edges
                        row(List(size) { EnclosureBlock })
                    } else {
                        row(
                            buildList<Block> {
                                // Wall
                                add(EnclosureBlock)
                                // Empty space (inside)
                                addAll(List(size - 2) { AIR })
                                // Wall
                                add(EnclosureBlock)
                            }
                        )
                    }
                }
            }
        }
        // Ceiling
        layer {
            repeat(size) {
                row(List(size) { EnclosureBlock })
            }
        }
    }
}

@DslMarker
annotation class BuildDSL

@BuildDSL
class BuildContext(val origin: BlockPos, val ctx: TestContext) {
    private var yShift = 0

    fun layer(content: LayerContext. () -> Unit) {
        LayerContext(origin.plusY(yShift), ctx).content()
        yShift++
    }
}

@BuildDSL
class LayerContext(val origin: BlockPos, val ctx: TestContext) {
    private var zShift = 0
    fun row(vararg blocks: Block): List<BlockPos> = row(blocks.toList())
    fun row(blocks: List<Block>): List<BlockPos> = stateRow(blocks.map { it.defaultState })
    fun row(vararg blocks: BlockState): List<BlockPos> = stateRow(blocks.toList())
    fun stateRow(blocks: List<BlockState>): List<BlockPos> = with(ctx) {
        val positions = mutableListOf<BlockPos>()
        var xShift = 0
        for (block in blocks) {
            val pos = origin.plus(x = xShift, z = zShift)
            pos.place(block)
            positions.add(pos)
            xShift++
        }
        zShift++
        positions
    }
}


context(ctx: TestContext)
fun BlockPos.place(block: BlockState) {
    with(ctx.world) {
        ctx.getAbsolutePos(this@place).place(block)
    }
}

context(ctx: TestContext)
fun BlockPos.place(block: Block) {
    with(ctx.world) {
        ctx.getAbsolutePos(this@place).place(block)
    }
}
