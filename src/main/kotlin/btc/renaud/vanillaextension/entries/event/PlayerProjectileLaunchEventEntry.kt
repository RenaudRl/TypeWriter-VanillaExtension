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
import org.bukkit.event.entity.ProjectileLaunchEvent
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "player_projectile_launch_event",
    "Triggers when a player launches a projectile",
    Colors.YELLOW,
    icon = "mdi:bow-arrow"
)
class PlayerProjectileLaunchEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(PlayerProjectileLaunchEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onPlayerProjectileLaunch(event: ProjectileLaunchEvent, query: Query<PlayerProjectileLaunchEventEntry>) {
    val player = event.entity.shooter as? Player ?: return
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
