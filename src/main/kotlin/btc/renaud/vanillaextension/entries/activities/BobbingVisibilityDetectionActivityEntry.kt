package btc.renaud.vanillaextension.entries.activities

import btc.renaud.vanillaextension.entries.VisibilityDetectionEntry
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
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
import com.typewritermc.engine.paper.entry.entity.IdleActivity
import com.typewritermc.entity.entries.activity.BobbingActivity
import java.time.Duration
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "bobbing_visibility_detection_activity",
    "Bob up and down while detecting players",
    Colors.BLUE,
    "mdi:arrow-up-down-bold"
)
class BobbingVisibilityDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    
    @Help("The speed of the bobbing motion in cycles per second")
    val speed: Var<Float> = ConstVar(1.0f),
    @Help("The amplitude of the bobbing in blocks")
    val amplitude: Var<Float> = ConstVar(0.5f),
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
        val bobbingActivity = BobbingActivity(speed, amplitude, baseActivity)
        
        val vision = VisibilityDetectionActivity(
            radius = visionRadius,
            fov = fov,
            shape = shape,
            showDisplay = showDisplay,
            criteria = criteria,
            triggers = triggers,
            startLocation = currentLocation
        )
        return BobbingVisibilityDetectionActivity(bobbingActivity, vision)
    }
}

class BobbingVisibilityDetectionActivity(
    val bobbingActivity: BobbingActivity,
    val vision: VisibilityDetectionActivity
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty
        get() = bobbingActivity.currentPosition
        set(_) {}

    override val currentProperties: List<EntityProperty>
        get() = bobbingActivity.currentProperties + vision.currentProperties

    override fun initialize(context: ActivityContext) {
        bobbingActivity.initialize(context)
        vision.initialize(context)
    }

    override fun tick(context: ActivityContext): TickResult {
        val bobResult = bobbingActivity.tick(context)
        
        vision.currentPosition = bobbingActivity.currentPosition
        val visionResult = vision.tick(context)

        return if (bobResult == TickResult.CONSUMED || visionResult == TickResult.CONSUMED) {
            TickResult.CONSUMED
        } else {
            TickResult.IGNORED
        }
    }

    override fun dispose(context: ActivityContext) {
        bobbingActivity.dispose(context)
        vision.dispose(context)
    }
}
