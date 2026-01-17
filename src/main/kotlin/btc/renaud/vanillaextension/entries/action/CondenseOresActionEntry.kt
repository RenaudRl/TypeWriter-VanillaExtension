package btc.renaud.vanillaextension.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Entry("condense_ores_action", "Condense ores in inventory", Colors.RED, icon = "mdi:anvil")
class CondenseOresActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),

) : ActionEntry {
    override fun ActionTrigger.execute() {

        val inventory = player.inventory
        val ores = mapOf(
            Material.IRON_INGOT to Material.IRON_BLOCK,
            Material.GOLD_INGOT to Material.GOLD_BLOCK,
            Material.DIAMOND to Material.DIAMOND_BLOCK,
            Material.EMERALD to Material.EMERALD_BLOCK,
            Material.REDSTONE to Material.REDSTONE_BLOCK,
            Material.LAPIS_LAZULI to Material.LAPIS_BLOCK,
            Material.COAL to Material.COAL_BLOCK,
            Material.COPPER_INGOT to Material.COPPER_BLOCK,
            Material.NETHERITE_INGOT to Material.NETHERITE_BLOCK,
            Material.RAW_IRON to Material.RAW_IRON_BLOCK,
            Material.RAW_GOLD to Material.RAW_GOLD_BLOCK,
            Material.RAW_COPPER to Material.RAW_COPPER_BLOCK,
            Material.QUARTZ to Material.QUARTZ_BLOCK,
        )
        for ((ore, block) in ores) {
            val count = inventory.all(ore).values.sumOf { it.amount }
            val blocks = count / 9
            if (blocks > 0) {
                inventory.removeItem(ItemStack(ore, blocks * 9))
                inventory.addItem(ItemStack(block, blocks))
            }
        }
    
    }
}

