package btc.renaud.vanillaextension.entries.fact

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.engine.paper.facts.FactData
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import org.bukkit.entity.Player

@Entry("is_in_raid_fact", "Is player currently in a raid", Colors.BLUE, icon = "mdi:axe-battle")
class IsInRaidFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),
) : ReadableFactEntry {
    override fun readSinglePlayer(player: Player): FactData {
        val raid = player.world.locateNearestRaid(player.location, 64)
        return FactData(if (raid != null) 1 else 0)
    }
}
