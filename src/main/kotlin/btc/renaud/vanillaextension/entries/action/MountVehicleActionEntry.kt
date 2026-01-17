package btc.renaud.vanillaextension.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.entries.ActionTrigger

@Entry("mount_vehicle_action", "Spawn and mount a vehicle", Colors.RED, icon = "mdi:car")
class MountVehicleActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val type: String = "HORSE", // EntityType name
) : ActionEntry {
    override fun ActionTrigger.execute() {
        try {
            val entityType = EntityType.valueOf(type.uppercase())
            val vehicle = player.world.spawnEntity(player.location, entityType)
            vehicle.addPassenger(player)
        } catch (e: IllegalArgumentException) {
            // Invalid entity type
        }
    }
}
