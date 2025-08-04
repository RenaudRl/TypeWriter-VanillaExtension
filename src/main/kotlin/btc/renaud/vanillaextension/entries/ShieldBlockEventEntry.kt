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
import org.bukkit.event.entity.EntityDamageByEntityEvent
import kotlin.reflect.KClass

@Entry("shield_block_event", "Triggered when a player blocks damage with a shield", Colors.YELLOW, "mdi:shield")
@ContextKeys(ShieldBlockContextKeys::class)
/**
 * The `Shield Block Event` is triggered when a player successfully blocks damage using a shield.
 * 
 * ## How could this be used?
 * This could be used to track defensive combat actions,
 * complete quests involving shield usage, or trigger events when players defend themselves.
 */
class ShieldBlockEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class ShieldBlockContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    BLOCK_LOCATION(Position::class),

    @KeyType(Double::class)
    DAMAGE_BLOCKED(Double::class),

    @KeyType(String::class)
    ATTACKER_TYPE(String::class),
}

@EntryListener(ShieldBlockEventEntry::class)
fun onShieldBlock(event: EntityDamageByEntityEvent, query: Query<ShieldBlockEventEntry>) {
    val player = event.entity as? Player ?: return
    
    // Check if the player is holding a shield and blocking
    val mainHand = player.inventory.itemInMainHand
    val offHand = player.inventory.itemInOffHand
    
    val hasShield = mainHand.type == Material.SHIELD || offHand.type == Material.SHIELD
    if (!hasShield || !player.isBlocking) return
    
    val blockLocation = player.location
    val damageBlocked = event.damage
    val attackerType = event.damager.type.name
    
    query.findWhere { true } // No specific conditions for blocking with shield
        .triggerAllFor(player) {
            ShieldBlockContextKeys.BLOCK_LOCATION += blockLocation.toPosition()
            ShieldBlockContextKeys.DAMAGE_BLOCKED += damageBlocked
            ShieldBlockContextKeys.ATTACKER_TYPE += attackerType
        }
}
