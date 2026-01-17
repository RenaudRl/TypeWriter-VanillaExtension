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

@Entry("jukebox_play_event", "Triggered when a player starts playing a record in a jukebox", Colors.YELLOW, "mdi:music-box")
@ContextKeys(JukeboxPlayContextKeys::class)
/**
 * The `Jukebox Play Event` is triggered when a player starts playing a record in a jukebox.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to play a specific music disc,
 * or to give the player a reward when they play music for entertainment.
 */
class JukeboxPlayEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class JukeboxPlayContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    DISC_TYPE(String::class),
}

@EntryListener(JukeboxPlayEventEntry::class)
fun onJukeboxPlay(event: PlayerInteractEvent, query: Query<JukeboxPlayEventEntry>) {
    // Only trigger for right-click on blocks
    if (event.action != Action.RIGHT_CLICK_BLOCK) return
    
    val clickedBlock = event.clickedBlock ?: return
    val player = event.player
    
    // Check if it's a jukebox
    if (clickedBlock.type.name != "JUKEBOX") return
    
    // Check if jukebox is empty and player has a music disc
    val jukeboxState = clickedBlock.state as? org.bukkit.block.Jukebox ?: return
    if (jukeboxState.record.type != org.bukkit.Material.AIR) return // Jukebox already has a record
    
    val itemInHand = event.item ?: return
    
    // Check if item is a music disc
    if (!itemInHand.type.name.contains("MUSIC_DISC")) return
    
    val discType = itemInHand.type.name
    
    // Find matching entries and trigger them
    query.findWhere { true } // Trigger for all disc plays
        .triggerAllFor(player) {
            JukeboxPlayContextKeys.DISC_TYPE += discType
        }
}

