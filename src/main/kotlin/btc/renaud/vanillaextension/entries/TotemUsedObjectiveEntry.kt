package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityResurrectEvent
import java.util.*

@Entry("totem_used_objective", "An objective to use a totem of undying", Colors.BLUE_VIOLET, "ph:heart-bold")
class TotemUsedObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The amount of times the player needs to use a totem.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
    @Help("If true, the event will be cancelled.")
    val cancelEvent: Var<Boolean> = ConstVar(false),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return TotemUsedObjectiveDisplay(ref())
    }
}

private class TotemUsedObjectiveDisplay(ref: Ref<TotemUsedObjectiveEntry>) :
    BaseCountObjectiveDisplay<TotemUsedObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEntityResurrect(event: EntityResurrectEvent) {
        val player = event.entity as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        if (event.isCancelled) return

        if (entry.cancelEvent.get(player, com.typewritermc.core.interaction.context())) {
            event.isCancelled = true
        }

        incrementCount(player, 1)
    }
}
