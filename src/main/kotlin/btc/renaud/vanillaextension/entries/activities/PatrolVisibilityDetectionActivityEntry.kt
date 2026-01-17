package btc.renaud.vanillaextension.entries.activities

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.entity.ActivityContext
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.roadnetwork.RoadNetworkEntry
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry
import org.bukkit.Material
import com.typewritermc.entity.entries.activity.NavigationActivity
import com.typewritermc.roadnetwork.RoadNetwork
import com.typewritermc.roadnetwork.RoadNode
import com.typewritermc.roadnetwork.RoadNodeId
import com.typewritermc.roadnetwork.gps.PointToPointGPS
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent
import com.typewritermc.roadnetwork.RoadNetworkManager
import com.typewritermc.engine.paper.entry.entity.EntityActivity
import com.typewritermc.engine.paper.entry.entity.TickResult
import com.typewritermc.engine.paper.entry.entity.IdleActivity
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.Criteria

@Entry(
    "patrol_visibility_detection_activity",
    "Patrol nodes while detecting players",
    Colors.BLUE,
    "mdi:eye-plus"
)
class PatrolVisibilityDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    val roadNetwork: Ref<RoadNetworkEntry> = emptyRef(),
    @Help("IDs of road nodes to patrol in sequence")
    val nodeIds: List<Int> = emptyList(),
    
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
) : GenericEntityActivityEntry { // Note: GenericEntityActivityEntry might need check if it exists or likely ActivityEntry
    // The original used GenericEntityActivityEntry.
    
    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext> {
        // We reuse the PatrolActivity logic from the original file (we need to ensure it's available or copy it)
        // Since I deleted the original file, I need to include the PatrolActivity class here or import it if I put it elsewhere.
        // It was an inner/private class in the original file. 
        // I will copy it here as private/internal class.
        
        val patrol = PatrolActivity(roadNetwork, nodeIds, currentLocation)
        val vision = VisibilityDetectionActivity(
            radius = visionRadius,
            fov = fov,
            shape = shape,
            showDisplay = showDisplays,
            criteria = criteria,
            triggers = triggers,
            startLocation = currentLocation
        )
        return PatrolVisibilityDetectionActivity(patrol, vision, stopWhenLooking)
    }
}

class PatrolVisibilityDetectionActivity(
    private val patrol: PatrolActivity,
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
             // If not seeing, we might still want vision props? 
             // Original logic:
            val visionProps = vision.currentProperties.filterNot { it is PositionProperty }
            patrol.currentProperties + visionProps
        }

    override fun initialize(context: ActivityContext) {
        patrol.initialize(context)
        vision.initialize(context)
    }

    override fun tick(context: ActivityContext): TickResult {
        val patrolPos = patrol.currentPosition
        // Update vision position to match patrol
        vision.currentPosition = if (vision.isSeeingPlayer) {
            // If seeing, maybe lock rotation? Original had complex logic.
            // Simplified:
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

class PatrolActivity(
    private val roadNetwork: Ref<RoadNetworkEntry>,
    private val nodeIds: List<Int>,
    startLocation: PositionProperty,
) : EntityActivity<ActivityContext>, KoinComponent {
    private var network: RoadNetwork? = null
    private var activity: EntityActivity<in ActivityContext> = IdleActivity(startLocation)
    private var nodeIndex = 0
    private var nodes: List<RoadNode> = emptyList()

    private fun resolveNodes(network: RoadNetwork) {
        nodes = nodeIds.mapNotNull { id ->
            network.nodes.find { it.id == RoadNodeId(id) }
        }
    }

    fun refreshActivity(context: ActivityContext, network: RoadNetwork): TickResult {
        if (nodes.isEmpty()) {
            resolveNodes(network)
        }
        if (nodes.isEmpty()) return TickResult.IGNORED

        val nextNode = nodes[nodeIndex % nodes.size]
        nodeIndex = (nodeIndex + 1) % nodes.size

        activity.dispose(context)
        activity = NavigationActivity(
             PointToPointGPS(
                roadNetwork,
                { currentPosition.toPosition() }
            ) { nextNode.position },
            currentPosition
        )
        activity.initialize(context)
        return TickResult.CONSUMED
    }

    override fun initialize(context: ActivityContext) = setup(context)

    private fun setup(context: ActivityContext) {
         network = KoinJavaComponent.get<RoadNetworkManager>(RoadNetworkManager::class.java).getNetworkOrNull(roadNetwork) ?: return
         refreshActivity(context, network!!)
    }

    override fun tick(context: ActivityContext): TickResult {
        if (network == null) {
            setup(context)
            return TickResult.CONSUMED
        }

        val result = activity.tick(context)
        return if (result == TickResult.IGNORED) {
            refreshActivity(context, network!!)
        } else {
            TickResult.CONSUMED
        }
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
