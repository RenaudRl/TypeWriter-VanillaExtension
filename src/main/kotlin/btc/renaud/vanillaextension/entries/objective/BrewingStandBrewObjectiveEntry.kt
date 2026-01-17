package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.entries.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import com.typewritermc.engine.paper.utils.item.Item
import org.bukkit.entity.Player
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("brewing_stand_brew_objective", "An objective to brew potions using a brewing stand", Colors.BLUE_VIOLET, "mdi:flask")
class BrewingStandBrewObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The potion that needs to be brewed. Leave empty to count any brewed potion.")
    val brewedPotion: Var<Item> = ConstVar(Item.Empty),
    @Help("The minimum number of potions that need to be brewed at once. Set to 1 to count any amount.")
    val minimumPotionsCount: Var<Int> = ConstVar(1),
    @Help("The amount of times the player needs to brew potions.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return BrewingStandBrewObjectiveDisplay(ref())
    }
}

private class BrewingStandBrewObjectiveDisplay(ref: Ref<BrewingStandBrewObjectiveEntry>) :
    BaseCountObjectiveDisplay<BrewingStandBrewObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBrewingStandBrew(event: BrewEvent) {
        val entry = ref.get() ?: return
        
        val location = event.block.location
        
        // Find nearby players (within reasonable distance)
        val nearbyPlayers = location.world?.players?.filter { 
            it.location.distanceSquared(location) <= 100.0 
        } ?: emptyList()
        
        // Get the brewed results (final potions after brewing)
        val results = event.results
        
        // Get all brewed potions that are not air
        val brewedPotions = results.filter { !it.type.isAir }
        
        for (player in nearbyPlayers) {
            if (!filter(player)) continue
            
            val requiredPotion = entry.brewedPotion.get(player)
            val minimumCount = entry.minimumPotionsCount.get(player)
            
            // If no specific potion is required, count all brewing operations
            if (requiredPotion == Item.Empty) {
                // Check if we have enough potions brewed (any type)
                if (brewedPotions.size >= minimumCount) {
                    incrementCount(player, 1)
                }
                continue
            }
            
            // Count how many of the brewed potions match the required potion
            val matchingPotionsCount = brewedPotions.count { potion ->
                requiredPotion.isSameAs(player, potion, context())
            }
            
            // Check if we have enough matching potions
            if (matchingPotionsCount >= minimumCount) {
                incrementCount(player, 1)
            }
        }
    }
}

