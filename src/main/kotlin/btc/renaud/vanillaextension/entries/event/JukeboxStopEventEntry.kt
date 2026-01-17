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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("jukebox_stop_event", "Triggered when a player stops a record in a jukebox", Colors.YELLOW, "mdi:stop")
@ContextKeys(JukeboxStopContextKeys::class)
/**
 * The `Jukebox Stop Event` is triggered when a player stops a record in a jukebox.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to stop music,
 * or to trigger events when certain records are stopped playing.
 */
class JukeboxStopEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class JukeboxStopContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    RECORD_TYPE(String::class),
}

@EntryListener(JukeboxStopEventEntry::class)
fun onJukeboxStop(event: PlayerInteractEvent, query: Query<JukeboxStopEventEntry>) {
    // Only trigger for right-click on jukebox
    if (event.action != Action.RIGHT_CLICK_BLOCK) return
    
    val clickedBlock = event.clickedBlock ?: return
    if (clickedBlock.type.name != "JUKEBOX") return
    
    val player = event.player
    
    // Check if jukebox has a record (we're stopping it)
    val jukebox = clickedBlock.state as? org.bukkit.block.Jukebox ?: return
    val record = jukebox.record
    
    // Only trigger if there's a record to stop
    if (record.type.isAir) return
    
    query.findWhere { true } // No specific conditions for stopping records
        .triggerAllFor(player) {
            JukeboxStopContextKeys.RECORD_TYPE += record.type.name
        }
}

