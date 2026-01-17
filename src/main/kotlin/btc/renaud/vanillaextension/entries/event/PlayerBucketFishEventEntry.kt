package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.entries.Query
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import org.bukkit.event.player.PlayerBucketEntityEvent
import org.bukkit.entity.Fish
import com.typewritermc.loader.ListenerPriority

@Entry(
    "player_bucket_fish_event",
    "Triggers when a player catches a fish with a bucket",
    Colors.BLUE,
    icon = "mdi:fish"
)
class PlayerBucketFishEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

// Use PlayerBucketEntityEvent instead of deprecated PlayerBucketFishEvent
@EntryListener(PlayerBucketFishEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onPlayerBucketFish(event: PlayerBucketEntityEvent, query: Query<PlayerBucketFishEventEntry>) {
    // Only trigger for fish entities
    if (event.entity !is Fish) return
    val player = event.player
    val entries = query.findWhere { true }.toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}
