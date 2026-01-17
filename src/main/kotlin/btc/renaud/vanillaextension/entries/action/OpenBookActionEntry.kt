package btc.renaud.vanillaextension.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.engine.paper.entry.triggerFor
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import net.kyori.adventure.text.Component

@Entry("open_book_action", "Open a written book for the player", Colors.RED, icon = "mdi:book-open-page-variant")
class OpenBookActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val title: String = "Book",
    val author: String = "Server",
    val pages: List<String> = emptyList(),
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val book = ItemStack(Material.WRITTEN_BOOK)
        val meta = book.itemMeta as BookMeta
        meta.title(Component.text(title))
        meta.author(Component.text(author))
        // Use Component-based pages instead of deprecated String pages
        meta.pages(pages.map { Component.text(it) })
        book.itemMeta = meta
        player.openBook(book)
    }
}
