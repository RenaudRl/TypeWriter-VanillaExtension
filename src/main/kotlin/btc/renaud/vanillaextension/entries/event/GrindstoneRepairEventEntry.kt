package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.triggerAllFor
import com.typewritermc.engine.paper.utils.item.Item
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("grindstone_repair_event", "Triggered when a player repairs items or removes enchantments using a grindstone", Colors.YELLOW, "mdi:cog")
@ContextKeys(GrindstoneRepairContextKeys::class)
/**
 * The `Grindstone Repair Event` is triggered when a player repairs items or removes enchantments using a grindstone.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to repair a certain item,
 * or to give the player a reward when they remove enchantments from items.
 */
class GrindstoneRepairEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The item that needs to be processed. Leave empty to trigger for any processed item.")
    val processedItem: Var<Item> = ConstVar(Item.Empty),
) : EventEntry

enum class GrindstoneRepairContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Item::class)
    PROCESSED_ITEM(Item::class),

    @KeyType(Item::class)
    FIRST_INPUT_ITEM(Item::class),

    @KeyType(Item::class)
    SECOND_INPUT_ITEM(Item::class),

    @KeyType(Boolean::class)
    ENCHANTMENTS_REMOVED(Boolean::class),

    @KeyType(Int::class)
    EXPERIENCE_GAINED(Int::class),
}

@EntryListener(GrindstoneRepairEventEntry::class)
fun onGrindstoneRepair(event: InventoryClickEvent, query: Query<GrindstoneRepairEventEntry>) {
    // Only trigger for grindstone
    if (event.inventory.type != InventoryType.GRINDSTONE) return
    
    // Get player
    val player = event.whoClicked as? Player ?: return
    
    // Check if clicking on the result slot (slot 2 in grindstone)
    if (event.rawSlot != 2) return
    
    // Get the result item
    val resultItem = event.currentItem ?: return
    if (resultItem.type.isAir) return
    
    // Get input items
    val firstInput = event.inventory.getItem(0)
    val secondInput = event.inventory.getItem(1)
    
    // Check if enchantments were removed
    val enchantmentsRemoved = (firstInput?.enchantments?.isNotEmpty() == true || 
                              secondInput?.enchantments?.isNotEmpty() == true) && 
                             resultItem.enchantments.isEmpty()
    
    // Calculate experience gained from removed enchantments
    val experienceGained = if (enchantmentsRemoved) {
        val firstInputExp = firstInput?.enchantments?.values?.sum() ?: 0
        val secondInputExp = secondInput?.enchantments?.values?.sum() ?: 0
        (firstInputExp + secondInputExp) / 2 // Simplified calculation
    } else 0
    
    // Find matching entries and trigger them
    query.findWhere { entry ->
        val requiredItem = entry.processedItem.get(player)
        // If no specific item is required, trigger for all processed items
        requiredItem == Item.Empty || requiredItem.isSameAs(player, resultItem, context())
    }.triggerAllFor(player) {
        GrindstoneRepairContextKeys.PROCESSED_ITEM += resultItem
        GrindstoneRepairContextKeys.FIRST_INPUT_ITEM += (firstInput ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        GrindstoneRepairContextKeys.SECOND_INPUT_ITEM += (secondInput ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        GrindstoneRepairContextKeys.ENCHANTMENTS_REMOVED += enchantmentsRemoved
        GrindstoneRepairContextKeys.EXPERIENCE_GAINED += experienceGained
    }
}

