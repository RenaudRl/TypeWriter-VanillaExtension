package btc.renaud.vanillaextension.entries.activities

import btc.renaud.vanillaextension.entries.VisibilityDetectionEntry
import btc.renaud.vanillaextension.entries.VisibilityDetectionEvent
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
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
import com.typewritermc.entity.entries.activity.RandomLookActivity
import java.time.Duration
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "random_visibility_detection_activity",
    "Randomly look around while detecting players",
    Colors.BLUE,
    "mdi:eye-refresh"
)
class RandomVisibilityDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    
    @Default("{\"start\": -90.0, \"end\": 90.0}")
    val pitchRange: ClosedRange<Float> = -90f..90f,
    @Default("{\"start\": -180.0, \"end\": 180.0}")
    val yawRange: ClosedRange<Float> = -180f..180f,
    @Help("The duration between each look")
    val duration: Var<Duration> = ConstVar(Duration.ofSeconds(1)),
    
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
        val randomLook = RandomLookActivity(pitchRange, yawRange, duration, currentLocation)
        val vision = VisibilityDetectionActivity(
            radius = visionRadius,
            fov = fov,
            shape = shape,
            showDisplay = showDisplay,
            criteria = criteria,
            triggers = triggers,
            startLocation = currentLocation
        )
        return RandomVisibilityDetectionActivity(randomLook, vision)
    }
}

class RandomVisibilityDetectionActivity(
    val randomLook: RandomLookActivity,
    val vision: VisibilityDetectionActivity
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty
        get() = randomLook.currentPosition
        set(_) {}

    override val currentProperties: List<EntityProperty>
        get() = randomLook.currentProperties + vision.currentProperties

    override fun initialize(context: ActivityContext) {
        randomLook.initialize(context)
        vision.initialize(context)
    }

    override fun tick(context: ActivityContext): TickResult {
        val lookResult = randomLook.tick(context)
        
        // Sync vision position with the new look direction
        vision.currentPosition = randomLook.currentPosition
        
        val visionResult = vision.tick(context)

        return if (lookResult == TickResult.CONSUMED || visionResult == TickResult.CONSUMED) {
            TickResult.CONSUMED
        } else {
            TickResult.IGNORED
        }
    }

    override fun dispose(context: ActivityContext) {
        randomLook.dispose(context)
        vision.dispose(context)
    }
}
