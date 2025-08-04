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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*

@Entry("trident_recall_objective", "An objective to recall tridents with loyalty", Colors.BLUE_VIOLET, "mdi:boomerang")
class TridentRecallObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The minimum loyalty level required for the recall to count. Set to 0 to count any loyalty level.")
    val minimumLoyaltyLevel: Var<Int> = ConstVar(1),
    @Help("The total number of tridents the player needs to recall.")
    override val amount: Var<Int> = ConstVar(3),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return TridentRecallObjectiveDisplay(ref())
    }
}

private class TridentRecallObjectiveDisplay(ref: Ref<TridentRecallObjectiveEntry>) :
    BaseCountObjectiveDisplay<TridentRecallObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onTridentRecall(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val item = event.item.itemStack
        
        // Check if the picked up item is a trident
        if (item.type != Material.TRIDENT) return
        
        // Check if the trident has loyalty enchantment (indicating it was recalled)
        val loyaltyLevel = item.enchantments.entries.find { it.key.key.key == "loyalty" }?.value ?: 0
        if (loyaltyLevel == 0) return
        
        val minLoyalty = entry.minimumLoyaltyLevel.get(player)
        
        // Check if the loyalty level meets the minimum requirement
        if (loyaltyLevel >= minLoyalty) {
            incrementCount(player, 1)
        }
    }
}
