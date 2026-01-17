package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import org.bukkit.event.raid.RaidFinishEvent
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "raid_finish_event",
    "Triggers when a raid is finished",
    Colors.YELLOW,
    icon = "mdi:flag-checkered"
)
class RaidFinishEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(RaidFinishEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onRaidFinish(event: RaidFinishEvent, query: Query<RaidFinishEventEntry>) {
    val entries = query.findWhere { true }.toList()
    event.winners.forEach { player ->
        entries.startDialogueWithOrNextDialogue(player) { }
    }
}
