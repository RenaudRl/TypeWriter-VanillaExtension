package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*

@Entry("jukebox_stop_objective", "An objective to stop music in jukeboxes", Colors.BLUE_VIOLET, "mdi:stop")
class JukeboxStopObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The total number of times the player needs to stop music in jukeboxes.")
    override val amount: Var<Int> = ConstVar(3),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return JukeboxStopObjectiveDisplay(ref())
    }
}

private class JukeboxStopObjectiveDisplay(ref: Ref<JukeboxStopObjectiveEntry>) :
    BaseCountObjectiveDisplay<JukeboxStopObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJukeboxStop(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        
        val clickedBlock = event.clickedBlock ?: return
        val player = event.player
        if (!filter(player)) return
        
        // Check if it's a jukebox
        if (clickedBlock.type.name != "JUKEBOX") return
        
        // Check if jukebox has a record (we're stopping it)
        val jukebox = clickedBlock.state as? org.bukkit.block.Jukebox ?: return
        val record = jukebox.record
        
        // Only trigger if there's a record to stop
        if (record.type == Material.AIR) return
        
        incrementCount(player, 1)
    }
}
