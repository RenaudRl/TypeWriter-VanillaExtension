package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.AudienceFilterEntry
import com.typewritermc.engine.paper.entry.entries.Invertible
import com.typewritermc.engine.paper.entry.entries.TickableDisplay
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.core.entries.ref
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

@Entry(
    "looking_at_entity_audience",
    "In audience while looking at a specific vanilla entity",
    Colors.GREEN,
    icon = "mdi:eye"
)
class LookingAtEntityAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
    @Help("Entity type to detect")
    private val entityType: Var<EntityType> = ConstVar(EntityType.ZOMBIE),
    @Help("Maximum distance to check")
    private val maxDistance: Double = 6.0,
) : AudienceFilterEntry, TickableDisplay, Invertible {

    override suspend fun display(): AudienceFilter = object : AudienceFilter(ref()), TickableDisplay {
        override fun filter(player: Player): Boolean {
            val type = entityType.get(player, context())
            val target = player.getTargetEntity(maxDistance.toInt())
            return target?.type == type
        }

        override fun tick() { consideredPlayers.forEach { it.refresh() } }
    }

    override fun tick() {}
}

