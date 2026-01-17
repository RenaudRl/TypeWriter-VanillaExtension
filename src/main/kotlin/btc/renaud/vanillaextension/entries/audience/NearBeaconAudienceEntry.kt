package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType

@Entry("near_beacon_audience", "Filter players near an active beacon (ambient effects)", Colors.GREEN, icon = "mdi:lighthouse")
class NearBeaconAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean {
        // Checks generally for ambient effects which beacons provide
        return player.activePotionEffects.any { it.isAmbient }
    }
}
