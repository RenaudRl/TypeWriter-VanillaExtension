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
import com.typewritermc.engine.paper.utils.item.Item
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.ConstVar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

@Entry("on_smelt_trigger", "A trigger for when players extract smelted items from furnaces", Colors.YELLOW, "mdi:fire")
class SmeltTriggerEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    @Help("The item that needs to be smelted. Leave empty to trigger for any smelted item.")
    val smeltedItem: Var<Item> = ConstVar(Item.Empty),
) : AudienceFilterEntry, TriggerableEntry {

    override suspend fun display(): AudienceFilter {
        return SmeltTriggerDisplay(ref())
    }
}

private class SmeltTriggerDisplay(private val ref: Ref<SmeltTriggerEntry>) : AudienceFilter(ref) {

    override fun filter(player: Player): Boolean {
        val entry = ref.get() ?: return false
        return entry.criteria.matches(player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val entry = ref.get() ?: return
        
        if (!filter(player)) return

        // Check if it's a furnace-type inventory
        val inventory = event.inventory
        if (inventory.type != InventoryType.FURNACE && 
            inventory.type != InventoryType.BLAST_FURNACE && 
            inventory.type != InventoryType.SMOKER) {
            return
        }

        // Check if clicking on the result slot (slot 2 in furnace inventories)
        if (event.rawSlot != 2) return

        val clickedItem = event.currentItem ?: return
        if (clickedItem.type.isAir) return

        val requiredItem = entry.smeltedItem.get(player)
        
        // If no specific item is required, trigger for all smelted items
        if (requiredItem == Item.Empty) {
            entry.triggers.forEach { it.triggerFor(player, context()) }
            return
        }
        
        // Check if the clicked item matches the required item
        if (requiredItem.isSameAs(player, clickedItem, context())) {
            entry.triggers.forEach { it.triggerFor(player, context()) }
        }
    }
}
