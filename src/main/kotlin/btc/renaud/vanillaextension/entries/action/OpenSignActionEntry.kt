package btc.renaud.vanillaextension.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.entries.ActionTrigger

@Entry("open_sign_action", "Open a sign editor for the player", Colors.RED, icon = "mdi:sign-text")
class OpenSignActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    // This action typically requires a sign block to exist at a location,
    // or we can place a temporary one, open it, and remove it?
    // For safety, we usually require a valid sign location or fallback to current location block if it is a sign.
    // However, opening a sign editor without a sign is tricky in pure Bukkit without packets.
    // We will assume usage on a sign block or skip if invalid.
    val useCurrentLocation: Boolean = true,
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val block = if (useCurrentLocation) player.location.block else null
        if (block != null && block.state is Sign) {
            player.openSign(block.state as Sign, org.bukkit.block.sign.Side.FRONT)
        }
    }
}
