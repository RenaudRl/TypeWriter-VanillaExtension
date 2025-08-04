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
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*

@Entry("campfire_cook_objective", "An objective to cook food on campfires", Colors.BLUE_VIOLET, "mdi:fire")
class CampfireCookObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The specific food type to cook. Leave empty to count any food.")
    val foodType: Var<String> = ConstVar(""),
    @Help("The total number of food items the player needs to cook on campfires.")
    override val amount: Var<Int> = ConstVar(10),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return CampfireCookObjectiveDisplay(ref())
    }
}

private class CampfireCookObjectiveDisplay(ref: Ref<CampfireCookObjectiveEntry>) :
    BaseCountObjectiveDisplay<CampfireCookObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onCampfireCook(event: BlockCookEvent) {
        val entry = ref.get() ?: return
        
        // Check if it's a campfire or soul campfire
        val blockType = event.block.type
        if (blockType != Material.CAMPFIRE && blockType != Material.SOUL_CAMPFIRE) return
        
        // Find the nearest player (within reasonable distance)
        val location = event.block.location
        val nearbyPlayers = location.world?.getNearbyEntities(location, 10.0, 10.0, 10.0)
            ?.filterIsInstance<Player>() ?: return
        
        if (nearbyPlayers.isEmpty()) return
        
        // Use the closest player
        val player = nearbyPlayers.minByOrNull { it.location.distance(location) } ?: return
        if (!filter(player)) return
        
        val cookedItem = event.result
        val requiredFood = entry.foodType.get(player)
        
        // Check if specific food type is required
        if (requiredFood.isNotEmpty()) {
            val materialName = cookedItem.type.name.lowercase()
            if (!materialName.contains(requiredFood.lowercase())) return
        }
        
        incrementCount(player, cookedItem.amount)
    }
}
