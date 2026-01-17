package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import org.bukkit.event.entity.SheepDyeWoolEvent
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "sheep_dye_wool_event",
    "Triggers when a player dyes a sheep's wool",
    Colors.YELLOW,
    icon = "mdi:palette"
)
class SheepDyeWoolEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(SheepDyeWoolEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onSheepDyeWool(event: SheepDyeWoolEvent, query: Query<SheepDyeWoolEventEntry>) {
    val player = event.player ?: return
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
