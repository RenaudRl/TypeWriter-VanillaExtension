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
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.block.Barrel
import java.util.*

@Entry("barrel_objective", "An objective to interact with barrels", Colors.BLUE_VIOLET, "mdi:barrel")
class BarrelObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The type of interaction: 'open', 'close', or 'any'")
    val interactionType: Var<String> = ConstVar("any"),
    @Help("The total number of barrel interactions the player needs to perform.")
    override val amount: Var<Int> = ConstVar(5),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return BarrelObjectiveDisplay(ref())
    }
}

private class BarrelObjectiveDisplay(ref: Ref<BarrelObjectiveEntry>) :
    BaseCountObjectiveDisplay<BarrelObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBarrelOpen(event: InventoryOpenEvent) {
        val player = event.player as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val holder = event.inventory.holder
        if (holder !is Barrel) return
        
        val requiredType = entry.interactionType.get(player).lowercase()
        if (requiredType != "any" && requiredType != "open") return
        
        incrementCount(player, 1)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBarrelClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val holder = event.inventory.holder
        if (holder !is Barrel) return
        
        val requiredType = entry.interactionType.get(player).lowercase()
        if (requiredType != "any" && requiredType != "close") return
        
        incrementCount(player, 1)
    }
}
