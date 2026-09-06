package btcrenaud.vanillaextension.entries.activities

import btcrenaud.vanillaextension.entries.VisibilityDetectionEntry
import btcrenaud.vanillaextension.entries.VisibilityDetectionEvent
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entity.ActivityContext
import com.typewritermc.engine.paper.entry.entity.EntityActivity
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entity.TickResult
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.EntityActivityEntry
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerEntriesFor
import com.typewritermc.engine.paper.plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

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

    private val isFolia: Boolean by lazy {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    private fun runGlobal(task: () -> Unit) {
        if (isFolia) {
            try {
                val globalRegionScheduler = Bukkit::class.java.getMethod("getGlobalRegionScheduler").invoke(null)
                val runMethod = globalRegionScheduler.javaClass.getMethod("execute", org.bukkit.plugin.Plugin::class.java, Runnable::class.java)
                runMethod.invoke(globalRegionScheduler, plugin, Runnable { task() })
            } catch (e: Exception) {
                // Fallback to direct execution
                task()
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, Runnable { task() })
        }
    }

    private var currentPos: PositionProperty = startLocation
    var isSeeingPlayer: Boolean = false
    private val seenPlayers = ConcurrentHashMap.newKeySet<Player>()
    private val ticks = AtomicInteger(0)

    override var currentPosition: PositionProperty
        get() = currentPos
        set(value) { currentPos = value }

    override val currentProperties: List<EntityProperty>
        get() = emptyList()

    override fun activate(context: ActivityContext, position: PositionProperty) {
        currentPos = position
        seenPlayers.clear()
    }

    override fun tick(context: ActivityContext): TickResult {
        val viewer = context.randomViewer

        val applyR = viewer?.let { radius.get(it) } ?: 10.0
        val f = viewer?.let { fov.get(it) } ?: 90.0
        val s = viewer?.let { shape.get(it) } ?: VisionShape.CONE
        val show = viewer?.let { showDisplay.get(it) } ?: false

        val npcLoc = Location(
            Bukkit.getWorld(java.util.UUID.fromString(currentPos.world.identifier)),
            currentPos.x,
            currentPos.y,
            currentPos.z,
            currentPos.yaw,
            currentPos.pitch
        )
        val eyeLoc = npcLoc.clone().add(0.0, 1.62, 0.0)
        val applyRSquared = applyR * applyR
        val nearby = context.viewers.filter {
            it.location.world == npcLoc.world && it.location.distanceSquared(npcLoc) <= applyRSquared
        }

        val currentlySeen = ConcurrentHashMap.newKeySet<Player>()

        nearby.forEach { player ->
            if (hasLineOfSight(eyeLoc, player.eyeLocation, applyR, f, s) && criteria.matches(player)) {
                currentlySeen.add(player)
                if (seenPlayers.add(player)) {
                    triggers.triggerEntriesFor(player) { }

                    runGlobal {
                        Bukkit.getPluginManager().callEvent(VisibilityDetectionEvent(
                            context.instanceRef,
                            player
                        ))
                    }
                }
            }
        }

        seenPlayers.removeIf { it !in currentlySeen }
        isSeeingPlayer = currentlySeen.isNotEmpty()

        val currentTick = ticks.incrementAndGet()
        if (currentTick % 10 == 0) {
            val observers = context.viewers.filter { showDisplay.get(it) }
            if (observers.isNotEmpty()) {
                runGlobal {
                    displayVision(observers, eyeLoc, applyR, f, s)
                }
            }
        }

        return TickResult.IGNORED
    }

    private fun hasLineOfSight(origin: Location, target: Location, radius: Double, fov: Double, shape: VisionShape): Boolean {
        val direction = target.toVector().subtract(origin.toVector())
        if (direction.length() > radius) return false

        if (shape == VisionShape.CONE && fov < 360) {
            val angle = Math.toDegrees(origin.direction.angle(direction).toDouble())
            if (angle > fov / 2) return false
        }

        if (!Bukkit.isPrimaryThread()) return true

        val result = origin.world.rayTraceBlocks(origin, direction, radius)
        return result == null || result.hitBlock == null
    }

    override fun deactivate(context: ActivityContext) {
        seenPlayers.clear()
    }

    override fun dispose() {
        seenPlayers.clear()
    }

    private fun displayVision(observers: List<Player>, origin: Location, radius: Double, fovDegrees: Double, shape: VisionShape) {
        val particle = Particle.FLAME

        if (shape == VisionShape.SPHERE) {
            val particles = 36
            for (i in 0 until particles) {
                val angle = 2 * Math.PI * i / particles
                val x = Math.cos(angle) * radius
                val z = Math.sin(angle) * radius
                val point = origin.clone().add(x, 0.0, z)
                observers.forEach { it.spawnParticle(particle, point, 1, 0.0, 0.0, 0.0, 0.0) }
            }
        } else {
            val dir = origin.direction.clone().normalize()
            val halfFovRad = Math.toRadians(fovDegrees / 2)
            val segments = 20

            for (i in 0..segments) {
                val fraction = i.toDouble() / segments
                val angle = -halfFovRad + (fraction * (2 * halfFovRad))
                val arcDir = rotateAroundY(dir.clone(), angle)
                val arcPoint = origin.clone().add(arcDir.multiply(radius))
                observers.forEach { it.spawnParticle(particle, arcPoint, 1, 0.0, 0.0, 0.0, 0.0) }

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
