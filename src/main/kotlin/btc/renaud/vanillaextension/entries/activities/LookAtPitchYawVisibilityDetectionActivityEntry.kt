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
import com.typewritermc.engine.paper.entry.entity.*
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.entity.entries.activity.LookAtPitchYawActivity
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "look_at_pitch_yaw_visibility_detection_activity",
    "Look at specific pitch/yaw while detecting players",
    Colors.BLUE,
    "mdi:compass-outline"
)
class LookAtPitchYawVisibilityDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    
    val yaw: Var<Float> = ConstVar(0f),
    val pitch: Var<Float> = ConstVar(0f),
    
    @Help("The radius of vision")
    val visionRadius: Var<Double> = ConstVar(10.0),
    
    @Help("Field of View in degrees (up to 360)")
    val fov: Var<Double> = ConstVar(90.0),
    
    @Help("The shape of the vision detection")
    val shape: Var<VisionShape> = ConstVar(VisionShape.CONE),

    @Help("Show debug display (Client-side optimized)")
    val showDisplay: Var<Boolean> = ConstVar(false),
    
    val criteria: List<Criteria> = emptyList()
) : GenericEntityActivityEntry {

    override fun create(context: ActivityContext, currentLocation: PositionProperty): EntityActivity<ActivityContext> {
        // LookAtPitchYawActivity constructor: (startLocation, yaw, pitch, childActivity)
        val lookActivity = LookAtPitchYawActivity(currentLocation, yaw, pitch, IdleActivity(currentLocation))
        
        val vision = VisibilityDetectionActivity(
            radius = visionRadius,
            fov = fov,
            shape = shape,
            showDisplay = showDisplay,
            criteria = criteria,
            startLocation = currentLocation
        )
        return LookAtPitchYawVisibilityDetectionActivity(lookActivity, vision)
    }
}

class LookAtPitchYawVisibilityDetectionActivity(
    val lookActivity: LookAtPitchYawActivity,
    val vision: VisibilityDetectionActivity
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty
        get() = lookActivity.currentPosition
        set(_) {}

    override val currentProperties: List<EntityProperty>
        get() = lookActivity.currentProperties + vision.currentProperties

    override fun initialize(context: ActivityContext) {
        lookActivity.initialize(context)
        vision.initialize(context)
    }

    override fun tick(context: ActivityContext): TickResult {
        val lookResult = lookActivity.tick(context)
        
        vision.currentPosition = lookActivity.currentPosition
        val visionResult = vision.tick(context)

        return if (lookResult == TickResult.CONSUMED || visionResult == TickResult.CONSUMED) {
            TickResult.CONSUMED
        } else {
            TickResult.IGNORED
        }
    }

    override fun dispose(context: ActivityContext) {
        lookActivity.dispose(context)
        vision.dispose(context)
    }
}
