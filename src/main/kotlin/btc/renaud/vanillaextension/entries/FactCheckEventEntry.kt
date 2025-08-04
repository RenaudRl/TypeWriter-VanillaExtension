package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.triggerAllFor
import org.bukkit.entity.Player
import io.papermc.paper.event.player.AsyncChatEvent
import kotlin.reflect.KClass

@Entry("fact_check_event", "Triggered when a player sends a message that can be fact-checked", Colors.YELLOW, "mdi:fact-check")
@ContextKeys(FactCheckContextKeys::class)
/**
 * The `Fact Check Event` is triggered when a player sends a message that can be fact-checked.
 * 
 * ## How could this be used?
 * This could be used to verify information shared by players, trigger educational content,
 * or provide corrections to misinformation in chat.
 */
class FactCheckEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("Keywords that trigger fact-checking. Leave empty to check all messages.")
    val keywords: Var<String> = ConstVar(""),
) : EventEntry

enum class FactCheckContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    MESSAGE(String::class),

    @KeyType(String::class)
    PLAYER_NAME(String::class),
}

@EntryListener(FactCheckEventEntry::class)
fun onFactCheck(event: AsyncChatEvent, query: Query<FactCheckEventEntry>) {
    val player = event.player
    val message = event.message().toString()

    query.findWhere { entry ->
        val keywords = entry.keywords.get(player)
        if (keywords.isEmpty()) {
            true // Check all messages if no keywords specified
        } else {
            // Check if message contains any of the keywords
            keywords.split(",").any { keyword ->
                message.contains(keyword.trim(), ignoreCase = true)
            }
        }
    }.triggerAllFor(player) {
        FactCheckContextKeys.MESSAGE += message
        FactCheckContextKeys.PLAYER_NAME += player.name
    }
}
