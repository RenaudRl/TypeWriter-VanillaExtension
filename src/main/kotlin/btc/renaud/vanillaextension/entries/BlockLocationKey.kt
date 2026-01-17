package btc.renaud.vanillaextension.entries

import org.bukkit.Location
import org.bukkit.block.Block
import java.util.UUID

internal data class BlockLocationKey(
    val world: UUID,
    val x: Int,
    val y: Int,
    val z: Int,
)

internal fun Block.toLocationKey(): BlockLocationKey = BlockLocationKey(world.uid, x, y, z)

internal fun Location.toLocationKey(): BlockLocationKey = BlockLocationKey(world.uid, blockX, blockY, blockZ)

