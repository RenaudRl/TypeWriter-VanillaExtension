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
import com.typewritermc.engine.paper.utils.toPosition
import com.typewritermc.core.utils.point.Position
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import kotlin.reflect.KClass

@Entry("trident_recall_event", "Triggered when a player recalls a trident", Colors.YELLOW, "mdi:boomerang")
@ContextKeys(TridentRecallContextKeys::class)
/**
 * The `Trident Recall Event` is triggered when a player recalls a trident (loyalty enchantment).
 * 
 * ## How could this be used?
 * This could be used to track trident recall mechanics,
 * complete quests involving trident loyalty, or trigger events when players use loyalty tridents.
 */
class TridentRecallEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class TridentRecallContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    RECALL_LOCATION(Position::class),

    @KeyType(String::class)
    TRIDENT_NAME(String::class),
}

@EntryListener(TridentRecallEventEntry::class)
fun onTridentRecall(event: EntityPickupItemEvent, query: Query<TridentRecallEventEntry>) {
    val player = event.entity as? Player ?: return
    val item = event.item.itemStack
    
    // Check if the picked up item is a trident
    if (item.type != Material.TRIDENT) return
    
    // Check if the trident has loyalty enchantment (indicating it was recalled)
    val loyaltyLevel = item.enchantments.entries.find { it.key.key.key == "loyalty" }?.value ?: 0
    if (loyaltyLevel == 0) return
    
    val recallLocation = player.location
    val tridentName = item.itemMeta?.displayName() ?: "Trident"
    
    query.findWhere { true } // No specific conditions for recalling tridents
        .triggerAllFor(player) {
            TridentRecallContextKeys.RECALL_LOCATION += recallLocation.toPosition()
            TridentRecallContextKeys.TRIDENT_NAME += tridentName
        }
}
