package btcrenaud.vanillaextension.entries.objective

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
import btcrenaud.vanillaextension.BaseCountObjectiveEntry
import btcrenaud.vanillaextension.BaseCountObjectiveDisplay
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Piglin
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.plugin

@Entry("piglin_barter_objective", "An objective to barter with piglins", Colors.BLUE_VIOLET, "mdi:pig")
class PiglinBarterObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The specific item type to receive from bartering. Leave empty to count any item.")
    val itemType: Var<String> = ConstVar(""),
    @Help("The total number of barter trades the player needs to complete.")
    override val amount: Var<Int> = ConstVar(5),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return PiglinBarterObjectiveDisplay(ref())
    }
}

private class PiglinBarterObjectiveDisplay(ref: Ref<PiglinBarterObjectiveEntry>) :
    BaseCountObjectiveDisplay<PiglinBarterObjectiveEntry>(ref) {

    private val scheduler by lazy {
        org.bukkit.Bukkit.getGlobalRegionScheduler()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPiglinBarter(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val item = event.item.itemStack
        val plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("Typewriter") as? org.bukkit.plugin.java.JavaPlugin ?: return

        scheduler.run(plugin) { _ ->
            val nearbyPiglins = player.location.world.getNearbyEntities(player.location, 10.0, 10.0, 10.0)
                .filterIsInstance<Piglin>()

            if (nearbyPiglins.isEmpty()) return@run

            val commonBarterItems = setOf(
                Material.ENDER_PEARL, Material.STRING, Material.QUARTZ,
                Material.OBSIDIAN, Material.CRYING_OBSIDIAN, Material.FIRE_CHARGE,
                Material.LEATHER, Material.SOUL_SAND, Material.NETHER_BRICK,
                Material.SPECTRAL_ARROW, Material.GRAVEL, Material.BLACKSTONE
            )

            val requiredItem = entry.itemType.get(player)
            if (requiredItem.isNotEmpty()) {
                val materialName = item.type.name.lowercase()
                if (!materialName.contains(requiredItem.lowercase())) return@run
            } else {
                if (item.type !in commonBarterItems) return@run
            }

            incrementCount(player, 1)
        }
    }
}
