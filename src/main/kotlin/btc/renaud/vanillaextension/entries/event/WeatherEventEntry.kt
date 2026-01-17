package btc.renaud.vanillaextension.entries.event

import btc.renaud.vanillaextension.weather.WeatherCondition
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.triggerEntriesFor
import com.typewritermc.loader.ListenerPriority
import org.bukkit.event.weather.WeatherChangeEvent

@Entry(
    "weather_event",
    "Trigger entries when the weather changes",
    Colors.CYAN,
    "mdi:weather-cloudy"
)
class WeatherEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<com.typewritermc.engine.paper.entry.TriggerableEntry>> = emptyList(),
    
    @Help("The world to listen for weather changes in. If empty, triggers for all worlds.")
    val world: Var<String> = ConstVar(""),
    
    @Help("The weather condition triggering the event (RAIN or CLEAR). If null, triggers on any change.")
    val condition: WeatherCondition? = null
) : EventEntry

@EntryListener(WeatherEventEntry::class, priority = ListenerPriority.MONITOR, ignoreCancelled = true)
fun onWeatherChange(event: WeatherChangeEvent, query: Query<WeatherEventEntry>) {
    val newCondition = if (event.toWeatherState()) WeatherCondition.RAIN else WeatherCondition.CLEAR
    
    event.world.players.forEach { player ->
        val ctx = context()
        query.find().forEach { entry ->
            val targetWorld = entry.world.get(player, ctx)
            
            // Check world filter
            if (targetWorld.isNotEmpty() && targetWorld != event.world.name) return@forEach
            
            // Check condition filter
            val targetCondition = entry.condition
            if (targetCondition != null && targetCondition != newCondition) return@forEach
            
            // Trigger
            entry.triggers.triggerEntriesFor(player, ctx)
        }
    }
}
