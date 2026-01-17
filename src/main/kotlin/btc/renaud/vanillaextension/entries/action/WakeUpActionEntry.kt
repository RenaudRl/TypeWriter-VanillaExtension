package btc.renaud.vanillaextension.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import org.bukkit.entity.Player
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.entries.ActionTrigger

@Entry("wake_up_action", "Wake player up from bed", Colors.RED, icon = "mdi:bed-empty")
class WakeUpActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val setSpawn: Boolean = false,
) : ActionEntry {
    override fun ActionTrigger.execute() {
        if (player.isSleeping) {
            player.wakeup(setSpawn)
        }
    }
}
