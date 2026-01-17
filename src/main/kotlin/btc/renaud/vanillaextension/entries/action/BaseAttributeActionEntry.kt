package btc.renaud.vanillaextension.entries

import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import org.bukkit.attribute.Attribute

/** Base class for actions that set a player attribute */
abstract class BaseAttributeActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val amount: Var<Double> = ConstVar(0.0),
) : ActionEntry {
    protected abstract val attribute: Attribute

    override fun ActionTrigger.execute() {
        player.getAttribute(attribute)?.baseValue = amount.get(player, context)
    }
}

