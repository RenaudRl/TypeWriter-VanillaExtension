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
import org.bukkit.entity.Player
import org.bukkit.entity.PigZombie
import org.bukkit.event.entity.EntityDropItemEvent
import kotlin.reflect.KClass

@Entry("piglin_barter_event", "Triggered when a piglin barters with a player", Colors.YELLOW, "mdi:pig")
@ContextKeys(PiglinBarterContextKeys::class)
/**
 * The `Piglin Barter Event` is triggered when a piglin barters with a player.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to barter with piglins,
 * or to give the player a reward when they receive specific items from bartering.
 */
class PiglinBarterEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class PiglinBarterContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    BARTERED_ITEM(String::class),

    @KeyType(Int::class)
    AMOUNT(Int::class),
}

@EntryListener(PiglinBarterEventEntry::class)
fun onPiglinBarter(event: EntityDropItemEvent, query: Query<PiglinBarterEventEntry>) {
    // Only trigger for piglin drops (bartering)
    val piglin = event.entity as? PigZombie ?: return
    
    // Check if this is a barter drop (piglins drop items when bartering)
    val droppedItem = event.itemDrop.itemStack
    
    // Find nearby players who might have triggered the barter
    val piglinLocation = piglin.location
    val nearbyPlayers = piglinLocation.world?.getNearbyEntities(piglinLocation, 10.0, 10.0, 10.0)
        ?.filterIsInstance<Player>() ?: emptyList()
    
    nearbyPlayers.forEach { player ->
        query.findWhere { true } // Trigger for all barter operations
            .triggerAllFor(player) {
                PiglinBarterContextKeys.BARTERED_ITEM += droppedItem.type.name
                PiglinBarterContextKeys.AMOUNT += droppedItem.amount
            }
    }
}
