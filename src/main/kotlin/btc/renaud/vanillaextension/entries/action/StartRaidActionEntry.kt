package btc.renaud.vanillaextension.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import org.bukkit.Raid
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.entries.ActionTrigger

@Entry("start_raid_action", "Trigger a raid at player location", Colors.RED, icon = "mdi:axe-battle")
class StartRaidActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val level: Int = 1,
) : ActionEntry {
    override fun ActionTrigger.execute() {
        // Only way to force start a raid in Bukkit is often giving Bad Omen.
        // Or using Raid API if location is a village.
        // We will try giving Bad Omen first as it's the natural way.
        player.addPotionEffect(PotionEffect(PotionEffectType.BAD_OMEN, 1200, level - 1))
    }
}
