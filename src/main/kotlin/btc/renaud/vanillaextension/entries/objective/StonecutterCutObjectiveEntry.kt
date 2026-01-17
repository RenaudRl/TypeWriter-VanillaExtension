package btc.renaud.vanillaextension.entries.objective

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.entries.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import com.typewritermc.engine.paper.utils.item.Item
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.entity.Player
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("on_stonecutter_cut_objective", "An objective to cut items using a stonecutter", Colors.BLUE_VIOLET, "ph:scissors-bold")
class StonecutterCutObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The item that needs to be cut. Leave empty to count any cut item.")
    val cutItem: Var<Item> = ConstVar(Item.Empty),
    @Help("The amount of times the player needs to cut items.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return StonecutterCutObjectiveDisplay(ref())
    }
}

private class StonecutterCutObjectiveDisplay(ref: Ref<StonecutterCutObjectiveEntry>) :
    BaseCountObjectiveDisplay<StonecutterCutObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        // Only trigger for stonecutter
        if (event.inventory.type != InventoryType.STONECUTTER) return

        // Check if clicking on the result slot (slot 1 in stonecutter)
        if (event.rawSlot != 1) return

        val clickedItem = event.currentItem ?: return
        if (clickedItem.type.isAir) return
        
        val requiredItem = entry.cutItem.get(player)
        
        // If no specific item is required, count all cut items
        if (requiredItem == Item.Empty) {
            incrementCount(player, clickedItem.amount)
            return
        }
        
        // Check if the clicked item matches the required item
        if (requiredItem.isSameAs(player, clickedItem, context())) {
            incrementCount(player, clickedItem.amount)
        }
    }
}

