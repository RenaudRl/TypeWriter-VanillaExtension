package btc.renaud.vanillaextension.entries.bound

import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput
import com.typewritermc.basic.entries.bound.LockInteractionBoundEntry
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.InteractionBound
import com.typewritermc.core.interaction.InteractionBoundState
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.core.utils.point.Position
import com.typewritermc.core.utils.point.Vector
import com.typewritermc.core.utils.point.toVector
import btc.renaud.vanillaextension.events.LockNpcCameraNavigationDirection
import btc.renaud.vanillaextension.events.LockNpcCameraNavigationEvent
import btc.renaud.vanillaextension.events.LockNpcCameraNavigationInput
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.InteractionBoundEntry
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entity.AudienceEntityDisplay
import com.typewritermc.engine.paper.entry.entries.ComputeVar
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntityInstanceEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.get
import com.typewritermc.engine.paper.entry.findDisplay
import com.typewritermc.engine.paper.interaction.InterceptionBundle
import com.typewritermc.engine.paper.interaction.ListenerInteractionBound
import com.typewritermc.engine.paper.interaction.interactionContext
import com.typewritermc.engine.paper.interaction.interceptPackets
import com.typewritermc.engine.paper.logger
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.position
import com.typewritermc.entity.entries.activity.getLookPitch
import com.typewritermc.entity.entries.activity.getLookYaw
import com.typewritermc.entity.entries.event.InteractingEntityInstance
import org.bukkit.Bukkit
import org.bukkit.Input
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInputEvent
import java.util.Optional
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Entry(
    "lock_npc_camera_bound",
    "Lock the camera around an NPC",
    Colors.MEDIUM_PURPLE,
    "mdi:target-account"
)
class LockNpcCameraBoundEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val interruptTriggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("The NPC instance to use as camera focus. Leave empty to reuse the interacting NPC from the context.")
    val npc: Ref<out EntityInstanceEntry> = emptyRef(),
    @Help("Horizontal offset (X axis) relative to the NPC position.")
    val offsetX: Var<Double> = ConstVar(0.0),
    @Help("Vertical offset (Y axis) relative to the NPC position.")
    val offsetY: Var<Double> = ConstVar(0.0),
    @Help("Horizontal offset (Z axis) relative to the NPC position.")
    val offsetZ: Var<Double> = ConstVar(0.0),
    @Help("Horizontal offset (X axis) for the point the camera should look at.")
    val lookOffsetX: Var<Double> = ConstVar(0.0),
    @Help("Vertical offset (Y axis) added on top of the NPC eye height for the look at point.")
    val lookOffsetY: Var<Double> = ConstVar(0.0),
    @Help("Horizontal offset (Z axis) for the point the camera should look at.")
    val lookOffsetZ: Var<Double> = ConstVar(0.0),
    @Help("Additional pitch offset (in degrees) applied after looking at the NPC.")
    val pitchOffset: Var<Float> = ConstVar(0f),
    @Help("Rotate offsets based on the NPC yaw so X/Z offsets are relative to its facing direction.")
    val relativeToNpcRotation: Boolean = true,
    @Help("Optional fallback location when the NPC display is unavailable. Defaults to the player's current position.")
    val fallbackPosition: Optional<Var<Position>> = Optional.empty(),
) : InteractionBoundEntry {

    override fun build(player: Player): InteractionBound {
        val baseBound = LockInteractionBoundEntry(
            id = id,
            name = name,
            criteria = criteria,
            modifiers = modifiers,
            triggers = triggers,
            interruptTriggers = interruptTriggers,
            targetPosition = Optional.of(
                ComputeVar { resolvedPlayer, rawContext ->
                    val ctx = rawContext ?: resolvedPlayer.interactionContext
                    computeTargetPosition(resolvedPlayer, ctx)
                }
            )
        ).build(player)

        return if (baseBound is ListenerInteractionBound) {
            LockNpcCameraInteractionBound(baseBound, player)
        } else {
            baseBound
        }
    }

    private fun computeTargetPosition(player: Player, interactionContext: InteractionContext?): Position {
        val offsetVector = Vector(
            offsetX.get(player, interactionContext),
            offsetY.get(player, interactionContext),
            offsetZ.get(player, interactionContext)
        )
        val lookOffsetVector = Vector(
            lookOffsetX.get(player, interactionContext),
            lookOffsetY.get(player, interactionContext),
            lookOffsetZ.get(player, interactionContext)
        )
        val pitchOffsetValue = pitchOffset.get(player, interactionContext)

        val anchor = resolveAnchor(player, interactionContext)
        val fallback = resolveFallbackPosition(player, interactionContext)
        val basePosition = anchor?.position ?: fallback
        val playerYawOffset = resolvePlayerYawOffset(player, basePosition.yaw)

        if (anchor == null) {
            val source = if (npc.isSet) "NPC '${npc.id}'" else "context NPC"
            val fallbackTarget = if (fallbackPosition.isPresent) "configured fallback position" else "player position"
            logger.fine(
                "LockNpcCameraBoundEntry '$id' is falling back to $fallbackTarget for ${player.name} because $source display is not available."
            )
        }

        val rotatedOffset = if (anchor != null && relativeToNpcRotation) {
            rotateOffset(offsetVector, anchor.position.yaw)
        } else {
            offsetVector
        }

        val cameraPosition = basePosition.add(rotatedOffset.x, rotatedOffset.y, rotatedOffset.z)

        if (anchor == null) {
            val finalYaw = normalizeYaw(basePosition.yaw + playerYawOffset)
            val finalPitch = (basePosition.pitch + pitchOffsetValue).coerceIn(-90f, 90f)
            return cameraPosition.withRotation(finalYaw, finalPitch)
        }

        val rotatedLookOffset = if (relativeToNpcRotation) {
            rotateOffset(lookOffsetVector, anchor.position.yaw)
        } else {
            lookOffsetVector
        }

        val lookPosition = anchor.position.add(
            rotatedLookOffset.x,
            rotatedLookOffset.y + anchor.eyeHeight,
            rotatedLookOffset.z
        )

        val cameraVector = cameraPosition.toVector()
        val lookVector = lookPosition.toVector()
        val direction = lookVector.sub(cameraVector.x, cameraVector.y, cameraVector.z)
        val baseTargetYaw = normalizeYaw(getLookYaw(direction.x, direction.z))
        val effectiveYawOffset = if (shouldKeepPlayerFacing(player.position.yaw, baseTargetYaw)) {
            0f
        } else {
            playerYawOffset
        }
        val finalYaw = normalizeYaw(baseTargetYaw + effectiveYawOffset)
        val finalPitch = (getLookPitch(direction.x, direction.y, direction.z) + pitchOffsetValue).coerceIn(-90f, 90f)

        return cameraPosition.withRotation(finalYaw, finalPitch)
    }

    private fun resolveAnchor(player: Player, interactionContext: InteractionContext?): Anchor? {
        val contextNpcRef = interactionContext?.get(InteractingEntityInstance) as? Ref<*>
        val ref =
            @Suppress("UNCHECKED_CAST")
            when {
                npc.isSet -> npc
                else -> contextNpcRef as? Ref<out EntityInstanceEntry>
            } ?: return null

        val display = ref.findDisplay<AudienceEntityDisplay>() ?: return null
        if (!display.canView(player.uniqueId)) return null
        val position = display.position(player.uniqueId) ?: return null
        val state = display.entityState(player.uniqueId)
        return Anchor(position, state.eyeHeight)
    }

    private fun resolveFallbackPosition(player: Player, interactionContext: InteractionContext?): Position {
        val fallbackVar = fallbackPosition.orElse(null)
        return fallbackVar?.get(player, interactionContext) ?: player.position
    }

    private fun rotateOffset(offset: Vector, yaw: Float): Vector {
        val radians = Math.toRadians(yaw.toDouble())
        val cosYaw = cos(radians)
        val sinYaw = sin(radians)
        val x = offset.x * cosYaw - offset.z * sinYaw
        val z = offset.x * sinYaw + offset.z * cosYaw
        return Vector(x, offset.y, z)
    }

    private fun resolvePlayerYawOffset(player: Player, referenceYaw: Float): Float {
        val playerYaw = player.position.yaw
        val offset = playerYaw - referenceYaw
        return normalizeYaw(offset)
    }

    private fun normalizeYaw(yaw: Float): Float {
        var normalized = yaw % 360f
        if (normalized < -180f) normalized += 360f
        if (normalized >= 180f) normalized -= 360f
        return normalized
    }

    private fun shouldKeepPlayerFacing(playerYaw: Float, targetYaw: Float): Boolean {
        val delta = abs(normalizeYaw(playerYaw - targetYaw))
        return delta <= 10f
    }

    private data class Anchor(
        val position: Position,
        val eyeHeight: Double,
    )

    private class LockNpcCameraInteractionBound(
        private val delegate: ListenerInteractionBound,
        private val player: Player,
    ) : ListenerInteractionBound {

        private var movementInterceptor: InterceptionBundle? = null

        override val priority: Int
            get() = delegate.priority

        override val interruptionTriggers
            get() = delegate.interruptionTriggers

        override suspend fun initialize() {
            registerMovementSanitizer()
            delegate.initialize()
            super<ListenerInteractionBound>.initialize()
        }

        override suspend fun tick() {
            delegate.tick()
        }

        override suspend fun boundStateChange(
            previousBoundState: InteractionBoundState,
            newBoundState: InteractionBoundState,
        ) {
            delegate.boundStateChange(previousBoundState, newBoundState)
        }

        override suspend fun teardown() {
            movementInterceptor?.cancel()
            movementInterceptor = null
            super<ListenerInteractionBound>.teardown()
            delegate.teardown()
        }

        private fun registerMovementSanitizer() {
            movementInterceptor = player.interceptPackets {
                Play.Client.PLAYER_INPUT { event ->
                    val packet = WrapperPlayClientPlayerInput(event)
                    val forward = packet.isForward
                    val backward = packet.isBackward
                    val left = packet.isLeft
                    val right = packet.isRight

                    if (!forward && !backward && !left && !right) return@PLAYER_INPUT

                    dispatchMovementInput(
                        forward = forward,
                        backward = backward,
                        left = left,
                        right = right,
                        jump = packet.isJump,
                        sneak = packet.isShift,
                        sprint = packet.isSprint,
                    )

                    if (packet.isForward) packet.isForward = false
                    if (packet.isBackward) packet.isBackward = false
                    if (packet.isLeft) packet.isLeft = false
                    if (packet.isRight) packet.isRight = false
                }
            }
        }

        private fun dispatchMovementInput(
            forward: Boolean,
            backward: Boolean,
            left: Boolean,
            right: Boolean,
            jump: Boolean,
            sneak: Boolean,
            sprint: Boolean,
        ) {
            val navigationDirection = resolveNavigationDirection(forward, backward, left, right)
            val syntheticInput = SyntheticPlayerInput(
                forward = forward,
                backward = backward,
                left = left,
                right = right,
                jump = jump,
                sneak = sneak,
                sprint = sprint,
                navigationDirection = navigationDirection,
            )

            val callEvent: () -> Unit = {
                val event = PlayerInputEvent(player, syntheticInput)
                Bukkit.getPluginManager().callEvent(event)

                navigationDirection?.let {
                    Bukkit.getPluginManager().callEvent(
                        LockNpcCameraNavigationEvent(player, it)
                    )
                }
            }

            if (Bukkit.isPrimaryThread()) {
                callEvent()
            } else {
                plugin.server.scheduler.runTask(plugin, Runnable { callEvent() })
            }
        }

        private fun resolveNavigationDirection(
            forward: Boolean,
            backward: Boolean,
            left: Boolean,
            right: Boolean,
        ): LockNpcCameraNavigationDirection? {
            val wantsPrevious = forward || left
            val wantsNext = backward || right

            return when {
                wantsPrevious && !wantsNext -> LockNpcCameraNavigationDirection.PREVIOUS
                wantsNext && !wantsPrevious -> LockNpcCameraNavigationDirection.NEXT
                else -> null
            }
        }

        private data class SyntheticPlayerInput(
            private val forward: Boolean,
            private val backward: Boolean,
            private val left: Boolean,
            private val right: Boolean,
            private val jump: Boolean,
            private val sneak: Boolean,
            private val sprint: Boolean,
            override val navigationDirection: LockNpcCameraNavigationDirection?,
        ) : LockNpcCameraNavigationInput {
            override fun isForward(): Boolean = forward
            override fun isBackward(): Boolean = backward
            override fun isLeft(): Boolean = left
            override fun isRight(): Boolean = right
            override fun isJump(): Boolean = jump
            override fun isSneak(): Boolean = sneak
            override fun isSprint(): Boolean = sprint
        }
    }
}

