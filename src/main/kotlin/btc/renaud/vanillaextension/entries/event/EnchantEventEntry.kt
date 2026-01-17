package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.triggerAllFor
import org.bukkit.entity.Player
import org.bukkit.event.enchantment.EnchantItemEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("enchant_event", "Triggered when a player enchants an item", Colors.PURPLE, "mdi:auto-fix")
@ContextKeys(EnchantContextKeys::class)
/**
 * The `Enchant Event` is triggered when a player enchants an item using an enchanting table.
 * 
 * ## How could this be used?
 * This could be used to complete quests where the player has to enchant items,
 * or to track enchanting progress for achievements.
 */
class EnchantEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class EnchantContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    ENCHANTED_ITEM(String::class),

    @KeyType(Int::class)
    EXPERIENCE_COST(Int::class),

    @KeyType(String::class)
    ENCHANTMENTS(String::class),
}

@EntryListener(EnchantEventEntry::class)
fun onEnchant(event: EnchantItemEvent, query: Query<EnchantEventEntry>) {
    val player = event.enchanter
    val enchantedItem = event.item.type.name
    val experienceCost = event.expLevelCost
    val enchantments = event.enchantsToAdd.map { "${it.key.key}:${it.value}" }.joinToString(", ")
    
    query.findWhere { true } // No specific conditions for enchanting
        .triggerAllFor(player) {
            EnchantContextKeys.ENCHANTED_ITEM += enchantedItem
            EnchantContextKeys.EXPERIENCE_COST += experienceCost
            EnchantContextKeys.ENCHANTMENTS += enchantments
        }
}

