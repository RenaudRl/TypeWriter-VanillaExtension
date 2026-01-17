package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.Material
import org.bukkit.entity.Player

@Entry("tool_in_hand_audience", "Filter players holding a tool", Colors.GREEN, icon = "mdi:pickaxe")
class ToolInHandAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean {
        val type = player.inventory.itemInMainHand.type
        return type.name.endsWith("_PICKAXE") || type.name.endsWith("_SHOVEL") || type.name.endsWith("_HOE") || type.name.endsWith("_AXE") || type == Material.SHEARS || type == Material.FISHING_ROD
    }
}
