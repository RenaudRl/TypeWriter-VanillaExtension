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
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.meta.CrossbowMeta
import kotlin.reflect.KClass

@Entry("crossbow_reload_event", "Triggered when a player reloads a crossbow", Colors.YELLOW, "mdi:bow-arrow")
@ContextKeys(CrossbowReloadContextKeys::class)
/**
 * The `Crossbow Reload Event` is triggered when a player reloads a crossbow.
 * 
 * ## How could this be used?
 * This could be used to track crossbow usage,
 * complete quests involving crossbow reloading, or trigger events when players prepare their crossbows.
 */
class CrossbowReloadEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class CrossbowReloadContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    RELOAD_LOCATION(Position::class),

    @KeyType(String::class)
    PROJECTILE_TYPE(String::class),

    @KeyType(Int::class)
    PROJECTILE_COUNT(Int::class),
}

@EntryListener(CrossbowReloadEventEntry::class)
fun onCrossbowReload(event: PlayerItemHeldEvent, query: Query<CrossbowReloadEventEntry>) {
    val player = event.player
    val newItem = player.inventory.getItem(event.newSlot) ?: return
    
    // Check if the new item is a loaded crossbow
    if (newItem.type != Material.CROSSBOW) return
    
    val crossbowMeta = newItem.itemMeta as? CrossbowMeta ?: return
    if (!crossbowMeta.hasChargedProjectiles()) return
    
    val reloadLocation = player.location
    val projectiles = crossbowMeta.chargedProjectiles
    val projectileType = if (projectiles.isNotEmpty()) projectiles[0].type.name else "ARROW"
    val projectileCount = projectiles.size
    
    query.findWhere { true } // No specific conditions for reloading crossbows
        .triggerAllFor(player) {
            CrossbowReloadContextKeys.RELOAD_LOCATION += reloadLocation.toPosition()
            CrossbowReloadContextKeys.PROJECTILE_TYPE += projectileType
            CrossbowReloadContextKeys.PROJECTILE_COUNT += projectileCount
        }
}
