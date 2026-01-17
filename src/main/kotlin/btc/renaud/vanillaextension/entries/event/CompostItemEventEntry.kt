package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import io.papermc.paper.event.entity.EntityCompostItemEvent
import org.bukkit.entity.Player
import com.typewritermc.loader.ListenerPriority

@Entry(
    "compost_item_event",
    "Triggers when a player composts an item",
    Colors.YELLOW,
    icon = "mdi:recycle"
)
class CompostItemEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(CompostItemEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onCompostItem(event: EntityCompostItemEvent, query: Query<CompostItemEventEntry>) {
    val player = event.entity as? Player ?: return
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
