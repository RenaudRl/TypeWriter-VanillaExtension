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
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import kotlin.reflect.KClass

@Entry("lectern_take_book_event", "Triggered when a player takes a book from a lectern", Colors.YELLOW, "mdi:book-remove")
@ContextKeys(LecternTakeBookContextKeys::class)
/**
 * The `Lectern Take Book Event` is triggered when a player takes a book from a lectern.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to retrieve a specific book,
 * or to trigger story events when certain books are taken from lecterns.
 */
class LecternTakeBookEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class LecternTakeBookContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    BOOK_TYPE(String::class),
}

@EntryListener(LecternTakeBookEventEntry::class)
fun onLecternTakeBook(event: PlayerTakeLecternBookEvent, query: Query<LecternTakeBookEventEntry>) {
    val player = event.player
    val book = event.book ?: return
    
    query.findWhere { true } // Trigger for all book taking operations
        .triggerAllFor(player) {
            LecternTakeBookContextKeys.BOOK_TYPE += book.type.name
        }
}
