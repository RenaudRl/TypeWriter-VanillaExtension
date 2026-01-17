package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EntityInstanceEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.triggerEntriesFor
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "on_player_visibility_detection",
    "Trigger entries when a player is detected by an entity's visibility system",
    Colors.GREEN,
    "mdi:eye-check"
)
class VisibilityDetectionEntry(
    override val id: String = "",
    override val name: String = "",
    val entity: Ref<out EntityInstanceEntry> = emptyRef(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList()
) : EventEntry

class VisibilityDetectionEvent(
    val instance: Ref<out EntityInstanceEntry>,
    val player: Player
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }
}

object VisibilityDetectionListener : Listener {
    @EventHandler
    fun onVisibilityDetection(event: VisibilityDetectionEvent) {
        Query(VisibilityDetectionEntry::class)
            .findWhere { it.entity == event.instance }
            .forEach { entry ->
                entry.triggers.triggerEntriesFor(event.player) { }
            }
    }
}
