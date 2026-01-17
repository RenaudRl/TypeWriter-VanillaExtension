package btc.renaud.vanillaextension.entries.objective

import btc.renaud.vanillaextension.BaseCountObjectiveDisplay
import btc.renaud.vanillaextension.BaseCountObjectiveEntry
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.quest.entries.QuestEntry
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerBucketFillEvent
import java.util.*
import com.typewritermc.loader.ListenerPriority
import com.typewritermc.engine.paper.entry.entries.AudienceFilter

@Entry("bucket_fill_objective", "Bucket fill objective", Colors.BLUE_VIOLET, "mdi:bucket")
class BucketFillObjectiveEntry(
    override val id: String = "",
    override val name: String = "",
    override val quest: Ref<QuestEntry> = emptyRef(),
    override val criteria: List<Criteria> = emptyList(),
    override val children: List<Ref<AudienceEntry>> = emptyList(),
    override val fact: Ref<CachableFactEntry> = emptyRef(),
    override val amount: Var<Int> = ConstVar(1),
    override val display: Var<String> = ConstVar(""),
    override val onComplete: Ref<TriggerableEntry> = emptyRef(),
    override val onCompleteModifiers: List<Modifier> = emptyList(),
    override val priorityOverride: Optional<Int> = Optional.empty(),
    @Help("Material to fill bucket with.")
    val bucketType: Material? = null,
) : BaseCountObjectiveEntry {
    override suspend fun display(): AudienceFilter {
        return BucketFillObjectiveDisplay(ref())
    }
}

private class BucketFillObjectiveDisplay(ref: Ref<BucketFillObjectiveEntry>) :
    BaseCountObjectiveDisplay<BucketFillObjectiveEntry>(ref) {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerBucketFill(event: PlayerBucketFillEvent) {
        val player = event.player
        val entry = ref.get() ?: return
        
        // bucketType usually refers to what we get? e.g. LAVA_BUCKET
        // The event doesn't easily give 'result' except via itemStack? 
        // Paper API might be useful here.
        // event.itemStack is the result.
        
        if (entry.bucketType != null && event.itemStack?.type != entry.bucketType) return

        if (!filter(player)) return
        incrementCount(player)
    }
}
