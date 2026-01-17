package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.entity.Player

private const val TICKS_PER_DAY = 24000
private const val DEFAULT_DAY_START = 1000
private const val DEFAULT_DAY_END = 12000
private const val DEFAULT_NIGHT_START = 13000
private const val DEFAULT_NIGHT_END = 23000

@Entry(
    "time_period_audience",
    "Filter players by the current world time",
    Colors.GREEN,
    icon = "mdi:clock-time-four-outline"
)
open class TimePeriodAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    open val startTick: Int = 0,
    open val endTick: Int = 12000,
    open val useFullTime: Boolean = false,
    override val inverted: Boolean = false,
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {

    override fun state(player: Player): Boolean {
        val time = currentTime(player)
        val start = startTick.normalizeTick()
        val end = endTick.normalizeTick()
        return if (start <= end) {
            time in start..end
        } else {
            time >= start || time <= end
        }
    }

    protected fun currentTime(player: Player): Int {
        val raw = if (useFullTime) player.world.fullTime else player.world.time
        val modulo = raw % TICKS_PER_DAY
        val normalized = if (modulo < 0) modulo + TICKS_PER_DAY else modulo
        return normalized.toInt()
    }

    private fun Int.normalizeTick(): Int {
        val modulo = this % TICKS_PER_DAY
        return if (modulo < 0) modulo + TICKS_PER_DAY else modulo
    }
}

@Entry(
    "daytime_audience",
    "Audience active during the day",
    Colors.GREEN,
    icon = "mdi:weather-sunny"
)
class DaytimeAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val startTick: Int = DEFAULT_DAY_START,
    override val endTick: Int = DEFAULT_DAY_END,
    override val useFullTime: Boolean = false,
    override val inverted: Boolean = false,
) : TimePeriodAudienceEntry(id, name, children, startTick, endTick, useFullTime, inverted)

@Entry(
    "nighttime_audience",
    "Audience active during the night",
    Colors.GREEN,
    icon = "mdi:weather-night"
)
class NighttimeAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val startTick: Int = DEFAULT_NIGHT_START,
    override val endTick: Int = DEFAULT_NIGHT_END,
    override val useFullTime: Boolean = false,
    override val inverted: Boolean = false,
) : TimePeriodAudienceEntry(id, name, children, startTick, endTick, useFullTime, inverted)

