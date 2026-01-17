package btc.renaud.vanillaextension.entries.activities

import btc.renaud.vanillaextension.entries.VisibilityDetectionEntry
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.entity.ActivityContext
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntityActivityEntry
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entity.EntityActivity
import com.typewritermc.engine.paper.entry.entity.TickResult
import com.typewritermc.engine.paper.entry.entity.IdleActivity
import com.typewritermc.entity.entries.activity.SpinActivityEntry
import com.typewritermc.entity.entries.activity.SpinActivity
import com.typewritermc.entity.entries.activity.SpinAxis
import java.time.Duration
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.Criteria

@Entry(
    "spin_visibility_detection_activity",
    "Spin around while detecting players",
    Colors.BLUE,
    "mdi:rotate-3d-variant"
)
class SpinVisibilityDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    
    @Help("The duration of one full rotation")
    val duration: Var<Duration> = ConstVar(Duration.ofSeconds(2)),
    @Default("true")
    val clockwise: Boolean = true,
    val axis: SpinAxis = SpinAxis.YAW,
    @Help("The activity that supplies the base position")
    val childActivity: Ref<GenericEntityActivityEntry> = emptyRef(),
    
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
        val baseActivity = childActivity.get()?.create(context, currentLocation) ?: IdleActivity(currentLocation)
        val spinActivity = SpinActivity(duration, clockwise, axis, baseActivity)
        
        val vision = VisibilityDetectionActivity(
            radius = visionRadius,
            fov = fov,
            shape = shape,
            showDisplay = showDisplay,
            criteria = criteria,
            triggers = triggers,
            startLocation = currentLocation
        )
        return SpinVisibilityDetectionActivity(spinActivity, vision)
    }
}

class SpinVisibilityDetectionActivity(
    val spinActivity: SpinActivity,
    val vision: VisibilityDetectionActivity
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty
        get() = spinActivity.currentPosition
        set(_) {}

    override val currentProperties: List<EntityProperty>
        get() = spinActivity.currentProperties + vision.currentProperties

    override fun initialize(context: ActivityContext) {
        spinActivity.initialize(context)
        vision.initialize(context)
    }

    override fun tick(context: ActivityContext): TickResult {
        val spinResult = spinActivity.tick(context)
        
        // Sync vision to the spun position
        vision.currentPosition = spinActivity.currentPosition
        
        val visionResult = vision.tick(context)

        return if (spinResult == TickResult.CONSUMED || visionResult == TickResult.CONSUMED) {
            TickResult.CONSUMED
        } else {
            TickResult.IGNORED
        }
    }

    override fun dispose(context: ActivityContext) {
        spinActivity.dispose(context)
        vision.dispose(context)
    }
}
