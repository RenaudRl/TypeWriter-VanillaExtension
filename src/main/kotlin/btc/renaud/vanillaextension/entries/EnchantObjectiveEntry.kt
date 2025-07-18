package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.QuestEntry
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.entity.Player
import btc.renaud.vanillaextension.EnchantmentType
import java.util.*

@Entry("vanillaenchant_objective", "An objective to vanilla enchant items", Colors.BLUE_VIOLET, "ph:magic-wand-bold")
class EnchantObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    @Help("The enchantment needed to be applied.")
    val enchantment: Optional<EnchantmentType> = Optional.empty(),
    @Help("The minimum level of the enchantment required +1")
    val level: Optional<Int> = Optional.empty(),
    @Help("The amount of times the player needs to enchant.")
    override val amount: Var<Int> = ConstVar(0),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return EnchantObjectiveDisplay(ref())
    }
}

private class EnchantObjectiveDisplay(ref: Ref<EnchantObjectiveEntry>) :
    BaseCountObjectiveDisplay<EnchantObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEnchantItem(event: EnchantItemEvent) {
        val player = event.enchanter
        val entry = ref.get() ?: return
        if (!filter(player)) return

        val enchantmentsToAdd = event.enchantsToAdd
        if (enchantmentsToAdd.isEmpty()) return
        
        val requiredEnchantment = entry.enchantment
        val requiredLevel = entry.level
        
        // If no specific enchantment is required, count all enchanted items
        if (!requiredEnchantment.isPresent) {
            incrementCount(player)
            return
        }
        
        // Check if the required enchantment is present with the right level
        val requiredEnchantmentBukkit = requiredEnchantment.get().toEnchantment()
        if (requiredEnchantmentBukkit != null) {
            for ((enchantment, enchantLevel) in enchantmentsToAdd) {
                val enchantmentMatches = enchantment == requiredEnchantmentBukkit
                val levelMatches = !requiredLevel.isPresent || enchantLevel >= requiredLevel.get()
                
                if (enchantmentMatches && levelMatches) {
                    incrementCount(player)
                    break
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
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
        
        // If no specific enchantment is required, count all enchanted items
        if (!requiredEnchantment.isPresent) {
            incrementCount(player)
            return
        }
        
        // Check if the required enchantment is present with the right level
        val requiredEnchantmentBukkit = requiredEnchantment.get().toEnchantment()
        if (requiredEnchantmentBukkit != null) {
            for ((enchantment, enchantLevel) in enchantments) {
                val enchantmentMatches = enchantment == requiredEnchantmentBukkit
                val levelMatches = !requiredLevel.isPresent || enchantLevel >= requiredLevel.get()
                
                if (enchantmentMatches && levelMatches) {
                    incrementCount(player)
                    break
                }
            }
        }
    }
}
