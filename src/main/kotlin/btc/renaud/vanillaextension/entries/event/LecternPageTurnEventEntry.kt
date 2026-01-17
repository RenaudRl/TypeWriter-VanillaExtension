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
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("lectern_page_turn_event", "Triggered when a player turns a page on a lectern", Colors.YELLOW, "mdi:book-open-page-variant")
@ContextKeys(LecternPageTurnContextKeys::class)
/**
 * The `Lectern Page Turn Event` is triggered when a player turns a page on a lectern.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to read through a book,
 * or to trigger story events when certain pages are reached.
 */
class LecternPageTurnEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class LecternPageTurnContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Int::class)
    PAGE_NUMBER(Int::class),
}

@EntryListener(LecternPageTurnEventEntry::class)
fun onLecternPageTurn(event: PlayerTakeLecternBookEvent, query: Query<LecternPageTurnEventEntry>) {
    val player = event.player
    
    query.findWhere { true } // Trigger for all page turns
        .triggerAllFor(player) {
            LecternPageTurnContextKeys.PAGE_NUMBER += 1 // Simple page tracking
        }
}

