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
import com.typewritermc.quest.entries.inactiveObjectiveDisplay
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("cachable_fact_check_objective", "An objective that validates based on a numeric fact value", Colors.BLUE_VIOLET, "ph:target-bold")
class FactCheckObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The comparison operator to use (EQUAL, GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL).")
    val operator: ComparisonOperator = ComparisonOperator.EQUAL,
    override val amount: Var<Int> = ConstVar(0),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    
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
        if (!criteria.matches(player)) return inactiveObjectiveDisplay
        
        val displayText = display.get(player) ?: ""
        
        // Ajoute les informations de progression
        val progressText = if (isComplete) {
            "<green>✔</green> <st>$displayText</st> <dark_gray>(<gray>$currentValue<dark_gray>/<gray>$requiredAmount<dark_gray>)"
        } else {
            "$displayText <dark_gray>(<gray>$currentValue<dark_gray>/<gray>$requiredAmount<dark_gray>)"
        }

        return progressText.asMiniWithResolvers(
            parsed("display", progressText)
        ).asMini().parsePlaceholders(player)
    }
    
    override suspend fun display(): AudienceFilter {
        return FactCheckObjectiveDisplay(ref())
    }
}

private class FactCheckObjectiveDisplay(ref: Ref<FactCheckObjectiveEntry>) :
    BaseCountObjectiveDisplay<FactCheckObjectiveEntry>(ref) {

    /**
     * Public method used to re-check the fact value for a player.
     * You can call this from an external event/listener if facts are updated elsewhere.
     */
    fun recheckFor(player: Player) {
        val entry = ref.get() ?: return
        val fact = entry.fact.get() ?: return

        // Force une vérification des changements externes en "incrémentant" de 0
        // Cela va déclencher la vérification de completion dans BaseCountObjectiveDisplay
        val currentValue = fact.readForPlayersGroup(player).value
        // Trigger une mise à jour en "incrémentant" de 0
        // Cela va déclencher la vérification de completion
        incrementCount(player, 0)
    }
}

