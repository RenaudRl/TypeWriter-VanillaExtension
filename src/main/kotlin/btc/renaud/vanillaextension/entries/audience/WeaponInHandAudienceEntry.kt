package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.Material
import org.bukkit.entity.Player

@Entry("weapon_in_hand_audience", "Filter players holding a weapon", Colors.GREEN, icon = "mdi:sword")
class WeaponInHandAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean {
        val item = player.inventory.itemInMainHand
        val type = item.type
        return type.name.endsWith("_SWORD") || type.name.endsWith("_AXE") || type == Material.BOW || type == Material.CROSSBOW || type == Material.TRIDENT
    }
}
