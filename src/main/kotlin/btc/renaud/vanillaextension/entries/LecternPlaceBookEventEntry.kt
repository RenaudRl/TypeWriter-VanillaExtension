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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import kotlin.reflect.KClass

@Entry("lectern_place_book_event", "Triggered when a player places a book on a lectern", Colors.YELLOW, "mdi:book-open-page-variant")
@ContextKeys(LecternPlaceBookContextKeys::class)
/**
 * The `Lectern Place Book Event` is triggered when a player places a book on a lectern.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to place a specific book,
 * or to give the player a reward when they share knowledge by placing books on lecterns.
 */
class LecternPlaceBookEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class LecternPlaceBookContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    BOOK_TYPE(String::class),

    @KeyType(String::class)
    BOOK_TITLE(String::class),

    @KeyType(String::class)
    BOOK_AUTHOR(String::class),
}

@EntryListener(LecternPlaceBookEventEntry::class)
fun onLecternPlaceBook(event: PlayerInteractEvent, query: Query<LecternPlaceBookEventEntry>) {
    // Only trigger for right-click on blocks
    if (event.action != Action.RIGHT_CLICK_BLOCK) return
    
    val clickedBlock = event.clickedBlock ?: return
    val player = event.player
    
    // Check if it's a lectern
    if (clickedBlock.type.name != "LECTERN") return
    
    // Check if lectern is empty and player has a book
    val lecternState = clickedBlock.state as? org.bukkit.block.Lectern ?: return
    if (lecternState.inventory.getItem(0) != null) return // Lectern already has a book
    
    val itemInHand = event.item ?: return
    
    // Check if item is a book or written book
    if (!itemInHand.type.name.contains("BOOK")) return
    
    // Get book metadata
    val bookMeta = itemInHand.itemMeta as? org.bukkit.inventory.meta.BookMeta
    val bookTitle = bookMeta?.title ?: "Unknown"
    val bookAuthor = bookMeta?.author ?: "Unknown"
    
    // Find matching entries and trigger them
    query.findWhere { true } // Trigger for all book placements
        .triggerAllFor(player) {
            LecternPlaceBookContextKeys.BOOK_TYPE += itemInHand.type.name
            LecternPlaceBookContextKeys.BOOK_TITLE += bookTitle
            LecternPlaceBookContextKeys.BOOK_AUTHOR += bookAuthor
        }
}
