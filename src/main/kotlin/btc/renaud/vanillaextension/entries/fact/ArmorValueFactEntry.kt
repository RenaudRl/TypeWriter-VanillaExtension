package btc.renaud.vanillaextension.entries.fact

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.engine.paper.facts.FactData
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

@Entry("armor_value_fact", "Current armor points", Colors.BLUE, icon = "mdi:shield")
class ArmorValueFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
) : ReadableFactEntry {
    override fun readSinglePlayer(player: Player): FactData {
        val armor = player.getAttribute(Attribute.ARMOR)?.value ?: 0.0
        return FactData(armor.toInt())
    }
}
