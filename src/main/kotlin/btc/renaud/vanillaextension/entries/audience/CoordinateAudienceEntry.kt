package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.entity.Player

@Entry(
    "coordinate_audience",
    "Filter players by coordinates",
    Colors.GREEN,
    icon = "mdi:map"
)
class CoordinateAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    val minX: Double? = null,
    val maxX: Double? = null,
    val minY: Double? = null,
    val maxY: Double? = null,
    val minZ: Double? = null,
    val maxZ: Double? = null,
    override val inverted: Boolean = false,
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean {
        val loc = player.location
        return (minX == null || loc.x >= minX) &&
               (maxX == null || loc.x <= maxX) &&
               (minY == null || loc.y >= minY) &&
               (maxY == null || loc.y <= maxY) &&
               (minZ == null || loc.z >= minZ) &&
               (maxZ == null || loc.z <= maxZ)
    }
}

