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
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEntityEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("item_frame_rotate_event", "Triggered when a player rotates an item in an item frame", Colors.YELLOW, "mdi:rotate-right")
@ContextKeys(ItemFrameRotateContextKeys::class)
/**
 * The `Item Frame Rotate Event` is triggered when a player rotates an item in an item frame.
 * 
 * ## How could this be used?
 * This could be used to track item frame interactions,
 * complete quests involving item frame rotation, or trigger events when players rotate items in frames.
 */
class ItemFrameRotateEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class ItemFrameRotateContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    ITEM_FRAME_LOCATION(Position::class),

    @KeyType(Int::class)
    ROTATION(Int::class),

    @KeyType(String::class)
    ITEM_TYPE(String::class),
}

@EntryListener(ItemFrameRotateEventEntry::class)
fun onItemFrameRotate(event: PlayerInteractEntityEvent, query: Query<ItemFrameRotateEventEntry>) {
    // Only trigger for item frame interactions
    val itemFrame = event.rightClicked as? ItemFrame ?: return
    val player = event.player
    
    // Check if the item frame has an item
    val item = itemFrame.item
    if (item.type.isAir) return
    
    val itemFrameLocation = itemFrame.location
    val rotation = itemFrame.rotation.ordinal
    val itemType = item.type.name
    
    query.findWhere { true } // No specific conditions for rotating items in frames
        .triggerAllFor(player) {
            ItemFrameRotateContextKeys.ITEM_FRAME_LOCATION += itemFrameLocation.toPosition()
            ItemFrameRotateContextKeys.ROTATION += rotation
            ItemFrameRotateContextKeys.ITEM_TYPE += itemType
        }
}

