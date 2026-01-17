package btc.renaud.vanillaextension.entries.event

import btc.renaud.vanillaextension.entries.ComparisonOperator
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
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import com.typewritermc.loader.ListenerPriority

@Entry("readable_fact_check_event", "An event that triggers when a readable fact matches a value", Colors.YELLOW, "ph:check-circle-bold")
class ReadableFactCheckEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    @Help("The readable fact to check.")
    val fact: Ref<ReadableFactEntry> = emptyRef(),
    @Help("The amount that represents the required value to trigger the event.")
    val amount: Var<Int> = ConstVar(0),
    @Help("The comparison operator to use (EQUAL, GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL).")
    val operator: ComparisonOperator = ComparisonOperator.EQUAL,
    @Help("If true, the event will check the fact for players when they join.")
    val checkOnJoin: Boolean = false,
    @Help("Interval in seconds for periodic fact checks when enabled.")
    val checkIntervalSeconds: Long = 10L,
) : AudienceFilterEntry, TriggerableEntry {

    override suspend fun display(): AudienceFilter {
        return ReadableFactCheckEventDisplay(ref())
    }
}

private class ReadableFactCheckEventDisplay(private val ref: Ref<ReadableFactCheckEventEntry>) : AudienceFilter(ref) {

    private var timerJob: Job? = null

    override fun initialize() {
        super.initialize()
        startTimer()
    }

    private fun startTimer() {
        val entry = ref.get() ?: return
        val intervalSeconds = entry.checkIntervalSeconds
        if (intervalSeconds == 0L) return
        require(intervalSeconds > 0) {
            "checkIntervalSeconds must be positive when periodic checks are enabled for entry '${entry.id}'."
        }

        timerJob = Dispatchers.IO.launch {
            while (true) {
                delay(intervalSeconds * 1000L) // Convert to milliseconds

                val currentEntry = ref.get() ?: break

                // If there are no triggers configured, stop the timer
                val hasTriggers = currentEntry.triggers.any { it.get() != null }
                if (!hasTriggers) break

                // Check if there are any players that match the criteria
                val matchingPlayers = Bukkit.getOnlinePlayers().filter { currentEntry.criteria.matches(it) }
                if (matchingPlayers.isEmpty()) break

                matchingPlayers.forEach { player ->
                    if (matchesFactFor(player, currentEntry)) {
                        currentEntry.triggers.forEach { it.triggerFor(player, context()) }
                    }
                }
            }
        }
    }

    override fun onPlayerRemove(player: Player) {
        super.onPlayerRemove(player)
        
        // If no players are left, stop the timer
        if (players.isEmpty()) {
            timerJob?.cancel()
            timerJob = null
        }
    }

    override fun dispose() {
        timerJob?.cancel()
        timerJob = null
        super.dispose()
    }

    override fun filter(player: Player): Boolean {
        val entry = ref.get() ?: return false
        return entry.criteria.matches(player)
    }

    private fun matchesFactFor(player: Player, entry: ReadableFactCheckEventEntry): Boolean {
        val factEntry = entry.fact.get() ?: return false
        val currentValue = factEntry.readForPlayersGroup(player).value
        val required = entry.amount.get(player).absoluteValue

        return when (entry.operator) {
            ComparisonOperator.EQUAL -> currentValue == required
            ComparisonOperator.GREATER_THAN -> currentValue > required
            ComparisonOperator.LESS_THAN -> currentValue < required
            ComparisonOperator.GREATER_OR_EQUAL -> currentValue >= required
            ComparisonOperator.LESS_OR_EQUAL -> currentValue <= required
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val entry = ref.get() ?: return

        if (!filter(player)) return
        if (!entry.checkOnJoin) return

        if (matchesFactFor(player, entry)) {
            entry.triggers.forEach { it.triggerFor(player, context()) }
        }
    }

    /**
     * Public API: check the fact for a player and trigger if it matches.
     * Can be used by other code to force evaluation.
     */
    fun checkAndTrigger(player: Player) {
        val entry = ref.get() ?: return
        if (!filter(player)) return

        if (matchesFactFor(player, entry)) {
            entry.triggers.forEach { it.triggerFor(player, context()) }
        }
    }
}

