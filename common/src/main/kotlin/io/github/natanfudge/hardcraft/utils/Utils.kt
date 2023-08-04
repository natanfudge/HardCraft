package io.github.natanfudge.hardcraft.utils

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

fun <K, V> Map<K, V>.asMutableMap() = if (this is MutableMap<K, V>) this else toMutableMap()
fun Float.roundUp() = ceil(this).toInt()

fun rangeFromSmallToBig(num1: Int, num2: Int) = min(num1, num2)..max(num1, num2)
fun BlockPos.withX(x: Int) = BlockPos(x, y, z)
fun BlockPos.withZ(z: Int) = BlockPos(x, y, z)

//operator fun Vec3d.plus(other: Vec3d)
operator fun Vec3d.plus(other: BlockPos) = Vec3d(this.x + other.x, this.y + other.y, this.z + other.z)
operator fun Vec3d.minus(other: BlockPos) = Vec3d(this.x - other.x, this.y - other.y, this.z - other.z)
operator fun BlockPos.minus(other: Vec3d) = Vec3d(this.x - other.x, this.y - other.y, this.z - other.z)
fun Vec3d.toDirection(): SpatialDirection {
    return SpatialDirection(directionAxis(x), directionAxis(y), directionAxis(z))
}

fun Vec3d.directionTo(pos: BlockPos) = (pos - this).toDirection()


private inline fun directionAxis(num: Double) = when {
    num > 0 -> 1
    // To prevent floating point errors we do 0.0001 instead of 0
    num >= -0.0001 -> 0
    else -> -1
}

// True: positive, False: negative
data class SpatialDirection(val x: Int, val y: Int, val z: Int)

operator fun BlockPos.plus(direction: SpatialDirection): BlockPos {
    return BlockPos(this.x + direction.x, this.y + direction.y, this.z + direction.z)
}

 fun valuesBetween(start: Int, end: Int): IntProgression {
    return if (start <= end) start..end else start downTo end
}

//data class Direction()