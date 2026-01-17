
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
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.block.Barrel
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("barrel_close_objective", "An objective to close barrels", Colors.BLUE_VIOLET, "mdi:barrel")
class BarrelCloseObjectiveEntry(

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
        return BarrelCloseObjectiveEntryDisplay(ref())
    }
}

private class BarrelCloseObjectiveEntryDisplay(ref: Ref<BarrelCloseObjectiveEntry>) :
    BaseCountObjectiveDisplay<BarrelCloseObjectiveEntry>(ref) {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onBarrelClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        if (!filter(player)) return
        val holder = event.inventory.holder
        if (holder !is Barrel) return
        incrementCount(player, 1)
    }
}

