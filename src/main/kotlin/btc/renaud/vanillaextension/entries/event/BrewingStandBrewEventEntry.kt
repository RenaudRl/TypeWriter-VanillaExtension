package btc.renaud.vanillaextension.entries.event

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
import org.bukkit.entity.Player
import org.bukkit.event.inventory.BrewEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("brewing_stand_brew_event", "Triggered when a brewing process completes in a brewing stand", Colors.YELLOW, "mdi:flask")
@ContextKeys(BrewingStandBrewContextKeys::class)
/**
 * The `Brewing Stand Brew Event` is triggered when a brewing process completes in a brewing stand.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to brew a certain potion,
 * or to give the player a reward when they brew specific potions or use certain ingredients.
 */
class BrewingStandBrewEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The potion that needs to be brewed. Leave empty to trigger for any brewed potion.")
    val brewedPotion: Var<Item> = ConstVar(Item.Empty),
    
    @Help("The minimum number of potions that need to be brewed at once. Set to 1 to trigger for any amount.")
    val minimumPotionsCount: Var<Int> = ConstVar(1),
) : EventEntry

enum class BrewingStandBrewContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Item::class)
    BREWED_POTION_1(Item::class),

    @KeyType(Item::class)
    BREWED_POTION_2(Item::class),

    @KeyType(Item::class)
    BREWED_POTION_3(Item::class),

    @KeyType(Item::class)
    INGREDIENT_USED(Item::class),

    @KeyType(Int::class)
    POTIONS_BREWED_COUNT(Int::class),
}

@EntryListener(BrewingStandBrewEventEntry::class)
fun onBrewingStandBrew(event: BrewEvent, query: Query<BrewingStandBrewEventEntry>) {
    // Get the brewing stand inventory (contains items before brewing)
    val inventory = event.contents
    
    // Get the ingredient used (top slot)
    val ingredient = inventory.ingredient
    
    // Get the brewed results (final potions after brewing)
    val results = event.results
    
    // Get the brewed potions (up to 3 results)
    val potion1 = results.getOrNull(0)
    val potion2 = results.getOrNull(1)
    val potion3 = results.getOrNull(2)
    
    // Count how many potions were actually brewed
    val potionsBrewedCount = results.count { !it.type.isAir }
    
    // Get the brewing stand location
    val brewingStandLocation = event.block.location
    
    // Find nearby players (brewing stands don't have a direct player reference)
    val nearbyPlayers = brewingStandLocation.world?.players?.filter { 
        it.location.distanceSquared(brewingStandLocation) <= 100.0 
    } ?: emptyList()
    
    // Trigger for each nearby player
    nearbyPlayers.forEach { player ->
        query.findWhere { entry ->
            val requiredPotion = entry.brewedPotion.get(player)
            val minimumCount = entry.minimumPotionsCount.get(player)
            
            if (requiredPotion == Item.Empty) {
                // If no specific potion is required, check if we have enough potions brewed (any type)
                potionsBrewedCount >= minimumCount
            } else {
                // Count how many of the brewed potions match the required potion
                val matchingPotionsCount = results.count { potion ->
                    !potion.type.isAir && requiredPotion.isSameAs(player, potion, context())
                }
                // Check if we have enough matching potions
                matchingPotionsCount >= minimumCount
            }
        }.triggerAllFor(player) {
            BrewingStandBrewContextKeys.BREWED_POTION_1 += (potion1 ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
            BrewingStandBrewContextKeys.BREWED_POTION_2 += (potion2 ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
            BrewingStandBrewContextKeys.BREWED_POTION_3 += (potion3 ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
            BrewingStandBrewContextKeys.INGREDIENT_USED += (ingredient ?: org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR))
            BrewingStandBrewContextKeys.POTIONS_BREWED_COUNT += potionsBrewedCount
        }
    }
}

