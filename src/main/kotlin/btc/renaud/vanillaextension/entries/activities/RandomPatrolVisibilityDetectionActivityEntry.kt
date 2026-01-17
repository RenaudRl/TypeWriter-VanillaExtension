package btc.renaud.vanillaextension.entries.activities

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.distanceSqrt
import com.typewritermc.engine.paper.entry.entity.ActivityContext
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.roadnetwork.RoadNetworkEntry
import org.bukkit.Material
import com.typewritermc.entity.entries.activity.NavigationActivity
import com.typewritermc.roadnetwork.RoadNetwork
import com.typewritermc.roadnetwork.gps.PointToPointGPS
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent
import com.typewritermc.roadnetwork.RoadNetworkManager
import com.typewritermc.engine.paper.entry.entity.IdleActivity
import com.typewritermc.engine.paper.entry.entity.EntityActivity
import com.typewritermc.engine.paper.entry.entity.TickResult
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry

@Entry(
    "random_patrol_visibility_detection_activity",
    "Randomly patrol nodes while detecting players",
    Colors.BLUE,
    "mdi:eye-plus"
)
class RandomPatrolVisibilityDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    val roadNetwork: Ref<RoadNetworkEntry> = emptyRef(),
    
    @Help("The maximum distance (in blocks) from the entity's current position to consider nodes for random selection.")
    @Default("100.0")
    val patrolRadius: Double = 100.0,
    
    @Help("Maximum distance in blocks the NPC can see")
    val visionRadius: Var<Double> = ConstVar(5.0),
    
    @Help("Field of view in degrees (max 360)")
    val fov: Var<Double> = ConstVar(90.0),
    
    @Help("Shape of the vision area")
    val shape: Var<VisionShape> = ConstVar(VisionShape.CONE),
    
    @Help("Display debug visualization")
    val showDisplays: Var<Boolean> = ConstVar(true),
    
    @Help("Pause patrolling while a player is visible")
    val stopWhenLooking: Boolean = true,
    
    val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val criteria: List<Criteria> = emptyList()
) : GenericEntityActivityEntry, com.typewritermc.core.entries.Entry {

    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext> {
        val patrol = RandomPatrolActivity(roadNetwork, patrolRadius * patrolRadius, currentLocation)
        val vision = VisibilityDetectionActivity(
            radius = visionRadius,
            fov = fov,
            shape = shape,
            showDisplay = showDisplays,
            criteria = criteria,
            triggers = triggers,
            startLocation = currentLocation
        )
        return RandomPatrolVisibilityDetectionActivity(patrol, vision, stopWhenLooking)
    }
}

class RandomPatrolVisibilityDetectionActivity(
    private val patrol: RandomPatrolActivity,
    private val vision: VisibilityDetectionActivity,
    private val stopWhenLooking: Boolean,
) : EntityActivity<ActivityContext> {
    private var unseenTicks: Int = 0

    override var currentPosition: PositionProperty
        get() = patrol.currentPosition
        set(_) {}

    override val currentProperties: List<EntityProperty>
        get() = if (vision.isSeeingPlayer) {
            val patrolProps = patrol.currentProperties.filterNot { it is PositionProperty }
            patrolProps + vision.currentProperties
        } else {
            val visionProps = vision.currentProperties.filterNot { it is PositionProperty }
            patrol.currentProperties + visionProps
        }

    override fun initialize(context: ActivityContext) {
        patrol.initialize(context)
        vision.initialize(context)
    }

    override fun tick(context: ActivityContext): TickResult {
        val patrolPos = patrol.currentPosition
        vision.currentPosition = if (vision.isSeeingPlayer) {
            patrolPos
        } else {
            patrolPos
        }
        val visionResult = vision.tick(context)

        var patrolResult = TickResult.IGNORED
        if (stopWhenLooking && vision.isSeeingPlayer) {
            patrol.stop(context)
            unseenTicks = 0
        } else {
            val resumeDelayTicks = 10
            if (unseenTicks < resumeDelayTicks) {
                unseenTicks++
                patrol.stop(context)
                patrolResult = TickResult.IGNORED
            } else {
                patrolResult = patrol.tick(context)
            }
        }
        return if (patrolResult == TickResult.CONSUMED || visionResult == TickResult.CONSUMED) {
            TickResult.CONSUMED
        } else {
            TickResult.IGNORED
        }
    }

    override fun dispose(context: ActivityContext) {
        patrol.dispose(context)
        vision.dispose(context)
    }
}

class RandomPatrolActivity(
    private val roadNetwork: Ref<RoadNetworkEntry>,
    private val radiusSquared: Double,
    startLocation: PositionProperty,
) : EntityActivity<ActivityContext>, KoinComponent {
    private var network: RoadNetwork? = null
    private var activity: EntityActivity<in ActivityContext> = IdleActivity(startLocation)

    fun refreshActivity(context: ActivityContext, network: RoadNetwork): TickResult {
        val currentPos = currentPosition.toPosition()
        val nextNode = network.nodes
            .filter { (it.position.distanceSqrt(currentPos) ?: Double.MAX_VALUE) <= radiusSquared }
            .randomOrNull()
            ?: return TickResult.IGNORED

        activity.dispose(context)
        activity = NavigationActivity(
            PointToPointGPS(
                roadNetwork,
                { currentPosition.toPosition() }) {
                nextNode.position
            }, currentPosition
        )
        activity.initialize(context)
        return TickResult.CONSUMED
    }

    override fun initialize(context: ActivityContext) = setup(context)

    private fun setup(context: ActivityContext) {
        network =
            KoinJavaComponent.get<RoadNetworkManager>(RoadNetworkManager::class.java).getNetworkOrNull(roadNetwork)
                ?: return

        refreshActivity(context, network!!)
    }

    override fun tick(context: ActivityContext): TickResult {
        if (network == null) {
            setup(context)
            return TickResult.CONSUMED
        }

        val result = activity.tick(context)
        if (result == TickResult.IGNORED) {
            return refreshActivity(context, network!!)
        }

        return TickResult.CONSUMED
    }

    fun stop(context: ActivityContext) {
        if (activity !is IdleActivity) {
            val oldPosition = currentPosition
            activity.dispose(context)
            activity = IdleActivity(oldPosition)
        }
    }

    override fun dispose(context: ActivityContext) {
        val oldPosition = currentPosition
        activity.dispose(context)
        activity = IdleActivity(oldPosition)
    }

    override val currentPosition: PositionProperty
        get() = activity.currentPosition

    override val currentProperties: List<EntityProperty>
        get() = activity.currentProperties
}
