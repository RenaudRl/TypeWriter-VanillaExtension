package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.AudienceFilterEntry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.Invertible
import com.typewritermc.engine.paper.entry.entries.TickableDisplay
import com.typewritermc.core.entries.ref
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.math.abs

@Entry(
    "spin_me_round_audience",
    "In audience while spinning 360 degrees",
    Colors.GREEN,
    icon = "mdi:sync"
)
class SpinMeRoundAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
) : AudienceFilterEntry, TickableDisplay, Invertible {
    override suspend fun display(): AudienceFilter = object : AudienceFilter(ref()), TickableDisplay {
        private val lastYaw = mutableMapOf<UUID, Float>()
        private val progress = mutableMapOf<UUID, Float>()

        override fun filter(player: Player): Boolean {
            val uid = player.uniqueId
            val current = player.location.yaw
            val prev = lastYaw.getOrPut(uid) { current }
            val delta = abs(current - prev)
            lastYaw[uid] = current
            val total = (progress[uid] ?: 0f) + delta
            return if (total >= 360f) {
                progress[uid] = 0f
                true
            } else {
                progress[uid] = total
                false
            }
        }

        override fun tick() { consideredPlayers.forEach { it.refresh() } }
    }

    override fun tick() {}
}

