package kr.toxicity.damage.player

import kr.toxicity.damage.api.player.DamagePlayer
import kr.toxicity.damage.api.player.DamagePlayerData
import kr.toxicity.damage.util.CONFIG
import kr.toxicity.damage.util.async
import kr.toxicity.damage.util.handle
import kr.toxicity.damage.util.toPlayerData
import org.bukkit.entity.Player

class DamagePlayerImpl(
    private val player: Player,
) : DamagePlayer {

    private val data = player.uniqueId.toPlayerData()
    private var lastSavedSkin = data.savedSkin()?.name()

    fun reload() {
        data.reload()
    }

    private val task = async(
        CONFIG.autoSaveTime(),
        CONFIG.autoSaveTime()
    ) {
        save()
    }

    override fun player(): Player = player
    override fun data(): DamagePlayerData = data

    override fun save() {
        synchronized(data) {
            val selected = data.savedSkin()?.name()
            if (selected == lastSavedSkin) return
            runCatching {
                data.save(player.uniqueId)
                lastSavedSkin = selected
            }.onFailure {
                it.handle("${player.name} (${player.uniqueId})님의 장착 정보를 저장하지 못했습니다.")
            }
        }
    }

    override fun cancel() {
        task.cancel()
    }
}
