package btc.renaud.vanillaextension.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.entries.ActionTrigger

@Entry("set_velocity_action", "Set player velocity", Colors.RED, icon = "mdi:transfer-right")
class SetVelocityActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
    val relative: Boolean = true,
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val vector = Vector(x, y, z)
        if (relative) {
            player.velocity = player.velocity.add(vector)
        } else {
            player.velocity = vector
        }
    }
}
