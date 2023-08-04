//package net.minecraft.entity.ai.goal
//
//import net.minecraft.entity.LivingEntity
//import net.minecraft.entity.ai.TargetPredicate
//import net.minecraft.entity.attribute.EntityAttributes
//import net.minecraft.entity.mob.MobEntity
//
///**
// * A goal that maintains the target of a mob entity. If the goal stops,
// * such as because the target is not valid, the target is removed from
// * the owner mob.
// *
// *
// * Compared to other goals, this goal and its subclasses are added
// * to the [target][MobEntity.targetSelector] than the regular
// * goal selector, and should use the [Goal.Control.TARGET]
// * control if it sets the owner's target.
// */
//abstract class HuntPlayersGoal @JvmOverloads constructor(
//    protected val mob: MobEntity,
//    protected val checkVisibility: Boolean,
//    private val checkCanNavigate: Boolean = false
//) :
//    Goal() {
//    private var canNavigateFlag = 0
//    private var checkCanNavigateCooldown = 0
//    private var timeWithoutVisibility = 0
//    protected var target: LivingEntity? = null
//    protected var maxTimeWithoutVisibility = 60
//    override fun shouldContinue(): Boolean {
//        var livingEntity = mob.target
//        if (livingEntity == null) {
//            livingEntity = target
//        }
//        return if (livingEntity == null) {
//            false
//        } else if (!mob.canTarget(livingEntity)) {
//            false
//        } else {
//            val abstractTeam = mob.scoreboardTeam
//            val abstractTeam2 = livingEntity.scoreboardTeam
//            if (abstractTeam != null && abstractTeam2 === abstractTeam) {
//                false
//            } else {
//                val d = followRange
//                if (mob.squaredDistanceTo(livingEntity) > d * d) {
//                    false
//                } else {
//                    if (checkVisibility) {
//                        if (mob.visibilityCache.canSee(livingEntity)) {
//                            timeWithoutVisibility = 0
//                        } else if (++timeWithoutVisibility > toGoalTicks(maxTimeWithoutVisibility)) {
//                            return false
//                        }
//                    }
//                    mob.target = livingEntity
//                    true
//                }
//            }
//        }
//    }
//
//    protected open val followRange: Double
//        protected get() = mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE)
//
//    override fun start() {
//        canNavigateFlag = 0
//        checkCanNavigateCooldown = 0
//        timeWithoutVisibility = 0
//    }
//
//    override fun stop() {
//        mob.target = null
//        target = null
//    }
//
//    protected fun canTrack(target: LivingEntity?, targetPredicate: TargetPredicate): Boolean {
//        return if (target == null) {
//            false
//        } else if (!targetPredicate.test(mob, target)) {
//            false
//        } else if (!mob.isInWalkTargetRange(target.blockPos)) {
//            false
//        } else {
//            if (checkCanNavigate) {
//                if (--checkCanNavigateCooldown <= 0) {
//                    canNavigateFlag = 0
//                }
//                if (canNavigateFlag == 0) {
//                    canNavigateFlag = if (canNavigateToEntity(target)) 1 else 2
//                }
//                if (canNavigateFlag == 2) {
//                    return false
//                }
//            }
//            true
//        }
//    }
//
//    private fun canNavigateToEntity(entity: LivingEntity): Boolean {
//        checkCanNavigateCooldown = toGoalTicks(10 + mob.random.nextInt(5))
//        val path = mob.navigation.findPathTo(entity, 0)
//        return if (path == null) {
//            false
//        } else {
//            val pathNode = path.end
//            if (pathNode == null) {
//                false
//            } else {
//                val i = pathNode.x - entity.blockX
//                val j = pathNode.z - entity.blockZ
//                (i * i + j * j).toDouble() <= 2.25
//            }
//        }
//    }
//
//    fun setMaxTimeWithoutVisibility(time: Int): TrackTargetGoal {
//        maxTimeWithoutVisibility = time
//        return this
//    }
//
//    companion object {
//        private const val UNSET = 0
//        private const val CAN_TRACK = 1
//        private const val CANNOT_TRACK = 2
//    }
//}
