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
import org.bukkit.event.hanging.HangingPlaceEvent
import kotlin.reflect.KClass

@Entry("item_frame_place_event", "Triggered when a player places an item frame", Colors.YELLOW, "mdi:image-frame")
@ContextKeys(ItemFramePlaceContextKeys::class)
/**
 * The `Item Frame Place Event` is triggered when a player places an item frame.
 * 
 * ## How could this be used?
 * This could be used to track decoration placement,
 * complete quests involving item frame placement, or trigger events when players place item frames.
 */
class ItemFramePlaceEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class ItemFramePlaceContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    ITEM_FRAME_LOCATION(Position::class),

    @KeyType(String::class)
    FACING_DIRECTION(String::class),
}

@EntryListener(ItemFramePlaceEventEntry::class)
fun onItemFramePlace(event: HangingPlaceEvent, query: Query<ItemFramePlaceEventEntry>) {
    // Only trigger for item frame placement
    val itemFrame = event.entity as? ItemFrame ?: return
    val player = event.player ?: return
    
    val itemFrameLocation = itemFrame.location
    val facingDirection = itemFrame.facing.name
    
    query.findWhere { true } // No specific conditions for placing item frames
        .triggerAllFor(player) {
            ItemFramePlaceContextKeys.ITEM_FRAME_LOCATION += itemFrameLocation.toPosition()
            ItemFramePlaceContextKeys.FACING_DIRECTION += facingDirection
        }
}
