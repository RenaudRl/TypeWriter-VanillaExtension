
package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.entries.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import java.util.*
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.block.data.Lightable
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("campfire_extinguish_objective", "An objective to extinguish campfires", Colors.BLUE_VIOLET, "mdi:fire-off")
class CampfireExtinguishObjectiveEntry(

    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The total number required to complete this objective.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return CampfireExtinguishObjectiveEntryDisplay(ref())
    }
}

private class CampfireExtinguishObjectiveEntryDisplay(ref: Ref<CampfireExtinguishObjectiveEntry>) :
    BaseCountObjectiveDisplay<CampfireExtinguishObjectiveEntry>(ref) {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onCampfireExtinguish(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        val player = event.player
        if (!filter(player)) return
        if (!block.type.name.contains("CAMPFIRE")) return
        val data = block.blockData as? Lightable ?: return
        if (!data.isLit) return
        val item = event.item ?: return
        val type = item.type.name
        if (!type.contains("WATER_BUCKET") && !type.contains("SHOVEL")) return
        incrementCount(player, 1)
    }
}

