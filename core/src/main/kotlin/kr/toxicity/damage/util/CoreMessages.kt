package kr.toxicity.damage.util

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.inventory.meta.ItemMeta

/** Calls Core public APIs; does not access Core internals or broadcast notices. */
object CoreMessages {
    private val core by lazy { checkNotNull(Bukkit.getPluginManager().getPlugin("Kokonut-Core")) }
    private fun type(name: String): Class<*> = core.javaClass.classLoader.loadClass(name)
    private val send by lazy {
        type("kr.gomsv.kokonutCore.api.util.MessageUtil").getMethod("send", CommandSender::class.java, Component::class.java)
    }
    private val itemText by lazy { type("kr.gomsv.kokonutCore.api.util.ItemText").getMethod("apply", ItemMeta::class.java) }
    fun applyItemText(meta: ItemMeta) { itemText.invoke(null, meta) }
    fun send(sender: CommandSender, body: Component) { send.invoke(null, sender, body) }
}