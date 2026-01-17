package btc.renaud.vanillaextension.entries.objective

import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.entries.QuestEntry
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.vehicle.VehicleDestroyEvent
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("destroy_vehicle_objective", "Destroy vehicle objective", Colors.BLUE_VIOLET, "mdi:car-off")
class DestroyVehicleObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
    @Help("Type of vehicle to destroy. If null, any vehicle counts.")
    val vehicleType: EntityType? = null,
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return DestroyVehicleObjectiveDisplay(ref())
    }
}

private class DestroyVehicleObjectiveDisplay(ref: Ref<DestroyVehicleObjectiveEntry>) :
    BaseCountObjectiveDisplay<DestroyVehicleObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onVehicleDestroy(event: VehicleDestroyEvent) {
        val player = event.attacker as? Player ?: return
        val entry = ref.get() ?: return
        
        if (entry.vehicleType != null && event.vehicle.type != entry.vehicleType) return

        if (!filter(player)) return
        incrementCount(player)
    }
}
