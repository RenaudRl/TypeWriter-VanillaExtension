package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.triggerAllFor
import com.typewritermc.engine.paper.utils.toPosition
import com.typewritermc.core.utils.point.Position
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("cake_slice_event", "Triggered when a player eats a slice of cake", Colors.YELLOW, "mdi:cake")
@ContextKeys(CakeSliceContextKeys::class)
/**
 * The `Cake Slice Event` is triggered when a player eats a slice of cake.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to eat cake,
 * or to trigger celebratory events when players consume cake.
 */
class CakeSliceEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class CakeSliceContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    CAKE_LOCATION(Position::class),

    @KeyType(Int::class)
    REMAINING_SLICES(Int::class),
}

@EntryListener(CakeSliceEventEntry::class)
fun onCakeSlice(event: PlayerInteractEvent, query: Query<CakeSliceEventEntry>) {
    // Only trigger for right-click on cake
    if (event.action != Action.RIGHT_CLICK_BLOCK) return
    
    val clickedBlock = event.clickedBlock ?: return
    if (clickedBlock.type != Material.CAKE) return
    
    val player = event.player
    val cakeLocation = clickedBlock.location
    
    // Calculate remaining slices (cake has 7 slices, bites property goes from 0 to 6)
    val cakeData = clickedBlock.blockData as? org.bukkit.block.data.type.Cake
    val bites = cakeData?.bites ?: 0
    val remainingSlices = 7 - bites - 1 // -1 because we're about to eat one
    
    query.findWhere { true } // No specific conditions for eating cake
        .triggerAllFor(player) {
            CakeSliceContextKeys.CAKE_LOCATION += cakeLocation.toPosition()
            CakeSliceContextKeys.REMAINING_SLICES += remainingSlices
        }
}

