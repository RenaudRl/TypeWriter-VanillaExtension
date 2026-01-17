package btc.renaud.vanillaextension.entries.fact

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.engine.paper.facts.FactData
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import org.bukkit.entity.Player

@Entry("player_world_fact", "Current player world", Colors.BLUE, icon = "mdi:earth")
class PlayerWorldFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
) : ReadableFactEntry {
    override fun readSinglePlayer(player: Player): FactData {
        return FactData(player.world.name.hashCode())
    }
}
