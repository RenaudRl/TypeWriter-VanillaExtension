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
import org.bukkit.event.entity.EntityBreedEvent
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "entity_breed_event",
    "Triggers when a player breeds entities",
    Colors.YELLOW,
    icon = "mdi:heart-multiple"
)
class EntityBreedEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(EntityBreedEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onEntityBreed(event: EntityBreedEvent, query: Query<EntityBreedEventEntry>) {
    val player = event.breeder as? Player ?: return
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
