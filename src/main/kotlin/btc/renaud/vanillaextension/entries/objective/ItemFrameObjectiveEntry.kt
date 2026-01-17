package btc.renaud.vanillaextension.entries.objective

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
import org.bukkit.entity.Player
import org.bukkit.entity.ItemFrame
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("item_frame_objective", "An objective to interact with item frames", Colors.BLUE_VIOLET, "mdi:image-frame")
class ItemFrameObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The type of interaction: 'place', 'remove', 'rotate', or 'any'")
    val interactionType: Var<String> = ConstVar("any"),
    @Help("The total number of item frame interactions the player needs to perform.")
    override val amount: Var<Int> = ConstVar(5),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return ItemFrameObjectiveDisplay(ref())
    }
}

private class ItemFrameObjectiveDisplay(ref: Ref<ItemFrameObjectiveEntry>) :
    BaseCountObjectiveDisplay<ItemFrameObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemFramePlace(event: HangingPlaceEvent) {
        val player = event.player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        if (event.entity !is ItemFrame) return
        
        val requiredType = entry.interactionType.get(player).lowercase()
        if (requiredType != "any" && requiredType != "place") return
        
        incrementCount(player, 1)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemFrameRemove(event: HangingBreakByEntityEvent) {
        val player = event.remover as? Player ?: return
        val entry = ref.get() ?: return
        if (!filter(player)) return

        if (event.entity !is ItemFrame) return
        
        val requiredType = entry.interactionType.get(player).lowercase()
        if (requiredType != "any" && requiredType != "remove") return
        
        incrementCount(player, 1)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemFrameRotate(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val itemFrame = event.rightClicked as? ItemFrame ?: return
        
        // Check if the item frame has an item (indicating rotation)
        if (itemFrame.item.type.isAir) return
        
        val requiredType = entry.interactionType.get(player).lowercase()
        if (requiredType != "any" && requiredType != "rotate") return
        
        incrementCount(player, 1)
    }
}

