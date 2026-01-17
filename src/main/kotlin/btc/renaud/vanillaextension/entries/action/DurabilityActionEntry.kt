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

import org.bukkit.inventory.meta.Damageable

@Entry("durability_action", "Set held item durability", Colors.RED, icon = "mdi:wrench")
class DurabilityActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Durability")
    val durability: Var<Int> = ConstVar(0),

) : ActionEntry {
    override fun ActionTrigger.execute() {

        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta
        if (meta is Damageable) {
            meta.damage = durability.get(player, context)
            item.itemMeta = meta
        }
    
    }
}

