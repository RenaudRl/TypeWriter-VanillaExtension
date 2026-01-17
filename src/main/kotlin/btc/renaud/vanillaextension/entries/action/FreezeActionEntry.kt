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

@Entry("freeze_action", "Set player freezing ticks", Colors.RED, icon = "mdi:snowflake")
class FreezeActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val ticks: Int = 140, // 7 seconds (fully frozen)
) : ActionEntry {
    override fun ActionTrigger.execute() {
        player.freezeTicks = ticks
    }
}
