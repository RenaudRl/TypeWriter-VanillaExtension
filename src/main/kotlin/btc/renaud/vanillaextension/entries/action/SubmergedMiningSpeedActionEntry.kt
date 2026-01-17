package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import org.bukkit.attribute.Attribute
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("set_submerged_mining_speed_attribute", "Set player's submerged mining speed attribute", Colors.RED, icon = "mdi:pickaxe")
class SubmergedMiningSpeedActionEntry(
    id: String = "",
    name: String = "",
    amount: Var<Double> = ConstVar(0.0),
) : BaseAttributeActionEntry(id, name, amount = amount) {
    override val attribute: Attribute
        get() = Attribute.SUBMERGED_MINING_SPEED
}

