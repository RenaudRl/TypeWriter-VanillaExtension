package btc.renaud.vanillaextension.entries

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
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.inventory.meta.CrossbowMeta
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("crossbow_reload_objective", "An objective to reload crossbows", Colors.BLUE_VIOLET, "mdi:bow-arrow")
class CrossbowReloadObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The minimum number of projectiles that must be loaded for it to count. Set to 0 to count any reload.")
    val minimumProjectiles: Var<Int> = ConstVar(1),
    @Help("The total number of crossbows the player needs to reload.")
    override val amount: Var<Int> = ConstVar(5),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return CrossbowReloadObjectiveDisplay(ref())
    }
}

private class CrossbowReloadObjectiveDisplay(ref: Ref<CrossbowReloadObjectiveEntry>) :
    BaseCountObjectiveDisplay<CrossbowReloadObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onCrossbowReload(event: PlayerItemHeldEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val newItem = player.inventory.getItem(event.newSlot) ?: return
        
        // Check if the new item is a loaded crossbow
        if (newItem.type != Material.CROSSBOW) return
        
        val crossbowMeta = newItem.itemMeta as? CrossbowMeta ?: return
        if (!crossbowMeta.hasChargedProjectiles()) return
        
        val projectileCount = crossbowMeta.chargedProjectiles.size
        val minProjectiles = entry.minimumProjectiles.get(player)
        
        // Check if the projectile count meets the minimum requirement
        if (projectileCount >= minProjectiles) {
            incrementCount(player, 1)
        }
    }
}

