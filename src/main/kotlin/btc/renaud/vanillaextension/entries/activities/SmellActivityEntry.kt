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
import org.bukkit.Material
import java.util.UUID
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "smell_activity",
    "Detect players carrying a specific item",
    Colors.GREEN,
    "mdi:nose"
)
class SmellActivityEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("Material that attracts the NPC")
    val material: Material = Material.COD,
    @Help("Maximum distance in blocks the NPC can smell")
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
        return SmellActivity(material, radius, detectionRadius, currentLocation, triggers, criteria)
    }
}

class SmellActivity(
    private val material: Material,
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
    val smelledPlayers: MutableSet<UUID> = mutableSetOf()
    var isSmelling: Boolean = false
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
                smelledPlayers.clear()
                isSmelling = false
                return TickResult.IGNORED
            }
            val maxStep = context.entityState.speed.toDouble()
            if (move.lengthSquared() > maxStep * maxStep) {
                move = move.normalize().multiply(maxStep)
            }
            currentPosition = currentPosition.add(move.x, move.y, move.z)
            smelledPlayers.clear()
            isSmelling = false
            return TickResult.CONSUMED
        }

        val currentlySmelled = mutableSetOf<UUID>()
        players.forEach { player ->
            val dist = currentPosition.distanceSqrt(player.location) ?: return@forEach
            if (dist <= radiusSquared && player.inventory.contains(material)) {
                currentlySmelled.add(player.uniqueId)
                if (smelledPlayers.add(player.uniqueId)) {
                    triggers.triggerEntriesFor(player) { }
                }
            }
        }
        
        smelledPlayers.removeIf { it !in currentlySmelled }
        isSmelling = currentlySmelled.isNotEmpty()
        return TickResult.IGNORED
    }

    override fun dispose(context: ActivityContext) {
        smelledPlayers.clear()
    }
}
