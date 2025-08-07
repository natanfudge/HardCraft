package io.github.natanfudge.hardcraft.utils

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun <K, V> Map<K, V>.asMutableMap() = if (this is MutableMap<K, V>) this else toMutableMap()
fun Float.roundUp() = ceil(this).toInt()

fun rangeFromSmallToBig(num1: Int, num2: Int) = min(num1, num2)..max(num1, num2)
fun BlockPos.withX(x: Int) = BlockPos(x, y, z)
fun BlockPos.withZ(z: Int) = BlockPos(x, y, z)

//operator fun Vec3d.plus(other: Vec3d)
operator fun Vec3d.plus(other: BlockPos) = Vec3d(this.x + other.x, this.y + other.y, this.z + other.z)
operator fun Vec3d.minus(other: BlockPos) = Vec3d(this.x - other.x, this.y - other.y, this.z - other.z)
operator fun Vec3d.minus(other: Vec3d) = Vec3d(this.x - other.x, this.y - other.y, this.z - other.z)
 fun Vec3d.minusY(y: Double) = Vec3d(this.x, this.y - y, this.z)
 fun BlockPos.minusY(y: Int) = BlockPos(this.x, this.y - y, this.z)
operator fun BlockPos.minus(other: Vec3d) = Vec3d(this.x - other.x, this.y - other.y, this.z - other.z)
fun Vec3d.toDirection(): DoubleDirection {
    return DoubleDirection(x, y, z)
}

//fun Vec3d.directionTo(pos: BlockPos): IntDirection = (pos - this)
fun Vec3d.directionTo(pos: Vec3d): DoubleDirection = (pos - this).normalize()

fun BlockState.canSupportOtherBlocks(world: WorldAccess, pos: BlockPos) = getCollisionShape(world, pos) != VoxelShapes.empty()
fun World.canSupportOtherBlocks(pos: BlockPos) = getBlockState(pos).canSupportOtherBlocks(this, pos)

fun WorldAccess.getBlock(pos: BlockPos): Block {
    val bs = getBlockState(pos)
    return bs.block
}

fun WorldAccess.setBlock(pos: BlockPos, block: Block): Boolean {
    return setBlockState(pos, block.defaultState, Block.NOTIFY_ALL)
}

fun Double.ceilToInt() = ceil(this).toInt()
fun Double.floorToInt() = floor(this).toInt()


private inline fun directionAxis(num: Double) = when {
    num > 0 -> 1
    // To prevent floating point errors we do 0.0001 instead of 0
    num >= -0.0001 -> 0
    else -> -1
}

// True: positive, False: negative
//data class IntDirection(val x: Int, val y: Int, val z: Int) {
//    operator fun plus(pos: BlockPos) = BlockPos(pos.x + x, pos.y + y, pos.z + z)
//    fun withoutY() = XZDirection(x,z)
//}

typealias IntDirection = BlockPos
typealias DoubleDirection = Vec3d

fun DoubleDirection.withoutY(): DoubleXZDirection {
    val norm = sqrt(x * x + z * z)
    return DoubleXZDirection(x / norm, z / norm)
}


//typealias DoubleXZDirection = Vec2f

//data class DoubleDirection(val x: Double, val y: Double, val z: Double)

data class DoubleXZDirection(val x: Double, val z: Double) {
    operator fun plus(pos: Vec3d) = Vec3d(pos.x + x, pos.y, pos.z + z)
}

operator fun BlockPos.plus(direction: IntDirection): BlockPos {
    return BlockPos(this.x + direction.x, this.y + direction.y, this.z + direction.z)
}

fun valuesBetween(start: Int, end: Int): IntProgression {
    return if (start <= end) start..end else start downTo end
}

inline fun <T, R> cartesianProduct(
    list1: Iterable<T>,
    list2: Iterable<T>,
    list3: Iterable<T>,
    map: (T, T, T) -> R,
): List<R> {
    return buildList {
        for (item1 in list1) {
            for (item2 in list2) {
                for (item3 in list3) {
                    add(map(item1, item2, item3))
                }
            }
        }
    }
}

inline fun Double.squared() = this * this


inline fun <T> aggregate(root: T, children: (T) -> List<T>): List<T> {
    val aggregated = mutableListOf<T>()
    var index = 0
    aggregated.addAll(children(root))
    while (index < aggregated.size) {
        aggregated.addAll(children(aggregated[index]))
        index++
    }
    return aggregated
}