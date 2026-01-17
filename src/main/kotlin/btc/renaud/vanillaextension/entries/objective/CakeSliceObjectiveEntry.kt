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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("cake_slice_objective", "An objective to eat cake slices", Colors.BLUE_VIOLET, "mdi:cake")
class CakeSliceObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The minimum remaining slices for the cake to count. Set to 0 to count any cake slice.")
    val minimumRemainingSlices: Var<Int> = ConstVar(0),
    @Help("The total number of cake slices the player needs to eat.")
    override val amount: Var<Int> = ConstVar(7),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return CakeSliceObjectiveDisplay(ref())
    }
}

private class CakeSliceObjectiveDisplay(ref: Ref<CakeSliceObjectiveEntry>) :
    BaseCountObjectiveDisplay<CakeSliceObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onCakeSlice(event: PlayerInteractEvent) {
        // Only trigger for right-click on cake
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.type != Material.CAKE) return
        
        val player = event.player
        val entry = ref.get() ?: return
        if (!filter(player)) return
        
        // Calculate remaining slices (cake has 7 slices, bites property goes from 0 to 6)
        val cakeData = clickedBlock.blockData as? org.bukkit.block.data.type.Cake
        val bites = cakeData?.bites ?: 0
        val remainingSlices = 7 - bites - 1 // -1 because we're about to eat one
        
        val minRemaining = entry.minimumRemainingSlices.get(player)
        
        // Check if the remaining slices meet the minimum requirement
        if (remainingSlices >= minRemaining) {
            incrementCount(player, 1)
        }
    }
}

