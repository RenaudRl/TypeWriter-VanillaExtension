package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import org.bukkit.event.player.PlayerTeleportEvent
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "player_teleport_event",
    "Triggers when a player teleports",
    Colors.YELLOW,
    icon = "mdi:transition-masked"
)
class PlayerTeleportEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(PlayerTeleportEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onPlayerTeleport(event: PlayerTeleportEvent, query: Query<PlayerTeleportEventEntry>) {
    val player = event.player
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
