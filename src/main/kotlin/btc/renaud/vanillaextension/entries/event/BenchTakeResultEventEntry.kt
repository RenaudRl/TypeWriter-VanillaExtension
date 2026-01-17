package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "bench_take_result_event",
    "Triggers when a player takes the result from various benches",
    Colors.YELLOW,
    icon = "mdi:anvil"
)
class BenchTakeResultEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : EventEntry

@EntryListener(BenchTakeResultEventEntry::class)
fun onBenchTakeResult(event: InventoryClickEvent, query: Query<BenchTakeResultEventEntry>) {
    val player = event.whoClicked as? Player ?: return
    if (event.slotType != InventoryType.SlotType.RESULT) return
    val type = event.view.topInventory.type
    if (type !in setOf(
            InventoryType.ANVIL,
            InventoryType.GRINDSTONE,
            InventoryType.CARTOGRAPHY,
            InventoryType.LOOM,
            InventoryType.STONECUTTER,
            InventoryType.SMITHING
        )
    ) return

    val entries = query.find().toList()
    entries.startDialogueWithOrNextDialogue(player) { }
}

