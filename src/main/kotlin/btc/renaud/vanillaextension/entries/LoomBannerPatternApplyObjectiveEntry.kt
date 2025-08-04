package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import com.typewritermc.engine.paper.utils.item.Item
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.entity.Player
import java.util.*

@Entry("loom_banner_pattern_apply_objective", "An objective to apply banner patterns using a loom", Colors.BLUE_VIOLET, "mdi:palette")
class LoomBannerPatternApplyObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The banner that needs to be created. Leave empty to count any banner pattern.")
    val resultBanner: Var<Item> = ConstVar(Item.Empty),
    @Help("The amount of times the player needs to apply banner patterns.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return LoomBannerPatternApplyObjectiveDisplay(ref())
    }
}

private class LoomBannerPatternApplyObjectiveDisplay(ref: Ref<LoomBannerPatternApplyObjectiveEntry>) :
    BaseCountObjectiveDisplay<LoomBannerPatternApplyObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        // Only trigger for loom
        if (event.inventory.type != InventoryType.LOOM) return

        // Check if clicking on the result slot (slot 3 in loom)
        if (event.rawSlot != 3) return

        val resultItem = event.currentItem ?: return
        if (resultItem.type.isAir) return
        
        val requiredBanner = entry.resultBanner.get(player)
        
        // If no specific banner is required, count all banner patterns
        if (requiredBanner == Item.Empty) {
            incrementCount(player, 1)
            return
        }
        
        // Check if the result banner matches the required banner
        if (requiredBanner.isSameAs(player, resultItem, context())) {
            incrementCount(player, 1)
        }
    }
}
