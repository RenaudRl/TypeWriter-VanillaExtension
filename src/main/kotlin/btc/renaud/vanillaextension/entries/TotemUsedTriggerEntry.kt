package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.AudienceFilterEntry
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.ConstVar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityResurrectEvent

@Entry("totem_used_trigger", "A trigger for when players use a totem of undying", Colors.PURPLE, "ph:heart-bold")
/**
 * The `Totem Used Trigger` is triggered when a player uses a totem of undying to cheat death.
 *
 * ## How could this be used?
 * This could be used to complete a quest where the player has to use a totem,
 * or to give the player a special effect when they use a totem.
 */
class TotemUsedTriggerEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    @Help("If true, the event will be cancelled.")
    val cancelEvent: Var<Boolean> = ConstVar(false),
) : AudienceFilterEntry, TriggerableEntry {

    override suspend fun display(): AudienceFilter {
        return TotemUsedTriggerDisplay(ref())
    }
}

private class TotemUsedTriggerDisplay(private val ref: Ref<TotemUsedTriggerEntry>) : AudienceFilter(ref) {

    override fun filter(player: Player): Boolean {
        val entry = ref.get() ?: return false
        return entry.criteria.matches(player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onTotemUsed(event: EntityResurrectEvent) {
        val player = event.entity as? Player ?: return
        val entry = ref.get() ?: return
        
        if (!filter(player)) return

        if (event.isCancelled) return

        if (entry.cancelEvent.get(player, context())) {
            event.isCancelled = true
        }

        entry.triggers.forEach { it.triggerFor(player, context()) }
    }
}
