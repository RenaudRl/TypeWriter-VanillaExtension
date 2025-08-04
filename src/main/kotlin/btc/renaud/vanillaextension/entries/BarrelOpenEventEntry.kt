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
import org.bukkit.event.inventory.InventoryOpenEvent
import kotlin.reflect.KClass

@Entry("barrel_open_event", "Triggered when a player opens a barrel", Colors.YELLOW, "mdi:barrel")
@ContextKeys(BarrelOpenContextKeys::class)
/**
 * The `Barrel Open Event` is triggered when a player opens a barrel.
 * 
 * ## How could this be used?
 * This could be used to track storage access,
 * complete quests involving barrel interaction, or trigger events when players access specific barrels.
 */
class BarrelOpenEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class BarrelOpenContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    BARREL_LOCATION(Position::class),
}

@EntryListener(BarrelOpenEventEntry::class)
fun onBarrelOpen(event: InventoryOpenEvent, query: Query<BarrelOpenEventEntry>) {
    val player = event.player as? Player ?: return
    val inventory = event.inventory
    val location = inventory.location ?: return
    
    // Check if the opened inventory is a barrel
    val block = location.block
    if (block.type != Material.BARREL) return
    
    query.findWhere { true } // No specific conditions for opening barrels
        .triggerAllFor(player) {
            BarrelOpenContextKeys.BARREL_LOCATION += location.toPosition()
        }
}
