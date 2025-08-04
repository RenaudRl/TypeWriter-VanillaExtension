package btc.renaud.vanillaextension.entries

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

@Entry("anvil_repair_event", "Triggered when a player repairs or combines items in an anvil", Colors.YELLOW, "mdi:anvil")
@ContextKeys(AnvilRepairContextKeys::class)
/**
 * The `Anvil Repair Event` is triggered when a player repairs or combines items using an anvil.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to repair a certain item,
 * or to give the player a reward when they repair items in an anvil.
 */
class AnvilRepairEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The item that needs to be repaired. Leave empty to trigger for any repaired item.")
    val repairedItem: Var<Item> = ConstVar(Item.Empty),
) : EventEntry

enum class AnvilRepairContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Item::class)
    REPAIRED_ITEM(Item::class),

    @KeyType(Item::class)
    FIRST_INPUT_ITEM(Item::class),

    @KeyType(Item::class)
    SECOND_INPUT_ITEM(Item::class),

    @KeyType(Int::class)
    EXPERIENCE_COST(Int::class),
}

@EntryListener(AnvilRepairEventEntry::class)
fun onAnvilRepair(event: InventoryClickEvent, query: Query<AnvilRepairEventEntry>) {
    // Only trigger for anvil
    if (event.inventory.type != InventoryType.ANVIL) return
    
    // Get player
    val player = event.whoClicked as? Player ?: return
    
    // Check if clicking on the result slot (slot 2 in anvil)
    if (event.rawSlot != 2) return
    
    // Get the result item
    val resultItem = event.currentItem ?: return
    if (resultItem.type.isAir) return
    
    // Get input items
    val firstInput = event.inventory.getItem(0)
    val secondInput = event.inventory.getItem(1)
    
    // Find matching entries and trigger them
    query.findWhere { entry ->
        val requiredItem = entry.repairedItem.get(player)
        // If no specific item is required, trigger for all repaired items
        requiredItem == Item.Empty || requiredItem.isSameAs(player, resultItem, context())
    }.triggerAllFor(player) {
        AnvilRepairContextKeys.REPAIRED_ITEM += resultItem
        AnvilRepairContextKeys.FIRST_INPUT_ITEM += (firstInput ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        AnvilRepairContextKeys.SECOND_INPUT_ITEM += (secondInput ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        AnvilRepairContextKeys.EXPERIENCE_COST += event.inventory.getItem(2)?.let { 
            // Calculate experience cost based on anvil mechanics
            // This is a simplified calculation
            val firstInputLevel = firstInput?.enchantments?.values?.sum() ?: 0
            val secondInputLevel = secondInput?.enchantments?.values?.sum() ?: 0
            firstInputLevel + secondInputLevel
        } ?: 0
    }
}
