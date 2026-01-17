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
import org.bukkit.util.Vector
import java.util.UUID
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "follow_player_activity",
    "Follow the nearest player within range",
    Colors.GREEN,
    "mdi:account-arrow-right"
)
class FollowPlayerActivityEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("Distance to keep from the player")
    val followDistance: Double = 2.0,
    @Help("Maximum range to start following")
    val radius: Double = 15.0,
    @Help("Maximum distance from the spawn location the player can move before the activity stops")
    val detectionRadius: Double = 30.0,
    val criteria: List<Criteria> = emptyList()
) : GenericEntityActivityEntry {
    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<ActivityContext> {
        return FollowPlayerActivity(followDistance, radius, detectionRadius, currentLocation, criteria)
    }
}

class FollowPlayerActivity(
    private val followDistance: Double,
    private val radius: Double,
    private val detectionRadius: Double,
    start: PositionProperty,
    private val criteria: List<Criteria>
) : EntityActivity<ActivityContext> {

    override var currentPosition: PositionProperty = start
    private val radiusSquared = radius * radius
    private val detectionRadiusSquared = detectionRadius * detectionRadius
    private val origin = start
    val followedPlayers: MutableSet<UUID> = mutableSetOf()

    override fun initialize(context: ActivityContext) {}

    override fun tick(context: ActivityContext): TickResult {
        val world = Bukkit.getWorld(UUID.fromString(currentPosition.world.identifier)) ?: return TickResult.IGNORED
        val npcLoc = Location(world, currentPosition.x, currentPosition.y, currentPosition.z)

        val nearbyPlayers = context.viewers.filter {
            val distFromOrigin = origin.distanceSqrt(it.location) ?: Double.MAX_VALUE
            distFromOrigin <= detectionRadiusSquared && criteria.matches(it)
        }

        if (nearbyPlayers.isEmpty()) {
            val originLoc = Location(world, origin.x, origin.y, origin.z)
            var move = originLoc.toVector().subtract(npcLoc.toVector())
            if (move.lengthSquared() <= 0.01) {
                followedPlayers.clear()
                return TickResult.IGNORED
            }
            val maxStep = context.entityState.speed.toDouble()
            if (move.lengthSquared() > maxStep * maxStep) {
                move = move.normalize().multiply(maxStep)
            }
            currentPosition = currentPosition.add(move.x, move.y, move.z)
            followedPlayers.clear()
            return TickResult.CONSUMED
        }

        val target = nearbyPlayers
            .filter {
                (currentPosition.distanceSqrt(it.location) ?: Double.MAX_VALUE) <= radiusSquared
            }
            .minByOrNull { it.location.distanceSquared(npcLoc) }
            ?: return TickResult.IGNORED

        val dir = target.location.toVector().subtract(npcLoc.toVector())
        val distance = dir.length()
        if (distance <= followDistance) {
            followedPlayers.clear()
            return TickResult.IGNORED
        }

        var move = dir.normalize().multiply(distance - followDistance)
        val maxStep = context.entityState.speed.toDouble()
        if (move.lengthSquared() > maxStep * maxStep) {
            move = dir.normalize().multiply(maxStep)
        }
        currentPosition = currentPosition.add(move.x, move.y, move.z)
        followedPlayers.clear()
        followedPlayers.add(target.uniqueId)
        return TickResult.CONSUMED
    }

    override fun dispose(context: ActivityContext) {
        followedPlayers.clear()
    }
}
