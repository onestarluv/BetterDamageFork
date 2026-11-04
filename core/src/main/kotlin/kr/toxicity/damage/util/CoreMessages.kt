package kr.toxicity.damage.util

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin

/** Reuses the installed Core's renderer and client delivery; never publishes a global notice. */
object CoreMessages {
    private val core by lazy { checkNotNull(Bukkit.getPluginManager().getPlugin("Kokonut-Core")) }
    private fun type(name: String): Class<*> = core.javaClass.classLoader.loadClass(name)
    private val noticeText by lazy { type("kr.gomsv.kokonutCore.notice.NoticeText") }
    private val noticeType by lazy { type("kr.gomsv.kokonutCore.notice.NoticeType") }
    private val systemType by lazy { noticeType.enumConstants.single { it.toString() == "SYSTEM" } }
    private val renderer by lazy {
        noticeText.getDeclaredMethod("create", JavaPlugin::class.java).apply { isAccessible = true }.invoke(null, core)
    }
    private val message by lazy {
        noticeText.getDeclaredMethod("message", noticeType, String::class.java).apply { isAccessible = true }
    }
    private val prefix by lazy {
        noticeText.getDeclaredMethod("prefix", noticeType).apply { isAccessible = true }
    }
    private val console by lazy {
        noticeText.getDeclaredMethod("console", String::class.java).apply { isAccessible = true }
    }
    private val miniType by lazy { type("net.kyori.adventure.text.minimessage.MiniMessage") }
    private val mini by lazy { miniType.getMethod("miniMessage").invoke(null) }
    private val serialize by lazy { miniType.getMethod("serialize", Component::class.java) }
    private val clientType by lazy { type("kr.gomsv.kokonutCore.chat.client.ClientChatService") }
    private val category by lazy { type("kr.gomsv.kokonutCore.chat.client.ChatFrame\$Category") }
    private val systemCategory by lazy { category.enumConstants.single { it.toString() == "SYSTEM" } }
    private val client by lazy { checkNotNull(Bukkit.getServicesManager().load(clientType)) }
    private val send by lazy { clientType.getMethod("send", Player::class.java, category, String::class.java, Component::class.java) }
    private val itemText by lazy { type("kr.gomsv.kokonutCore.api.util.ItemText").getMethod("apply", ItemMeta::class.java) }

    fun applyItemText(meta: ItemMeta) { itemText.invoke(null, meta) }

    fun send(sender: CommandSender, body: Component) {
        val serialized = serialize.invoke(mini, body) as String
        if (sender is Player) {
            sender.onPlayerThread {
                if (sender.isOnline) {
                    val formatted = message.invoke(renderer, systemType, serialized) as Component
                    send.invoke(client, sender, systemCategory, "system", formatted)
                }
            }
        } else {
            val formatted = console.invoke(renderer, "${prefix.invoke(null, systemType)} $serialized") as Component
            sender.sendMessage(formatted)
        }
    }
}
