package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.engine.paper.facts.FactData
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import org.bukkit.entity.Player

private const val TICKS_PER_DAY = 24000

@Entry(
    "time_of_day_fact",
    "Expose the current time of day for the player's world",
    Colors.BLUE,
    icon = "mdi:clock-time-three"
)
class TimeOfDayFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
    val mode: Mode = Mode.SEGMENT,
    val startTick: Int = 0,
    val endTick: Int = 12000,
    val useFullTime: Boolean = false,
) : ReadableFactEntry {

    enum class Mode {
        SEGMENT,
        RANGE,
    }

    enum class Segment {
        MORNING,
        DAY,
        EVENING,
        NIGHT,
        ;

        companion object {
            fun fromTick(tick: Int): Segment = when (tick) {
                in 0..5999 -> MORNING
                in 6000..11999 -> DAY
                in 12000..17999 -> EVENING
                else -> NIGHT
            }
        }
    }

    override fun readSinglePlayer(player: Player): FactData {
        val time = currentTime(player)
        val value = when (mode) {
            Mode.SEGMENT -> Segment.fromTick(time).ordinal
            Mode.RANGE -> if (isWithinRange(time)) 1 else 0
        }
        return FactData(value.toInt())
    }

    private fun currentTime(player: Player): Int {
        val raw = if (useFullTime) player.world.fullTime else player.world.time
        val modulo = raw % TICKS_PER_DAY
        val normalized = if (modulo < 0) modulo + TICKS_PER_DAY else modulo
        return normalized.toInt()
    }

    private fun isWithinRange(time: Int): Boolean {
        val start = startTick.normalizeTick()
        val end = endTick.normalizeTick()
        return if (start <= end) {
            time in start..end
        } else {
            time >= start || time <= end
        }
    }

    private fun Int.normalizeTick(): Int {
        val modulo = this % TICKS_PER_DAY
        return if (modulo < 0) modulo + TICKS_PER_DAY else modulo
    }
}

