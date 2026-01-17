package btc.renaud.vanillaextension.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import org.bukkit.Registry

@Entry("has_effect_audience", "Filter players with a specific potion effect", Colors.GREEN, icon = "mdi:bottle-tonic-plus")
class HasEffectAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),
    override val inverted: Boolean = false,
    val effectType: String = "SPEED",
    val minimumAmplifier: Int = 0,
) : BooleanPlayerStateAudienceEntry(id, name, children, inverted) {
    override fun state(player: Player): Boolean {
        // Use Registry.EFFECT.get() instead of deprecated PotionEffectType.getByName()
        val key = org.bukkit.NamespacedKey.minecraft(effectType.lowercase())
        val type = Registry.EFFECT.get(key) ?: return false
        val effect = player.getPotionEffect(type) ?: return false
        return effect.amplifier >= minimumAmplifier
    }
}
