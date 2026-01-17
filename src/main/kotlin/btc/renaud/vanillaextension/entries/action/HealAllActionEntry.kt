package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute

@Entry("heal_all_action", "Heal all online players", Colors.RED, icon = "mdi:account-group")
class HealAllActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : ActionEntry {
    override fun ActionTrigger.execute() {
        Bukkit.getOnlinePlayers().forEach { player ->
            val max = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: player.health
            player.health = max
            player.foodLevel = 20
        }
    }
}

