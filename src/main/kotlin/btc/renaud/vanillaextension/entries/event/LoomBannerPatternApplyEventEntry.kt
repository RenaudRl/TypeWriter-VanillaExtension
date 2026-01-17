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

@Entry("loom_banner_pattern_apply_event", "Triggered when a player applies a banner pattern using a loom", Colors.YELLOW, "mdi:palette")
@ContextKeys(LoomBannerPatternApplyContextKeys::class)
/**
 * The `Loom Banner Pattern Apply Event` is triggered when a player applies a banner pattern using a loom.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to create a specific banner pattern,
 * or to give the player a reward when they create banners with certain patterns.
 */
class LoomBannerPatternApplyEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The banner that needs to be created. Leave empty to trigger for any banner pattern.")
    val resultBanner: Var<Item> = ConstVar(Item.Empty),
) : EventEntry

enum class LoomBannerPatternApplyContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Item::class)
    RESULT_BANNER(Item::class),

    @KeyType(Item::class)
    BASE_BANNER(Item::class),

    @KeyType(Item::class)
    DYE_ITEM(Item::class),

    @KeyType(Item::class)
    PATTERN_ITEM(Item::class),
}

@EntryListener(LoomBannerPatternApplyEventEntry::class)
fun onLoomBannerPatternApply(event: InventoryClickEvent, query: Query<LoomBannerPatternApplyEventEntry>) {
    // Only trigger for loom
    if (event.inventory.type != InventoryType.LOOM) return
    
    // Get player
    val player = event.whoClicked as? Player ?: return
    
    // Check if clicking on the result slot (slot 3 in loom)
    if (event.rawSlot != 3) return
    
    // Get the result item
    val resultItem = event.currentItem ?: return
    if (resultItem.type.isAir) return
    
    // Get input items (banner, dye, pattern)
    val baseBanner = event.inventory.getItem(0)
    val dyeItem = event.inventory.getItem(1)
    val patternItem = event.inventory.getItem(2)
    
    // Find matching entries and trigger them
    query.findWhere { entry ->
        val requiredBanner = entry.resultBanner.get(player)
        // If no specific banner is required, trigger for all banner patterns
        requiredBanner == Item.Empty || requiredBanner.isSameAs(player, resultItem, context())
    }.triggerAllFor(player) {
        LoomBannerPatternApplyContextKeys.RESULT_BANNER += resultItem
        LoomBannerPatternApplyContextKeys.BASE_BANNER += (baseBanner ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        LoomBannerPatternApplyContextKeys.DYE_ITEM += (dyeItem ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
        LoomBannerPatternApplyContextKeys.PATTERN_ITEM += (patternItem ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
    }
}

