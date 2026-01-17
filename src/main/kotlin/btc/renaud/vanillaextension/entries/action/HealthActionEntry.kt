package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import org.bukkit.attribute.Attribute
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("set_health_attribute", "Set player's max health attribute", Colors.RED, icon = "mdi:heart")
class HealthActionEntry(
    id: String = "",
    name: String = "",
    amount: Var<Double> = ConstVar(20.0),
) : BaseAttributeActionEntry(id, name, amount = amount) {
    override val attribute: Attribute
        get() = Attribute.MAX_HEALTH
}

