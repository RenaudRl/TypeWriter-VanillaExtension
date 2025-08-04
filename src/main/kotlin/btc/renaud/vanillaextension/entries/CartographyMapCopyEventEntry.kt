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

@Entry("cartography_map_copy_event", "Triggered when a player copies or scales a map using a cartography table", Colors.YELLOW, "mdi:map")
@ContextKeys(CartographyMapCopyContextKeys::class)
/**
 * The `Cartography Map Copy Event` is triggered when a player copies or scales a map using a cartography table.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to copy a certain map,
 * or to give the player a reward when they create scaled versions of maps.
 */
class CartographyMapCopyEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The map that needs to be processed. Leave empty to trigger for any map operation.")
    val processedMap: Var<Item> = ConstVar(Item.Empty),
) : EventEntry

enum class CartographyMapCopyContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Item::class)
    RESULT_MAP(Item::class),

    @KeyType(Item::class)
    ORIGINAL_MAP(Item::class),

    @KeyType(Item::class)
    MATERIAL_USED(Item::class),

    @KeyType(Boolean::class)
    IS_COPY_OPERATION(Boolean::class),

    @KeyType(Boolean::class)
    IS_SCALE_OPERATION(Boolean::class),
}

@EntryListener(CartographyMapCopyEventEntry::class)
fun onCartographyMapCopy(event: InventoryClickEvent, query: Query<CartographyMapCopyEventEntry>) {
    // Only trigger for cartography table
    if (event.inventory.type != InventoryType.CARTOGRAPHY) return
    
    // Get player
    val player = event.whoClicked as? Player ?: return
    
    // Check if clicking on the result slot (slot 2 in cartography table)
    if (event.rawSlot != 2) return
    
    // Get the result item
    val resultItem = event.currentItem ?: return
    if (resultItem.type.isAir) return
    
    // Get input items
    val originalMap = event.inventory.getItem(0)
    val materialUsed = event.inventory.getItem(1)
    
    // Determine operation type
    val isCopyOperation = materialUsed?.type?.name?.contains("PAPER") == true
    val isScaleOperation = materialUsed?.type?.name?.contains("PAPER") == true && 
                          originalMap?.type?.name?.contains("MAP") == true
    
    // Find matching entries and trigger them
    query.findWhere { entry ->
        val requiredMap = entry.processedMap.get(player)
        // If no specific map is required, trigger for all map operations
        requiredMap == Item.Empty || requiredMap.isSameAs(player, resultItem, context())
    }.triggerAllFor(player) {
        CartographyMapCopyContextKeys.RESULT_MAP += resultItem
        CartographyMapCopyContextKeys.ORIGINAL_MAP += (originalMap ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        CartographyMapCopyContextKeys.MATERIAL_USED += (materialUsed ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        CartographyMapCopyContextKeys.IS_COPY_OPERATION += isCopyOperation
        CartographyMapCopyContextKeys.IS_SCALE_OPERATION += isScaleOperation
    }
}
