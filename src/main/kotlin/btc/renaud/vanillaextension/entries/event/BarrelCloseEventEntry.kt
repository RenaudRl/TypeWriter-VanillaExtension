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
import com.typewritermc.engine.paper.utils.toPosition
import com.typewritermc.core.utils.point.Position
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("barrel_close_event", "Triggered when a player closes a barrel", Colors.YELLOW, "mdi:barrel-outline")
@ContextKeys(BarrelCloseContextKeys::class)
/**
 * The `Barrel Close Event` is triggered when a player closes a barrel.
 * 
 * ## How could this be used?
 * This could be used to track storage access completion,
 * complete quests involving barrel interaction, or trigger events when players finish accessing barrels.
 */
class BarrelCloseEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class BarrelCloseContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    BARREL_LOCATION(Position::class),
}

@EntryListener(BarrelCloseEventEntry::class)
fun onBarrelClose(event: InventoryCloseEvent, query: Query<BarrelCloseEventEntry>) {
    val player = event.player as? Player ?: return
    val inventory = event.inventory
    val location = inventory.location ?: return
    
    // Check if the closed inventory is a barrel
    val block = location.block
    if (block.type != Material.BARREL) return
    
    query.findWhere { true } // No specific conditions for closing barrels
        .triggerAllFor(player) {
            BarrelCloseContextKeys.BARREL_LOCATION += location.toPosition()
        }
}

