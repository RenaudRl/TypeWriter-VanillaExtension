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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import org.bukkit.event.block.Action
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("lectern_objective", "An objective to interact with lecterns", Colors.BLUE_VIOLET, "mdi:book-open-page-variant")
class LecternObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The type of interaction: 'place', 'take', 'turn', or 'any'")
    val interactionType: Var<String> = ConstVar("any"),
    @Help("The total number of lectern interactions the player needs to perform.")
    override val amount: Var<Int> = ConstVar(5),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return LecternObjectiveDisplay(ref())
    }
}

private class LecternObjectiveDisplay(ref: Ref<LecternObjectiveEntry>) :
    BaseCountObjectiveDisplay<LecternObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLecternInteract(event: PlayerInteractEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.type != Material.LECTERN) return
        
        val requiredType = entry.interactionType.get(player).lowercase()
        
        when (event.action) {
            Action.RIGHT_CLICK_BLOCK -> {
                val item = player.inventory.itemInMainHand
                if (item.type == Material.WRITABLE_BOOK || item.type == Material.WRITTEN_BOOK) {
                    // Player is placing a book
                    if (requiredType == "any" || requiredType == "place") {
                        incrementCount(player, 1)
                    }
                } else {
                    // Player is turning pages
                    if (requiredType == "any" || requiredType == "turn") {
                        incrementCount(player, 1)
                    }
                }
            }
            else -> {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLecternTakeBook(event: PlayerTakeLecternBookEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        if (!filter(player)) return
        
        val requiredType = entry.interactionType.get(player).lowercase()
        if (requiredType == "any" || requiredType == "take") {
            incrementCount(player, 1)
        }
    }
}

