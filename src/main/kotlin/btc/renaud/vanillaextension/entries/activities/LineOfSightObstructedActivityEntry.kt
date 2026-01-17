package btc.renaud.vanillaextension.entries.activities

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entity.*
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerEntriesFor
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.UUID
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "line_of_sight_obstructed_activity",
    "Detect when a player is hidden behind a block",
    Colors.GREEN,
    "mdi:eye-off"
)
class LineOfSightObstructedActivityEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("Maximum distance to check for obstruction")
    val radius: Double = 15.0,
    @Help("Maximum distance from the spawn location the player can move before the activity stops")
    val detectionRadius: Double = 30.0,
    val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val criteria: List<Criteria> = emptyList()
) : GenericEntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext> {
        return LineOfSightObstructedActivity(radius, detectionRadius, currentLocation, triggers, criteria)
    }
}

class LineOfSightObstructedActivity(
    private val radius: Double,
    private val detectionRadius: Double,
    start: PositionProperty,
    private val triggers: List<Ref<TriggerableEntry>>,
    private val criteria: List<Criteria>
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty = start
    private val radiusSquared = radius * radius
    private val detectionRadiusSquared = detectionRadius * detectionRadius
    private val startLocation = start
    val obstructedPlayers: MutableSet<UUID> = mutableSetOf()
    var isObstructed: Boolean = false
        private set

    override fun initialize(context: ActivityContext) {}

    override fun tick(context: ActivityContext): TickResult {
        obstructedPlayers.clear()
        val world = Bukkit.getWorld(UUID.fromString(currentPosition.world.identifier)) ?: return TickResult.IGNORED
        val npcLoc = Location(world, currentPosition.x, currentPosition.y, currentPosition.z)

        val players = context.viewers.filter {
            val fromOrigin = startLocation.distanceSqrt(it.location) ?: Double.MAX_VALUE
            fromOrigin <= detectionRadiusSquared && criteria.matches(it)
        }

        if (players.isEmpty()) {
            val originLoc = Location(world, startLocation.x, startLocation.y, startLocation.z)
            var move = originLoc.toVector().subtract(npcLoc.toVector())
            if (move.lengthSquared() <= 0.01) {
                isObstructed = false
                return TickResult.IGNORED
            }
            val maxStep = context.entityState.speed.toDouble()
            if (move.lengthSquared() > maxStep * maxStep) {
                move = move.normalize().multiply(maxStep)
            }
            val updated = npcLoc.add(move)
            currentPosition = updated.toProperty().withYaw(currentPosition.yaw).withPitch(currentPosition.pitch)
            isObstructed = false
            return TickResult.CONSUMED
        }

        val eyeOrigin = Location(world, currentPosition.x, currentPosition.y + context.entityState.eyeHeight, currentPosition.z)
        players.forEach { player ->
            val dist = currentPosition.distanceSqrt(player.location) ?: return@forEach
            if (dist > radiusSquared) return@forEach
            val dir = player.eyeLocation.toVector().subtract(eyeOrigin.toVector())
            // Skip rayTrace if not on main thread to avoid async errors
            if (!Bukkit.isPrimaryThread()) return@forEach
            val hit = eyeOrigin.world.rayTraceBlocks(eyeOrigin, dir.normalize(), Math.sqrt(dist))
            if (hit != null) {
                obstructedPlayers.add(player.uniqueId)
                triggers.triggerEntriesFor(player) { }
            }
        }
        isObstructed = obstructedPlayers.isNotEmpty()
        return TickResult.IGNORED
    }

    override fun dispose(context: ActivityContext) {
        obstructedPlayers.clear()
    }
}
