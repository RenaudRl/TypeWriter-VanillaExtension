package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var

import org.bukkit.Bukkit

@Entry("ender_chest_see_action", "Open another player's ender chest", Colors.RED, icon = "mdi:eye")
class EnderChestSeeActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Target")
    val target: Var<String> = ConstVar(""),

) : ActionEntry {
    override fun ActionTrigger.execute() {

        val name = target.get(player, context)
        val targetPlayer = if (name.isBlank()) player else Bukkit.getPlayerExact(name) ?: return
        player.openInventory(targetPlayer.enderChest)
    
    }
}

