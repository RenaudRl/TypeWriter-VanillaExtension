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
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*

@Entry("shield_block_objective", "An objective to block damage with a shield", Colors.BLUE_VIOLET, "mdi:shield")
class ShieldBlockObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The minimum damage that must be blocked for it to count. Set to 0 to count any block.")
    val minimumDamage: Var<Double> = ConstVar(0.0),
    @Help("The total number of attacks the player needs to block.")
    override val amount: Var<Int> = ConstVar(10),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return ShieldBlockObjectiveDisplay(ref())
    }
}

private class ShieldBlockObjectiveDisplay(ref: Ref<ShieldBlockObjectiveEntry>) :
    BaseCountObjectiveDisplay<ShieldBlockObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onShieldBlock(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        // Check if the player is holding a shield and blocking
        val mainHand = player.inventory.itemInMainHand
        val offHand = player.inventory.itemInOffHand
        
        val hasShield = mainHand.type == Material.SHIELD || offHand.type == Material.SHIELD
        if (!hasShield || !player.isBlocking) return
        
        val damageBlocked = event.damage
        val minDamage = entry.minimumDamage.get(player)
        
        // Check if the damage blocked meets the minimum requirement
        if (damageBlocked >= minDamage) {
            incrementCount(player, 1)
        }
    }
}
