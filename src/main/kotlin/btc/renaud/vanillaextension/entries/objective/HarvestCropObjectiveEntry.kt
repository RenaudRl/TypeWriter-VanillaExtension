package btc.renaud.vanillaextension.entries.objective

import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
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
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerHarvestBlockEvent
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("harvest_crop_objective", "Harvest crop objective", Colors.BLUE_VIOLET, "mdi:barley")
class HarvestCropObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
    @Help("Type of crop to harvest.")
    val cropType: Material? = null,
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return HarvestCropObjectiveDisplay(ref())
    }
}

private class HarvestCropObjectiveDisplay(ref: Ref<HarvestCropObjectiveEntry>) :
    BaseCountObjectiveDisplay<HarvestCropObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerHarvestBlock(event: PlayerHarvestBlockEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        
        if (entry.cropType != null && event.harvestedBlock.type != entry.cropType) return

        if (!filter(player)) return
        incrementCount(player)
    }
}
