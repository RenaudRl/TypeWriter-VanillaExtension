package btc.renaud.vanillaextension.events

import org.bukkit.Input
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Fired whenever a player interacting with a `LockNpcCameraBoundEntry`
 * submits a directional movement input. This allows UI elements, such as
 * BetterHud option dialogues, to react to movement keys while the player's
 * camera is locked in place.
 */
class LockNpcCameraNavigationEvent(
    val player: Player,
    val direction: LockNpcCameraNavigationDirection,
) : Event() {

    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}

/**
 * Marks a [PlayerInputEvent][org.bukkit.event.player.PlayerInputEvent] input as synthetic navigation
 * produced while an NPC camera lock is active.
 */
interface LockNpcCameraNavigationInput : Input {
    val navigationDirection: LockNpcCameraNavigationDirection?
}

/**
 * Represents the logical navigation direction derived from movement input
 * while the NPC camera is locked.
 */
enum class LockNpcCameraNavigationDirection {
    PREVIOUS,
    NEXT,
}

