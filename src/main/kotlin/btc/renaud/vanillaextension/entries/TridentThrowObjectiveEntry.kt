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
import org.bukkit.entity.Player
import org.bukkit.entity.Trident
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*

@Entry("trident_throw_objective", "An objective to throw tridents", Colors.BLUE_VIOLET, "mdi:spear")
class TridentThrowObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The minimum velocity required for the throw to count. Set to 0 to count any throw.")
    val minimumVelocity: Var<Double> = ConstVar(0.0),
    @Help("The total number of tridents the player needs to throw.")
    override val amount: Var<Int> = ConstVar(5),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return TridentThrowObjectiveDisplay(ref())
    }
}

private class TridentThrowObjectiveDisplay(ref: Ref<TridentThrowObjectiveEntry>) :
    BaseCountObjectiveDisplay<TridentThrowObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onTridentThrow(event: ProjectileLaunchEvent) {
        val trident = event.entity as? Trident ?: return
        val player = trident.shooter as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val throwVelocity = trident.velocity.length()
        val minVelocity = entry.minimumVelocity.get(player)
        
        // Check if the throw velocity meets the minimum requirement
        if (throwVelocity >= minVelocity) {
            incrementCount(player, 1)
        }
    }
}
