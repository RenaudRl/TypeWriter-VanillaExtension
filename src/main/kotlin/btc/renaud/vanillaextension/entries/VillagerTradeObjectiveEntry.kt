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
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import com.typewritermc.engine.paper.utils.item.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*

@Entry("villager_trade_objective", "An objective to trade with villagers", Colors.BLUE_VIOLET, "mdi:account-cash")
class VillagerTradeObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The item that needs to be received from trades. Leave empty to count any trade.")
    val tradedItem: Var<Item> = ConstVar(Item.Empty),
    @Help("The amount of times the player needs to trade with villagers.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return VillagerTradeObjectiveDisplay(ref())
    }
}

private class VillagerTradeObjectiveDisplay(ref: Ref<VillagerTradeObjectiveEntry>) :
    BaseCountObjectiveDisplay<VillagerTradeObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onVillagerTrade(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        // Only trigger for merchant inventory (villager trading)
        if (event.inventory.type != InventoryType.MERCHANT) return

        // Check if clicking on the result slot (slot 2 in merchant inventory)
        if (event.rawSlot != 2) return

        // Get the result item
        val resultItem = event.currentItem ?: return
        if (resultItem.type.isAir) return

        val requiredItem = entry.tradedItem.get(player)

        // If no specific item is required, count all trades
        if (requiredItem == Item.Empty) {
            incrementCount(player, 1)
            return
        }

        // Check if the result item matches the required item
        if (requiredItem.isSameAs(player, resultItem, context())) {
            incrementCount(player, 1)
        }
    }
}
