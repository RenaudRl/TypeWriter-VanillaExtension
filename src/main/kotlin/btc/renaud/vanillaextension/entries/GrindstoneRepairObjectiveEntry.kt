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

@Entry("grindstone_repair_objective", "An objective to repair items using a grindstone", Colors.BLUE_VIOLET, "mdi:cog")
class GrindstoneRepairObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The item that needs to be processed. Leave empty to count any processed item.")
    val processedItem: Var<Item> = ConstVar(Item.Empty),
    @Help("The amount of times the player needs to use the grindstone.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return GrindstoneRepairObjectiveDisplay(ref())
    }
}

private class GrindstoneRepairObjectiveDisplay(ref: Ref<GrindstoneRepairObjectiveEntry>) :
    BaseCountObjectiveDisplay<GrindstoneRepairObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        // Only trigger for grindstone
        if (event.inventory.type != InventoryType.GRINDSTONE) return

        // Check if clicking on the result slot (slot 2 in grindstone)
        if (event.rawSlot != 2) return

        val resultItem = event.currentItem ?: return
        if (resultItem.type.isAir) return
        
        val requiredItem = entry.processedItem.get(player)
        
        // If no specific item is required, count all processed items
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
