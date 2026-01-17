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
import org.bukkit.event.block.BlockIgniteEvent
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "block_ignite_event",
    "Triggers when a player ignites a block",
    Colors.YELLOW,
    icon = "mdi:fire"
)
class BlockIgniteEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(BlockIgniteEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onBlockIgnite(event: BlockIgniteEvent, query: Query<BlockIgniteEventEntry>) {
    val player = event.ignitingEntity as? Player ?: return
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
