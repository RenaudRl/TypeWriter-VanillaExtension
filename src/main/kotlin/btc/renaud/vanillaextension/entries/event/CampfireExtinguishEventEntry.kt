package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.triggerAllFor
import com.typewritermc.engine.paper.utils.toPosition
import com.typewritermc.core.utils.point.Position
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("campfire_extinguish_event", "Triggered when a player extinguishes a campfire", Colors.YELLOW, "mdi:fire-off")
@ContextKeys(CampfireExtinguishContextKeys::class)
/**
 * The `Campfire Extinguish Event` is triggered when a player extinguishes a campfire.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to extinguish campfires,
 * or to give the player a reward when they put out fires for safety.
 */
class CampfireExtinguishEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class CampfireExtinguishContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    CAMPFIRE_TYPE(String::class),

    @KeyType(String::class)
    EXTINGUISH_METHOD(String::class),

    @KeyType(Position::class)
    CAMPFIRE_LOCATION(Position::class),
}

@EntryListener(CampfireExtinguishEventEntry::class)
fun onCampfireExtinguish(event: PlayerInteractEvent, query: Query<CampfireExtinguishEventEntry>) {
    // Only trigger for right-click on blocks
    if (event.action != Action.RIGHT_CLICK_BLOCK) return
    
    val clickedBlock = event.clickedBlock ?: return
    val player = event.player
    
    // Check if it's a lit campfire
    if (!clickedBlock.type.name.contains("CAMPFIRE")) return
    
    val blockData = clickedBlock.blockData
    if (blockData !is org.bukkit.block.data.Lightable || !blockData.isLit) return
    
    // Check if player is using water bucket or shovel to extinguish
    val itemInHand = event.item
    val extinguishMethod = when {
        itemInHand?.type?.name?.contains("WATER_BUCKET") == true -> "WATER_BUCKET"
        itemInHand?.type?.name?.contains("SHOVEL") == true -> "SHOVEL"
        else -> "UNKNOWN"
    }
    
    // Only proceed if it's a valid extinguish method
    if (extinguishMethod == "UNKNOWN") return
    
    val campfireType = clickedBlock.type.name
    
    // Find matching entries and trigger them
    query.findWhere { true } // No specific conditions for campfire extinguishing
        .triggerAllFor(player) {
            CampfireExtinguishContextKeys.CAMPFIRE_TYPE += campfireType
            CampfireExtinguishContextKeys.EXTINGUISH_METHOD += extinguishMethod
            CampfireExtinguishContextKeys.CAMPFIRE_LOCATION += clickedBlock.location.toPosition()
        }
}

