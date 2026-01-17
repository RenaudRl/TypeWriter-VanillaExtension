package btc.renaud.vanillaextension


import com.typewritermc.core.extension.Initializable
import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.engine.paper.logger
import com.typewritermc.engine.paper.plugin
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

@Singleton
object Initializer : Initializable {

    private val listeners: List<Listener> = listOf()

    override suspend fun initialize() {
        logger.info("VanillaExtension has been successfully initialized. By Renaud")
        listeners.forEach { Bukkit.getPluginManager().registerEvents(it, plugin) }
    }

    override suspend fun shutdown() {
        logger.info("VanillaExtension has been successfully stopped.")
        listeners.forEach { HandlerList.unregisterAll(it) }
    }
}

