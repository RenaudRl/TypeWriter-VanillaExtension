package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import org.bukkit.attribute.Attribute
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("set_scale_attribute", "Set player's scale attribute", Colors.RED, icon = "mdi:resize")
class ScaleActionEntry(
    id: String = "",
    name: String = "",
    amount: Var<Double> = ConstVar(0.0),
) : BaseAttributeActionEntry(id, name, amount = amount) {
    override val attribute: Attribute
        get() = Attribute.SCALE
}

