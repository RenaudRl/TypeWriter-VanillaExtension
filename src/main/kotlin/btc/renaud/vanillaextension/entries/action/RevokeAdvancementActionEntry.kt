package btc.renaud.vanillaextension.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.entries.ActionTrigger

@Entry("revoke_advancement_action", "Revoke a specific advancement", Colors.RED, icon = "mdi:trophy-broken")
class RevokeAdvancementActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val namespacedKey: String = "minecraft:story/root",
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val parts = namespacedKey.split(":")
        if (parts.size == 2) {
            val key = NamespacedKey(parts[0], parts[1])
            val advancement = Bukkit.getAdvancement(key)
            if (advancement != null) {
                val progress = player.getAdvancementProgress(advancement)
                progress.awardedCriteria.forEach { criterion ->
                    progress.revokeCriteria(criterion)
                }
            }
        }
    }
}
