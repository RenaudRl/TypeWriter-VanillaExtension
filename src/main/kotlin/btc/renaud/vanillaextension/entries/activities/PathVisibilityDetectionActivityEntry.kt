package btc.renaud.vanillaextension.entries.activities

import btc.renaud.vanillaextension.entries.VisibilityDetectionEntry
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entity.ActivityContext
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entity.EntityActivity
import com.typewritermc.engine.paper.entry.entity.TickResult
import com.typewritermc.entity.entries.activity.NavigationActivity
import com.typewritermc.roadnetwork.RoadNetworkEntry
import com.typewritermc.roadnetwork.gps.PointToPointGPS
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "path_visibility_detection_activity",
    "Follow a path while detecting players",
    Colors.BLUE,
    "mdi:vector-polyline"
)
class PathVisibilityDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    
    val network: Ref<RoadNetworkEntry> = emptyRef(),
    val target: Position = Position.ORIGIN,
    @Default("1.5")
    val precision: Double = 1.5,
    @Default("1.0")
    val speed: Double = 1.0,

    @Help("The radius of vision")
    val visionRadius: Var<Double> = ConstVar(10.0),
    
    @Help("Field of View in degrees (up to 360)")
    val fov: Var<Double> = ConstVar(90.0),
    
    @Help("The shape of the vision detection")
    val shape: Var<VisionShape> = ConstVar(VisionShape.CONE),

    @Help("Show debug display (Client-side optimized)")
    val showDisplay: Var<Boolean> = ConstVar(false),
    
    val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val criteria: List<Criteria> = emptyList()
) : GenericEntityActivityEntry {

    override fun create(context: ActivityContext, currentLocation: PositionProperty): EntityActivity<ActivityContext> {
        // We recreate the NavigationActivity manually as PathActivityEntry uses it internally.
        // Assuming we want similar behavior to PathActivityEntry but combined with vision.
        // PathActivityEntry code wasn't read, but NavigationActivityTask/NavigationActivity were seen earlier.
        // We will construct NavigationActivity with GPS.
        
        // NavigationActivity constructor: (gps, startLocation)
        val gps = PointToPointGPS(network, { currentLocation.toPosition() }, { target })
        val navigationActivity = NavigationActivity(gps, currentLocation)

        val vision = VisibilityDetectionActivity(
            radius = visionRadius,
            fov = fov,
            shape = shape,
            showDisplay = showDisplay,
            criteria = criteria,
            triggers = triggers,
            startLocation = currentLocation
        )
        return PathVisibilityDetectionActivity(navigationActivity, vision)
    }
}

class PathVisibilityDetectionActivity(
    val navigationActivity: NavigationActivity,
    val vision: VisibilityDetectionActivity
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty
        get() = navigationActivity.currentPosition
        set(_) {}

    override val currentProperties: List<EntityProperty>
        get() = navigationActivity.currentProperties + vision.currentProperties

    override fun initialize(context: ActivityContext) {
        navigationActivity.initialize(context)
        vision.initialize(context)
    }

    override fun tick(context: ActivityContext): TickResult {
        val navResult = navigationActivity.tick(context)
        
        vision.currentPosition = navigationActivity.currentPosition
        val visionResult = vision.tick(context)

        return if (navResult == TickResult.CONSUMED || visionResult == TickResult.CONSUMED) {
            TickResult.CONSUMED
        } else {
            TickResult.IGNORED
        }
    }

    override fun dispose(context: ActivityContext) {
        navigationActivity.dispose(context)
        vision.dispose(context)
    }
}
