package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.inventory.BeaconInventory
import java.util.*

@Entry("beacon_power_objective", "An objective to activate or modify beacon powers", Colors.BLUE_VIOLET, "mdi:lighthouse")
class BeaconPowerObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The minimum beacon level required for it to count. Set to 0 to count any beacon level.")
    val minimumBeaconLevel: Var<Int> = ConstVar(1),
    @Help("The total number of beacon powers the player needs to activate.")
    override val amount: Var<Int> = ConstVar(3),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return BeaconPowerObjectiveDisplay(ref())
    }
}

private class BeaconPowerObjectiveDisplay(ref: Ref<BeaconPowerObjectiveEntry>) :
    BaseCountObjectiveDisplay<BeaconPowerObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBeaconPower(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        // Check if it's a beacon inventory
        if (event.inventory !is BeaconInventory) return
        
        // Check if player is setting a beacon power (clicking on effect buttons)
        val clickedItem = event.currentItem ?: return
        if (clickedItem.type == Material.AIR) return
        
        // Beacon power items are typically emerald, diamond, gold ingot, or iron ingot
        val validPowerItems = setOf(
            Material.EMERALD, Material.DIAMOND, 
            Material.GOLD_INGOT, Material.IRON_INGOT,
            Material.NETHERITE_INGOT
        )
        
        if (clickedItem.type in validPowerItems) {
            val minLevel = entry.minimumBeaconLevel.get(player)
            
            // For simplicity, we'll count any beacon power activation
            // In a real implementation, you might want to check the actual beacon level
            if (minLevel <= 4) { // Max beacon level is 4
                incrementCount(player, 1)
            }
        }
    }
}
