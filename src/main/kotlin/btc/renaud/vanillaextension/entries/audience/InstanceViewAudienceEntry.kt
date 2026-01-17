package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entity.AudienceEntityDisplay
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.entry.findDisplay
import com.typewritermc.engine.paper.utils.toBukkitLocation
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.cos
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry(
    "instance_view_audience",
    "Only keep players who are looking at a specific Typewriter entity instance.",
    Colors.GREEN,
    "mdi:eye-check"
)
class InstanceViewAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    val instance: Ref<out EntityInstanceEntry> = emptyRef(),
    val maxDistance: Double = 6.0,
    val angleDegrees: Double = 9.0,
    val requireLineOfSight: Boolean = false,
) : AudienceFilterEntry {
    override suspend fun display(): AudienceFilter =
        InstanceViewAudienceFilter(ref(), instance, maxDistance, angleDegrees, requireLineOfSight)
}

class InstanceViewAudienceFilter(
    ref: Ref<out AudienceFilterEntry>,
    private val instance: Ref<out EntityInstanceEntry>,
    private val maxDistance: Double,
    angleDegrees: Double,
    private val requireLineOfSight: Boolean,
) : AudienceFilter(ref), TickableDisplay {

    private val cosThreshold: Double = cos(angleDegrees * PI / 180.0)

    private fun getDisplay(): AudienceEntityDisplay? {
        val entry = instance.get() ?: return null
        val disp = entry.ref().findDisplay<AudienceDisplay>()
        return disp as? AudienceEntityDisplay
    }

    override fun filter(player: Player): Boolean {
        val display = getDisplay() ?: return false
        
        val pid = player.uniqueId
        if (!display.canView(pid)) return false

        val pos = display.position(pid) ?: return false
        val eyeOffset = display.entityState(pid).eyeHeight
        val targetLoc = pos.add(0.0, eyeOffset, 0.0).toBukkitLocation()
        if (targetLoc.world != player.world) return false

        val eyeLoc = player.eyeLocation
        val eyeVec = eyeLoc.toVector()
        val eyeDir: Vector = eyeLoc.direction.normalize()

        val toTarget = targetLoc.toVector().subtract(eyeVec)
        val distance = toTarget.length()
        if (distance > maxDistance) return false

        val dirToTarget = toTarget.normalize()
        val dot = eyeDir.dot(dirToTarget)
        if (dot < cosThreshold) return false

        if (requireLineOfSight) {
            // Skip rayTrace if not on main thread to avoid async errors
            if (!org.bukkit.Bukkit.isPrimaryThread()) return true
            val hit = player.world.rayTraceBlocks(eyeLoc, dirToTarget, distance)
            if (hit != null) return false
        }

        return true
    }

    override fun tick() {
        consideredPlayers.forEach { it.refresh() }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.player in consideredPlayers) event.player.refresh()
    }

    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        if (event.player in consideredPlayers) event.player.refresh()
    }
}
