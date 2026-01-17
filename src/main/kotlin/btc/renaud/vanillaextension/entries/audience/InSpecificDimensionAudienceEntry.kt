package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.World
import org.bukkit.entity.Player

@Entry("in_specific_dimension_audience", "Filter players in a specific dimension type", Colors.GREEN, icon = "mdi:earth")
class InSpecificDimensionAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
    val dimensionType: String = "NORMAL", // NORMAL, NETHER, THE_END
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean {
        return try {
            player.world.environment == World.Environment.valueOf(dimensionType.uppercase())
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
