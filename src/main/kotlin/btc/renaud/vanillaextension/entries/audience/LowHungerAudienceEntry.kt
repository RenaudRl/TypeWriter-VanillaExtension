package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.entity.Player

@Entry("low_hunger_audience", "Filter players with low hunger (< 3 shanks)", Colors.GREEN, icon = "mdi:food-apple-outline")
class LowHungerAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
    val threshold: Int = 6, // 3 shanks
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean = player.foodLevel < threshold
}
