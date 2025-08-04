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
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerExpChangeEvent
import kotlin.reflect.KClass

@Entry("experience_orb_pickup_event", "Triggered when a player picks up experience orbs", Colors.YELLOW, "mdi:star-circle")
@ContextKeys(ExperienceOrbPickupContextKeys::class)
/**
 * The `Experience Orb Pickup Event` is triggered when a player picks up experience orbs.
 * 
 * ## How could this be used?
 * This could be used to complete a quest where the player has to gain a certain amount of experience,
 * or to give the player a reward when they collect experience from specific activities.
 */
class ExperienceOrbPickupEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The minimum amount of experience that needs to be gained. Set to 0 to trigger for any amount.")
    val minimumExperience: Var<Int> = ConstVar(0),
) : EventEntry

enum class ExperienceOrbPickupContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Int::class)
    EXPERIENCE_GAINED(Int::class),

    @KeyType(Int::class)
    PLAYER_TOTAL_EXPERIENCE(Int::class),

    @KeyType(Int::class)
    PLAYER_LEVEL(Int::class),
}

@EntryListener(ExperienceOrbPickupEventEntry::class)
fun onExperienceOrbPickup(event: PlayerExpChangeEvent, query: Query<ExperienceOrbPickupEventEntry>) {
    val player = event.player
    val experienceGained = event.amount
    
    // Only trigger for positive experience gains
    if (experienceGained <= 0) return
    
    // Find matching entries and trigger them
    query.findWhere { entry ->
        val minExp = entry.minimumExperience.get(player)
        experienceGained >= minExp
    }.triggerAllFor(player) {
        ExperienceOrbPickupContextKeys.EXPERIENCE_GAINED += experienceGained
        ExperienceOrbPickupContextKeys.PLAYER_TOTAL_EXPERIENCE += player.totalExperience
        ExperienceOrbPickupContextKeys.PLAYER_LEVEL += player.level
    }
}
