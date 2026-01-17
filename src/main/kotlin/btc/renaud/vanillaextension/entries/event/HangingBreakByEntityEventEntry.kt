package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import org.bukkit.entity.Player
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "hanging_break_by_player_event",
    "Triggers when a player breaks a hanging entity",
    Colors.YELLOW,
    icon = "mdi:image-broken"
)
class HangingBreakByEntityEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(HangingBreakByEntityEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onHangingBreakByEntity(event: HangingBreakByEntityEvent, query: Query<HangingBreakByEntityEventEntry>) {
    val player = event.remover as? Player ?: return
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
