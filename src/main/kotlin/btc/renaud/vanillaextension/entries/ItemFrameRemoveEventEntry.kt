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
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import kotlin.reflect.KClass

@Entry("item_frame_remove_event", "Triggered when a player removes an item frame", Colors.YELLOW, "mdi:image-off")
@ContextKeys(ItemFrameRemoveContextKeys::class)
/**
 * The `Item Frame Remove Event` is triggered when a player removes an item frame.
 * 
 * ## How could this be used?
 * This could be used to track decoration removal,
 * complete quests involving item frame removal, or trigger events when players remove item frames.
 */
class ItemFrameRemoveEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class ItemFrameRemoveContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    ITEM_FRAME_LOCATION(Position::class),

    @KeyType(String::class)
    FACING_DIRECTION(String::class),
}

@EntryListener(ItemFrameRemoveEventEntry::class)
fun onItemFrameRemove(event: HangingBreakByEntityEvent, query: Query<ItemFrameRemoveEventEntry>) {
    // Only trigger for item frame removal by players
    val itemFrame = event.entity as? ItemFrame ?: return
    val player = event.remover as? Player ?: return
    
    val itemFrameLocation = itemFrame.location
    val facingDirection = itemFrame.facing.name
    
    query.findWhere { true } // No specific conditions for removing item frames
        .triggerAllFor(player) {
            ItemFrameRemoveContextKeys.ITEM_FRAME_LOCATION += itemFrameLocation.toPosition()
            ItemFrameRemoveContextKeys.FACING_DIRECTION += facingDirection
        }
}
