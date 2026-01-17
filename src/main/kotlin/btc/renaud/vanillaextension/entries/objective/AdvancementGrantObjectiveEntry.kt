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
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("advancement_grant_objective", "An objective to complete advancements", Colors.BLUE_VIOLET, "mdi:trophy")
class AdvancementGrantObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The advancement key that needs to be completed. Leave empty to count any advancement.")
    val advancementKey: Var<String> = ConstVar(""),
    @Help("The amount of advancements the player needs to complete.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return AdvancementGrantObjectiveDisplay(ref())
    }
}

private class AdvancementGrantObjectiveDisplay(ref: Ref<AdvancementGrantObjectiveEntry>) :
    BaseCountObjectiveDisplay<AdvancementGrantObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onAdvancementGrant(event: PlayerAdvancementDoneEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val advancement = event.advancement
        val advancementKey = advancement.key.toString()
        
        val requiredKey = entry.advancementKey.get(player)
        
        // If no specific advancement is required, count all advancements
        if (requiredKey.isEmpty()) {
            incrementCount(player, 1)
            return
        }
        
        // Check if the advancement key matches the required key
        if (advancementKey.contains(requiredKey)) {
            incrementCount(player, 1)
        }
    }
}

