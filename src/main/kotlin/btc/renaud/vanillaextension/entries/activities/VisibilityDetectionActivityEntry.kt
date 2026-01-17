package btc.renaud.vanillaextension.entries.activities

import btc.renaud.vanillaextension.entries.VisibilityDetectionEntry
import btc.renaud.vanillaextension.entries.VisibilityDetectionEvent
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.entity.ActivityContext
import com.typewritermc.engine.paper.entry.entries.EntityActivityEntry
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.entity.EntityActivity
import com.typewritermc.engine.paper.entry.entity.TickResult
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.matches
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.bukkit.Particle

import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.triggerEntriesFor

@Entry(
    "visibility_detection_activity",
    "Detect players inside an NPC's field of view",
    Colors.GREEN,
    "mdi:eye"
)
class VisibilityDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    
    @Help("The radius of vision")
    val radius: Var<Double> = ConstVar(10.0),
    
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
        return VisibilityDetectionActivity(
            radius = radius,
            fov = fov,
            shape = shape,
            showDisplay = showDisplay,
            criteria = criteria,
            triggers = triggers,
            startLocation = currentLocation
        )
    }
}



class VisibilityDetectionActivity(
    private val radius: Var<Double>,
    private val fov: Var<Double>,
    private val shape: Var<VisionShape>,
    private val showDisplay: Var<Boolean>,
    private val criteria: List<Criteria>,
    private val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    private val startLocation: PositionProperty
) : EntityActivity<ActivityContext> {

    private val scheduler: com.typewritermc.engine.paper.scheduler.SchedulerAdapter by lazy { org.koin.java.KoinJavaComponent.get(com.typewritermc.engine.paper.scheduler.SchedulerAdapter::class.java) }

    var currentPos: PositionProperty = startLocation
    var isSeeingPlayer: Boolean = false
    private val seenPlayers = mutableSetOf<Player>()
    private var ticks = 0

    override var currentPosition: PositionProperty
        get() = currentPos
        set(value) { currentPos = value }

    override val currentProperties: List<EntityProperty>
        get() = emptyList() // Or return vision properties if needed?

    override fun initialize(context: ActivityContext) {
        // Initialization logic
    }

    override fun tick(context: ActivityContext): TickResult {

        // Use a viewer from context for Var.get() calls
        val viewer = context.randomViewer
        
        val applyR = viewer?.let { radius.get(it) } ?: 10.0
        val f = viewer?.let { fov.get(it) } ?: 90.0
        val s = viewer?.let { shape.get(it) } ?: VisionShape.CONE
        val show = viewer?.let { showDisplay.get(it) } ?: false

        val npcLoc = org.bukkit.Location(
            Bukkit.getWorld(java.util.UUID.fromString(currentPos.world.identifier)),
            currentPos.x,
            currentPos.y,
            currentPos.z,
            currentPos.yaw,
            currentPos.pitch
        )
        val eyeLoc = npcLoc.clone().add(0.0, 1.62, 0.0) // Approximate eye height
        val applyRSquared = applyR * applyR
        val nearby = context.viewers.filter { 
            it.location.world == npcLoc.world && it.location.distanceSquared(npcLoc) <= applyRSquared 
        }
        
        val currentlySeen = mutableSetOf<Player>()
        
        nearby.forEach { player ->
            if (hasLineOfSight(eyeLoc, player.eyeLocation, applyR, f, s) && criteria.matches(player)) {
                currentlySeen.add(player)
                if (seenPlayers.add(player)) {
                    // Trigger Actions
                    triggers.triggerEntriesFor(player) { }
                    
                    // Trigger Event: VisibilityDetectionEvent
                    scheduler.runGlobal(plugin, Runnable {
                        Bukkit.getPluginManager().callEvent(VisibilityDetectionEvent(
                            context.instanceRef,
                            player
                        ))
                    })
                }
            }
        }
        
        seenPlayers.removeIf { it !in currentlySeen }
        isSeeingPlayer = currentlySeen.isNotEmpty()

        ticks++
        if (ticks % 10 == 0) {
            val observers = context.viewers.filter { showDisplay.get(it) }
            if (observers.isNotEmpty()) {
                scheduler.runGlobal(plugin, Runnable {
                   displayVision(observers, eyeLoc, applyR, f, s)
                })
            }
        }
        
        return TickResult.IGNORED // We don't consume the tick, allowing other activities (movement) to run if composed? 
        // Or if this is the ONLY activity, we should maybe return CONSUMED/IGNORED based on logic. 
        // Usually, a passive detector returns IGNORED so it runs in parallel?
        // But EntityActivity usually runs exclusively if it's the main activity.
        // PatrolVision combines them.
        // If this is standalone, it's just idle but detecting.
    }

    private fun hasLineOfSight(origin: org.bukkit.Location, target: org.bukkit.Location, radius: Double, fov: Double, shape: VisionShape): Boolean {
        val direction = target.toVector().subtract(origin.toVector())
        if (direction.length() > radius) return false
        
        if (shape == VisionShape.CONE && fov < 360) {
            val angle = Math.toDegrees(origin.direction.angle(direction).toDouble())
            if (angle > fov / 2) return false
        }
        
        // Skip rayTrace if not on main thread to avoid async errors
        if (!Bukkit.isPrimaryThread()) return true
        
        val result = origin.world.rayTraceBlocks(origin, direction, radius)
        // Check if blocked
        if (result != null && result.hitBlock != null) return false
        
        return true
    }

    override fun dispose(context: ActivityContext) {
        // Cleanup displays
    }
    
    private fun displayVision(observers: List<Player>, origin: org.bukkit.Location, radius: Double, fovDegrees: Double, shape: VisionShape) {
        val particle = Particle.FLAME
        
        if (shape == VisionShape.SPHERE) {
            // Draw a circle at the radius
            val particles = 36
            for (i in 0 until particles) {
                val angle = 2 * Math.PI * i / particles
                val x = Math.cos(angle) * radius
                val z = Math.sin(angle) * radius
                val point = origin.clone().add(x, 0.0, z)
                observers.forEach { it.spawnParticle(particle, point, 1, 0.0, 0.0, 0.0, 0.0) }
            }
        } else {
            // Cone
            val dir = origin.direction.clone().normalize()
            
            // Draw arc
            val halfFovRad = Math.toRadians(fovDegrees / 2)
            val segments = 20
            
            for (i in 0..segments) {
                val fraction = i.toDouble() / segments
                val angle = -halfFovRad + (fraction * (2 * halfFovRad))
                val arcDir = rotateAroundY(dir.clone(), angle)
                val arcPoint = origin.clone().add(arcDir.multiply(radius))
                observers.forEach { it.spawnParticle(particle, arcPoint, 1, 0.0, 0.0, 0.0, 0.0) }
                
                // Draw lines to edges
                if (i == 0 || i == segments) {
                    val steps = 10
                    for (j in 1..steps) {
                        val linePoint = origin.clone().add(arcDir.clone().normalize().multiply(radius * j / steps))
                        observers.forEach { it.spawnParticle(particle, linePoint, 1, 0.0, 0.0, 0.0, 0.0) }
                    }
                }
            }
        }
    }
    
    private fun rotateAroundY(vector: Vector, angle: Double): Vector {
        val cos = Math.cos(angle)
        val sin = Math.sin(angle)
        val x = vector.x * cos - vector.z * sin
        val z = vector.x * sin + vector.z * cos
        return vector.setX(x).setZ(z)
    }
}
