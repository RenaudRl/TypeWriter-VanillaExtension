package btc.renaud.vanillaextension.entries.objective

import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.entries.QuestEntry
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerLevelChangeEvent
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("level_reach_objective", "Level reach objective", Colors.BLUE_VIOLET, "mdi:arrow-up-bold")
class LevelReachObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return LevelReachObjectiveDisplay(ref())
    }
}

private class LevelReachObjectiveDisplay(ref: Ref<LevelReachObjectiveEntry>) :
    BaseCountObjectiveDisplay<LevelReachObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerLevelChange(event: PlayerLevelChangeEvent) {
        val player = event.player
        if (!filter(player)) return
        
        val entry = ref.get() ?: return
        val target = entry.amount.get(player)
        
        if (event.newLevel >= target) {
            // How to complete?
            // BaseCountObjective counts 0->Target.
            // If we are already at level 100, we might auto-complete.
            // set count to target
             incrementCount(player, target) 
        }
    }
}
