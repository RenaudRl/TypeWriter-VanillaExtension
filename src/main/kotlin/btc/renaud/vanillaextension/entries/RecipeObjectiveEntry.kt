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
import org.bukkit.event.player.PlayerRecipeDiscoverEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*

@Entry("recipe_objective", "An objective to discover recipes", Colors.BLUE_VIOLET, "mdi:book-open")
class RecipeObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The specific recipe key to discover. Leave empty to count any recipe.")
    val recipeKey: Var<String> = ConstVar(""),
    @Help("The total number of recipes the player needs to discover.")
    override val amount: Var<Int> = ConstVar(10),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return RecipeObjectiveDisplay(ref())
    }
}

private class RecipeObjectiveDisplay(ref: Ref<RecipeObjectiveEntry>) :
    BaseCountObjectiveDisplay<RecipeObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onRecipeDiscover(event: PlayerRecipeDiscoverEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val discoveredRecipe = event.recipe
        val requiredRecipe = entry.recipeKey.get(player)
        
        // Check if specific recipe is required
        if (requiredRecipe.isNotEmpty()) {
            val recipeKey = discoveredRecipe.key.toString()
            if (!recipeKey.contains(requiredRecipe, ignoreCase = true)) return
        }
        
        incrementCount(player, 1)
    }
}
