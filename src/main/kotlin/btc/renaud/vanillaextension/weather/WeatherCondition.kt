package btc.renaud.vanillaextension.weather

import org.bukkit.World

/**
 * Represents the high-level weather state a player can experience in a world.
 */
enum class WeatherCondition {
    /** No precipitation is active in the world. */
    CLEAR,

    /** The world is raining (or snowing) without thunder. */
    RAIN,

    /** Thunder is active alongside precipitation. */
    THUNDER;

    fun matches(world: World): Boolean = when (this) {
        CLEAR -> world.isClearWeather
        RAIN -> world.hasStorm() && !world.isThundering
        THUNDER -> world.isThundering
    }

    companion object {
        fun fromWorld(world: World): WeatherCondition = when {
            world.isThundering -> THUNDER
            world.hasStorm() -> RAIN
            else -> CLEAR
        }
    }
}

