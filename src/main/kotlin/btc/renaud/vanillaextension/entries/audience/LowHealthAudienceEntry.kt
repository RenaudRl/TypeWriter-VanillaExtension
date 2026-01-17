package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.entity.Player

@Entry("low_health_audience", "Filter players with low health (< 3 hearts)", Colors.GREEN, icon = "mdi:heart-broken")
class LowHealthAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
    val threshold: Double = 6.0, // 3 hearts
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean = player.health < threshold
}
