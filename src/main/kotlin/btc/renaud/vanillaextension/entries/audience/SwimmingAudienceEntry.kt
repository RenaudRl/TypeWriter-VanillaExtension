package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.entity.Player

@Entry(
    "swimming_audience",
    "In audience while swimming",
    Colors.GREEN,
    icon = "mdi:swim"
)
class SwimmingAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean = player.isSwimming
}

