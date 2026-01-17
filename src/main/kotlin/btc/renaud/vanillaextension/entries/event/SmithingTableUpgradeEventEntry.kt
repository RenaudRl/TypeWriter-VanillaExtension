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
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("smithing_table_upgrade_event", "Triggered when a player upgrades items using a smithing table", Colors.YELLOW, "mdi:hammer")
@ContextKeys(SmithingTableUpgradeContextKeys::class)
/**
 * The `Smithing Table Upgrade Event` is triggered when a player upgrades items using a smithing table.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to upgrade a certain item,
 * or to give the player a reward when they upgrade items using netherite or other materials.
 */
class SmithingTableUpgradeEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The item that needs to be upgraded. Leave empty to trigger for any upgraded item.")
    val upgradedItem: Var<Item> = ConstVar(Item.Empty),
) : EventEntry

enum class SmithingTableUpgradeContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Item::class)
    UPGRADED_ITEM(Item::class),

    @KeyType(Item::class)
    BASE_ITEM(Item::class),

    @KeyType(Item::class)
    UPGRADE_MATERIAL(Item::class),

    @KeyType(Item::class)
    TEMPLATE_ITEM(Item::class),
}

@EntryListener(SmithingTableUpgradeEventEntry::class)
fun onSmithingTableUpgrade(event: InventoryClickEvent, query: Query<SmithingTableUpgradeEventEntry>) {
    // Only trigger for smithing table
    if (event.inventory.type != InventoryType.SMITHING) return
    
    // Get player
    val player = event.whoClicked as? Player ?: return
    
    // Check if clicking on the result slot (slot 3 in smithing table)
    if (event.rawSlot != 3) return
    
    // Get the result item
    val resultItem = event.currentItem ?: return
    if (resultItem.type.isAir) return
    
    // Get input items (template, base, addition)
    val templateItem = event.inventory.getItem(0)
    val baseItem = event.inventory.getItem(1)
    val upgradeItem = event.inventory.getItem(2)
    
    // Find matching entries and trigger them
    query.findWhere { entry ->
        val requiredItem = entry.upgradedItem.get(player)
        // If no specific item is required, trigger for all upgraded items
        requiredItem == Item.Empty || requiredItem.isSameAs(player, resultItem, context())
    }.triggerAllFor(player) {
        SmithingTableUpgradeContextKeys.UPGRADED_ITEM += resultItem
        SmithingTableUpgradeContextKeys.BASE_ITEM += (baseItem ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        SmithingTableUpgradeContextKeys.UPGRADE_MATERIAL += (upgradeItem ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        SmithingTableUpgradeContextKeys.TEMPLATE_ITEM += (templateItem ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
    }
}

