package btc.renaud.vanillaextension.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import btc.renaud.vanillaextension.entries.PlayerInputType
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import com.typewritermc.engine.paper.utils.item.Item
import com.typewritermc.engine.paper.utils.item.allowsAnyItem
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInputEvent
import java.util.Optional
import kotlin.reflect.KClass
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier

@Entry(
    "on_player_input",
    "Triggers when a player performs a chosen input (jump, sprint, sneak, or movement keys).",
    Colors.YELLOW,
    icon = "game-icons:abstract-016"
)
@ContextKeys(PlayerInputContextKeys::class)
class PlayerInputEventEntry(
    override val id: String = "",
    override val name: String = "Player Input Event",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("If set, only triggers when this specific input is pressed.")
    val inputType: Optional<PlayerInputType> = Optional.empty(),
    @Help("Require the player to be holding any of these items.")
    val heldItem: Optional<Var<List<Item>>> = Optional.empty()
) : EventEntry

enum class PlayerInputContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(PlayerInputType::class)
    INPUT_TYPE(PlayerInputType::class),
    @KeyType(Boolean::class)
    IS_JUMP(Boolean::class),
    @KeyType(Boolean::class)
    IS_SPRINT(Boolean::class),
    @KeyType(Boolean::class)
    IS_SNEAK(Boolean::class),
    @KeyType(Boolean::class)
    IS_FORWARD(Boolean::class),
    @KeyType(Boolean::class)
    IS_BACKWARD(Boolean::class),
    @KeyType(Boolean::class)
    IS_LEFT(Boolean::class),
    @KeyType(Boolean::class)
    IS_RIGHT(Boolean::class),
}

@EntryListener(PlayerInputEventEntry::class)
fun onPlayerInput(event: PlayerInputEvent, query: Query<PlayerInputEventEntry>) {
    val player = event.player
    val entries = query.findWhere { entry ->
        val inputOk = entry.inputType.map { type ->
            when (type) {
                PlayerInputType.JUMP -> event.input.isJump
                PlayerInputType.SPRINT -> event.input.isSprint
                PlayerInputType.SNEAK -> event.input.isSneak
                PlayerInputType.FORWARD -> event.input.isForward
                PlayerInputType.BACKWARD -> event.input.isBackward
                PlayerInputType.LEFT -> event.input.isLeft
                PlayerInputType.RIGHT -> event.input.isRight
            }
        }.orElse(true)
        val interactionContext = context()
        val itemOk = entry.heldItem.map { varItem ->
            val expectedItems = varItem.get(player, interactionContext)
            hasMatchingItem(player, expectedItems, interactionContext)
        }.orElse(true)
        inputOk && itemOk
    }.toList()
    entries.startDialogueWithOrNextDialogue(player) {
        PlayerInputContextKeys.INPUT_TYPE += derivePrimaryType(event)
        PlayerInputContextKeys.IS_JUMP += event.input.isJump
        PlayerInputContextKeys.IS_SPRINT += event.input.isSprint
        PlayerInputContextKeys.IS_SNEAK += event.input.isSneak
        PlayerInputContextKeys.IS_FORWARD += event.input.isForward
        PlayerInputContextKeys.IS_BACKWARD += event.input.isBackward
        PlayerInputContextKeys.IS_LEFT += event.input.isLeft
        PlayerInputContextKeys.IS_RIGHT += event.input.isRight
    }
}

private fun hasMatchingItem(
    player: Player,
    expectedItems: Collection<Item>,
    interactionContext: InteractionContext?
): Boolean {
    if (expectedItems.allowsAnyItem()) return true

    val main = player.inventory.itemInMainHand.takeUnless { it.type.isAir }
    val off = player.inventory.itemInOffHand.takeUnless { it.type.isAir }

    return expectedItems.any { item ->
        (main != null && item.isSameAs(player, main, interactionContext)) ||
            (off != null && item.isSameAs(player, off, interactionContext))
    }
}

private fun derivePrimaryType(event: PlayerInputEvent): PlayerInputType {
    val input = event.input
    return when {
        input.isJump -> PlayerInputType.JUMP
        input.isSprint -> PlayerInputType.SPRINT
        input.isSneak -> PlayerInputType.SNEAK
        input.isForward -> PlayerInputType.FORWARD
        input.isBackward -> PlayerInputType.BACKWARD
        input.isLeft -> PlayerInputType.LEFT
        input.isRight -> PlayerInputType.RIGHT
        else -> PlayerInputType.FORWARD
    }
}

