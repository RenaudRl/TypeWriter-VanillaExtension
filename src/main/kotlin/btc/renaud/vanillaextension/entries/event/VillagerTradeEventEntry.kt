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
import org.bukkit.entity.Villager
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("villager_trade_event", "Triggered when a player trades with a villager", Colors.YELLOW, "mdi:account-cash")
@ContextKeys(VillagerTradeContextKeys::class)
/**
 * The `Villager Trade Event` is triggered when a player trades with a villager.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to trade with villagers,
 * or to give the player a reward when they trade specific items or with certain villager professions.
 */
class VillagerTradeEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The item that needs to be received from the trade. Leave empty to trigger for any trade.")
    val tradedItem: Var<Item> = ConstVar(Item.Empty),
) : EventEntry

enum class VillagerTradeContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Item::class)
    TRADED_ITEM(Item::class),

    @KeyType(Item::class)
    FIRST_INGREDIENT(Item::class),

    @KeyType(Item::class)
    SECOND_INGREDIENT(Item::class),

    @KeyType(String::class)
    VILLAGER_PROFESSION(String::class),

    @KeyType(Int::class)
    VILLAGER_LEVEL(Int::class),
}

@EntryListener(VillagerTradeEventEntry::class)
fun onVillagerTrade(event: InventoryClickEvent, query: Query<VillagerTradeEventEntry>) {
    // Only trigger for merchant inventory (villager trading)
    if (event.inventory.type != InventoryType.MERCHANT) return
    
    val player = event.whoClicked as? Player ?: return
    
    // Check if clicking on the result slot (slot 2 in merchant inventory)
    if (event.rawSlot != 2) return
    
    // Get the result item
    val resultItem = event.currentItem ?: return
    if (resultItem.type.isAir) return
    
    // Get the ingredients
    val firstIngredient = event.inventory.getItem(0)
    val secondIngredient = event.inventory.getItem(1)
    
    // Try to find the villager entity directly from the merchant inventory
    val villager = event.inventory.holder as? Villager
    
    val villagerProfession = villager?.profession?.toString() ?: "UNKNOWN"
    val villagerLevel = villager?.villagerLevel ?: 0
    
    // Find matching entries and trigger them
    query.findWhere { entry ->
        val requiredItem = entry.tradedItem.get(player)
        // If no specific item is required, trigger for all trades
        requiredItem == Item.Empty || requiredItem.isSameAs(player, resultItem, context())
    }.triggerAllFor(player) {
        VillagerTradeContextKeys.TRADED_ITEM += resultItem
        VillagerTradeContextKeys.FIRST_INGREDIENT += (firstIngredient ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        VillagerTradeContextKeys.SECOND_INGREDIENT += (secondIngredient ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        VillagerTradeContextKeys.VILLAGER_PROFESSION += villagerProfession
        VillagerTradeContextKeys.VILLAGER_LEVEL += villagerLevel
    }
}

