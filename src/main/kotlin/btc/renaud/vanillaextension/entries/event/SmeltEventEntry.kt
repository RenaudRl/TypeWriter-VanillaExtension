package btc.renaud.vanillaextension.entries

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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceExtractEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("smelt_event", "Triggered when a player smelts an item", Colors.YELLOW, "mdi:fire")
@ContextKeys(SmeltContextKeys::class)
/**
 * The `Smelt Event` is triggered when a player smelts an item in a furnace.
 * 
 * ## How could this be used?
 * This could be used to complete quests where the player has to smelt specific items,
 * or to track smelting progress for achievements.
 */
class SmeltEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class SmeltContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    SMELTED_MATERIAL(String::class),

    @KeyType(Int::class)
    AMOUNT(Int::class),
}

@EntryListener(SmeltEventEntry::class)
fun onSmelt(event: FurnaceExtractEvent, query: Query<SmeltEventEntry>) {
    val player = event.player
    val smeltedMaterial = event.itemType.name
    val amount = event.itemAmount
    
    query.findWhere { true } // No specific conditions for smelting
        .triggerAllFor(player) {
            SmeltContextKeys.SMELTED_MATERIAL += smeltedMaterial
            SmeltContextKeys.AMOUNT += amount
        }
}

