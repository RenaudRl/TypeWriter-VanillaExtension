package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import org.bukkit.event.entity.PlayerLeashEntityEvent
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "player_leash_entity_event",
    "Triggers when a player leashes an entity",
    Colors.YELLOW,
    icon = "mdi:lasso"
)
class PlayerLeashEntityEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(PlayerLeashEntityEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onPlayerLeashEntity(event: PlayerLeashEntityEvent, query: Query<PlayerLeashEntityEventEntry>) {
    val player = event.player
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
