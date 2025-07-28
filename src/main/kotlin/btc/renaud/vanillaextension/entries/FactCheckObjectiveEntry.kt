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
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.snippets.snippet
import com.typewritermc.engine.paper.utils.asMini
import com.typewritermc.engine.paper.utils.asMiniWithResolvers
import com.typewritermc.quest.ObjectiveEntry
import com.typewritermc.quest.QuestEntry
import com.typewritermc.quest.inactiveObjectiveDisplay
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val displaySnippet by snippet(
    "factObjective.display",
    "<display> <dark_gray>(<gray><current><dark_gray>/<gray><required><dark_gray>)"
)

private val completedDisplaySnippet by snippet(
    "factObjective.completed",
    "<green>✔</green> <st><display></st> <dark_gray>(<gray><current><dark_gray>/<gray><required><dark_gray>)"
)


@Entry("fact_check_objective", "An objective that validates when a fact matches a specific value", Colors.BLUE_VIOLET, "ph:target-bold")
class FactCheckObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    @Help("The fact to check.")
    val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The value that the fact should match to complete the objective.")
    val expectedValue: Int = 0,
    @Help("The comparison operator to use (EQUAL, GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL).")
    val operator: ComparisonOperator = ComparisonOperator.EQUAL,
    @Help("The display text for the objective.")
    override val display: Var<String> = ConstVar(""),
    @Help("The sequence to trigger when the player completes the objective.")
    val onComplete: Ref<TriggerableEntry> = emptyRef(),
    @Help("The priority override for this objective.")
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : ObjectiveEntry {

    override suspend fun display(): AudienceFilter {
        return FactCheckObjectiveDisplay(ref())
    }

    override fun display(player: Player?): String {
        if (player == null) return inactiveObjectiveDisplay

        val factEntry = fact.get() ?: return inactiveObjectiveDisplay
        val currentValue = factEntry.readForPlayersGroup(player).value

        if (!criteria.matches(player)) {
            return inactiveObjectiveDisplay
        }

        val requiredValue = expectedValue
        val isComplete = when (operator) {
            ComparisonOperator.EQUAL -> currentValue == requiredValue
            ComparisonOperator.GREATER_THAN -> currentValue > requiredValue
            ComparisonOperator.LESS_THAN -> currentValue < requiredValue
            ComparisonOperator.GREATER_OR_EQUAL -> currentValue >= requiredValue
            ComparisonOperator.LESS_OR_EQUAL -> currentValue <= requiredValue
        }

        val text = if (isComplete) completedDisplaySnippet else displaySnippet
        val displayText = display.get(player)

        return text.asMiniWithResolvers(
            parsed("display", displayText),
            parsed("current", currentValue.toString()),
            parsed("required", requiredValue.toString())
        ).asMini().parsePlaceholders(player)
    }
}

private class FactCheckObjectiveDisplay(private val ref: Ref<FactCheckObjectiveEntry>) : AudienceFilter(ref) {

    private val completionStatus = ConcurrentHashMap<UUID, Boolean>()
    private val lastKnownValues = ConcurrentHashMap<UUID, Int>()

    override fun filter(player: Player): Boolean {
        val entry = ref.get() ?: return false
        return entry.criteria.matches(player)
    }

    override fun onPlayerAdd(player: Player) {
        val entry = ref.get() ?: return
        val fact = entry.fact.get()
        
        val isCurrentlyComplete = if (fact != null) {
            val currentValue = fact.readForPlayersGroup(player).value
            lastKnownValues[player.uniqueId] = currentValue
            
            when (entry.operator) {
                ComparisonOperator.EQUAL -> currentValue == entry.expectedValue
                ComparisonOperator.GREATER_THAN -> currentValue > entry.expectedValue
                ComparisonOperator.LESS_THAN -> currentValue < entry.expectedValue
                ComparisonOperator.GREATER_OR_EQUAL -> currentValue >= entry.expectedValue
                ComparisonOperator.LESS_OR_EQUAL -> currentValue <= entry.expectedValue
            }
        } else false

        completionStatus[player.uniqueId] = isCurrentlyComplete
        super.onPlayerAdd(player)
    }

    override fun onPlayerRemove(player: Player) {
        super.onPlayerRemove(player)
        val playerId = player.uniqueId
        completionStatus.remove(playerId)
        lastKnownValues.remove(playerId)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        
        if (!filter(player)) return
        
        checkFactAndComplete(player)
    }

    private fun checkFactAndComplete(player: Player) {
        val entry = ref.get() ?: return
        val fact = entry.fact.get() ?: return
        val playerId = player.uniqueId

        val currentValue = fact.readForPlayersGroup(player).value
        val lastKnownValue = lastKnownValues[playerId] ?: currentValue

        if (currentValue != lastKnownValue) {
            lastKnownValues[playerId] = currentValue

            val wasComplete = completionStatus[playerId] ?: false
            val isNowComplete = when (entry.operator) {
                ComparisonOperator.EQUAL -> currentValue == entry.expectedValue
                ComparisonOperator.GREATER_THAN -> currentValue > entry.expectedValue
                ComparisonOperator.LESS_THAN -> currentValue < entry.expectedValue
                ComparisonOperator.GREATER_OR_EQUAL -> currentValue >= entry.expectedValue
                ComparisonOperator.LESS_OR_EQUAL -> currentValue <= entry.expectedValue
            }

            if (isNowComplete != wasComplete) {
                completionStatus[playerId] = isNowComplete

                if (!wasComplete && isNowComplete) {
                    entry.onComplete.triggerFor(player, context())
                }
            }
        }
    }
}
