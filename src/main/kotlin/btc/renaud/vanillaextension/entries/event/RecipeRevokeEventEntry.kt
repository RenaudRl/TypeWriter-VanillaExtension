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
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("recipe_revoke_event", "Triggered when a player's recipe is revoked", Colors.YELLOW, "mdi:book-minus")
@ContextKeys(RecipeRevokeContextKeys::class)
/**
 * The `Recipe Revoke Event` is triggered when a player's recipe is revoked.
 * 
 * ## How could this be used?
 * This could be used to track when players lose access to recipes,
 * or to trigger events when certain recipes are removed from a player's knowledge.
 * Note: This is a simulated event as Bukkit doesn't have a direct recipe revoke event.
 */
class RecipeRevokeEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The recipe key that was revoked. Leave empty to trigger for any recipe.")
    val revokedRecipeKey: Var<String> = ConstVar(""),
) : EventEntry

enum class RecipeRevokeContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    REVOKED_RECIPE_KEY(String::class),

    @KeyType(String::class)
    RECIPE_NAMESPACE(String::class),
}

@EntryListener(RecipeRevokeEventEntry::class)
fun onRecipeRevoke(event: PlayerInteractEvent, query: Query<RecipeRevokeEventEntry>) {
    // This is a placeholder implementation since Bukkit doesn't have a direct recipe revoke event
    // In a real implementation, you would need to track recipe removals manually
    // or use a custom event system
    
    val player = event.player
    
    // This would be triggered manually when recipes are revoked
    // For now, we'll just provide the structure
    query.findWhere { entry ->
        val requiredRecipe = entry.revokedRecipeKey.get(player)
        // If no specific recipe is required, trigger for all recipe revokes
        requiredRecipe.isEmpty()
    }.triggerAllFor(player) {
        RecipeRevokeContextKeys.REVOKED_RECIPE_KEY += "example:recipe"
        RecipeRevokeContextKeys.RECIPE_NAMESPACE += "minecraft"
    }
}

