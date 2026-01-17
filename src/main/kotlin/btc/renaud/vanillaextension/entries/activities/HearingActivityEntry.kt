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
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.UUID
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "hearing_activity",
    "Detect players producing noise around the NPC",
    Colors.GREEN,
    "mdi:ear-hearing"
)
class HearingActivityEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("Maximum distance in blocks the NPC can hear")
    val radius: Double = 10.0,
    @Help("Velocity length considered noisy")
    val speedThreshold: Double = 0.1,
    @Help("Maximum distance from the spawn location the player can move before the activity stops")
    val detectionRadius: Double = 30.0,
    val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val criteria: List<Criteria> = emptyList()
) : GenericEntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext> {
        return HearingActivity(radius, speedThreshold, detectionRadius, currentLocation, triggers, criteria)
    }
}

class HearingActivity(
    private val radius: Double,
    private val speedThreshold: Double,
    private val detectionRadius: Double,
    start: PositionProperty,
    private val triggers: List<Ref<TriggerableEntry>>,
    private val criteria: List<Criteria>
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty = start
    private val radiusSquared = radius * radius
    private val detectionRadiusSquared = detectionRadius * detectionRadius
    private val origin = start
    val heardPlayers: MutableSet<UUID> = mutableSetOf()
    var isHearingNoise: Boolean = false
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
                heardPlayers.clear()
                isHearingNoise = false
                return TickResult.IGNORED
            }
            val maxStep = context.entityState.speed.toDouble()
            if (move.lengthSquared() > maxStep * maxStep) {
                move = move.normalize().multiply(maxStep)
            }
            currentPosition = currentPosition.add(move.x, move.y, move.z)
            heardPlayers.clear()
            isHearingNoise = false
            return TickResult.CONSUMED
        }

        val currentlyHeard = mutableSetOf<UUID>()
        players.forEach { player ->
            val dist = currentPosition.distanceSqrt(player.location) ?: return@forEach
            if (dist <= radiusSquared && isNoisy(player)) {
                currentlyHeard.add(player.uniqueId)
                if (heardPlayers.add(player.uniqueId)) {
                    triggers.triggerEntriesFor(player) { }
                }
            }
        }
        
        heardPlayers.removeIf { it !in currentlyHeard }
        isHearingNoise = currentlyHeard.isNotEmpty()
        return TickResult.IGNORED
    }

    private fun isNoisy(player: Player): Boolean {
        val velocity: Vector = player.velocity
        return player.isSprinting || velocity.lengthSquared() > speedThreshold * speedThreshold
    }

    override fun dispose(context: ActivityContext) {
        heardPlayers.clear()
    }
}
