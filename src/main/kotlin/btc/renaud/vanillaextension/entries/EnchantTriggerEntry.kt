package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.AudienceFilterEntry
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerFor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.enchantments.Enchantment
import btc.renaud.vanillaextension.EnchantmentType
import java.util.*

@Entry("on_enchant_trigger", "A trigger for when players enchant items", Colors.YELLOW, "ph:sparkle-bold")
class EnchantTriggerEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    @Help("The enchantment that needs to be applied.")
    val enchantment: Optional<EnchantmentType> = Optional.empty(),
    @Help("The minimum level of the enchantment required.")
    val level: Optional<Int> = Optional.empty(),
) : AudienceFilterEntry, TriggerableEntry {

    override suspend fun display(): AudienceFilter {
        return EnchantTriggerDisplay(ref())
    }
}

private class EnchantTriggerDisplay(private val ref: Ref<EnchantTriggerEntry>) : AudienceFilter(ref) {

    override fun filter(player: Player): Boolean {
        val entry = ref.get() ?: return false
        return entry.criteria.matches(player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onEnchantItem(event: EnchantItemEvent) {
        val player = event.enchanter
        val entry = ref.get() ?: return
        
        if (!filter(player)) return

        val enchantmentsToAdd = event.enchantsToAdd
        if (enchantmentsToAdd.isEmpty()) return
        
        val requiredEnchantment = entry.enchantment
        val requiredLevel = entry.level
        
        // If no specific enchantment is required, trigger for all enchanted items
        if (!requiredEnchantment.isPresent) {
            entry.triggers.forEach { it.triggerFor(player, context()) }
            return
        }
        
        // Check if the required enchantment is present with the right level
        val requiredEnchantmentBukkit = requiredEnchantment.get().toEnchantment()
        if (requiredEnchantmentBukkit != null) {
            for ((enchantment, enchantLevel) in enchantmentsToAdd) {
                val enchantmentMatches = enchantment == requiredEnchantmentBukkit
                val levelMatches = !requiredLevel.isPresent || enchantLevel >= requiredLevel.get()
                
                if (enchantmentMatches && levelMatches) {
                    entry.triggers.forEach { it.triggerFor(player, context()) }
                    break
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onAnvilClick(event: InventoryClickEvent) {
        // Only handle anvil inventories
        if (event.inventory.type != InventoryType.ANVIL) return
        
        // Only handle clicks on the result slot (raw slot 2 in anvil)
        if (event.rawSlot != 2) return
        
        val player = event.whoClicked as? Player ?: return
        val entry = ref.get() ?: return
        
        if (!filter(player)) return

        val resultItem = event.currentItem ?: return
        if (!resultItem.hasItemMeta()) return
        
        val enchantments = resultItem.enchantments
        if (enchantments.isEmpty()) return
        
        val requiredEnchantment = entry.enchantment
        val requiredLevel = entry.level
        
        // If no specific enchantment is required, trigger for all enchanted items
        if (!requiredEnchantment.isPresent) {
            entry.triggers.forEach { it.triggerFor(player, context()) }
            return
        }
        
        // Check if the required enchantment is present with the right level
        val requiredEnchantmentBukkit = requiredEnchantment.get().toEnchantment()
        if (requiredEnchantmentBukkit != null) {
            for ((enchantment, enchantLevel) in enchantments) {
                val enchantmentMatches = enchantment == requiredEnchantmentBukkit
                val levelMatches = !requiredLevel.isPresent || enchantLevel >= requiredLevel.get()
                
                if (enchantmentMatches && levelMatches) {
                    entry.triggers.forEach { it.triggerFor(player, context()) }
                    break
                }
            }
        }
    }
}
