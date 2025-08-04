package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.triggerAllFor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerRecipeDiscoverEvent
import kotlin.reflect.KClass

@Entry("recipe_unlock_event", "Triggered when a player unlocks a recipe", Colors.YELLOW, "mdi:book-plus")
@ContextKeys(RecipeUnlockContextKeys::class)
/**
 * The `Recipe Unlock Event` is triggered when a player unlocks a recipe.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to discover specific recipes,
 * or to give the player a reward when they unlock certain crafting recipes.
 */
class RecipeUnlockEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The recipe key that needs to be unlocked. Leave empty to trigger for any recipe.")
    val recipeKey: Var<String> = ConstVar(""),
) : EventEntry

enum class RecipeUnlockContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    RECIPE_KEY(String::class),

    @KeyType(String::class)
    RECIPE_NAMESPACE(String::class),
}

@EntryListener(RecipeUnlockEventEntry::class)
fun onRecipeUnlock(event: PlayerRecipeDiscoverEvent, query: Query<RecipeUnlockEventEntry>) {
    val player = event.player
    val recipe = event.recipe
    val recipeKey = recipe.key
    val recipeKeyString = recipeKey.toString()
    
    query.findWhere { entry ->
        val requiredRecipe = entry.recipeKey.get(player)
        // If no specific recipe is required, trigger for all recipe unlocks
        requiredRecipe.isEmpty() || recipeKeyString.contains(requiredRecipe)
    }.triggerAllFor(player) {
        RecipeUnlockContextKeys.RECIPE_KEY += recipeKeyString
        RecipeUnlockContextKeys.RECIPE_NAMESPACE += "minecraft" // Default namespace
    }
}
