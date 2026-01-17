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
    "health_detection_activity",
    "Detect players with low health",
    Colors.GREEN,
    "mdi:heart-pulse"
)
class HealthDetectionActivityEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("Health threshold to trigger detection")
    val healthThreshold: Double = 5.0,
    @Help("Maximum distance to check")
    val radius: Double = 10.0,
    @Help("Maximum distance from the spawn location the player can move before the activity stops")
    val detectionRadius: Double = 30.0,
    val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val criteria: List<Criteria> = emptyList()
) : GenericEntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext> {
        return HealthDetectionActivity(healthThreshold, radius, detectionRadius, currentLocation, triggers, criteria)
    }
}

class HealthDetectionActivity(
    private val healthThreshold: Double,
    private val radius: Double,
    private val detectionRadius: Double,
    start: PositionProperty,
    private val triggers: List<Ref<TriggerableEntry>>,
    private val criteria: List<Criteria>
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty = start
    private val radiusSquared = radius * radius
    private val detectionRadiusSquared = detectionRadius * detectionRadius
    private val origin = start
    val lowHealthPlayers: MutableSet<UUID> = mutableSetOf()
    var hasLowHealth: Boolean = false
        private set

    override fun initialize(context: ActivityContext) {}

    override fun tick(context: ActivityContext): TickResult {
        val players = context.viewers.filter {
            val fromOrigin = origin.distanceSqrt(it.location) ?: Double.MAX_VALUE
            fromOrigin <= detectionRadiusSquared && criteria.matches(it)
        }

        val world = Bukkit.getWorld(UUID.fromString(currentPosition.world.identifier)) ?: return TickResult.IGNORED
        val npcLoc = Location(world, currentPosition.x, currentPosition.y, currentPosition.z)

        if (players.isEmpty()) {
            val originLoc = Location(world, origin.x, origin.y, origin.z)
            var move = originLoc.toVector().subtract(npcLoc.toVector())
            if (move.lengthSquared() <= 0.01) {
                lowHealthPlayers.clear()
                hasLowHealth = false
                return TickResult.IGNORED
            }
            val maxStep = context.entityState.speed.toDouble()
            if (move.lengthSquared() > maxStep * maxStep) {
                move = move.normalize().multiply(maxStep)
            }
            currentPosition = currentPosition.add(move.x, move.y, move.z)
            lowHealthPlayers.clear()
            hasLowHealth = false
            return TickResult.CONSUMED
        }

        val currentlyLowHealth = mutableSetOf<UUID>()
        players.forEach { player ->
            val dist = currentPosition.distanceSqrt(player.location) ?: return@forEach
            if (dist > radiusSquared) return@forEach
            if (player.health <= healthThreshold) {
                currentlyLowHealth.add(player.uniqueId)
                if (lowHealthPlayers.add(player.uniqueId)) {
                    triggers.triggerEntriesFor(player) { }
                }
            }
        }
        
        lowHealthPlayers.removeIf { it !in currentlyLowHealth }
        hasLowHealth = currentlyLowHealth.isNotEmpty()
        return TickResult.IGNORED
    }

    override fun dispose(context: ActivityContext) {
        lowHealthPlayers.clear()
    }
}
