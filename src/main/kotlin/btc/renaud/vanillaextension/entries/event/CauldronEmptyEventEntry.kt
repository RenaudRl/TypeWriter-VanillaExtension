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
import org.bukkit.event.player.PlayerBucketFillEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("cauldron_empty_event", "Triggered when a cauldron is emptied", Colors.YELLOW, "mdi:pot-outline")
@ContextKeys(CauldronEmptyContextKeys::class)
/**
 * The `Cauldron Empty Event` is triggered when a cauldron is emptied with a bucket.
 * 
 * ## How could this be used?
 * This could be used to track cauldron usage,
 * complete quests involving cauldron emptying, or trigger events when players empty cauldrons.
 */
class CauldronEmptyEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class CauldronEmptyContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    CAULDRON_LOCATION(Position::class),

    @KeyType(String::class)
    LIQUID_TYPE(String::class),
}

@EntryListener(CauldronEmptyEventEntry::class)
fun onCauldronEmpty(event: PlayerBucketFillEvent, query: Query<CauldronEmptyEventEntry>) {
    val player = event.player
    val clickedBlock = event.blockClicked
    
    // Check if the bucket is being filled from a cauldron
    if (clickedBlock.type != Material.WATER_CAULDRON && clickedBlock.type != Material.LAVA_CAULDRON) return
    
    val cauldronLocation = clickedBlock.location
    val liquidType = when (clickedBlock.type) {
        Material.WATER_CAULDRON -> "WATER"
        Material.LAVA_CAULDRON -> "LAVA"
        else -> "UNKNOWN"
    }
    
    query.findWhere { true } // No specific conditions for emptying cauldrons
        .triggerAllFor(player) {
            CauldronEmptyContextKeys.CAULDRON_LOCATION += cauldronLocation.toPosition()
            CauldronEmptyContextKeys.LIQUID_TYPE += liquidType
        }
}

