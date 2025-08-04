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
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.triggerAllFor
import com.typewritermc.engine.paper.utils.item.Item
import com.typewritermc.engine.paper.utils.toPosition
import com.typewritermc.core.utils.point.Position
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockCookEvent
import kotlin.reflect.KClass

@Entry("campfire_cook_event", "Triggered when food is cooked on a campfire", Colors.YELLOW, "mdi:fire")
@ContextKeys(CampfireCookContextKeys::class)
/**
 * The `Campfire Cook Event` is triggered when food is cooked on a campfire.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to cook a certain food item,
 * or to give the player a reward when they cook food on campfires.
 */
class CampfireCookEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The food that needs to be cooked. Leave empty to trigger for any cooked food.")
    val cookedFood: Var<Item> = ConstVar(Item.Empty),
) : EventEntry

enum class CampfireCookContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Item::class)
    COOKED_FOOD(Item::class),

    @KeyType(Item::class)
    RAW_FOOD(Item::class),

    @KeyType(Position::class)
    CAMPFIRE_LOCATION(Position::class),
}

@EntryListener(CampfireCookEventEntry::class)
fun onCampfireCook(event: BlockCookEvent, query: Query<CampfireCookEventEntry>) {
    // Only trigger for campfire cooking
    if (!event.block.type.name.contains("CAMPFIRE")) return
    
    val rawFood = event.source
    val cookedFood = event.result
    
    // Find nearby players (campfires don't have a direct player reference)
    val campfireLocation = event.block.location
    val nearbyPlayers = campfireLocation.world?.getNearbyEntities(campfireLocation, 10.0, 10.0, 10.0)
        ?.filterIsInstance<Player>() ?: emptyList()
    
    // Trigger for each nearby player
    nearbyPlayers.forEach { player ->
        query.findWhere { entry ->
            val requiredFood = entry.cookedFood.get(player)
            // If no specific food is required, trigger for all cooking operations
            requiredFood == Item.Empty || requiredFood.isSameAs(player, cookedFood, context())
        }.triggerAllFor(player) {
            CampfireCookContextKeys.COOKED_FOOD += cookedFood
            CampfireCookContextKeys.RAW_FOOD += rawFood
            CampfireCookContextKeys.CAMPFIRE_LOCATION += campfireLocation.toPosition()
        }
    }
}
