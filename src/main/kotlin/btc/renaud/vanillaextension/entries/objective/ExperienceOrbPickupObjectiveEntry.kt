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
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.entity.Player
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("experience_orb_pickup_objective", "An objective to pick up experience orbs", Colors.BLUE_VIOLET, "mdi:star-circle")
class ExperienceOrbPickupObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The minimum experience amount per pickup to count. Set to 0 to count any amount.")
    val minimumExperience: Var<Int> = ConstVar(0),
    @Help("The total amount of experience the player needs to collect.")
    override val amount: Var<Int> = ConstVar(10),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return ExperienceOrbPickupObjectiveDisplay(ref())
    }
}

private class ExperienceOrbPickupObjectiveDisplay(ref: Ref<ExperienceOrbPickupObjectiveEntry>) :
    BaseCountObjectiveDisplay<ExperienceOrbPickupObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onExperienceOrbPickup(event: PlayerExpChangeEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val experienceGained = event.amount
        
        // Only trigger for positive experience gains
        if (experienceGained <= 0) return
        
        val minExp = entry.minimumExperience.get(player)
        
        // Check if the experience gained meets the minimum requirement
        if (experienceGained >= minExp) {
            incrementCount(player, experienceGained)
        }
    }
}

