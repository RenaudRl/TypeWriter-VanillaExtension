package btc.renaud.vanillaextension.entries

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
import org.bukkit.event.player.PlayerBucketEmptyEvent
import kotlin.reflect.KClass

@Entry("cauldron_fill_event", "Triggered when a cauldron is filled", Colors.YELLOW, "mdi:pot")
@ContextKeys(CauldronFillContextKeys::class)
/**
 * The `Cauldron Fill Event` is triggered when a cauldron is filled with water or other liquids.
 * 
 * ## How could this be used?
 * This could be used to track cauldron usage,
 * complete quests involving cauldron filling, or trigger events when players fill cauldrons.
 */
class CauldronFillEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class CauldronFillContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    CAULDRON_LOCATION(Position::class),

    @KeyType(String::class)
    LIQUID_TYPE(String::class),
}

@EntryListener(CauldronFillEventEntry::class)
fun onCauldronFill(event: PlayerBucketEmptyEvent, query: Query<CauldronFillEventEntry>) {
    val player = event.player
    val clickedBlock = event.blockClicked
    
    // Check if the bucket is being emptied into a cauldron
    if (clickedBlock.type != Material.CAULDRON && clickedBlock.type != Material.WATER_CAULDRON) return
    
    val cauldronLocation = clickedBlock.location
    val liquidType = when (event.bucket) {
        Material.WATER_BUCKET -> "WATER"
        Material.LAVA_BUCKET -> "LAVA"
        else -> "UNKNOWN"
    }
    
    query.findWhere { true } // No specific conditions for filling cauldrons
        .triggerAllFor(player) {
            CauldronFillContextKeys.CAULDRON_LOCATION += cauldronLocation.toPosition()
            CauldronFillContextKeys.LIQUID_TYPE += liquidType
        }
}
