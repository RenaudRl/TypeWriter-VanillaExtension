package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import org.bukkit.event.player.PlayerItemBreakEvent
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "player_item_break_event",
    "Triggers when a player's item breaks",
    Colors.RED,
    icon = "mdi:hammer-wrench"
)
class PlayerItemBreakEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(PlayerItemBreakEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onPlayerItemBreak(event: PlayerItemBreakEvent, query: Query<PlayerItemBreakEventEntry>) {
    val player = event.player
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
