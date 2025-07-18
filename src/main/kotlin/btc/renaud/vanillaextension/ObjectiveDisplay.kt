package btc.renaud.vanillaextension

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.priority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.facts.FactListenerSubscription
import com.typewritermc.engine.paper.facts.listenForFacts
import com.typewritermc.engine.paper.utils.server
import com.typewritermc.quest.*
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class ObjectiveDisplay<T : ObjectiveEntry>(
    val ref: Ref<T>,
) : AudienceFilter(ref) {
    private val factWatcherSubscriptions = ConcurrentHashMap<UUID, FactListenerSubscription>()

    override fun filter(player: Player): Boolean {
        return ref.get()?.criteria?.matches(player) == true
    }

    override fun onPlayerAdd(player: Player) {
        val entry = ref.get() ?: return

        factWatcherSubscriptions.compute(player.uniqueId) { _, subscription ->
            subscription?.cancel(player)
            player.listenForFacts(
                entry.criteria.map { it.fact },
                ::onFactChange,
            )
        }

        if (filter(player)) {
            super.onPlayerAdd(player)
        }
    }

    private fun onFactChange(player: Player, _fact: Ref<ReadableFactEntry>) {
        player.refresh()
    }

    override fun onPlayerRemove(player: Player) {
        super.onPlayerRemove(player)
        factWatcherSubscriptions.remove(player.uniqueId)?.cancel(player)
    }

    override fun onPlayerFilterAdded(player: Player) {
        super.onPlayerFilterAdded(player)
        val quest = ref.get()?.quest ?: return

        if (!player.isQuestActive(quest)) {
            return
        }

        if (player.trackedQuest() == null) {
            player.trackQuest(quest)
            return
        }

        player.trackedShowingObjectives().maxOfOrNull { it.priority }?.let { highestPriority ->
            if (ref.priority >= highestPriority) {
                player.trackQuest(quest)
            }
        } ?: player.trackQuest(quest)
    }

    override fun dispose() {
        super.dispose()
        factWatcherSubscriptions.forEach { (playerId, subscription) ->
            server.getPlayer(playerId)?.let { player ->
                subscription.cancel(player)
            }
        }
    }
}
