package btc.renaud.vanillaextension.entries.fact

import btc.renaud.vanillaextension.weather.WeatherCondition
import com.typewritermc.engine.paper.facts.FactData
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import com.typewritermc.engine.paper.entry.PlaceholderParser
import com.typewritermc.engine.paper.entry.include
import com.typewritermc.engine.paper.entry.literal
import com.typewritermc.engine.paper.entry.placeholderParser
import com.typewritermc.engine.paper.entry.supply
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import org.bukkit.entity.Player

@Entry(
    "weather_fact",
    "Expose the weather state as a fact",
    Colors.CYAN,
    icon = "mdi:weather-cloudy",
)
class WeatherFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
    @Help("The world to check weather in. If empty, uses the player's current world.")
    val world: Var<String> = ConstVar("")
) : ReadableFactEntry {

    override fun readSinglePlayer(player: Player): FactData {
        val worldName = world.get(player, context())
        val targetWorld = if (worldName.isNotEmpty()) {
            org.bukkit.Bukkit.getWorld(worldName) ?: player.world
        } else {
            player.world
        }
        val condition = WeatherCondition.fromWorld(targetWorld)
        return FactData(condition.ordinal)
    }

    override fun parser(): PlaceholderParser = placeholderParser {
        include(super.parser())
        literal("weather") {
            supply { player ->
                val bukkitPlayer = player ?: return@supply ""
                val worldName = world.get(bukkitPlayer, context())
                val targetWorld = if (worldName.isNotEmpty()) {
                    org.bukkit.Bukkit.getWorld(worldName) ?: bukkitPlayer.world
                } else {
                    bukkitPlayer.world
                }
                WeatherCondition.fromWorld(targetWorld).name.lowercase()
            }
        }
    }
}

