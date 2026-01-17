package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.GameMode
import org.bukkit.entity.Player

@Entry("specific_gamemode_audience", "Filter players in a specific gamemode", Colors.GREEN, icon = "mdi:controller")
class SpecificGameModeAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
    val requiredGameMode: String = "SURVIVAL",
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean {
        return try {
            player.gameMode == GameMode.valueOf(requiredGameMode.uppercase())
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
