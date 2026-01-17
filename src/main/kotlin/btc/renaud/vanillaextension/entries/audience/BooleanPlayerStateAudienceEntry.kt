package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.AudienceFilterEntry
import com.typewritermc.engine.paper.entry.entries.Invertible
import com.typewritermc.engine.paper.entry.entries.TickableDisplay
import com.typewritermc.core.entries.ref
import org.bukkit.entity.Player

/**
 * Base class for simple audience filters that depend on a boolean player state.
 */
abstract class BooleanPlayerStateAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
) : AudienceFilterEntry, TickableDisplay, Invertible {

    protected abstract fun state(player: Player): Boolean

    override suspend fun display(): AudienceFilter = object : AudienceFilter(ref()), TickableDisplay {
        override fun filter(player: Player): Boolean = state(player)
        override fun tick() { consideredPlayers.forEach { it.refresh() } }
    }

    override fun tick() {}
}

