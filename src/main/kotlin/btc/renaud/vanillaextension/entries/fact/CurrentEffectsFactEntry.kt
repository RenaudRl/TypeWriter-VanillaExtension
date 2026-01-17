package btc.renaud.vanillaextension.entries.fact

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.engine.paper.facts.FactData
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import org.bukkit.entity.Player

@Entry("current_effects_fact", "List of current potion effects", Colors.BLUE, icon = "mdi:bottle-tonic")
class CurrentEffectsFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
) : ReadableFactEntry {
    override fun readSinglePlayer(player: Player): FactData {
        // Use key().asString() instead of deprecated name property
        val effects = player.activePotionEffects.joinToString(", ") { it.type.key.asString() }
        // FactData requires Int - use hashCode for the effect combination  
        return FactData(effects.hashCode())
    }
}
