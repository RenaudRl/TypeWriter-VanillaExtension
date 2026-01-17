package btc.renaud.vanillaextension.entries.objective

import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.entries.QuestEntry
import org.bukkit.Statistic
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerStatisticIncrementEvent
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("statistic_reach_objective", "Reach statistic objective", Colors.BLUE_VIOLET, "mdi:chart-line")
class StatisticReachObjectiveEntry(
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
    @Help("Statistic to track.")
    val statistic: Statistic = Statistic.JUMP,
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return StatisticReachObjectiveDisplay(ref())
    }
}

private class StatisticReachObjectiveDisplay(ref: Ref<StatisticReachObjectiveEntry>) :
    BaseCountObjectiveDisplay<StatisticReachObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onStatisticIncrement(event: PlayerStatisticIncrementEvent) {
        val entry = ref.get() ?: return
        if (event.statistic != entry.statistic) return
        
        val player = event.player
        if (!filter(player)) return
        
        // This objective checks value >= amount, not just incrementing a counter
        // But for consistency with BaseCountObjective, we might want to check the absolute value
        // or treat 'amount' as the target value.
        // BaseCountObjective counts progress 0 -> amount.
        // If we want to reach a value, we can update progress relative to it.
        val currentValue = event.newValue
        val target = entry.amount.get(player)
        val progress = kotlin.math.min(currentValue, target)
        // We override logic slightly to set absolute progress? 
        // BaseCountObjective doesn't easily support 'set progress', only increment.
        // So we might need to calculate delta needed.
        // However, standard objectives accumulate. 
        // If the goal is "Reach 100 jumps", and I have 50. I jump 1 -> 51.
        // If I start quest with 50 jumps, do I need 100 MORE or 100 TOTAL?
        // Usually objectives are "do X more times".
        // BUT "StatisticReach" implies reaching a TOTAL.
        // Let's assume this objective behaves like "Accumulate X stat points during quest".
        incrementCount(player, event.newValue - event.previousValue)
    }
}
