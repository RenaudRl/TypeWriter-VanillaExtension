package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.AudienceFilterEntry
import com.typewritermc.engine.paper.entry.entries.Invertible
import com.typewritermc.engine.paper.entry.entries.TickableDisplay
import com.typewritermc.core.entries.ref
import org.bukkit.entity.Player

@Entry(
    "moving_audience",
    "In audience while any movement key is active",
    Colors.GREEN,
    icon = "game-icons:run"
)
class MovingAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
) : AudienceFilterEntry, TickableDisplay, Invertible {

    override suspend fun display(): AudienceFilter = object : AudienceFilter(ref()), TickableDisplay {
        override fun filter(player: Player): Boolean {
            // Fallback: treat the player as "moving" when any obvious movement state is active
            val movingByVelocity = player.velocity.lengthSquared() > 0.001
            return player.isSprinting || player.isSneaking || player.isFlying || player.isGliding || movingByVelocity
        }

        override fun tick() { consideredPlayers.forEach { it.refresh() } }
    }

    override fun tick() {}
}

