package kr.toxicity.damage.util

import kr.toxicity.damage.api.BetterDamage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.command.CommandSender
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import java.util.concurrent.CompletableFuture
import java.util.UUID

fun Player.onPlayerThread(action: () -> Unit) {
    if (BetterDamage.IS_PAPER) scheduler.run(PLUGIN, { action() }, null)
    else Bukkit.getScheduler().runTask(PLUGIN, Runnable(action))
}

/** Uses Core's public nickname API without bundling Core. Call from an async task. */
object SkinPlayers {
    fun suggestions(): SuggestionProvider<CommandSender> = SuggestionProvider { _, _ ->
        val result = CompletableFuture<List<Suggestion>>()
        async {
            runCatching { names().map(Suggestion::suggestion) }
                .onSuccess(result::complete).onFailure(result::completeExceptionally)
        }
        result
    }

    private val nicknameApi by lazy {
        val core = checkNotNull(Bukkit.getPluginManager().getPlugin("Kokonut-Core"))
        core.javaClass.classLoader.loadClass("kr.gomsv.kokonutCore.api.util.ServerNicknameUtil")
    }
    private val identifierToUuid by lazy { nicknameApi.getMethod("identifierToUuid", String::class.java) }
    private val uuidToNickname by lazy { nicknameApi.getMethod("uuidToNickname", UUID::class.java) }

    fun find(identifier: String): Player? {
        Bukkit.getPlayerExact(identifier)?.let { return it }
        val uuid = identifierToUuid.invoke(null, identifier) as? UUID ?: return null
        return Bukkit.getPlayer(uuid)
    }

    fun names(): List<String> {
        return Bukkit.getOnlinePlayers().flatMap { player ->
            val nickname = uuidToNickname.invoke(null, player.uniqueId) as? String
            listOfNotNull(player.name, nickname?.takeIf(String::isNotBlank))
        }.distinct()
    }
}
