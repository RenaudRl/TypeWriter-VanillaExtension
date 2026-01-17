package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.entity.Player

@Entry(
    "looking_up_audience",
    "In audience while looking up",
    Colors.GREEN,
    icon = "mdi:arrow-up"
)
class LookingUpAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
    @Help("Maximum pitch to consider looking up")
    private val maxPitch: Float = -60f,
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean = player.location.pitch <= maxPitch
}

