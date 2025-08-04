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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.block.data.Levelled
import java.util.*

@Entry("cauldron_objective", "An objective to interact with cauldrons", Colors.BLUE_VIOLET, "mdi:pot")
class CauldronObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The type of interaction: 'fill', 'empty', or 'any'")
    val interactionType: Var<String> = ConstVar("any"),
    @Help("The total number of cauldron interactions the player needs to perform.")
    override val amount: Var<Int> = ConstVar(5),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return CauldronObjectiveDisplay(ref())
    }
}

private class CauldronObjectiveDisplay(ref: Ref<CauldronObjectiveEntry>) :
    BaseCountObjectiveDisplay<CauldronObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onCauldronInteract(event: PlayerInteractEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        if (!filter(player)) return

        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        
        val clickedBlock = event.clickedBlock ?: return
        val cauldronTypes = setOf(
            Material.CAULDRON, Material.WATER_CAULDRON, 
            Material.LAVA_CAULDRON, Material.POWDER_SNOW_CAULDRON
        )
        
        if (clickedBlock.type !in cauldronTypes) return
        
        val item = player.inventory.itemInMainHand
        val requiredType = entry.interactionType.get(player).lowercase()
        
        // Check if player is filling or emptying
        when {
            item.type == Material.WATER_BUCKET && clickedBlock.type == Material.CAULDRON -> {
                // Filling with water
                if (requiredType == "any" || requiredType == "fill") {
                    incrementCount(player, 1)
                }
            }
            item.type == Material.LAVA_BUCKET && clickedBlock.type == Material.CAULDRON -> {
                // Filling with lava
                if (requiredType == "any" || requiredType == "fill") {
                    incrementCount(player, 1)
                }
            }
            item.type == Material.BUCKET && clickedBlock.type != Material.CAULDRON -> {
                // Emptying cauldron
                if (requiredType == "any" || requiredType == "empty") {
                    incrementCount(player, 1)
                }
            }
            item.type == Material.GLASS_BOTTLE -> {
                // Filling bottle from cauldron (emptying cauldron)
                if (requiredType == "any" || requiredType == "empty") {
                    incrementCount(player, 1)
                }
            }
            item.type == Material.POTION -> {
                // Pouring potion into cauldron (filling)
                if (requiredType == "any" || requiredType == "fill") {
                    incrementCount(player, 1)
                }
            }
        }
    }
}
