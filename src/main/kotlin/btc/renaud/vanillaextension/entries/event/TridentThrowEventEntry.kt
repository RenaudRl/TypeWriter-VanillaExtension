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
import org.bukkit.entity.Player
import org.bukkit.entity.Trident
import org.bukkit.event.entity.ProjectileLaunchEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("trident_throw_event", "Triggered when a player throws a trident", Colors.YELLOW, "mdi:spear")
@ContextKeys(TridentThrowContextKeys::class)
/**
 * The `Trident Throw Event` is triggered when a player throws a trident.
 * 
 * ## How could this be used?
 * This could be used to track trident usage,
 * complete quests involving trident throwing, or trigger events when players use tridents.
 */
class TridentThrowEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class TridentThrowContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    THROW_LOCATION(Position::class),

    @KeyType(Double::class)
    THROW_VELOCITY(Double::class),
}

@EntryListener(TridentThrowEventEntry::class)
fun onTridentThrow(event: ProjectileLaunchEvent, query: Query<TridentThrowEventEntry>) {
    // Only trigger for trident throws
    val trident = event.entity as? Trident ?: return
    val player = trident.shooter as? Player ?: return
    
    val throwLocation = player.location
    val throwVelocity = trident.velocity.length()
    
    query.findWhere { true } // No specific conditions for throwing tridents
        .triggerAllFor(player) {
            TridentThrowContextKeys.THROW_LOCATION += throwLocation.toPosition()
            TridentThrowContextKeys.THROW_VELOCITY += throwVelocity
        }
}

