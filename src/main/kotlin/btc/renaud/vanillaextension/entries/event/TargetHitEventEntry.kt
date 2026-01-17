package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import io.papermc.paper.event.block.TargetHitEvent
import org.bukkit.entity.Player
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "target_hit_event",
    "Triggers when a player hits a target block",
    Colors.YELLOW,
    icon = "mdi:target"
)
class TargetHitEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(TargetHitEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onTargetHit(event: TargetHitEvent, query: Query<TargetHitEventEntry>) {
    val player = event.hitEntity as? Player ?: return
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
