package btc.renaud.vanillaextension.entries.objective

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import btc.renaud.vanillaextension.entries.PlayerInputType
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.entries.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.Optional
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("player_input_objective", "An objective to perform specific player inputs", Colors.BLUE_VIOLET, "game-icons:abstract-016")
class PlayerInputObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("If set, only counts when this specific input is pressed.")
    val inputType: Optional<PlayerInputType> = Optional.empty(),
    @Help("The total number of inputs the player needs to perform.")
    override val amount: Var<Int> = ConstVar(10),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return PlayerInputObjectiveDisplay(ref())
    }
}

private class PlayerInputObjectiveDisplay(ref: Ref<PlayerInputObjectiveEntry>) :
    BaseCountObjectiveDisplay<PlayerInputObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerInput(event: PlayerInputEvent) {
        val player = event.player
        if (!filter(player)) return
        val entry = ref.get() ?: return
        val ok = entry.inputType.map { type ->
            when (type) {
                PlayerInputType.JUMP -> event.input.isJump
                PlayerInputType.SPRINT -> event.input.isSprint
                PlayerInputType.SNEAK -> event.input.isSneak
                PlayerInputType.FORWARD -> event.input.isForward
                PlayerInputType.BACKWARD -> event.input.isBackward
                PlayerInputType.LEFT -> event.input.isLeft
                PlayerInputType.RIGHT -> event.input.isRight
            }
        }.orElse(true)
        if (ok) {
            incrementCount(player, 1)
        }
    }
}

