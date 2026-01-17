package btc.renaud.vanillaextension.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.entries.ActionTrigger

@Entry("launch_firework_action", "Launch a firework at player location", Colors.RED, icon = "mdi:firework")
class LaunchFireworkActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val power: Int = 1,
    val type: String = "BALL", // BALL, BALL_LARGE, STAR, BURST, CREEPER
    val color: Int = 0xFF0000,
    val fadeColor: Int = 0xFFFFFF,
    val flicker: Boolean = false,
    val trail: Boolean = false,
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val location = player.location
        val firework = location.world.spawnEntity(location, EntityType.FIREWORK_ROCKET) as Firework
        val meta = firework.fireworkMeta
        val builder = FireworkEffect.builder()
        
        try {
             builder.with(FireworkEffect.Type.valueOf(type.uppercase()))
        } catch (e: IllegalArgumentException) {
             builder.with(FireworkEffect.Type.BALL)
        }

        builder.withColor(Color.fromRGB(color))
        builder.withFade(Color.fromRGB(fadeColor))
        if(flicker) builder.withFlicker()
        if(trail) builder.withTrail()
        
        meta.addEffect(builder.build())
        meta.power = power
        firework.fireworkMeta = meta
    }
}
