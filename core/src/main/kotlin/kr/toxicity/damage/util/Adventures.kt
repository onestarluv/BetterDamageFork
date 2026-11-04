package kr.toxicity.damage.util

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

fun createAdventureKey(path: String) = Key.key(NAMESPACE, path)

fun <T : Any> (() -> T?).memoizeNullable(): () -> T? = this as? MemoizedSupplier ?: MemoizedSupplier(this)

private class MemoizedSupplier<T : Any>(private val delegate: () -> T?) : () -> T? {
    private var cache: T? = null

    @Synchronized
    override fun invoke(): T? {
        if (cache == null) cache = delegate()
        return cache
    }
}

fun CommandSender.audience(): Audience = object : Audience {
    override fun sendMessage(message: Component) {
        CoreMessages.send(this@audience, message)
    }
}

fun Audience.info(message: String) = sendMessage(Component.text(message))
fun Audience.warn(message: String) = sendMessage(Component.text(message, NamedTextColor.RED))
