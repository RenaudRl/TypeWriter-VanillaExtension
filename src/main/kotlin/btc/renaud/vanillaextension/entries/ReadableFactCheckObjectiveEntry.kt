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
import com.typewritermc.quest.ObjectiveEntry
import btc.renaud.vanillaextension.ObjectiveDisplay
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import com.typewritermc.core.interaction.context
import kotlin.math.absoluteValue
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.asMini
import com.typewritermc.engine.paper.utils.asMiniWithResolvers
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed
import com.typewritermc.quest.inactiveObjectiveDisplay
import com.typewritermc.quest.showingObjectiveDisplay

@Entry("readable_fact_check_objective", "An objective that validates based on a readable fact value", Colors.BLUE_VIOLET, "ph:target-bold")
class ReadableFactCheckObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    @Help("The readable fact to check.")
    val fact: Ref<ReadableFactEntry> = emptyRef(),
    @Help("The comparison operator to use (EQUAL, GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL).")
    val operator: ComparisonOperator = ComparisonOperator.EQUAL,
    @Help("The required amount to complete the objective.")
    val amount: Var<Int> = ConstVar(0),
    override val display: Var<String> = ConstVar(""),
    @Help("The sequence to trigger when the player completes the objective.")
    val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : ObjectiveEntry {
    
    // Logique de completion personnalisée pour les opérateurs de comparaison
    fun isCompleteWithOperator(player: Player): Boolean {
        val factEntry = fact.get() ?: return false
        val currentValue = factEntry.readForPlayersGroup(player).value
        val requiredAmount = amount.get(player).absoluteValue
        
        return when (operator) {
            ComparisonOperator.EQUAL -> currentValue == requiredAmount
            ComparisonOperator.GREATER_THAN -> currentValue > requiredAmount
            ComparisonOperator.LESS_THAN -> currentValue < requiredAmount
            ComparisonOperator.GREATER_OR_EQUAL -> currentValue >= requiredAmount
            ComparisonOperator.LESS_OR_EQUAL -> currentValue <= requiredAmount
        }
    }

    // Override l'affichage pour utiliser notre logique de completion personnalisée
    override fun display(player: Player?): String {
        if (player == null) return inactiveObjectiveDisplay

        val factEntry = fact.get() ?: return inactiveObjectiveDisplay
        val currentValue = factEntry.readForPlayersGroup(player).value

        if (!criteria.matches(player)) {
            return inactiveObjectiveDisplay
        }

        val requiredAmount = amount.get(player).absoluteValue
        val isComplete = isCompleteWithOperator(player) // Utilise notre logique personnalisée

        // Utilise les snippets standards de Typewriter comme base
        val baseText = if (criteria.matches(player)) showingObjectiveDisplay else inactiveObjectiveDisplay
        val displayText = display.get(player) ?: ""
        
        // Ajoute les informations de progression
        val progressText = if (isComplete) {
            "<green>✔</green> <st>$displayText</st> <dark_gray>(<gray>$currentValue<dark_gray>/<gray>$requiredAmount<dark_gray>)"
        } else {
            "$displayText <dark_gray>(<gray>$currentValue<dark_gray>/<gray>$requiredAmount<dark_gray>)"
        }

        return baseText.asMiniWithResolvers(
            parsed("display", progressText)
        ).asMini().parsePlaceholders(player)
    }
    
    override suspend fun display(): AudienceFilter {
        return ReadableFactCheckObjectiveDisplay(ref())
    }
}

private class ReadableFactCheckObjectiveDisplay(ref: Ref<ReadableFactCheckObjectiveEntry>) :
    ObjectiveDisplay<ReadableFactCheckObjectiveEntry>(ref) {

    private val completionStatus = ConcurrentHashMap<UUID, Boolean>()

    override fun onPlayerAdd(player: Player) {
        val entry = ref.get() ?: return
        val isCurrentlyComplete = entry.isCompleteWithOperator(player)
        completionStatus[player.uniqueId] = isCurrentlyComplete
        super.onPlayerAdd(player)
    }

    override fun onPlayerRemove(player: Player) {
        super.onPlayerRemove(player)
        completionStatus.remove(player.uniqueId)
    }

    /**
     * Public method used to re-check the fact value for a player.
     * You can call this from an external event/listener if facts are updated elsewhere.
     */
    fun recheckFor(player: Player) {
        val entry = ref.get() ?: return
        val playerId = player.uniqueId

        val wasComplete = completionStatus[playerId] ?: false
        val isNowComplete = entry.isCompleteWithOperator(player)

        if (isNowComplete != wasComplete) {
            completionStatus[playerId] = isNowComplete

            if (!wasComplete && isNowComplete) {
                entry.onComplete.triggerFor(player, context())
            }
        }
    }
}
