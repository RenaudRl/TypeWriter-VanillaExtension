package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.triggerAllFor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry("advancement_grant_event", "Triggered when a player completes an advancement", Colors.YELLOW, "mdi:trophy")
@ContextKeys(AdvancementGrantContextKeys::class)
/**
 * The `Advancement Grant Event` is triggered when a player completes an advancement.
 * 
 * ## How could this be used?
 * This could be used to complete a quest when the player achieves specific advancements,
 * or to give the player additional rewards when they complete certain achievements.
 */
class AdvancementGrantEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    
    @Help("The advancement key that needs to be completed. Leave empty to trigger for any advancement.")
    val advancementKey: Var<String> = ConstVar(""),
) : EventEntry

enum class AdvancementGrantContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    ADVANCEMENT_KEY(String::class),

    @KeyType(String::class)
    ADVANCEMENT_TITLE(String::class),

    @KeyType(String::class)
    ADVANCEMENT_DESCRIPTION(String::class),
}

@EntryListener(AdvancementGrantEventEntry::class)
fun onAdvancementGrant(event: PlayerAdvancementDoneEvent, query: Query<AdvancementGrantEventEntry>) {
    val player = event.player
    val advancement = event.advancement
    
    val advancementKey = advancement.key.toString()
    val advancementTitle = advancement.display?.title()?.toString() ?: "Unknown"
    val advancementDescription = advancement.display?.description()?.toString() ?: "Unknown"
    
    // Find matching entries and trigger them
    query.findWhere { entry ->
        val requiredKey = entry.advancementKey.get(player)
        // If no specific advancement is required, trigger for all advancements
        requiredKey.isEmpty() || advancementKey.contains(requiredKey)
    }.triggerAllFor(player) {
        AdvancementGrantContextKeys.ADVANCEMENT_KEY += advancementKey
        AdvancementGrantContextKeys.ADVANCEMENT_TITLE += advancementTitle
        AdvancementGrantContextKeys.ADVANCEMENT_DESCRIPTION += advancementDescription
    }
}

