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
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.entity.Player
import java.util.*

@Entry("anvil_repair_objective", "An objective to repair items using an anvil", Colors.BLUE_VIOLET, "mdi:anvil")
class AnvilRepairObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The item that needs to be repaired. Leave empty to count any repaired item.")
    val repairedItem: Var<Item> = ConstVar(Item.Empty),
    @Help("The amount of times the player needs to repair items.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return AnvilRepairObjectiveDisplay(ref())
    }
}

private class AnvilRepairObjectiveDisplay(ref: Ref<AnvilRepairObjectiveEntry>) :
    BaseCountObjectiveDisplay<AnvilRepairObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        // Only trigger for anvil
        if (event.inventory.type != InventoryType.ANVIL) return

        // Check if clicking on the result slot (slot 2 in anvil)
        if (event.rawSlot != 2) return

        val resultItem = event.currentItem ?: return
        if (resultItem.type.isAir) return
        
        val requiredItem = entry.repairedItem.get(player)
        
        // If no specific item is required, count all repaired items
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
