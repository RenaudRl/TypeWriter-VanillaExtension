package btc.renaud.vanillaextension.entries.activities

import btc.renaud.vanillaextension.entries.VisibilityDetectionEntry
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
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
import com.typewritermc.entity.entries.activity.LookCloseActivity
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "look_close_visibility_detection_activity",
    "Look at nearby players while detecting them",
    Colors.BLUE,
    "mdi:eye-plus"
)
class LookCloseVisibilityDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    
    @Help("The range to look at players")
    val lookRange: Var<Double> = ConstVar(5.0),
    @Help("How fast to turn head")
    val turnSpeed: Var<Double> = ConstVar(10.0),
    
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
        // LookCloseActivity only takes startPosition - range/turnSpeed are internal
        val lookClose = LookCloseActivity(currentLocation)
        
        val vision = VisibilityDetectionActivity(
            radius = visionRadius,
            fov = fov,
            shape = shape,
            showDisplay = showDisplay,
            criteria = criteria,
            triggers = triggers,
            startLocation = currentLocation
        )
        return LookCloseVisibilityDetectionActivity(lookClose, vision)
    }
}

class LookCloseVisibilityDetectionActivity(
    val lookClose: LookCloseActivity,
    val vision: VisibilityDetectionActivity
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty
        get() = lookClose.currentPosition
        set(_) {}

    override val currentProperties: List<EntityProperty>
        get() = lookClose.currentProperties + vision.currentProperties

    override fun initialize(context: ActivityContext) {
        lookClose.initialize(context)
        vision.initialize(context)
    }

    override fun tick(context: ActivityContext): TickResult {
        val lookResult = lookClose.tick(context)
        
        vision.currentPosition = lookClose.currentPosition
        val visionResult = vision.tick(context)

        return if (lookResult == TickResult.CONSUMED || visionResult == TickResult.CONSUMED) {
            TickResult.CONSUMED
        } else {
            TickResult.IGNORED
        }
    }

    override fun dispose(context: ActivityContext) {
        lookClose.dispose(context)
        vision.dispose(context)
    }
}
