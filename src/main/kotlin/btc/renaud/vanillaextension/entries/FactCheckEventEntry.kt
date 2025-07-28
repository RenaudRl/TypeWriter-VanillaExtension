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
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.AudienceFilterEntry
import com.typewritermc.engine.paper.entry.entries.CachableFactEntry
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerFor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

@Entry("on_fact_check_event", "An event that triggers when a fact matches a specific value", Colors.YELLOW, "ph:check-circle-bold")
class FactCheckEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    @Help("The fact to check.")
    val fact: Ref<ReadableFactEntry> = emptyRef(),
    @Help("The value that the fact should match to trigger the event.")
    val expectedValue: Int = 0,
    @Help("The comparison operator to use (EQUAL, GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL).")
    val operator: ComparisonOperator = ComparisonOperator.EQUAL,
    @Help("Check the fact continuously while the player is online.")
    val continuous: Boolean = false,
) : AudienceFilterEntry, TriggerableEntry {

    override suspend fun display(): AudienceFilter {
        return FactCheckEventDisplay(ref())
    }
}

enum class ComparisonOperator {
    EQUAL,
    GREATER_THAN,
    LESS_THAN,
    GREATER_OR_EQUAL,
    LESS_OR_EQUAL
}

private class FactCheckEventDisplay(private val ref: Ref<FactCheckEventEntry>) : AudienceFilter(ref) {

    private val checkedPlayers = mutableSetOf<UUID>()

    override fun filter(player: Player): Boolean {
        val entry = ref.get() ?: return false
        return entry.criteria.matches(player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        
        if (!filter(player)) return
        
        checkFactAndTrigger(player)
        
        if (entry.continuous) {
            // Start continuous checking for this player
            startContinuousCheck(player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        checkedPlayers.remove(event.player.uniqueId)
    }

    private fun checkFactAndTrigger(player: Player) {
        val entry = ref.get() ?: return
        val factEntry = entry.fact.get() ?: return
        
        val currentValue = factEntry.readForPlayersGroup(player).value
        val expectedValue = entry.expectedValue
        
        val matches = when (entry.operator) {
            ComparisonOperator.EQUAL -> currentValue == expectedValue
            ComparisonOperator.GREATER_THAN -> currentValue > expectedValue
            ComparisonOperator.LESS_THAN -> currentValue < expectedValue
            ComparisonOperator.GREATER_OR_EQUAL -> currentValue >= expectedValue
            ComparisonOperator.LESS_OR_EQUAL -> currentValue <= expectedValue
        }
        
        if (matches) {
            entry.triggers.forEach { it.triggerFor(player, context()) }
        }
    }

    private fun startContinuousCheck(player: Player) {
        // This would ideally use a scheduler to check periodically
        // For now, we'll check on each join
        checkFactAndTrigger(player)
    }
}
