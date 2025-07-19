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

@Entry("on_stonecutter_cut_event", "Trigger when player cuts specific item in stonecutter", Colors.YELLOW, "ph:scissors-bold")
@ContextKeys(StonecutterCutContextKeys::class)
/**
 * The `Stonecutter Cut Event` is triggered when a player cuts an item using a stonecutter.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to cut a certain item, 
 * or to give the player a reward when they cut a certain item in a stonecutter.
 */
class StonecutterCutEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The item that needs to be cut. Leave empty to trigger for any cut item.")
    val cutItem: Var<Item> = ConstVar(Item.Empty),
) : EventEntry

enum class StonecutterCutContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Item::class)
    CUT_ITEM(Item::class),

    @KeyType(Int::class)
    CUT_AMOUNT(Int::class),
}

@EntryListener(StonecutterCutEventEntry::class)
fun onStonecutterCut(event: InventoryClickEvent, query: Query<StonecutterCutEventEntry>) {
    // Only trigger for stonecutter
    if (event.inventory.type != InventoryType.STONECUTTER) return
    
    // Get player
    val player = event.whoClicked as? Player ?: return
    
    // Check if clicking on the result slot (slot 1 in stonecutter)
    if (event.rawSlot != 1) return
    
    // Get the clicked item (result)
    val clickedItem = event.currentItem ?: return
    if (clickedItem.type.isAir) return
    
    // Find matching entries and trigger them
    query.findWhere { entry ->
        val requiredItem = entry.cutItem.get(player)
        // If no specific item is required, trigger for all cut items
        requiredItem == Item.Empty || requiredItem.isSameAs(player, clickedItem, context())
    }.triggerAllFor(player) {
        StonecutterCutContextKeys.CUT_ITEM += clickedItem
        StonecutterCutContextKeys.CUT_AMOUNT += clickedItem.amount
    }
}
