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
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.block.Beacon
import kotlin.reflect.KClass

@Entry("beacon_power_event", "Triggered when a beacon is activated or modified", Colors.YELLOW, "mdi:lighthouse")
@ContextKeys(BeaconPowerContextKeys::class)
/**
 * The `Beacon Power Event` is triggered when a beacon is activated or its power is modified.
 * 
 * ## How could this be used?
 * This could be used to track beacon construction and activation,
 * complete quests involving beacon building, or trigger events when players set up beacons.
 */
class BeaconPowerEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

enum class BeaconPowerContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    BEACON_LOCATION(Position::class),

    @KeyType(Int::class)
    BEACON_LEVEL(Int::class),

    @KeyType(String::class)
    PRIMARY_EFFECT(String::class),

    @KeyType(String::class)
    SECONDARY_EFFECT(String::class),
}

@EntryListener(BeaconPowerEventEntry::class)
fun onBeaconPower(event: InventoryCloseEvent, query: Query<BeaconPowerEventEntry>) {
    val player = event.player as? Player ?: return
    val inventory = event.inventory
    val location = inventory.location ?: return
    
    // Check if the closed inventory is a beacon
    val block = location.block
    val beacon = block.state as? Beacon ?: return
    
    val beaconLevel = beacon.tier
    val primaryEffect = beacon.primaryEffect?.toString() ?: "NONE"
    val secondaryEffect = beacon.secondaryEffect?.toString() ?: "NONE"
    
    query.findWhere { true } // No specific conditions for beacon activation
        .triggerAllFor(player) {
            BeaconPowerContextKeys.BEACON_LOCATION += location.toPosition()
            BeaconPowerContextKeys.BEACON_LEVEL += beaconLevel
            BeaconPowerContextKeys.PRIMARY_EFFECT += primaryEffect
            BeaconPowerContextKeys.SECONDARY_EFFECT += secondaryEffect
        }
}
