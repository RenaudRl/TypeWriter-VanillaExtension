
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
import java.util.*
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("lectern_take_book_objective", "An objective to take books from lecterns", Colors.BLUE_VIOLET, "mdi:book-remove")
class LecternTakeBookObjectiveEntry(

    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The total number required to complete this objective.")
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return LecternTakeBookObjectiveEntryDisplay(ref())
    }
}

private class LecternTakeBookObjectiveEntryDisplay(ref: Ref<LecternTakeBookObjectiveEntry>) :
    BaseCountObjectiveDisplay<LecternTakeBookObjectiveEntry>(ref) {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onLecternTakeBook(event: PlayerTakeLecternBookEvent) {
        val player = event.player
        if (!filter(player)) return
        if (event.book == null) return
        incrementCount(player, 1)
    }
}

