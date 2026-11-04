package kr.toxicity.damage.player

import kr.toxicity.damage.api.player.DamagePlayerData
import kr.toxicity.damage.api.skin.DamageSkin
import kr.toxicity.damage.manager.DamageSkinManagerImpl
import org.bukkit.Bukkit
import java.util.UUID
import java.util.Collections

class DamagePlayerDataImpl(
    private val uuid: UUID,
    private var selected: DamageSkin?,
    private val owns: (DamageSkin) -> Boolean = { skin ->
        Bukkit.getPlayer(uuid)?.hasPermission("betterdamage.skin.${skin.name()}") == true
    },
    private val available: () -> Collection<DamageSkin> = {
        DamageSkinManagerImpl.allNames().mapNotNull(DamageSkinManagerImpl::skin)
    }
) : DamagePlayerData {
    override fun skins(): Collection<DamageSkin> = Collections.unmodifiableList(available().filter(owns))

    @Synchronized
    fun reload() {
        selected = selected?.let { DamageSkinManagerImpl.skin(it.name()) }
    }

    @Synchronized
    override fun select(skin: DamageSkin): Boolean {
        if (!owns(skin)) return false
        selected = skin
        return true
    }

    // Revocation takes effect the next time an effect is used.
    @Synchronized
    override fun selectedSkin(): DamageSkin? = selected?.takeIf(owns)

    @Synchronized
    override fun clearSelection() { selected = null }

    // Saving also works after the player disconnects.
    @Synchronized
    fun savedSkin(): DamageSkin? = selected
}
