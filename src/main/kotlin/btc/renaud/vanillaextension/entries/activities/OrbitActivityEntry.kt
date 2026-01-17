package btc.renaud.vanillaextension.entries.activities

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.entity.*
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry
import com.typewritermc.engine.paper.entry.matches
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "orbit_activity",
    "Orbit around a player",
    Colors.GREEN,
    "mdi:orbit"
)
class OrbitActivityEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("Radius of the orbit")
    val radius: Double = 3.0,
    @Help("Angular speed in radians per tick")
    val speed: Double = 0.1,
    @Help("Maximum distance from the spawn location the player can move before the activity stops")
    val detectionRadius: Double = 30.0,
    val criteria: List<Criteria> = emptyList()
) : GenericEntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext> {
        return OrbitActivity(radius, speed, detectionRadius, currentLocation, criteria)
    }
}

class OrbitActivity(
    private val radius: Double,
    private val speed: Double,
    private val detectionRadius: Double,
    start: PositionProperty,
    private val criteria: List<Criteria>
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty = start
    private val detectionRadiusSquared = detectionRadius * detectionRadius
    private val origin = start
    private var angle = 0.0
    val orbitingPlayers: MutableSet<UUID> = mutableSetOf()

    override fun initialize(context: ActivityContext) {}

    override fun tick(context: ActivityContext): TickResult {
        val players = context.viewers.filter {
            val distFromOrigin = origin.distanceSqrt(it.location) ?: Double.MAX_VALUE
            distFromOrigin <= detectionRadiusSquared && criteria.matches(it)
        }

        val world = Bukkit.getWorld(UUID.fromString(currentPosition.world.identifier)) ?: return TickResult.IGNORED
        val npcLoc = Location(world, currentPosition.x, currentPosition.y, currentPosition.z)

        if (players.isEmpty()) {
            val originLoc = Location(world, origin.x, origin.y, origin.z)
            var move = originLoc.toVector().subtract(npcLoc.toVector())
            if (move.lengthSquared() <= 0.01) {
                orbitingPlayers.clear()
                return TickResult.IGNORED
            }
            val maxStep = context.entityState.speed.toDouble()
            if (move.lengthSquared() > maxStep * maxStep) {
                move = move.normalize().multiply(maxStep)
            }
            val updated = npcLoc.add(move)
            currentPosition = updated.toProperty().withYaw(currentPosition.yaw).withPitch(currentPosition.pitch)
            orbitingPlayers.clear()
            return TickResult.CONSUMED
        }

        val player = players.random()
        angle += speed
        val loc = player.location
        val x = loc.x + cos(angle) * radius
        val z = loc.z + sin(angle) * radius
        val newLoc = Location(loc.world, x, loc.y, z)
        var move = newLoc.toVector().subtract(npcLoc.toVector())
        val maxStep = context.entityState.speed.toDouble()
        if (move.lengthSquared() > maxStep * maxStep) {
            move = move.normalize().multiply(maxStep)
        }
        val updated = npcLoc.add(move)
        currentPosition = updated.toProperty().withYaw(currentPosition.yaw).withPitch(currentPosition.pitch)
        orbitingPlayers.clear()
        orbitingPlayers.add(player.uniqueId)
        return TickResult.CONSUMED
    }

    override fun dispose(context: ActivityContext) {
        orbitingPlayers.clear()
    }
}
