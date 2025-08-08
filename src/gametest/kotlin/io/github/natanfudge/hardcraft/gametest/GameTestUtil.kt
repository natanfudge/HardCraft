package io.github.natanfudge.hardcraft.gametest

import io.github.natanfudge.genericutils.place
import io.github.natanfudge.hardcraft.block.EnclosureBlock
import io.github.natanfudge.hardcraft.utils.plus
import io.github.natanfudge.hardcraft.utils.plusY
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.Blocks.AIR
import net.minecraft.block.Blocks.GLASS
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.test.TestContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

context(ctx: TestContext)
fun <T: Entity> EntityType<T>.spawn(pos: Vec3d): T {
    val entity = create(ctx.world) ?: error("Could not create entity $this")
    entity.setPosition(ctx.getAbsolute(pos))
    ctx.world.spawnEntity(entity)
    return entity
}

fun TestContext.build(origin: BlockPos, builder: BuildContext.() -> Unit) {
    BuildContext(origin, this).builder()
}

fun TestContext.buildEnclosure(origin: BlockPos = BlockPos(0,1,0), size: Int = 8) {
    build(origin) {
        // Floor
        layer {
            repeat(size) {
                row(List(size){ EnclosureBlock })
            }
        }
        // Walls
        repeat(size - 2) { h ->
            layer {
                repeat(size) { r ->
                    if(r == 0 || r == size - 1) {
                        // Edges
                        row(List(size){ EnclosureBlock })
                    } else {
                        row(
                            buildList {
                                // Wall
                                add(EnclosureBlock)
                                // Empty space (inside)
                                addAll(List(size - 2){ AIR })
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
                row(List(size){ EnclosureBlock })
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
    fun row(vararg blocks: Block) = row(blocks.toList())
    fun row( blocks: List<Block>) = with(ctx) {
        var xShift = 0
        for (block in blocks) {
            origin.plus(x = xShift, z = zShift).place(block)
            xShift++
        }
        zShift++
    }
}


context(ctx: TestContext)
fun BlockPos.place(block: Block) {
    with(ctx.world) {
        ctx.getAbsolutePos(this@place).place(block)
    }
}
